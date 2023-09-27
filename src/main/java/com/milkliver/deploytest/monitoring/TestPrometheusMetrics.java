package com.milkliver.deploytest.monitoring;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

@Service
public class TestPrometheusMetrics {

	private Gauge testGauge01;

	public TestPrometheusMetrics(CollectorRegistry collectorRegistry) {
		testGauge01 = Gauge.build().namespace("custom_deploy_test").name("test_metric01").help("This is test gauge01")
				.register(collectorRegistry);
	}

	public void setTestMetric01(int value) {
		testGauge01.set(value);
	}

	@PostConstruct
	public void testMetric01Initial() {
		testGauge01.set(0);
	}

}
