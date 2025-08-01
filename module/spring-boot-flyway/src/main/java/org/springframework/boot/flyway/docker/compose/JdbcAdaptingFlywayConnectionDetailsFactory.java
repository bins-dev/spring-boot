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

package org.springframework.boot.flyway.docker.compose;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetailsFactory;
import org.springframework.boot.flyway.autoconfigure.FlywayConnectionDetails;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;

/**
 * {@link ConnectionDetailsFactory} that produces {@link FlywayConnectionDetails} by
 * adapting {@link JdbcConnectionDetails}.
 *
 * @author Andy Wilkinson
 */
class JdbcAdaptingFlywayConnectionDetailsFactory
		implements ConnectionDetailsFactory<JdbcConnectionDetails, FlywayConnectionDetails> {

	@Override
	public FlywayConnectionDetails getConnectionDetails(JdbcConnectionDetails input) {
		return new FlywayConnectionDetails() {

			@Override
			public @Nullable String getUsername() {
				return input.getUsername();
			}

			@Override
			public @Nullable String getPassword() {
				return input.getPassword();
			}

			@Override
			public String getJdbcUrl() {
				return input.getJdbcUrl();
			}

			@Override
			public String getDriverClassName() {
				return input.getDriverClassName();
			}

		};
	}

}
