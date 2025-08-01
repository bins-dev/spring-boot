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

package org.springframework.boot.jdbc.metrics;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.jdbc.metadata.CompositeDataSourcePoolMetadataProvider;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * A {@link MeterBinder} for a {@link DataSource}.
 *
 * @author Jon Schneider
 * @author Phillip Webb
 * @since 4.0.0
 */
public class DataSourcePoolMetrics implements MeterBinder {

	private final DataSource dataSource;

	private final CachingDataSourcePoolMetadataProvider metadataProvider;

	private final Iterable<Tag> tags;

	public DataSourcePoolMetrics(DataSource dataSource, Collection<DataSourcePoolMetadataProvider> metadataProviders,
			String dataSourceName, Iterable<Tag> tags) {
		this(dataSource, new CompositeDataSourcePoolMetadataProvider(metadataProviders), dataSourceName, tags);
	}

	public DataSourcePoolMetrics(DataSource dataSource, DataSourcePoolMetadataProvider metadataProvider, String name,
			Iterable<Tag> tags) {
		Assert.notNull(dataSource, "'dataSource' must not be null");
		Assert.notNull(metadataProvider, "'metadataProvider' must not be null");
		this.dataSource = dataSource;
		this.metadataProvider = new CachingDataSourcePoolMetadataProvider(metadataProvider);
		this.tags = Tags.concat(tags, "name", name);
	}

	@Override
	@SuppressWarnings("NullAway") // Lambda isn't detected with the correct nullability
	public void bindTo(MeterRegistry registry) {
		if (this.metadataProvider.getDataSourcePoolMetadata(this.dataSource) != null) {
			bindPoolMetadata(registry, "active",
					"Current number of active connections that have been allocated from the data source.",
					DataSourcePoolMetadata::getActive);
			bindPoolMetadata(registry, "idle", "Number of established but idle connections.",
					DataSourcePoolMetadata::getIdle);
			bindPoolMetadata(registry, "max",
					"Maximum number of active connections that can be allocated at the same time.",
					DataSourcePoolMetadata::getMax);
			bindPoolMetadata(registry, "min", "Minimum number of idle connections in the pool.",
					DataSourcePoolMetadata::getMin);
		}
	}

	@SuppressWarnings("NullAway") // Lambda isn't detected with the correct nullability
	private <N extends Number> void bindPoolMetadata(MeterRegistry registry, String metricName, String description,
			Function<DataSourcePoolMetadata, @Nullable N> function) {
		bindDataSource(registry, metricName, description, this.metadataProvider.getValueFunction(function));
	}

	private <N extends Number> void bindDataSource(MeterRegistry registry, String metricName, String description,
			Function<DataSource, @Nullable N> function) {
		if (function.apply(this.dataSource) != null) {
			Gauge.builder("jdbc.connections." + metricName, this.dataSource, (m) -> {
				Number value = function.apply(m);
				Assert.state(value != null, "'value' must not be null");
				return value.doubleValue();
			}).tags(this.tags).description(description).register(registry);
		}
	}

	private static class CachingDataSourcePoolMetadataProvider implements DataSourcePoolMetadataProvider {

		private static final Map<DataSource, DataSourcePoolMetadata> cache = new ConcurrentReferenceHashMap<>();

		private final DataSourcePoolMetadataProvider metadataProvider;

		CachingDataSourcePoolMetadataProvider(DataSourcePoolMetadataProvider metadataProvider) {
			this.metadataProvider = metadataProvider;
		}

		<N extends Number> Function<DataSource, @Nullable N> getValueFunction(
				Function<DataSourcePoolMetadata, @Nullable N> function) {
			return (dataSource) -> {
				DataSourcePoolMetadata dataSourcePoolMetadata = getDataSourcePoolMetadata(dataSource);
				Assert.state(dataSourcePoolMetadata != null, "'dataSourcePoolMetadata' must not be null");
				return function.apply(dataSourcePoolMetadata);
			};
		}

		@Override
		public @Nullable DataSourcePoolMetadata getDataSourcePoolMetadata(DataSource dataSource) {
			return cache.computeIfAbsent(dataSource,
					(key) -> this.metadataProvider.getDataSourcePoolMetadata(dataSource));
		}

	}

}
