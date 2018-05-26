package org.astanis.sbttest;

import org.apache.log4j.Logger;
import org.astanis.sbttest.client.Client;
import org.astanis.sbttest.client.ClientImpl;
import org.astanis.sbttest.exception.RmiException;

import java.util.Random;

/**
 * Запускает клиент. Хост и порт сервера передаются в качестве аргументов
 * командной строки. Первый аргумент - хост, второй - порт.
 */
public class ClientStarter {
	public static void main(String[] args) {
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		Client client = new ClientImpl(host, port);
		for (int i = 0; i < 10; i++) {
			new Thread(new Caller(client)).start();
		}
	}

	private static class Caller implements Runnable {
		private Logger logger = Logger.getLogger(Caller.class);
		private Client client;

		Caller(Client client) {
			this.client = client;
		}

		@SuppressWarnings("InfiniteLoopStatement")
		public void run() {
			while (true) {
				Random random = new Random();
				try {
					Thread.sleep((long) random.nextInt(2500) + 500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					client.remoteCall("service1", "sleep", new Object[]{1000L});
					logger.info("Current Date is: " + client.remoteCall("service1", "getCurrentDate", new Object[]{}));
					Integer x = new Random().nextInt(1000);
					Integer y = new Random().nextInt(1000);
					logger.info(x.toString() + " multiply by " + y.toString() + " equals " +
						client.remoteCall("service2", "multiply", new Object[]{x, y}));
				} catch (RmiException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
