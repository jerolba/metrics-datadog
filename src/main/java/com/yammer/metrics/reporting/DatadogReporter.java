package com.yammer.metrics.reporting;

import java.util.Locale;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.Transport.Request;

public class DatadogReporter extends AbstractPollingReporter {

	public boolean printVmMetrics = true;
	protected final Locale locale = Locale.US;
	protected final Clock clock;
	private final String host;
	protected final MetricPredicate predicate;
	protected final Transport transport;
	private static final Logger LOG = LoggerFactory.getLogger(DatadogReporter.class);
	private VmReporter vmReporter;
	
	public DatadogReporter(MetricsRegistry registry, String apiKey, String appKey,String host) {
		this(registry, MetricPredicate.ALL,
				VirtualMachineMetrics.getInstance(), new HttpTransport(
						"app.datadoghq.com", apiKey,appKey), Clock.defaultClock(),host);
	}

	public DatadogReporter(MetricsRegistry metricsRegistry,
			MetricPredicate predicate, VirtualMachineMetrics vm,
			Transport transport, Clock clock, String host) {
		super(metricsRegistry, "datadog-reporter");
		this.vmReporter = new VmReporter(vm);
		this.transport = transport;
		this.predicate = predicate;
		this.clock = clock;
		this.host = host;
	}

	@Override
	public void run() {
		SortedMap<String, SortedMap<MetricName, Metric>> metrics = getMetricsRegistry().groupedMetrics(predicate);
		Request request = null;
		final long epoch = clock.time() / 1000;
		try {
			request = transport.prepare();
			DatadogApi api = new DatadogApi(host, request.getBodyWriter());
			api.startSerie();
			if (printVmMetrics){
				vmReporter.pushVmMetrics(api, epoch);
			}
			MetricsReporter metricsReporter = new MetricsReporter(api);
			metricsReporter.pushRegularMetrics(metrics, epoch);
			api.finishSerie();

			request.send();
		} catch (Exception e) {
			LOG.error("Error sending metrics", e);
		}
	}

}
