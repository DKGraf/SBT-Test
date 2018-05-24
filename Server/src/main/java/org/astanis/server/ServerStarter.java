package org.astanis.server;

public class ServerStarter {
	public static void main(String[] args) {
//		int port = Integer.parseInt(args[0]);
//		Server server = new Server(port);
		Server server = new Server(9999);
		server.run();
	}
}
