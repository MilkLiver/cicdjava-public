package com.milkliver.deploytest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milkliver.deploytest.monitoring.TestPrometheusMetrics;
import com.milkliver.deploytest.simulator.SleepFunction;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

@Controller
public class MainController {

	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	// @Value("${version}")
	// String version;

//	static final String showPage = "before";
	static final String showPage = "after";

	private Counter requestCount;

	static String version = "v6.14.5";

//	static String mutateStr = "[{ \"op\": \"add\", \"path\": \"/metadata/labels/foo\", \"value\": \"bar\" }]";

	@Value("${mutate.command}")
	String mutateCommand;

	static Map statusProbability = new HashMap();

	@Value("${environment}")
	String environment;

	@Autowired
	TestPrometheusMetrics testPrometheusMetrics;

	@Autowired
	SleepFunction sleepFunction;

	@ResponseBody
	@RequestMapping(value = "/testDownload01/{fileName}", method = { RequestMethod.GET, RequestMethod.POST })
	public String testDownload01(Model model, HttpServletRequest request, HttpServletResponse response,
			@PathVariable("fileName") String fileName) {
		log.info(this.getClass().getName() + " download file: " + fileName + " ...");

		File file = null;

		try {
			file = new ClassPathResource("static/img/" + fileName + ".jpg").getFile();

			response.reset();
			response.setContentType("application/octet-stream");
			response.setCharacterEncoding("utf-8");
			response.setContentLength((int) file.length());
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".jpg");
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
				byte[] buff = new byte[1024];
				OutputStream os = response.getOutputStream();
				int i = 0;
				while ((i = bis.read(buff)) != -1) {
					os.write(buff, 0, i);
					os.flush();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				for (StackTraceElement elem : e.getStackTrace()) {
					log.error(elem.toString());
				}
				return "download file: " + fileName + " error";
			}
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			return e.getMessage();
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
		}

		log.info(this.getClass().getName() + " download file: " + fileName + " finish");
		return "download file: " + fileName + " finish";
	}

	@ResponseBody
	@RequestMapping(value = "/sleep/{sleepTime}", method = { RequestMethod.GET, RequestMethod.POST })
	public String sleep(Model model, HttpServletRequest request, HttpServletResponse response,
			@PathVariable("sleepTime") int sleepTime) {
		log.info(this.getClass().getName() + " sleep " + String.valueOf(sleepTime) + " seconds ...");

		sleepFunction.sleep(sleepTime);

		log.info(this.getClass().getName() + " sleep " + String.valueOf(sleepTime) + " seconds finish");
		return "sleep " + String.valueOf(sleepTime) + " seconds finish";
	}

	@ResponseBody
	@RequestMapping(value = "/showHeaders", method = { RequestMethod.GET, RequestMethod.POST })
	public String showHeaders(Model model, HttpServletRequest request, HttpServletResponse response) {
		log.info("showHeaders ...");

		StringBuilder res = new StringBuilder();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = String.valueOf(headerNames.nextElement());
			log.info("header: " + headerName + " value: " + String.valueOf(request.getHeader(headerName)));
			res.append(
					"header: " + headerName + " value: " + String.valueOf(request.getHeader(headerName)) + "  <br>  ");
		}

		log.info("showHeaders finish");
		return res.toString();
	}

	@ResponseBody
	@GetMapping(value = { "/setTestMetric01" })
	public String setTestMetric01(Model model, HttpServletRequest request, HttpServletResponse response,
			@RequestParam int value) {
		log.info("setTestMetric01 value: " + String.valueOf(value) + " ...");

		testPrometheusMetrics.setTestMetric01(value);

		log.info("setTestMetric01 value: " + String.valueOf(value) + " finish");
		return "setTestMetric01 value: " + String.valueOf(value);
	}

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
	@PostMapping(value = { "/mutate" }, produces = "application/json")
	public String mutate(Model model, HttpServletRequest request, HttpServletResponse response)
			throws JsonProcessingException {
		log.info("mutate ...");

		String encodedString = Base64.getEncoder().encodeToString(mutateCommand.getBytes());

		ObjectMapper returnJsonOM = new ObjectMapper();
		String returnJsonStr = "";

		Map mutateReturnJsonMap = new HashMap();
		Map responseJsonMap = new HashMap();
		responseJsonMap.put("allowed", true);
		responseJsonMap.put("patch", encodedString);
		responseJsonMap.put("patchType", "JSONPatch");
		mutateReturnJsonMap.put("response", responseJsonMap);
		returnJsonStr = returnJsonOM.writeValueAsString(mutateReturnJsonMap);

		log.info("mutate finish");
		return returnJsonStr;
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
		ObjectMapper objectMapper = new ObjectMapper();

		log.info("instanawebhook ...");
		try {
			while ((line = request.getReader().readLine()) != null) {
				sb.append(line);
			}

			log.info(sb.toString());
			// 將body裡的內容解析成JSON
//			JSONObject json = JSONObject.fromObject(sb.toString());
//			Map m = (Map) json;

			Map m = objectMapper.readValue(sb.toString(), new TypeReference<Map>() {
			});

			Map issueMap = (Map) m.get("issue");
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

//	@ResponseBody
	@GetMapping(value = { "/" })
	public String home(Model model, HttpServletRequest request, HttpServletResponse response) {
		model.addAttribute("environment", environment);
		model.addAttribute("version", version);
		log.info("test server: " + version + " environment: " + environment);
		return showPage;
	}

	@ResponseBody
	@GetMapping(value = { "/version" })
	public String version(Model model, HttpServletRequest request, HttpServletResponse response) {
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

	@RequestMapping(value = "/randomResponsePage", method = { RequestMethod.GET, RequestMethod.POST })
	public String randomResponsePage(HttpServletRequest request, HttpServletResponse response) {

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

		return "randomHttpStatusCode";
	}

}
