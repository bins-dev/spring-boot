/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.metrics.autoconfigure.export.humio;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.ipc.http.HttpUrlConnectionSender;
import io.micrometer.humio.HumioConfig;
import io.micrometer.humio.HumioMeterRegistry;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.metrics.autoconfigure.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.metrics.autoconfigure.export.ConditionalOnEnabledMetricsExport;
import org.springframework.boot.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for exporting metrics to Humio.
 *
 * @author Andy Wilkinson
 * @author Artsiom Yudovin
 * @since 4.0.0
 */
@AutoConfiguration(
		before = { CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class },
		after = MetricsAutoConfiguration.class)
@ConditionalOnBean(Clock.class)
@ConditionalOnClass(HumioMeterRegistry.class)
@ConditionalOnEnabledMetricsExport("humio")
@EnableConfigurationProperties(HumioProperties.class)
public final class HumioMetricsExportAutoConfiguration {

	private final HumioProperties properties;

	HumioMetricsExportAutoConfiguration(HumioProperties properties) {
		this.properties = properties;
	}

	@Bean
	@ConditionalOnMissingBean
	HumioConfig humioConfig() {
		return new HumioPropertiesConfigAdapter(this.properties);
	}

	@Bean
	@ConditionalOnMissingBean
	HumioMeterRegistry humioMeterRegistry(HumioConfig humioConfig, Clock clock) {
		return HumioMeterRegistry.builder(humioConfig)
			.clock(clock)
			.httpClient(
					new HttpUrlConnectionSender(this.properties.getConnectTimeout(), this.properties.getReadTimeout()))
			.build();

	}

}
