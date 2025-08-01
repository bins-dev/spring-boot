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

package org.springframework.boot.mustache.reactive.view;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;

import com.samskivert.mustache.Mustache;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import org.springframework.boot.testsupport.classpath.resources.WithResource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

/**
 * Tests for {@link MustacheView}.
 *
 * @author Brian Clozel
 */
class MustacheViewTests {

	private final StaticApplicationContext context = new StaticApplicationContext();

	@Test
	@WithResource(name = "template.html", content = "Hello {{World}}")
	void viewResolvesHandlebars() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
		MustacheView view = new MustacheView();
		view.setCompiler(Mustache.compiler());
		view.setUrl("classpath:template.html");
		view.setCharset(StandardCharsets.UTF_8.displayName());
		view.setApplicationContext(this.context);
		view.render(Collections.singletonMap("World", "Spring"), MediaType.TEXT_HTML, exchange)
			.block(Duration.ofSeconds(30));
		StepVerifier.create(exchange.getResponse().getBodyAsString())
			.assertNext((body) -> Assertions.assertThat(body).isEqualToIgnoringWhitespace("Hello Spring"))
			.expectComplete()
			.verify(Duration.ofSeconds(30));
	}

}
