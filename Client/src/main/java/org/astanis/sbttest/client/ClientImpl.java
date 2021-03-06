package org.astanis.sbttest.client;

import org.apache.log4j.Logger;
import org.astanis.sbttest.exception.RmiException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Реализация org.astanis.sbttest.client.Client. Устанавливает соединение
 * с удаленным сервером и производит вызов методов у определенных сервисов.
 * Производит логирование отправленных запросов и полученных ответов.
 *
 * @author dkgraf
 */
public class ClientImpl implements Client {
	private final static int DEFAULT_PORT = 9999;
	private final static String DEFAULT_HOST = "localhost";
	private final String host;
	private final int port;
	private final AtomicInteger uniqueId = new AtomicInteger(0);
	private final Logger logger = Logger.getLogger(ClientImpl.class);
	private final Map<Integer, Map<String, Object>> unusedResponses = new ConcurrentHashMap<>();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private final Object inLock = new Object();
	private final Object outLock = new Object();

	/**
	 * Создает клиента на, который пытается подключится к серверу на хосте
	 * DEFAULT_HOST и порту DEFAULT_PORT. Инициирует создание подключения
	 * к серверу.
	 */
	public ClientImpl() {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}
	/**
	 * Создает клиента с указанным хостом и портов. Инициирует создание
	 * подключения к серверу.
	 *
	 * @param host Хост, на котором находится сервер.
	 * @param port Порт, на котором сервер ожидает подключение.
	 */
	public ClientImpl(String host, int port) {
		this.host = host;
		this.port = port;
		openConnection();
	}

	/**
	 * Метод, Открывающий соединение к серверу и создающий ObjectInputStream
	 * и ObjectOutputStream для записи и чтения данных.
	 */
	private void openConnection() {
		try {
			Socket socket = new Socket(host, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			logger.error("Server unavailable!", e);
			System.exit(1);
		}
	}

	/**
	 * Метод, осуществляющий удаленный вызов. Осуществляет отправку запроса.
	 *
	 * @param serviceName Имя сервиса, у которога будет производится вызов метода.
	 * @param methodName  Название вызываемого метода.
	 * @param params      Массив аргументов, с которыми будет вызываться метод.
	 * @return Ответ сервера, полученный на удаленный вызов метода.
	 * @throws RmiException если был запрошен несуществующий серви, метод или
	 *                      неверное количество аргументов метода или их типы.
	 */
	@Override
	public Object remoteCall(String serviceName, String methodName, Object[] params) throws RmiException {
		int requestId = uniqueId.incrementAndGet();
		Map<String, Object> request = createRequest(requestId, serviceName, methodName, params);

		try {
			synchronized (outLock) {
				out.writeObject(request);
				out.flush();
			}

			logger.info("Sending request: " + "ID = " + requestId + ", serviceName = " + serviceName +
				", methodName = " + methodName + ", params = " + Arrays.toString(params));
		} catch (IOException e) {
			logger.error("IO exception during sending request to server! Server unavailable", e);
			System.exit(1);
		}

		Map<String, Object> response = getResponse(requestId);

		return getResult(response);
	}

	/**
	 * Создает запрос для удаленного вызова метода.
	 *
	 * @param requestId   Уникальный, в рамках клиентского соединения, идентификатор
	 *                    запроса.
	 * @param serviceName Имя сервиса, у которога будет производится вызов метода.
	 * @param methodName  Название вызываемого метода.
	 * @param params      Массив аргументов, с которыми будет вызываться метод.
	 * @return Map, содержащую запрос для сервера.
	 */
	private Map<String, Object> createRequest(int requestId, String serviceName, String methodName, Object[] params) {
		Map<String, Object> request = new HashMap<>();
		request.put("requestId", requestId);
		request.put("serviceName", serviceName);
		request.put("methodName", methodName);
		request.put("params", params);

		return request;
	}

	/**
	 * Получает ответ от удаленного сервера.
	 *
	 * @param requestId Уникальный, в рамках клиентского соединения, идентификатор
	 *                  запроса.
	 * @return Map, представляющую из себе ответ сервера на запрос.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getResponse(int requestId) {
		Map<String, Object> response = null;

		try {
			while (response == null) {
				Map<String, Object> temp = unusedResponses.get(requestId);
				if (temp != null) {
					response = temp;
					unusedResponses.remove(requestId);
				} else {
					synchronized (inLock) {
						temp = (Map<String, Object>) in.readObject();
						int id = (int) temp.get("requestId");
						if (id == requestId) {
							response = temp;
						} else {
							unusedResponses.put(id, temp);
						}
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			logger.error("IO exception during receiving request from server! Server unavailable", e);
			System.exit(1);
		}

		return response;
	}

	/**
	 * Получает результат выполнения удаленного вызова.
	 *
	 * @param response Объект, представляющий собой ответ сервера.
	 * @return Результат выполнения удаленного вызова.
	 * @throws RmiException если был запрошен несуществующий серви, метод или
	 *                      неверное количество аргументов метода или их типы.
	 */
	private Object getResult(Map<String, Object> response) throws RmiException {
		String exception = (String) response.get("exception");
		if (exception != null) {
			throw new RmiException(exception);
		}

		Object result = response.get("result");
		if (result != null) {
			logger.info("Response received: " + "ID = " + response.get("requestId") + ", result = " + result);
		} else {
			logger.info("Response received: " + "ID = " + response.get("requestId") +
				", method with return type \"void\" invoked successful!");
		}

		return result;
	}
}
