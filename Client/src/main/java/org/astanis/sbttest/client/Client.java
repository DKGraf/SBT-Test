package org.astanis.sbttest.client;

import org.apache.log4j.Logger;
import org.astanis.sbttest.exception.RMIException;

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
	private final AtomicInteger requestId = new AtomicInteger(0);
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
		int id = requestId.incrementAndGet();
		Map<String, Object> response;
		Map<String, Object> request = new HashMap<>();
		request.put("id", id);
		request.put("serviceName", serviceName);
		request.put("methodName", methodName);
		request.put("params", params);

		Object result = null;

		try {
			out.writeObject(request);
			logger.info("Sending request: " + "id = " + id + ", serviceName = " + serviceName +
				", methodName = " + methodName + ", params = " + Arrays.toString(params));

			response = (Map<String, Object>) in.readObject();

			String exception = (String) response.get("exception");
			if (exception != null) {
				throw new RMIException(exception);
			}

			result = response.get("result");
			logger.info("Response reieved: " + "id = " + response.get("id") + ", result = " + result);
		} catch (IOException | ClassNotFoundException | RMIException e) {
			e.printStackTrace();
		}


		return result;
	}
}

