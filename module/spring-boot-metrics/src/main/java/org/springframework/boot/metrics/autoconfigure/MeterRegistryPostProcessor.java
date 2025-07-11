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

package org.springframework.boot.metrics.autoconfigure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.util.LambdaSafe;
import org.springframework.context.ApplicationContext;

/**
 * {@link BeanPostProcessor} for {@link MeterRegistry} beans.
 *
 * @author Jon Schneider
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
class MeterRegistryPostProcessor implements BeanPostProcessor, SmartInitializingSingleton {

	private final CompositeMeterRegistries compositeMeterRegistries;

	private final ObjectProvider<MetricsProperties> properties;

	private final ObjectProvider<MeterRegistryCustomizer<?>> customizers;

	private final ObjectProvider<MeterFilter> filters;

	private final ObjectProvider<MeterBinder> binders;

	private volatile boolean deferBinding = true;

	private final Set<MeterRegistry> deferredBindings = new LinkedHashSet<>();

	MeterRegistryPostProcessor(ApplicationContext applicationContext,
			ObjectProvider<MetricsProperties> metricsProperties, ObjectProvider<MeterRegistryCustomizer<?>> customizers,
			ObjectProvider<MeterFilter> filters, ObjectProvider<MeterBinder> binders) {
		this(CompositeMeterRegistries.of(applicationContext), metricsProperties, customizers, filters, binders);
	}

	MeterRegistryPostProcessor(CompositeMeterRegistries compositeMeterRegistries,
			ObjectProvider<MetricsProperties> properties, ObjectProvider<MeterRegistryCustomizer<?>> customizers,
			ObjectProvider<MeterFilter> filters, ObjectProvider<MeterBinder> binders) {
		this.compositeMeterRegistries = compositeMeterRegistries;
		this.properties = properties;
		this.customizers = customizers;
		this.filters = filters;
		this.binders = binders;

	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof MeterRegistry meterRegistry) {
			postProcessMeterRegistry(meterRegistry);
		}
		return bean;
	}

	@Override
	public void afterSingletonsInstantiated() {
		synchronized (this.deferredBindings) {
			this.deferBinding = false;
			this.deferredBindings.forEach(this::applyBinders);
		}
	}

	private void postProcessMeterRegistry(MeterRegistry meterRegistry) {
		// Customizers must be applied before binders, as they may add custom tags or
		// alter timer or summary configuration.
		applyCustomizers(meterRegistry);
		applyFilters(meterRegistry);
		addToGlobalRegistryIfNecessary(meterRegistry);
		if (isBindable(meterRegistry)) {
			applyBinders(meterRegistry);
		}
	}

	@SuppressWarnings("unchecked")
	private void applyCustomizers(MeterRegistry meterRegistry) {
		List<MeterRegistryCustomizer<?>> customizers = this.customizers.orderedStream().toList();
		LambdaSafe.callbacks(MeterRegistryCustomizer.class, customizers, meterRegistry)
			.withLogger(MeterRegistryPostProcessor.class)
			.invoke((customizer) -> customizer.customize(meterRegistry));
	}

	private void applyFilters(MeterRegistry meterRegistry) {
		if (meterRegistry instanceof AutoConfiguredCompositeMeterRegistry) {
			return;
		}
		this.filters.orderedStream().forEach(meterRegistry.config()::meterFilter);
	}

	private void addToGlobalRegistryIfNecessary(MeterRegistry meterRegistry) {
		if (this.properties.getObject().isUseGlobalRegistry() && !isGlobalRegistry(meterRegistry)) {
			Metrics.addRegistry(meterRegistry);
		}
	}

	private boolean isGlobalRegistry(MeterRegistry meterRegistry) {
		return meterRegistry == Metrics.globalRegistry;
	}

	private boolean isBindable(MeterRegistry meterRegistry) {
		return isAutoConfiguredComposite(meterRegistry) || isCompositeWithOnlyUserDefinedComposites(meterRegistry)
				|| noCompositeMeterRegistries();
	}

	private boolean isAutoConfiguredComposite(MeterRegistry meterRegistry) {
		return meterRegistry instanceof AutoConfiguredCompositeMeterRegistry;
	}

	private boolean isCompositeWithOnlyUserDefinedComposites(MeterRegistry meterRegistry) {
		return this.compositeMeterRegistries == CompositeMeterRegistries.ONLY_USER_DEFINED
				&& meterRegistry instanceof CompositeMeterRegistry;
	}

	private boolean noCompositeMeterRegistries() {
		return this.compositeMeterRegistries == CompositeMeterRegistries.NONE;
	}

	void applyBinders(MeterRegistry meterRegistry) {
		if (this.deferBinding) {
			synchronized (this.deferredBindings) {
				if (this.deferBinding) {
					this.deferredBindings.add(meterRegistry);
					return;
				}
			}
		}
		this.binders.orderedStream().forEach((binder) -> binder.bindTo(meterRegistry));
	}

	enum CompositeMeterRegistries {

		NONE, AUTO_CONFIGURED, ONLY_USER_DEFINED;

		private static CompositeMeterRegistries of(ApplicationContext context) {
			if (hasBeansOfType(AutoConfiguredCompositeMeterRegistry.class, context)) {
				return AUTO_CONFIGURED;
			}
			return hasBeansOfType(CompositeMeterRegistry.class, context) ? ONLY_USER_DEFINED : NONE;
		}

		private static boolean hasBeansOfType(Class<?> type, ApplicationContext context) {
			return context.getBeanNamesForType(type, false, false).length > 0;
		}

	}

}
