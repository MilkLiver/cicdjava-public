package com.milkliver.deploytest.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.tomcat.jni.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ConnectTestFunctions {

	private static final Logger log = LoggerFactory.getLogger(ConnectTestFunctions.class);

	static int pingWayNumber = 0;

	@Value("${pingWay:0}")
	int pingWay;

	/*
	 * Example: icmp("www.google.com", 5000); tcp("127.0.0.1", 10000, 5000);
	 * http("http://127.0.0.1:8084/say", "POST", 1000,5000);
	 * https("https://127.0.0.1:8443/say", "POST", 1000,5000);
	 */

	@PostConstruct
	public void name() {
		pingWayNumber = pingWay;
	}

	public static boolean icmp(String host, int timeOut) {

		try {

			if (pingWayNumber == 1) {

				log.info("start ping " + host + " (InetAddress ping) ...");
				boolean status = InetAddress.getByName(host).isReachable(timeOut);
				log.info("ping " + host + " (InetAddress ping) finish");

				return status;
			} else {
				log.info("start ping " + host + " (console ping) ...");
				boolean status = ping(host, 1, timeOut);
				log.info("ping " + host + " (console ping) finish");

				return status;

			}

		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			return false;
		}
	}

	public static boolean ping(String ipAddress, int pingTimes, int timeOut) {
		BufferedReader in = null;
		Runtime r = Runtime.getRuntime();

		String pingCommand = null;

		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			// 將要執行的ping命令,此命令是windows格式的命令
			pingCommand = "ping " + ipAddress + " -n " + pingTimes + " -w " + timeOut;

		} else {
			pingCommand = "ping " + ipAddress + " -c " + pingTimes + " -W " + timeOut / 1000;
		}

		try { // 執行命令並獲取輸出
			log.info(pingCommand);
			Process p = r.exec(pingCommand);
			if (p == null) {
				return false;
			}
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			// 逐行檢查輸出,計算類似出現=23ms TTL=62字樣的次數
			int connectedCount = 0;
			String line = null;
			while ((line = in.readLine()) != null) {
				connectedCount += getCheckResult(line);
			} // 如果出現類似=23ms TTL=62這樣的字樣,出現的次數=測試次數則返回真
			return connectedCount == pingTimes;
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			return false;
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				log.error(e.getMessage());
				for (StackTraceElement elem : e.getStackTrace()) {
					log.error(elem.toString());
				}

			}
		}
	}

	private static int getCheckResult(String line) {
		log.info("console output: " + line);

		try {

			Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)([Tt][Tt][Ll]=\\d+)", Pattern.CASE_INSENSITIVE);
			Pattern pattern2 = Pattern.compile("([Tt][Tt][Ll])=\\d+(\\s+)[Tt][Ii][Mm][Ee]=(.*)(?>ms)",
					Pattern.CASE_INSENSITIVE);

			Matcher matcher = pattern.matcher(line);
			Matcher matcher2 = pattern2.matcher(line);

			while (matcher.find()) {
				return 1;
			}
			while (matcher2.find()) {
				return 1;
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			return 0;
		}
		return 0;
	}

	public static Map http(String connectUrl, String method, int connectTimeOut, int readTimeOut) {
		log.info("start connect http " + connectUrl + " ...");

		URL url;
		HttpURLConnection con;
		Map returnInfos = new HashMap();
		int responseCode = 0;

		try {
			url = new URL(connectUrl);

			con = (HttpURLConnection) url.openConnection();
			// 設定方法為GET
			con.setRequestMethod(method);
			con.setConnectTimeout(connectTimeOut);
			con.setReadTimeout(readTimeOut);
			con.setUseCaches(false);
			con.setDoOutput(true);
//			con.getResponseCode();
//			InputStream is = con.getInputStream();
			responseCode = con.getResponseCode();
			returnInfos.put("statusCode", responseCode);
			if (String.valueOf(responseCode).substring(0, 1).equals("4")
					|| String.valueOf(responseCode).substring(0, 1).equals("5")) {
				returnInfos.put("status", false);
			}
			returnInfos.put("status", true);
			log.info("connect http " + connectUrl + " finish");

			return returnInfos;

		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("status", false);
			return returnInfos;
		} catch (IOException e) {
			log.error("responseCode: " + String.valueOf(responseCode));
			log.error("connect http " + connectUrl + " fail");
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("statusCode", 408);
			returnInfos.put("status", false);
			return returnInfos;
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("status", false);
			return returnInfos;
		}
	}

	public static Map https(String connectUrl, String method, int connectTimeOut, int readTimeOut) {
		log.info("start connect https " + connectUrl + " ...");

		SSLContext sslcontext;
		HttpsURLConnection con;
		Map returnInfos = new HashMap();
		try {
			sslcontext = SSLContext.getInstance("SSL", "SunJSSE");

			sslcontext.init(null, new TrustManager[] { new MyX509TrustManager() }, new java.security.SecureRandom());
			URL url = new URL(connectUrl);
			HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
				public boolean verify(String s, SSLSession sslsession) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
			// 之後任何Https協議網站皆能正常訪問
			con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod(method);
			con.setRequestProperty("Content-type", "application/json");
			// 必須設置為false，否則會自動redirect到重定向後的地址
			con.setInstanceFollowRedirects(false);
			con.setConnectTimeout(connectTimeOut);
			con.setReadTimeout(readTimeOut);
			con.setUseCaches(false);
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			returnInfos.put("statusCode", responseCode);
			if (String.valueOf(responseCode).substring(0, 1).equals("4")
					|| String.valueOf(responseCode).substring(0, 1).equals("5")) {
				returnInfos.put("status", false);
			}
			returnInfos.put("status", true);
//			con.connect();
			log.info("connect https " + connectUrl + " finish");

			return returnInfos;

		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("status", false);
			return returnInfos;
		} catch (NoSuchProviderException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("status", false);
			return returnInfos;
		} catch (KeyManagementException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("status", false);
			return returnInfos;
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("status", false);
			return returnInfos;
		} catch (IOException e) {
			log.info("connect https " + connectUrl + " fail");
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("statusCode", 408);
			returnInfos.put("status", false);
			return returnInfos;
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			returnInfos.put("status", false);
			return returnInfos;
		}
	}

	public static boolean tcp(String ipAddress, int port, int connectTimeout, int readTimeout) {
		log.info("start connect tcp " + ipAddress + ":" + port + " ...");
		try {
			Socket socket = new Socket();
			socket.setSoTimeout(readTimeout);
			socket.connect(new InetSocketAddress(ipAddress, port), connectTimeout);
//			InputStream inFromServer = socket.getInputStream();
//			DataInputStream in = new DataInputStream(inFromServer);
//			inFromServer.read();
			log.info("connect tcp " + ipAddress + ":" + port + " finish");
			return true;
		} catch (IOException e) {
			log.info("connect tcp " + ipAddress + ":" + port + " fail");
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			return false;
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
			return false;
		}
	}

}
