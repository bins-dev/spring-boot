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

package org.springframework.boot.webmvc.autoconfigure.error;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the default error view.
 *
 * @author Dave Syer
 * @author Scott Frederick
 */
@SpringBootTest(properties = { "server.error.include-message=always" })
@DirtiesContext
class DefaultErrorViewIntegrationTests {

	@Autowired
	private WebApplicationContext wac;

	private MockMvcTester mvc;

	@BeforeEach
	void setup() {
		this.mvc = MockMvcTester.from(this.wac);
	}

	@Test
	void testErrorForBrowserClient() {
		assertThat(this.mvc.get().uri("/error").accept(MediaType.TEXT_HTML)).hasStatus5xxServerError()
			.bodyText()
			.contains("<html>", "999");
	}

	@Test
	void testErrorWithHtmlEscape() {
		assertThat(this.mvc.get()
			.uri("/error")
			.requestAttr("jakarta.servlet.error.exception",
					new RuntimeException("<script>alert('Hello World')</script>"))
			.accept(MediaType.TEXT_HTML)).hasStatus5xxServerError()
			.bodyText()
			.contains("&lt;script&gt;", "Hello World", "999");
	}

	@Test
	void testErrorWithSpelEscape() {
		String spel = "${T(" + getClass().getName() + ").injectCall()}";
		assertThat(this.mvc.get()
			.uri("/error")
			.requestAttr("jakarta.servlet.error.exception", new RuntimeException(spel))
			.accept(MediaType.TEXT_HTML)).hasStatus5xxServerError().bodyText().doesNotContain("injection");
	}

	static String injectCall() {
		return "injection";
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Import({ DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class,
			HttpMessageConvertersAutoConfiguration.class, ErrorMvcAutoConfiguration.class,
			PropertyPlaceholderAutoConfiguration.class })
	protected @interface MinimalWebConfiguration {

	}

	@Configuration(proxyBeanMethods = false)
	@MinimalWebConfiguration
	static class TestConfiguration {

		// For manual testing
		static void main(String[] args) {
			SpringApplication.run(TestConfiguration.class, args);
		}

	}

}
