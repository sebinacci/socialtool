package com.bepolite.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class HttpUtil {

	public static String getLatestUpdate(String url) throws IOException {
		String response = executeHttpGet(url);
		return reformatJson(response);
	}

	private static String reformatJson(String response) {
		Pattern p = Pattern
				.compile("\\/\\**\\/ ___GraphExplorerAsyncCallback___\\((.*)\\)");
		Matcher matcher = p.matcher(response);
		if (!matcher.find())
			throw new RuntimeException("Match not found");

		return matcher.group(1);
	}

	private static String executeHttpGet(String url) throws IOException,
			RestClientException {
		ResponseEntity<String> response = new RestTemplate().getForEntity(url,
				String.class);
		return response.getBody();
	}
}