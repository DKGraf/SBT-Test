package org.astanis.sbttest.exception;

/**
 * Класс, представляющий собой ошибку, выбрасываемую в сдучае,
 * если при вызове удаленного метода произовшла ошибка:
 * неверное имя сервиса, неверное имя метода, неверный тип или
 * количество аргументов, передаваемых для вызова метода.
 */
public class RmiException extends Exception {
	public RmiException() {
	}

	public RmiException(String message) {
		super(message);
	}
}
