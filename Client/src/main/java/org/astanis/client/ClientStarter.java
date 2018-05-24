package org.astanis.client;

public class ClientStarter {
	public static void main(String[] args) {
//		String host = args[0];
//		int port = Integer.parseInt(args[1]);
//		Client client = new Client(host, port);
		Client client = new Client("localhost", 9999);

	}
}
