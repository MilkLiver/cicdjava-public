package com.milkliver.deploytest;

import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
public class MainController {

	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	// @Value("${version}")
	// String version;

	static String version = "v6.3.3";

	@Value("${environment}")
	String environment;

	@GetMapping(value = { "/instanatest" })
	public String instanatest(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("instanatest ...");

		log.info("instanatest finish");
		return "test";
	}

	@GetMapping(value = { "/instanajstest" })
	public String instanajstest(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("instanajstest ...");

		log.info("instanajstest finish");
		return "testjs";
	}

	@GetMapping(value = { "/instana401test" })
	public String instana401test(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("instana401test ...");

		response.setStatus(401);

		log.info("instana401test finish");
		return "test401";
	}

	@GetMapping(value = { "/instanalongtest" })
	public String instanalongtest(Model model, HttpServletRequest request, HttpServletResponse response) {
		try {
			log.info("instanalongtest ...");
			float p = 0.5f;
			Random random = new Random();

			boolean randomResult = random.nextFloat() < p;

			randomResult = true;

			log.info("boolean: " + String.valueOf(randomResult));

			if (randomResult) {
				Thread.sleep(30000);
			}

			log.info("instanalongtest finish");
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
		}
		return "longtest";
	}

	@ResponseBody
	@RequestMapping(value = "/instanawebhook", method = { RequestMethod.GET, RequestMethod.POST })
	public String instanawebhook(Model model, HttpServletRequest request, HttpServletResponse response) {
		String line;
		StringBuilder sb = new StringBuilder();
		log.info("instanawebhook ...");
		try {
			while ((line = request.getReader().readLine()) != null) {
				sb.append(line);
			}

			log.info(sb.toString());
			// 將body裡的內容解析成JSON
			JSONObject json = JSONObject.fromObject(sb.toString());
			Map m = (Map) json;

			Map issueMap = new HashedMap((Map) m.get("issue"));
			log.info("msgid: " + String.valueOf(issueMap.get("text")));

		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
		}
		log.info("instanawebhook finish");
		return sb.toString();
	}

	@ResponseBody
	@GetMapping(value = { "/" })
	public String home(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("test server: " + version + " environment: " + environment);
		return "test server: " + version + " environment: " + environment;
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
		log.info("test version: " + version + " environment: " + environment);
		return "test version: " + version + " environment: " + environment;
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
