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

package org.springframework.boot.actuate.info;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.actuate.info.OsInfoContributor.OsInfoContributorRuntimeHints;
import org.springframework.boot.info.OsInfo;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * An {@link InfoContributor} that exposes {@link OsInfo}.
 *
 * @author Jonatan Ivanov
 * @since 2.7.0
 */
@ImportRuntimeHints(OsInfoContributorRuntimeHints.class)
public class OsInfoContributor implements InfoContributor {

	private final OsInfo osInfo;

	public OsInfoContributor() {
		this.osInfo = new OsInfo();
	}

	@Override
	public void contribute(Info.Builder builder) {
		builder.withDetail("os", this.osInfo);
	}

	static class OsInfoContributorRuntimeHints implements RuntimeHintsRegistrar {

		private final BindingReflectionHintsRegistrar bindingRegistrar = new BindingReflectionHintsRegistrar();

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			this.bindingRegistrar.registerReflectionHints(hints.reflection(), OsInfo.class);
		}

	}

}
