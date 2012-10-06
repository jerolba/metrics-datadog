package com.yammer.metrics.reporting;

import java.io.IOException;
import java.io.OutputStream;

import com.yammer.metrics.core.VirtualMachineMetrics;

public class ReportWriter {

	private String host;
	private VmReporter vmReporter;
	private MetricsReporter metricsReporter;
	
	public ReportWriter(VirtualMachineMetrics vm, String host){
		this.host=host;
		vmReporter=new VmReporter(vm);
	}

	public void generate(OutputStream os, long epoch) throws IOException {
		
	}
	

}
