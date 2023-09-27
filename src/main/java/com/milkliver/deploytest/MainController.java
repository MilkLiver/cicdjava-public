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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.milkliver.deploytest.files.FileDownloadUtil;
import com.milkliver.deploytest.files.FileTest;
import com.milkliver.deploytest.monitoring.TestPrometheusMetrics;
import com.milkliver.deploytest.simulator.SleepFunction;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

@Controller
public class MainController {

	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	// @Value("${version}")
	// String version;

	static final String showPage = "before";
	// static final String showPage = "after";

	private Counter requestCount;
	private Counter counter01;
	private Counter counter02;

	static String version = "v6.18.2";

	// static String mutateStr = "[{ \"op\": \"add\", \"path\":
	// \"/metadata/labels/foo\", \"value\": \"bar\" }]";

	@Value("${mutate.command}")
	String mutateCommand;

	static Map statusProbability = new HashMap();

	@Value("${environment}")
	String environment;

	@Autowired
	FileTest fileTest;

	@Autowired
	TestPrometheusMetrics testPrometheusMetrics;

	@Autowired
	SleepFunction sleepFunction;

	@Autowired
	FileDownloadUtil fileDownloadUtil;

	public MainController(CollectorRegistry collectorRegistry) {
		requestCount = Counter.build().name("request_count").help("Number of requests.").labelNames("request_name")
				.register(collectorRegistry);

		counter01 = Counter.build().name("test_count01").help("Test for counter, Counter01, Number of requests.")
				.labelNames("request_name").register(collectorRegistry);

		counter02 = Counter.build().name("test_count02").help("Test for counter, Counter02, Number of requests.")
				.labelNames("request_name").register(collectorRegistry);

	}

//	@GetMapping("/downloadFile/{fileCode}")
	@ResponseBody
	@RequestMapping(value = "/downloadFile/{fileCode}")
	public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode) {
		log.info("fileCode: " + fileCode);

		Resource resource = null;
		try {
			resource = fileDownloadUtil.getFileAsResource(fileCode);
		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		}

		if (resource == null) {
			return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
		}

		String contentType = "application/octet-stream";
		String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header("Last-Modified", "Sun, 21 Jan 2018 01:02:03 GMT").header(HttpHeaders.CONTENT_DISPOSITION, headerValue).body(resource);
	}

	@ResponseBody
	@RequestMapping(value = "/downloadRawFile/{filename}", method = { RequestMethod.GET, RequestMethod.POST })
	public String downloadRawFile(Model model, HttpServletRequest request, HttpServletResponse response,
			@PathVariable("filename") String filename) {
		log.info(this.getClass().getName() + " downloadRawFile " + filename + " ...");

		fileTest.downloadFilesWithRawData(filename);

		log.info(this.getClass().getName() + " downloadRawFile " + filename + " finish");
		return "downloadRawFile " + filename + " finish";
	}

	@ResponseBody
	@RequestMapping(value = "/testDownload01/{fileName}", method = { RequestMethod.GET, RequestMethod.POST })
	public String testDownload01(Model model, HttpServletRequest request, HttpServletResponse response,
			@PathVariable("fileName") String fileName) {

		log.info(this.getClass().getName() + " " + Thread.currentThread().getStackTrace()[1].getMethodName() + " ...");

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
		log.info(this.getClass().getName() + " " + Thread.currentThread().getStackTrace()[1].getMethodName()
				+ " finish");
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

//	============================= Prometheus Test Counter01 ============================= 

	@ResponseBody
	@GetMapping(value = { "/prometheus/test/counter01/inc" })
	public String prometheusTestCounter01Inc(Model model, HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "label", required = false) String label) {

		log.info("prometheusTestCounter01Inc ...");

		String defaultLabelName = "test";
		String finalLabelName = defaultLabelName;

		if (label == null || "".equals(label.trim())) {
			finalLabelName = defaultLabelName;
		} else {
			finalLabelName = label;
		}
		counter01.labels(finalLabelName).inc();

		log.info("prometheusTestCounter01Inc finish");

		return "Counter01 label: " + finalLabelName + " inc";
	}

	@ResponseBody
	@GetMapping(value = { "/prometheus/test/counter01/remove" })
	public String prometheusTestCounter01Remove(Model model, HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "label", required = false) String label) {

		log.info("prometheusTestCounter01Remove ...");

		String defaultLabelName = "test";
		String finalLabelName = defaultLabelName;

		if (label == null || "".equals(label.trim())) {
			finalLabelName = defaultLabelName;
		} else {
			finalLabelName = label;
		}
		counter01.remove(finalLabelName);

		log.info("prometheusTestCounter01Remove finish");

		return "Counter01 label: " + finalLabelName + " remove";
	}

//	============================= Prometheus Test Counter02 ============================= 

	@ResponseBody
	@GetMapping(value = { "/prometheus/test/counter02/inc" })
	public String prometheusTestCounter02Inc(Model model, HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "label", required = false) String label) {

		log.info("prometheusTestCounter02Inc ...");

		String defaultLabelName = "test";
		String finalLabelName = defaultLabelName;

		if (label == null || "".equals(label.trim())) {
			finalLabelName = defaultLabelName;
		} else {
			finalLabelName = label;
		}
		counter02.labels(finalLabelName).inc();

		log.info("prometheusTestCounter02Inc finish");

		return "Counter02 label: " + finalLabelName + " inc";
	}

	@ResponseBody
	@GetMapping(value = { "/prometheus/test/counter02/remove" })
	public String prometheusTestCounter02Remove(Model model, HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "label", required = false) String label) {

		log.info("prometheusTestCounter02Remove ...");

		String defaultLabelName = "test";
		String finalLabelName = defaultLabelName;

		if (label == null || "".equals(label.trim())) {
			finalLabelName = defaultLabelName;
		} else {
			finalLabelName = label;
		}
		counter02.remove(finalLabelName);

		log.info("prometheusTestCounter02Remove finish");

		return "Counter02 label: " + finalLabelName + " remove";
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
			// JSONObject json = JSONObject.fromObject(sb.toString());
			// Map m = (Map) json;

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

	// @ResponseBody
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

			log.info(request.getServletPath().toString() + " finish");

		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.info(request.getServletPath().toString() + " Error");
		}

		return "randomResponse http status Code: " + String.valueOf(httpStatusCode);
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

	@ResponseBody
	@RequestMapping(value = "/randomLatency", method = { RequestMethod.GET, RequestMethod.POST })
	public String randomLatency(HttpServletRequest request, HttpServletResponse response) {

		log.info(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + " "
				+ request.getServletPath().toString() + " ...");

		int httpTimeout = randomHttpLatency();

		log.info("http latency: " + String.valueOf(httpTimeout));

		requestCount.labels("randomLatency").inc();

		String line;
		StringBuilder sb = new StringBuilder();
		try {

			Thread.sleep(httpTimeout);

		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			log.info(request.getServletPath().toString() + " Error");
		}
		log.info(this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + " "
				+ request.getServletPath().toString() + " finish");
		return "randomLatency http latency: " + String.valueOf(httpTimeout);
	}

	@PostConstruct
	public int randomHttpLatency() {

		int L1Min = 10;
		int L1Max = 100;

		int L2Min = 100;
		int L2Max = 200;

		int L3Min = 200;
		int L3Max = 750;

		int L4Min = 750;
		int L4Max = 2000;

		Random randoI = new Random();
		Random randomF = new Random();

		float randomFloat = randomF.nextFloat();

		// ======================================================
		if (randomFloat >= 0 && randomFloat < 0.05) {
			return randoI.nextInt((L4Max - L4Min) + 1) + L4Min;
		}
		// ------------------------------------------------------
		else if (randomFloat >= 0.05 && randomFloat < 0.15) {
			return randoI.nextInt((L3Max - L3Min) + 1) + L3Min;
		}
		// ------------------------------------------------------
		else if (randomFloat >= 0.15 && randomFloat < 0.3) {
			return randoI.nextInt((L2Max - L2Min) + 1) + L2Min;
		}
		// ------------------------------------------------------
		else {
			return randoI.nextInt((L1Max - L1Min) + 1) + L1Min;
		}
		// ======================================================

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

}
