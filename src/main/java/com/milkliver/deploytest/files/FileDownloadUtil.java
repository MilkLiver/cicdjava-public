package com.milkliver.deploytest.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
public class FileDownloadUtil {
	private Path foundFile;

	@Value("${filetest.download.source.path}")
	String downloadSourcePath;

	public Resource getFileAsResource(String fileCode) throws IOException {
		System.out.println("filetest.download.source.path: " + downloadSourcePath);
		Path dirPath = Paths.get(downloadSourcePath);

		Files.list(dirPath).forEach(file -> {
			if (file.getFileName().toString().startsWith(fileCode)) {
				foundFile = file;
				return;
			}
		});

		if (foundFile != null) {
			return new UrlResource(foundFile.toUri());
		}

		return null;
	}
}
