package com.bepolite.utils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.bepolite.model.Result;

public class VelocityTemplateUtil {

	private static Template template;

	public static String constructEmailBody(ArrayList<Result> resultList) {

		Template template = getTemplate();
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("resultList", resultList);
		StringWriter writer = new StringWriter();
		template.merge(velocityContext, writer);
		return writer.toString();
	}

	private static Template getTemplate() {
		if (template != null)
			return template;
		Properties props = new Properties();
		props.setProperty("resource.loader", "class");
		props.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		VelocityEngine velocityEngine = new VelocityEngine(props);
		template = velocityEngine.getTemplate("emailBody.vm");
		return template;
	}
}