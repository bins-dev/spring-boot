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

package org.springframework.boot.test.autoconfigure.web.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Auto-configuration for a web-client {@link RestTemplate}. Used when
 * {@link AutoConfigureWebClient#registerRestTemplate()} is {@code true}.
 *
 * @author Phillip Webb
 * @since 1.4.0
 * @see AutoConfigureMockRestServiceServer
 */
@AutoConfiguration(afterName = "org.springframework.boot.restclient.autoconfigure.RestTemplateAutoConfiguration")
@ConditionalOnBooleanProperty("spring.test.webclient.register-rest-template")
@ConditionalOnClass(RestTemplateBuilder.class)
public final class WebClientRestTemplateAutoConfiguration {

	@Bean
	RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}
