package org.astanis.sbttest.services;

import java.util.Date;

public class Service1 {
	public void sleep(Long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Date getCurrentDate() {
		return new Date();
	}
}
