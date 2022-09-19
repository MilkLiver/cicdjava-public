package com.milkliver.deploytest.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SleepFunction {

	private static final Logger log = LoggerFactory.getLogger(SleepFunction.class);

	synchronized public void sleep(int sleepTime) {
		try {
			log.info("sleep for " + sleepTime + " seconds ...");
			Thread.sleep(sleepTime * 1000);
			log.info("sleep for " + sleepTime + " seconds finish");
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
		}
	}

}
