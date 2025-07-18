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

package org.springframework.boot.tomcat.autoconfigure.servlet;

import org.apache.catalina.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TomcatServletWebServerFactoryCustomizer}.
 *
 * @author Phillip Webb
 */
class TomcatServletWebServerFactoryCustomizerTests {

	private final TomcatServerProperties tomcatProperties = new TomcatServerProperties();

	private TomcatServletWebServerFactoryCustomizer customizer;

	private MockEnvironment environment;

	@BeforeEach
	void setup() {
		this.environment = new MockEnvironment();
		ConfigurationPropertySources.attach(this.environment);
		this.customizer = new TomcatServletWebServerFactoryCustomizer(this.tomcatProperties);
	}

	@Test
	void customTldSkip() {
		bind("server.tomcat.additional-tld-skip-patterns=foo.jar,bar.jar");
		testCustomTldSkip("foo.jar", "bar.jar");
	}

	@Test
	void customTldSkipAsList() {
		bind("server.tomcat.additional-tld-skip-patterns[0]=biz.jar",
				"server.tomcat.additional-tld-skip-patterns[1]=bah.jar");
		testCustomTldSkip("biz.jar", "bah.jar");
	}

	private void testCustomTldSkip(String... expectedJars) {
		TomcatServletWebServerFactory factory = customizeAndGetFactory();
		assertThat(factory.getTldSkipPatterns()).contains(expectedJars);
		assertThat(factory.getTldSkipPatterns()).contains("junit-*.jar", "spring-boot-*.jar");
	}

	@Test
	void redirectContextRootCanBeConfigured() {
		bind("server.tomcat.redirect-context-root=false");
		assertThat(this.tomcatProperties.getRedirectContextRoot()).isFalse();
		TomcatWebServer server = customizeAndGetServer();
		Context context = (Context) server.getTomcat().getHost().findChildren()[0];
		assertThat(context.getMapperContextRootRedirectEnabled()).isFalse();
	}

	@Test
	void useRelativeRedirectsCanBeConfigured() {
		bind("server.tomcat.use-relative-redirects=true");
		assertThat(this.tomcatProperties.isUseRelativeRedirects()).isTrue();
		TomcatWebServer server = customizeAndGetServer();
		Context context = (Context) server.getTomcat().getHost().findChildren()[0];
		assertThat(context.getUseRelativeRedirects()).isTrue();
	}

	private void bind(String... inlinedProperties) {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(this.environment, inlinedProperties);
		new Binder(ConfigurationPropertySources.get(this.environment)).bind("server.tomcat",
				Bindable.ofInstance(this.tomcatProperties));
	}

	private TomcatWebServer customizeAndGetServer() {
		TomcatServletWebServerFactory factory = customizeAndGetFactory();
		return (TomcatWebServer) factory.getWebServer();
	}

	private TomcatServletWebServerFactory customizeAndGetFactory() {
		TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory(0);
		this.customizer.customize(factory);
		return factory;
	}

}
