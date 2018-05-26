package org.astanis.sbttest;

import org.astanis.sbttest.server.Server;
import org.astanis.sbttest.server.ServerImpl;

/**
 * Запускает сервер, на порту, переданному в качестве аргумента командной строки.
 */
public class ServerStarter {
	public static void main(String[] args) {
		int port = Integer.parseInt(args[0]);
		Server server = new ServerImpl(port);
		server.run();
	}
}
