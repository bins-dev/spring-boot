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

package org.springframework.boot.logging.logback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * Allows programmatic configuration of logback which is usually faster than parsing XML.
 *
 * @author Phillip Webb
 */
class LogbackConfigurator {

	private final LoggerContext context;

	LogbackConfigurator(LoggerContext context) {
		Assert.notNull(context, "'context' must not be null");
		this.context = context;
	}

	LoggerContext getContext() {
		return this.context;
	}

	ReentrantLock getConfigurationLock() {
		return this.context.getConfigurationLock();
	}

	@SuppressWarnings("unchecked")
	<T extends Converter<?>> void conversionRule(String conversionWord, Class<T> converterClass,
			Supplier<T> converterSupplier) {
		Assert.hasLength(conversionWord, "'conversionWord' must not be empty");
		Assert.notNull(converterSupplier, "'converterSupplier' must not be null");
		Map<String, Supplier<?>> registry = (Map<String, Supplier<?>>) this.context
			.getObject(CoreConstants.PATTERN_RULE_REGISTRY_FOR_SUPPLIERS);
		if (registry == null) {
			registry = new HashMap<>();
			this.context.putObject(CoreConstants.PATTERN_RULE_REGISTRY_FOR_SUPPLIERS, registry);
		}
		registry.put(conversionWord, converterSupplier);
	}

	void appender(String name, Appender<?> appender) {
		appender.setName(name);
		start(appender);
	}

	void logger(String name, @Nullable Level level) {
		logger(name, level, true);
	}

	void logger(String name, @Nullable Level level, boolean additive) {
		logger(name, level, additive, null);
	}

	void logger(String name, @Nullable Level level, boolean additive, @Nullable Appender<ILoggingEvent> appender) {
		Logger logger = this.context.getLogger(name);
		if (level != null) {
			logger.setLevel(level);
		}
		logger.setAdditive(additive);
		if (appender != null) {
			logger.addAppender(appender);
		}
	}

	@SafeVarargs
	final void root(@Nullable Level level, Appender<ILoggingEvent>... appenders) {
		Logger logger = this.context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		if (level != null) {
			logger.setLevel(level);
		}
		for (Appender<ILoggingEvent> appender : appenders) {
			logger.addAppender(appender);
		}
	}

	void start(LifeCycle lifeCycle) {
		if (lifeCycle instanceof ContextAware contextAware) {
			contextAware.setContext(this.context);
		}
		lifeCycle.start();
	}

}
