package com.milkliver.deploytest;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import net.sf.json.JSONObject;

@Controller
public class MainController {

	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	// @Value("${version}")
	// String version;

	private Counter requestCount;

	static String version = "v6.9.5";

	static Map statusProbability = new HashedMap();

	@Value("${environment}")
	String environment;

	public MainController(CollectorRegistry collectorRegistry) {
		requestCount = Counter.build().name("request_count").help("Number of requests.").labelNames("request_name")
				.register(collectorRegistry);
	}

	public int randomHttpStatusCode() {
		Random random = new Random();
		float randomFloat = random.nextFloat();

		// HTTP 500
		if (randomFloat >= 0 && randomFloat < 0.05) {
			return 500;
		}
		// HTTP 400
		else if (randomFloat >= 0.05 && randomFloat < 0.15) {
			return 400;
		}
		// HTTP 300
		else if (randomFloat >= 0.15 && randomFloat < 0.3) {
			return 300;
		}
		// HTTP 100
		// else if (randomFloat >= 0.3 && randomFloat < 0.5) {
		// return 100;
		// }
		// HTTP 200
		else {
			return 200;
		}
	}

	@ResponseBody
	@GetMapping(value = { "/nyahello" })
	public String nyahello(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("nyahello ...");
		requestCount.labels("nyahello").inc();
		log.info("nyahello finish");
		return "nya hello ~~";
	}

	@ResponseBody
	@GetMapping(value = { "/nyahelloReset" })
	public String nyahelloReset(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("nyahello reset ...");
		// requestCount.clear();
		requestCount.remove("nyahello");
		log.info("nyahello reset finish");
		return "nya hello reset ~~";
	}

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

	@ResponseBody
	@RequestMapping(value = "/webhook", method = { RequestMethod.GET, RequestMethod.POST })
	public String getWebhook(HttpServletRequest request, HttpServletResponse response) {

		log.info(request.getServletPath().toString() + " ...");

		Random random = new Random();

		String line;
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = request.getReader().readLine()) != null) {
				sb.append(line);
			}

			if (sb.toString().replace(" ", "").equals("")) {
				log.error(request.getServletPath().toString() + " not content");
				return "meow?";
			}
			log.info(sb.toString());

			log.info(request.getServletPath().toString() + " finish");

		} catch (IOException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.info(request.getServletPath().toString() + " Error");
		}

		return "receive webhook: " + sb.toString();
	}

	@ResponseBody
	@GetMapping(value = { "/randomResponseReset" })
	public String randomResponseReset(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("randomResponse reset ...");
		requestCount.remove("randomResponse");
		log.info("randomResponse reset finish");
		return "randomResponse reset finish";
	}

	@ResponseBody
	@RequestMapping(value = "/randomResponse", method = { RequestMethod.GET, RequestMethod.POST })
	public String randomResponse(HttpServletRequest request, HttpServletResponse response) {

		log.info(request.getServletPath().toString() + " ...");

		int httpStatusCode = randomHttpStatusCode();

		response.setStatus(httpStatusCode);
		log.info("http status code: " + String.valueOf(httpStatusCode));

		requestCount.labels("randomResponse").inc();

		String line;
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = request.getReader().readLine()) != null) {
				sb.append(line);
			}

			if (sb.toString().replace(" ", "").equals("")) {
				log.error(request.getServletPath().toString() + " not content");
				return "meow?";
			}
			log.info(sb.toString());

			log.info(request.getServletPath().toString() + " finish");

		} catch (IOException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.info(request.getServletPath().toString() + " Error");
		}

		return "randomResponse receive webhook: " + sb.toString();
	}

}
