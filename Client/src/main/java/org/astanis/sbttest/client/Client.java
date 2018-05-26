package org.astanis.sbttest.client;

import org.astanis.sbttest.exception.RmiException;

/**
 * Интерфейс клиента для удаленного вызова методов.
 *
 * @author dkgraf
 */
public interface Client {
	/**
	 * Метод, осуществляющий удаленный вызов.
	 *
	 * @param serviceName Имя сервиса, у которога будет производится вызов метода.
	 * @param methodName  Название вызываемого метода.
	 * @param params      Массив аргументов, с которыми будет вызываться метод.
	 * @return Ответ сервера, полученный на удаленный вызов метода.
	 */
	Object remoteCall(String serviceName, String methodName, Object[] params) throws RmiException;
}
