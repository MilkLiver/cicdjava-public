package com.milkliver.deploytest.files;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.web.reactive.AdditionalHealthEndpointPathsWebFluxHandlerMapping;
import org.springframework.stereotype.Component;

@Component
public class FileTest {
	private static final Logger log = LoggerFactory.getLogger(FileTest.class);

	@Value("${filetest.download.path}")
	String downloadPath;

	public void downloadFilesWithRawData(String filename) {
		try {
			log.info("downloadFilesWithRawData ...");

//			String url = "http://127.0.0.1:19527/img/before.jpg";
			String url = "http://192.168.2.181:19527/img/before.jpg";
//			String url = "https://github.com/prometheus/blackbox_exporter/releases/download/v0.24.0/blackbox_exporter-0.24.0.windows-amd64.zip";

			System.out.println("downloadPath: " + downloadPath);

//			------------------------------------------------------------
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);

			CloseableHttpResponse response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();

			InputStream inputStream = response.getEntity().getContent();
			Path outputPath = Path.of(downloadPath + "/test.png");
			Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("文件已下载到: " + outputPath);

//			------------------------------------------------------------

			log.info("downloadFilesWithRawData finish");
		} catch (Exception e) {
			log.error(e.getMessage());
			for (StackTraceElement elem : e.getStackTrace()) {
				log.error(elem.toString());
			}
		}
	}
}