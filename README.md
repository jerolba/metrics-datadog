# Metrics Datadog Reporter
Simple Metrics reporter that sends The Goods to Datadog. Real person
documentation pending

## Usage

~~~java
import com.yammer.metrics.reporting.DatadogReporter

...

DatadogReporter dd = new DatadogReporter(Metrics.defaultRegistry(), yourApiKey,yourAppKey,hostName);
dd.start(30, TimeUnit.SECONDS);

~~~


##Thanks
This project is a refactor and mix from:

https://github.com/bazaarvoice/metrics-datadog

https://github.com/vistarmedia/metrics-datadog