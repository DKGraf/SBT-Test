package org.astanis.sbttest.client;

import org.astanis.sbttest.exception.RmiException;
import org.astanis.sbttest.server.Server;
import org.astanis.sbttest.server.ServerImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ClientImplTest {
	private static Server server;

	@BeforeAll
	static void setUp() {
		server = new ServerImpl(9999);
		new Thread(server::run).start();
	}

	@Test
	void remoteCall() throws RmiException {
		Client client = new ClientImpl("localhost", 9999);

		Integer x = 10;
		Integer y = 15;
		Object result = client.remoteCall("service2", "multiply", new Object[]{x, y});
		Assertions.assertEquals(result, 150);
	}

	@Test
	void shouldThrowException1() {
		Client client = new ClientImpl("localhost", 9999);

		Executable closureContainingCodeToTest = () ->
			client.remoteCall("service2", "something", new Object[]{});
		Assertions.assertThrows(RmiException.class, closureContainingCodeToTest,
			"No such method or invalid arguments or invalid arguments count!");
	}

	@Test
	void shouldThrowException2() {
		Client client = new ClientImpl("localhost", 9999);

		Executable closureContainingCodeToTest = () ->
			client.remoteCall("wrongService", "multiply", new Object[]{});
		Assertions.assertThrows(RmiException.class, closureContainingCodeToTest,
			"No such service!");
	}
}