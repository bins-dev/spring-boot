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

package org.springframework.boot.metrics.autoconfigure.export.properties;

import io.micrometer.core.instrument.push.PushRegistryConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base tests for {@link PushRegistryProperties} implementation.
 *
 * @author Stephane Nicoll
 */
public abstract class PushRegistryPropertiesTests {

	@SuppressWarnings("deprecation")
	protected void assertStepRegistryDefaultValues(PushRegistryProperties properties, PushRegistryConfig config) {
		assertThat(properties.getStep()).isEqualTo(config.step());
		assertThat(properties.isEnabled()).isEqualTo(config.enabled());
		assertThat(properties.getConnectTimeout()).isEqualTo(config.connectTimeout());
		assertThat(properties.getReadTimeout()).isEqualTo(config.readTimeout());
		assertThat(properties.getBatchSize()).isEqualTo(config.batchSize());
	}

}
