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

package org.springframework.boot.task;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Builder that can be used to configure and create a {@link ThreadPoolTaskExecutor}.
 * Provides convenience methods to set common {@link ThreadPoolTaskExecutor} settings and
 * register {@link #taskDecorator(TaskDecorator)}). For advanced configuration, consider
 * using {@link ThreadPoolTaskExecutorCustomizer}.
 * <p>
 * In a typical auto-configured Spring Boot application this builder is available as a
 * bean and can be injected whenever a {@link ThreadPoolTaskExecutor} is needed.
 *
 * @author Stephane Nicoll
 * @author Filip Hrisafov
 * @author Yanming Zhou
 * @since 3.2.0
 */
public class ThreadPoolTaskExecutorBuilder {

	private final @Nullable Integer queueCapacity;

	private final @Nullable Integer corePoolSize;

	private final @Nullable Integer maxPoolSize;

	private final @Nullable Boolean allowCoreThreadTimeOut;

	private final @Nullable Duration keepAlive;

	private final @Nullable Boolean acceptTasksAfterContextClose;

	private final @Nullable Boolean awaitTermination;

	private final @Nullable Duration awaitTerminationPeriod;

	private final @Nullable String threadNamePrefix;

	private final @Nullable TaskDecorator taskDecorator;

	private final @Nullable Set<ThreadPoolTaskExecutorCustomizer> customizers;

	public ThreadPoolTaskExecutorBuilder() {
		this.queueCapacity = null;
		this.corePoolSize = null;
		this.maxPoolSize = null;
		this.allowCoreThreadTimeOut = null;
		this.keepAlive = null;
		this.acceptTasksAfterContextClose = null;
		this.awaitTermination = null;
		this.awaitTerminationPeriod = null;
		this.threadNamePrefix = null;
		this.taskDecorator = null;
		this.customizers = null;
	}

	private ThreadPoolTaskExecutorBuilder(@Nullable Integer queueCapacity, @Nullable Integer corePoolSize,
			@Nullable Integer maxPoolSize, @Nullable Boolean allowCoreThreadTimeOut, @Nullable Duration keepAlive,
			@Nullable Boolean acceptTasksAfterContextClose, @Nullable Boolean awaitTermination,
			@Nullable Duration awaitTerminationPeriod, @Nullable String threadNamePrefix,
			@Nullable TaskDecorator taskDecorator, @Nullable Set<ThreadPoolTaskExecutorCustomizer> customizers) {
		this.queueCapacity = queueCapacity;
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
		this.keepAlive = keepAlive;
		this.acceptTasksAfterContextClose = acceptTasksAfterContextClose;
		this.awaitTermination = awaitTermination;
		this.awaitTerminationPeriod = awaitTerminationPeriod;
		this.threadNamePrefix = threadNamePrefix;
		this.taskDecorator = taskDecorator;
		this.customizers = customizers;
	}

	/**
	 * Set the capacity of the queue. An unbounded capacity does not increase the pool and
	 * therefore ignores {@link #maxPoolSize(int) maxPoolSize}.
	 * @param queueCapacity the queue capacity to set
	 * @return a new builder instance
	 */
	public ThreadPoolTaskExecutorBuilder queueCapacity(int queueCapacity) {
		return new ThreadPoolTaskExecutorBuilder(queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set the core number of threads. Effectively that maximum number of threads as long
	 * as the queue is not full.
	 * <p>
	 * Core threads can grow and shrink if {@link #allowCoreThreadTimeOut(boolean)} is
	 * enabled.
	 * @param corePoolSize the core pool size to set
	 * @return a new builder instance
	 */
	public ThreadPoolTaskExecutorBuilder corePoolSize(int corePoolSize) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set the maximum allowed number of threads. When the {@link #queueCapacity(int)
	 * queue} is full, the pool can expand up to that size to accommodate the load.
	 * <p>
	 * If the {@link #queueCapacity(int) queue capacity} is unbounded, this setting is
	 * ignored.
	 * @param maxPoolSize the max pool size to set
	 * @return a new builder instance
	 */
	public ThreadPoolTaskExecutorBuilder maxPoolSize(int maxPoolSize) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set whether core threads are allowed to time out. When enabled, this enables
	 * dynamic growing and shrinking of the pool.
	 * @param allowCoreThreadTimeOut if core threads are allowed to time out
	 * @return a new builder instance
	 */
	public ThreadPoolTaskExecutorBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set the time limit for which threads may remain idle before being terminated.
	 * @param keepAlive the keep alive to set
	 * @return a new builder instance
	 */
	public ThreadPoolTaskExecutorBuilder keepAlive(@Nullable Duration keepAlive) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set whether to accept further tasks after the application context close phase has
	 * begun.
	 * @param acceptTasksAfterContextClose whether to accept further tasks after the
	 * application context close phase has begun
	 * @return a new builder instance
	 * @since 3.3.0
	 */
	public ThreadPoolTaskExecutorBuilder acceptTasksAfterContextClose(boolean acceptTasksAfterContextClose) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set whether the executor should wait for scheduled tasks to complete on shutdown,
	 * not interrupting running tasks and executing all tasks in the queue.
	 * @param awaitTermination whether the executor needs to wait for the tasks to
	 * complete on shutdown
	 * @return a new builder instance
	 * @see #awaitTerminationPeriod(Duration)
	 */
	public ThreadPoolTaskExecutorBuilder awaitTermination(boolean awaitTermination) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set the maximum time the executor is supposed to block on shutdown. When set, the
	 * executor blocks on shutdown in order to wait for remaining tasks to complete their
	 * execution before the rest of the container continues to shut down. This is
	 * particularly useful if your remaining tasks are likely to need access to other
	 * resources that are also managed by the container.
	 * @param awaitTerminationPeriod the await termination period to set
	 * @return a new builder instance
	 */
	public ThreadPoolTaskExecutorBuilder awaitTerminationPeriod(@Nullable Duration awaitTerminationPeriod) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set the prefix to use for the names of newly created threads.
	 * @param threadNamePrefix the thread name prefix to set
	 * @return a new builder instance
	 */
	public ThreadPoolTaskExecutorBuilder threadNamePrefix(@Nullable String threadNamePrefix) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, threadNamePrefix, this.taskDecorator, this.customizers);
	}

	/**
	 * Set the {@link TaskDecorator} to use or {@code null} to not use any.
	 * @param taskDecorator the task decorator to use
	 * @return a new builder instance
	 */
	public ThreadPoolTaskExecutorBuilder taskDecorator(@Nullable TaskDecorator taskDecorator) {
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, taskDecorator, this.customizers);
	}

	/**
	 * Set the {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}
	 * that should be applied to the {@link ThreadPoolTaskExecutor}. Customizers are
	 * applied in the order that they were added after builder configuration has been
	 * applied. Setting this value will replace any previously configured customizers.
	 * @param customizers the customizers to set
	 * @return a new builder instance
	 * @see #additionalCustomizers(ThreadPoolTaskExecutorCustomizer...)
	 */
	public ThreadPoolTaskExecutorBuilder customizers(ThreadPoolTaskExecutorCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return customizers(Arrays.asList(customizers));
	}

	/**
	 * Set the {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers}
	 * that should be applied to the {@link ThreadPoolTaskExecutor}. Customizers are
	 * applied in the order that they were added after builder configuration has been
	 * applied. Setting this value will replace any previously configured customizers.
	 * @param customizers the customizers to set
	 * @return a new builder instance
	 * @see #additionalCustomizers(ThreadPoolTaskExecutorCustomizer...)
	 */
	public ThreadPoolTaskExecutorBuilder customizers(Iterable<? extends ThreadPoolTaskExecutorCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator, append(null, customizers));
	}

	/**
	 * Add {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers} that
	 * should be applied to the {@link ThreadPoolTaskExecutor}. Customizers are applied in
	 * the order that they were added after builder configuration has been applied.
	 * @param customizers the customizers to add
	 * @return a new builder instance
	 * @see #customizers(ThreadPoolTaskExecutorCustomizer...)
	 */
	public ThreadPoolTaskExecutorBuilder additionalCustomizers(ThreadPoolTaskExecutorCustomizer... customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return additionalCustomizers(Arrays.asList(customizers));
	}

	/**
	 * Add {@link ThreadPoolTaskExecutorCustomizer ThreadPoolTaskExecutorCustomizers} that
	 * should be applied to the {@link ThreadPoolTaskExecutor}. Customizers are applied in
	 * the order that they were added after builder configuration has been applied.
	 * @param customizers the customizers to add
	 * @return a new builder instance
	 * @see #customizers(ThreadPoolTaskExecutorCustomizer...)
	 */
	public ThreadPoolTaskExecutorBuilder additionalCustomizers(
			Iterable<? extends ThreadPoolTaskExecutorCustomizer> customizers) {
		Assert.notNull(customizers, "'customizers' must not be null");
		return new ThreadPoolTaskExecutorBuilder(this.queueCapacity, this.corePoolSize, this.maxPoolSize,
				this.allowCoreThreadTimeOut, this.keepAlive, this.acceptTasksAfterContextClose, this.awaitTermination,
				this.awaitTerminationPeriod, this.threadNamePrefix, this.taskDecorator,
				append(this.customizers, customizers));
	}

	/**
	 * Build a new {@link ThreadPoolTaskExecutor} instance and configure it using this
	 * builder.
	 * @return a configured {@link ThreadPoolTaskExecutor} instance.
	 * @see #build(Class)
	 * @see #configure(ThreadPoolTaskExecutor)
	 */
	public ThreadPoolTaskExecutor build() {
		return configure(new ThreadPoolTaskExecutor());
	}

	/**
	 * Build a new {@link ThreadPoolTaskExecutor} instance of the specified type and
	 * configure it using this builder.
	 * @param <T> the type of task executor
	 * @param taskExecutorClass the template type to create
	 * @return a configured {@link ThreadPoolTaskExecutor} instance.
	 * @see #build()
	 * @see #configure(ThreadPoolTaskExecutor)
	 */
	public <T extends ThreadPoolTaskExecutor> T build(Class<T> taskExecutorClass) {
		return configure(BeanUtils.instantiateClass(taskExecutorClass));
	}

	/**
	 * Configure the provided {@link ThreadPoolTaskExecutor} instance using this builder.
	 * @param <T> the type of task executor
	 * @param taskExecutor the {@link ThreadPoolTaskExecutor} to configure
	 * @return the task executor instance
	 * @see #build()
	 * @see #build(Class)
	 */
	public <T extends ThreadPoolTaskExecutor> T configure(T taskExecutor) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(this.queueCapacity).to(taskExecutor::setQueueCapacity);
		map.from(this.corePoolSize).to(taskExecutor::setCorePoolSize);
		map.from(this.maxPoolSize).to(taskExecutor::setMaxPoolSize);
		map.from(this.keepAlive).asInt(Duration::getSeconds).to(taskExecutor::setKeepAliveSeconds);
		map.from(this.allowCoreThreadTimeOut).to(taskExecutor::setAllowCoreThreadTimeOut);
		map.from(this.acceptTasksAfterContextClose).to(taskExecutor::setAcceptTasksAfterContextClose);
		map.from(this.awaitTermination).to(taskExecutor::setWaitForTasksToCompleteOnShutdown);
		map.from(this.awaitTerminationPeriod).as(Duration::toMillis).to(taskExecutor::setAwaitTerminationMillis);
		map.from(this.threadNamePrefix).whenHasText().to(taskExecutor::setThreadNamePrefix);
		map.from(this.taskDecorator).to(taskExecutor::setTaskDecorator);
		if (!CollectionUtils.isEmpty(this.customizers)) {
			this.customizers.forEach((customizer) -> customizer.customize(taskExecutor));
		}
		return taskExecutor;
	}

	private <T> Set<T> append(@Nullable Set<T> set, Iterable<? extends T> additions) {
		Set<T> result = new LinkedHashSet<>((set != null) ? set : Collections.emptySet());
		additions.forEach(result::add);
		return Collections.unmodifiableSet(result);
	}

}
