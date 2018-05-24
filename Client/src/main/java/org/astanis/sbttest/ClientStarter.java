package org.astanis.sbttest;

import org.apache.log4j.Logger;
import org.astanis.sbttest.client.Client;

import java.util.Random;

public class ClientStarter {
	public static void main(String[] args) {
//		String host = args[0];
//		int port = Integer.parseInt(args[1]);
//		Client client = new Client(host, port);
		Client client = new Client("localhost", 9999);
		for (int i = 0; i < 10; i++) {
			new Thread(new Caller(client)).start();
		}
	}

	private static class Caller implements Runnable {
		private Logger logger = Logger.getLogger(Caller.class);
		private Client client;

		Caller(Client cclient) {
			this.client = cclient;
		}

		@SuppressWarnings("InfiniteLoopStatement")
		public void run() {
			while (true) {
				Random random = new Random();
				try {
					Thread.sleep((long) random.nextInt(3000) + 2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				client.remoteCall("service1", "sleep", new Object[]{1000L});
				logger.info("Current Date is:" + client.remoteCall("service1", "getCurrentDate", new Object[]{}));

			}
		}
	}
}
