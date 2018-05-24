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
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
	private final String host;
	private final int port;
	private final AtomicInteger uniqueId = new AtomicInteger(0);
	private final Logger logger = Logger.getLogger(Client.class);
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

	@SuppressWarnings("unchecked")
	public Object remoteCall(String serviceName, String methodName, Object[] params) {
		int requestId = uniqueId.incrementAndGet();
		Map<String, Object> response;
		Map<String, Object> request = new HashMap<>();
		request.put("requestId", requestId);
		request.put("serviceName", serviceName);
		request.put("methodName", methodName);
		request.put("params", params);

		Object result = null;

		try {
			out.writeObject(request);
			logger.info("Sending request: " + "ID = " + requestId + ", serviceName = " + serviceName +
				", methodName = " + methodName + ", params = " + Arrays.toString(params));

			response = (Map<String, Object>) in.readObject();

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
		} catch (IOException | ClassNotFoundException | RmiException e) {
			e.printStackTrace();
		}


		return result;
	}
}

