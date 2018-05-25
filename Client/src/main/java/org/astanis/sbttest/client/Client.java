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

public class Client {
	private final String host;
	private final int port;
	private final AtomicInteger uniqueId = new AtomicInteger(0);
	private final Logger logger = Logger.getLogger(Client.class);
	private final Map<Integer, Map<String, Object>> unusedResponses = new ConcurrentHashMap<>();
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public Client(String host, int port) {
		this.host = host;
		this.port = port;
		openConnection();
	}

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

	public Object remoteCall(String serviceName, String methodName, Object[] params) {
		int requestId = uniqueId.incrementAndGet();
		Map<String, Object> request = createRequest(requestId, serviceName, methodName, params);
		Object result = null;

		try {
			synchronized (out) {
				out.writeObject(request);
			}
			logger.info("Sending request: " + "ID = " + requestId + ", serviceName = " + serviceName +
				", methodName = " + methodName + ", params = " + Arrays.toString(params));

			Map<String, Object> response = getResponse(requestId);

			String exception = (String) response.get("exception");
			if (exception != null) {
				throw new RmiException(exception);
			}

			result = response.get("result");
			if (result != null) {
				logger.info("Response reieved: " + "ID = " + response.get("requestId") + ", result = " + result);
			} else {
				logger.info("Response reieved: " + "ID = " + response.get("requestId") +
					", method with return type \"void\" invoked successful!");
			}
		} catch (IOException | RmiException e) {
			e.printStackTrace();
		}

		return result;
	}

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

	private Map<String, Object> createRequest(int requestId, String serviceName, String methodName, Object[] params) {
		Map<String, Object> request = new HashMap<>();
		request.put("requestId", requestId);
		request.put("serviceName", serviceName);
		request.put("methodName", methodName);
		request.put("params", params);

		return request;
	}
}

