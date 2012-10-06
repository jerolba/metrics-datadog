package com.yammer.metrics.reporting;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.reporting.model.DatadogCounter;
import com.yammer.metrics.reporting.model.DatadogGauge;

public class DatadogApi {

	private static final Logger LOG = LoggerFactory.getLogger(DatadogApi.class);
		    		  
	private static final JsonFactory jsonFactory = new JsonFactory();
	private static final ObjectMapper mapper = new ObjectMapper(jsonFactory);
	
	private JsonGenerator jsonOut;
	private String host;
	private OutputStream os;
	
	public DatadogApi(String host,OutputStream os){
		this.host=host;
		this.os=os;
	}
	
	public void startSerie() throws IOException{
		jsonOut = jsonFactory.createJsonGenerator(os);
		jsonOut.writeStartObject();
		jsonOut.writeFieldName("series");
		jsonOut.writeStartArray();
	}
	
	public void finishSerie() throws IOException{
		jsonOut.writeEndArray();
		jsonOut.writeEndObject();
		jsonOut.flush();
	}

	public void sendGauge(String name, Number count, Long epoch) {
		DatadogGauge gauge = new DatadogGauge(name, count, epoch, host);
		try {
			mapper.writeValue(jsonOut, gauge);
		} catch (Exception e) {
			LOG.error("Error writing gauge", e);
		}
	}

	public void pushCounter(MetricName metricName, Long count, Long epoch,
			String... path) {
		pushCounter(sanitizeName(metricName, path), count, epoch);

	}

	public void pushCounter(String name, Long count, Long epoch) {
		DatadogCounter counter = new DatadogCounter(name, count, epoch, host);
		try {
			mapper.writeValue(jsonOut, counter);
		} catch (Exception e) {
			LOG.error("Error writing counter", e);
		}
	}

	public void pushGauge(MetricName metricName, Number count, Long epoch,
			String... path) {
		sendGauge(sanitizeName(metricName, path), count, epoch);
	}

	public void pushGauge(String name, long count, long epoch) {
		sendGauge(name, new Long(count), epoch);
	}

	protected String sanitizeName(MetricName name, String... path) {
		final StringBuilder sb = new StringBuilder();
		sb.append(name.getType()).append('.');

		if (name.hasScope()) {
			sb.append(name.getScope()).append('.');
		}

		String[] metricParts = name.getName().split("\\[");
		sb.append(metricParts[0]);

		for (String part : path) {
			sb.append('.').append(part);
		}

		for (int i = 1; i < metricParts.length; i++) {
			sb.append('[').append(metricParts[i]);
		}
		return sb.toString();
	}

}
