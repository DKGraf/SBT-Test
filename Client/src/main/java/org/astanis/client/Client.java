package org.astanis.client;

import org.apache.log4j.Logger;

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
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Server unavailable!");
			e.printStackTrace();
		}
	}

	public Object remoteCall(String serviceName, String methodName, Object[] params) {
		int id = requestId.incrementAndGet();
		Map<String, Object> request = new HashMap<>();
		request.put("id", id);
		request.put("serviceName", serviceName);
		request.put("methodName", methodName);
		request.put("params", params);

		try {
			out.writeObject(request);
			logger.info("Sending request: " + "id = " + id + ", serviceName = " + serviceName +
				", methodName = " + methodName + ", params = " + Arrays.toString(params));
		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
	}

	private static class Caller implements Runnable {
		private Logger logger = Logger.getLogger(Caller.class);
		private Client client;

		public Caller(Client cclient) {
			this.client = cclient;
		}

		public void run() {
			while (true) {
				client.remoteCall("service1", "sleep", new Object[]{1000L});
				logger.info("Current Date is:" + client.remoteCall("service1", "getCurrentDate", new Object[]{}));
			}
		}
	}
}

