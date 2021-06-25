package com.milkliver.deploytest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	@Value("${version}")
	String version;

	@GetMapping(value = { "/instanatest" })
	public String instanatest(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("test version: " + version + " ...");
		log.info("test version: " + version + " finish");
		return "test";
	}

	@GetMapping(value = { "/instanalongtest" })
	public String instanalongtest(Model model, HttpServletRequest request, HttpServletResponse response) {
		try {
			log.info("longtest version: " + version + " ...");
			Thread.sleep(30000);
			log.info("longtest version: " + version + " finish");
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
		}
		return "longtest";
	}

	@ResponseBody
	@GetMapping(value = { "/" })
	public String home(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("test server " + version);
		return "test server " + version;
	}

	@ResponseBody
	@GetMapping(value = { "/kuro" })
	public String kuro(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("kero~kero~");
		return "kero~kero~";
	}

	@ResponseBody
	@GetMapping(value = { "/test" })
	public String test(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("test version: " + version);
		return "test version: " + version;
	}

	@ResponseBody
	@GetMapping(value = { "/longtest" })
	public String longtest(Model model, HttpServletRequest request, HttpServletResponse response) {
		try {
			log.info("longtest version: " + version + " ...");
			Thread.sleep(30000);
			log.info("longtest version: " + version + " finish");
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
		}
		return "longtest version: " + version;
	}

}
