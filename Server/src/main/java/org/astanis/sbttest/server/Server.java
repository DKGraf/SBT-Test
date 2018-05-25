package org.astanis.sbttest.server;

import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Сервер. Создает по одному экземпляру каждого из сервисов, к которым будет
 * обрабатывать запросы. Принимает соединения от удаленных клиентов и
 * обеспечивает выполнение запрошенных команд с передачей результата их
 * выполнения клиенту. Производит логирование запросов и ответов. На каждого
 * клиента создается отдельный поток выполнения. Для обработки запроса,
 * получения результата и отправки результата запрос передается на выполнение
 * в thread pool.
 *
 * @author dkgraf
 */
public class Server {
	private final int port;
	private final Logger logger = Logger.getLogger(Server.class);
	private final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
	private final Map<String, Object> services = new ConcurrentHashMap<>();

	/**
	 * Создает сервер на указанном порту и инициализирует по одному объекту
	 * каждого сервиса.
	 *
	 * @param port номер порта, на котором будет запущен сервер.
	 */
	public Server(int port) {
		this.port = port;
		initServices();
	}

	/**
	 * Метод для запуска сервера. Содержит бесконечный цикл, в котором принимаются
	 * подключения от клиентов. Инициализирует обработку запросов в отдельном потоке.
	 */
	@SuppressWarnings("InfiniteLoopStatement")
	public void run() {
		try (ServerSocket ss = new ServerSocket(port)) {
			while (true) {
				Socket client = ss.accept();
				new Thread(() -> receiveRequest(client)).start();
			}
		} catch (IOException e) {
			System.err.println("IO Exception during socket creation!");
			e.printStackTrace();
		}
	}

	/**
	 * Метод для получения клиентского запроса и инициирования его обработки.
	 * Кроме того, логирует параметры полученного запроса.
	 *
	 * @param client сокет, связаный с клиентом.
	 */
	@SuppressWarnings("unchecked")
	private void receiveRequest(Socket client) {
		try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
		     ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
			while (!client.isClosed()) {
				Map<String, Object> requestParams;

				synchronized (in) {
					requestParams = (Map<String, Object>) in.readObject();
				}

				int requestId = (int) requestParams.get("requestId");
				String serviceName = (String) requestParams.get("serviceName");
				String methodName = (String) requestParams.get("methodName");
				Object[] params = (Object[]) requestParams.get("params");

				logger.info("Received request: " + "ID = " + requestId + ", serviceName = " + serviceName +
					", methodName = " + methodName + ", params = " + Arrays.toString(params));

				threadPool.submit(() -> sendResponse(out, requestId, serviceName, methodName, params));
			}
		} catch (IOException e) {
			System.err.println("IO Exception during process request from client!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Метод, инициирующий обработку входящего запроса и производящий отправку
	 * результата выполнения клиенту. Произваодит логирование отправленного ответа.
	 *
	 * @param out         ObjectOutputStream для данного соединения, через который
	 *                    будет производится отправка результата.
	 * @param requestId   Уникальный, в рамках клиентского соединения, идентификатор
	 *                    запроса.
	 * @param serviceName Имя сервиса, у которога будет производится вызов метода.
	 * @param methodName  Название вызываемого метода.
	 * @param params      Массив аргументов, с которыми будет вызываться метод.
	 */
	private void sendResponse(ObjectOutputStream out,
	                          int requestId,
	                          String serviceName,
	                          String methodName,
	                          Object[] params) {

		Map<String, Object> response = createResponse(requestId, serviceName, methodName, params);

		String exception = (String) response.get("exception");
		try {
			if (exception != null) {
				logger.info("Sending response: " + "ID = " + requestId + ", Error processing request: " + exception);
				synchronized (out) {
					out.writeObject(response);
				}
			} else {
				Object result = response.get("result");
				logger.info("Sending response: " + "ID = " + requestId + ", result = " + (result != null ? result.toString() : null));
				synchronized (out) {
					out.writeObject(response);
				}
			}
		} catch (IOException e) {
			System.err.println("IO Exception during sending response to client!");
			e.printStackTrace();
		}
	}

	/**
	 * Метод, производящий непосредственную обработку запроса. Использует Reflection
	 * для вызова метода.
	 *
	 * @param requestId   Уникальный, в рамках клиентского соединения, идентификатор
	 *                    запроса.
	 * @param serviceName Имя сервиса, у которога будет производится вызов метода.
	 * @param methodName  Название вызываемого метода.
	 * @param params      Массив аргументов, с которыми будет вызываться метод.
	 * @return Map, содержащую ответ для клиента.
	 */
	private Map<String, Object> createResponse(int requestId,
	                                           String serviceName,
	                                           String methodName,
	                                           Object[] params) {

		Object service = services.get(serviceName);
		Object result;
		Map<String, Object> response = new HashMap<>();
		response.put("requestId", requestId);

		if (service != null) {
			try {
				Method method = service.getClass().getMethod(methodName, methodArgumentsClasses(params));
				result = method.invoke(service, params);
				response.put("result", result);
			} catch (NoSuchMethodException e) {
				response.put("exception", "No such method or invalid arguments or invalid arguments count!");
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			response.put("exception", "No such service!");
		}

		return response;
	}

	/**
	 * Инициализирует сервисы, для которых будет возможен удаленный вызов методов.
	 * Имена сервисов и их классы содержатся в файле server.properties.
	 */
	private void initServices() {
		FileInputStream fis;
		Properties property = new Properties();

		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			File propertyFile = new File(Objects.requireNonNull(classLoader.getResource("server.properties")).getFile());
			fis = new FileInputStream(propertyFile);
			property.load(fis);

			for (Map.Entry<Object, Object> entry : property.entrySet()) {
				services.put((String) entry.getKey(), Class.forName((String) entry.getValue()).newInstance());
			}

		} catch (IOException e) {
			System.err.println("Error: Property file not found!");
			e.printStackTrace();
		} catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			System.err.println("Exception during creation of service instance!");
			e.printStackTrace();
		}

	}

	/**
	 * Получает массив классов аргументов для вызова заданного метода. Необходимо
	 * для использования с мехонизмом Reflection.
	 *
	 * @param params Массив аргументов, с которыми будет вызываться метод, для
	 *               которых необходимо определить классы объектов.
	 * @return Массив классов аргументов для вызова метода сервиса.
	 */
	private Class[] methodArgumentsClasses(Object[] params) {
		Class[] classes = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			classes[i] = params[i].getClass();
		}
		return classes;
	}
}
