package com.yammer.metrics.reporting;

import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.core.VirtualMachineMetrics;

public class VmReporter {

	private VirtualMachineMetrics vm;
	
	public VmReporter(VirtualMachineMetrics vm){
		this.vm=vm;
	}

	protected void pushVmMetrics(DatadogApi api,long epoch) {
		api.sendGauge("jvm.memory.heap_usage", vm.heapUsage(), epoch);
		api.sendGauge("jvm.memory.non_heap_usage", vm.nonHeapUsage(), epoch);
		for (Entry<String, Double> pool : vm.memoryPoolUsage().entrySet()) {
			String gaugeName = String.format("jvm.memory.memory_pool_usage[pool:%s]", pool.getKey());
			api.sendGauge(gaugeName, pool.getValue(), epoch);
		}

		api.pushGauge("jvm.daemon_thread_count", vm.daemonThreadCount(), epoch);
		api.pushGauge("jvm.thread_count", vm.threadCount(), epoch);
		api.pushCounter("jvm.uptime", vm.uptime(), epoch);
		api.sendGauge("jvm.fd_usage", vm.fileDescriptorUsage(), epoch);

		for (Entry<Thread.State, Double> entry : vm.threadStatePercentages()
				.entrySet()) {
			String gaugeName = String.format("jvm.thread-states[state:%s]",
					entry.getKey());
			api.sendGauge(gaugeName, entry.getValue(), epoch);
		}

		for (Entry<String, VirtualMachineMetrics.GarbageCollectorStats> entry : vm
				.garbageCollectors().entrySet()) {
			api.pushGauge("jvm.gc.time",
					entry.getValue().getTime(TimeUnit.MILLISECONDS), epoch);
			api.pushCounter("jvm.gc.runs", entry.getValue().getRuns(), epoch);
		}
	}
}
