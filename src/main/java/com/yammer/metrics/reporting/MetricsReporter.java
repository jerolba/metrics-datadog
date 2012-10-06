package com.yammer.metrics.reporting;

import java.util.SortedMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.stats.Snapshot;

public class MetricsReporter implements MetricProcessor<Long> {

	private static final Logger LOG = LoggerFactory.getLogger(MetricsReporter.class);
	
	private DatadogApi api;
	
	public MetricsReporter(DatadogApi api){
		this.api=api;
	}
			
	protected void pushRegularMetrics(SortedMap<String, SortedMap<MetricName, Metric>> metrics, long epoch) {
		for (Entry<String, SortedMap<MetricName, Metric>> entry : metrics.entrySet()) {
			for (Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
				final Metric metric = subEntry.getValue();
				if (metric != null) {
					try {
						metric.processWith(this, subEntry.getKey(), epoch);
					} catch (Exception e) {
						LOG.error("Error pushing metric", e);
					}
				}
			}
		}
	}

	public void processCounter(MetricName name, Counter counter, Long epoch) throws Exception {
		api.pushCounter(name, counter.count(), epoch);
	}

	public void processGauge(MetricName name, Gauge<?> gauge, Long epoch) throws Exception {
		api.pushGauge(name, (Number) gauge.value(), epoch);
	}

	public void processHistogram(MetricName name, Histogram histogram, Long epoch) throws Exception {
		pushSummarizable(name, histogram, epoch);
		pushSampling(name, histogram, epoch);
	}

	public void processMeter(MetricName name, Metered meter, Long epoch) throws Exception {
		api.pushCounter(name, meter.count(), epoch);
		api.pushGauge(name, meter.meanRate(), epoch, "mean");
		api.pushGauge(name, meter.oneMinuteRate(), epoch, "1MinuteRate");
		api.pushGauge(name, meter.fiveMinuteRate(), epoch, "5MinuteRate");
		api.pushGauge(name, meter.fifteenMinuteRate(), epoch, "15MinuteRate");
	}

	public void processTimer(MetricName name, Timer timer, Long epoch) throws Exception {
		processMeter(name, timer, epoch);
		pushSummarizable(name, timer, epoch);
		pushSampling(name, timer, epoch);
	}

	private void pushSummarizable(MetricName name, Summarizable summarizable,Long epoch) {
		api.pushGauge(name, summarizable.min(), epoch, "min");
		api.pushGauge(name, summarizable.max(), epoch, "max");
		api.pushGauge(name, summarizable.mean(), epoch, "mean");
		api.pushGauge(name, summarizable.stdDev(), epoch, "stddev");
	}

	private void pushSampling(MetricName name, Sampling sampling, Long epoch) {
		final Snapshot snapshot = sampling.getSnapshot();
		api.pushGauge(name, snapshot.getMedian(), epoch, "median");
		api.pushGauge(name, snapshot.get75thPercentile(), epoch, "75percentile");
		api.pushGauge(name, snapshot.get95thPercentile(), epoch, "95percentile");
		api.pushGauge(name, snapshot.get98thPercentile(), epoch, "98percentile");
		api.pushGauge(name, snapshot.get99thPercentile(), epoch, "99percentile");
		api.pushGauge(name, snapshot.get999thPercentile(), epoch, "999percentile");
	}

}
