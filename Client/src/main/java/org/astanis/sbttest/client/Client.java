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
 * Клиент для удаленного вызова методов. Устанавливает соединение с удаленным
 * сервером и производит вызов методов у определенных сервисов. Производит
 * логирование отправленных запросов и полученных ответов.
 *
 * @author dkgraf
 */
public class Client {
	private final String host;
	private final int port;
	private final AtomicInteger uniqueId = new AtomicInteger(0);
	private final Logger logger = Logger.getLogger(Client.class);
	private final Map<Integer, Map<String, Object>> unusedResponses = new ConcurrentHashMap<>();
	private ObjectInputStream in;
	private ObjectOutputStream out;

	/**
	 * Создает клиента с указанным хостом и портов. Инициирует создание
	 * подключения у серверу.
	 *
	 * @param host Хост, на котором находится сервер.
	 * @param port Порт, на котором сервер ожидает подключение.
	 */
	public Client(String host, int port) {
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
			System.err.println("Server unavailable!");
			e.printStackTrace();
		}
	}

	/**
	 * Метод, осуществляющий удаленный вызов. Осуществляет отправку
	 *
	 * @param serviceName Имя сервиса, у которога будет производится вызов метода.
	 * @param methodName  Название вызываемого метода.
	 * @param params      Массив аргументов, с которыми будет вызываться метод.
	 * @return Ответ сервера, полученный на удаленный вызов метода.
	 */
	public Object remoteCall(String serviceName, String methodName, Object[] params) {
		int requestId = uniqueId.incrementAndGet();
		Map<String, Object> request = createRequest(requestId, serviceName, methodName, params);

		try {
			synchronized (out) {
				out.writeObject(request);
			}

			logger.info("Sending request: " + "ID = " + requestId + ", serviceName = " + serviceName +
				", methodName = " + methodName + ", params = " + Arrays.toString(params));
		} catch (IOException e) {
			e.printStackTrace();
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
					response = unusedResponses.get(requestId);
				} else {
					synchronized (in) {
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
			e.printStackTrace();
		}

		return response;
	}

	/**
	 * Получает результат выполнения удаленного вызова.
	 *
	 * @param response Объект, представляющий собой ответ сервера.
	 * @return Результат выполнения удаленного вызова.
	 */
	private Object getResult(Map<String, Object> response) {
		String exception = (String) response.get("exception");
		if (exception != null) {
			try {
				throw new RmiException(exception);
			} catch (RmiException e) {
				e.printStackTrace();
			}
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
