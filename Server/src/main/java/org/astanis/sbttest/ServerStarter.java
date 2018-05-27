package org.astanis.sbttest;

import org.astanis.sbttest.server.Server;
import org.astanis.sbttest.server.ServerImpl;

/**
 * Запускает сервер, на порту, переданному в качестве аргумента командной
 * строки. Если аргументы отсутствую, то сервер запускается на порту 9999.
 */
public class ServerStarter {
	public static void main(String[] args) {
		Server server;
		if (args.length >= 1) {
			int port = Integer.parseInt(args[0]);
			server = new ServerImpl(port);
		} else {
			server = new ServerImpl();
		}
		server.run();
	}
}
