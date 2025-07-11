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

package org.springframework.boot.actuate.docs.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.docs.MockMvcEndpointDocumentationTests;
import org.springframework.boot.cache.actuate.endpoint.CachesEndpoint;
import org.springframework.boot.cache.actuate.endpoint.CachesEndpointWebExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

/**
 * Tests for generating documentation describing the {@link CachesEndpoint}
 *
 * @author Stephane Nicoll
 */
class CachesEndpointDocumentationTests extends MockMvcEndpointDocumentationTests {

	private static final List<FieldDescriptor> levelFields = List.of(fieldWithPath("name").description("Cache name."),
			fieldWithPath("cacheManager").description("Cache manager name."),
			fieldWithPath("target").description("Fully qualified name of the native cache."));

	private static final List<ParameterDescriptor> queryParameters = Collections
		.singletonList(parameterWithName("cacheManager")
			.description("Name of the cacheManager to qualify the cache. May be omitted if the cache name is unique.")
			.optional());

	@Test
	void allCaches() {
		assertThat(this.mvc.get().uri("/actuator/caches")).hasStatusOk()
			.apply(MockMvcRestDocumentation.document("caches/all",
					responseFields(fieldWithPath("cacheManagers").description("Cache managers keyed by id."),
							fieldWithPath("cacheManagers.*.caches")
								.description("Caches in the application context keyed by name."))
						.andWithPrefix("cacheManagers.*.caches.*.",
								fieldWithPath("target").description("Fully qualified name of the native cache."))));
	}

	@Test
	void namedCache() {
		assertThat(this.mvc.get().uri("/actuator/caches/cities")).hasStatusOk()
			.apply(MockMvcRestDocumentation.document("caches/named", queryParameters(queryParameters),
					responseFields(levelFields)));
	}

	@Test
	void evictAllCaches() {
		assertThat(this.mvc.delete().uri("/actuator/caches")).hasStatus(HttpStatus.NO_CONTENT)
			.apply(MockMvcRestDocumentation.document("caches/evict-all"));
	}

	@Test
	void evictNamedCache() {
		assertThat(this.mvc.delete().uri("/actuator/caches/countries?cacheManager=anotherCacheManager"))
			.hasStatus(HttpStatus.NO_CONTENT)
			.apply(MockMvcRestDocumentation.document("caches/evict-named", queryParameters(queryParameters)));
	}

	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

		@Bean
		CachesEndpoint endpoint() {
			Map<String, CacheManager> cacheManagers = new HashMap<>();
			cacheManagers.put("cacheManager", new ConcurrentMapCacheManager("countries", "cities"));
			cacheManagers.put("anotherCacheManager", new ConcurrentMapCacheManager("countries"));
			return new CachesEndpoint(cacheManagers);
		}

		@Bean
		CachesEndpointWebExtension endpointWebExtension(CachesEndpoint endpoint) {
			return new CachesEndpointWebExtension(endpoint);
		}

	}

}
