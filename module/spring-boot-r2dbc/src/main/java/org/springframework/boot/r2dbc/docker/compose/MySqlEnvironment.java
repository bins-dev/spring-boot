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

package org.springframework.boot.r2dbc.docker.compose;

import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * MySQL environment details.
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Scott Frederick
 */
class MySqlEnvironment {

	private final String username;

	private final String password;

	private final String database;

	MySqlEnvironment(Map<String, String> env) {
		this.username = env.getOrDefault("MYSQL_USER", "root");
		this.password = extractPassword(env);
		this.database = extractDatabase(env);
	}

	private String extractPassword(Map<String, String> env) {
		Assert.state(!env.containsKey("MYSQL_RANDOM_ROOT_PASSWORD"), "MYSQL_RANDOM_ROOT_PASSWORD is not supported");
		boolean allowEmpty = env.containsKey("MYSQL_ALLOW_EMPTY_PASSWORD") || env.containsKey("ALLOW_EMPTY_PASSWORD");
		String password = env.get("MYSQL_PASSWORD");
		password = (password != null) ? password : env.get("MYSQL_ROOT_PASSWORD");
		Assert.state(StringUtils.hasLength(password) || allowEmpty, "No MySQL password found");
		return (password != null) ? password : "";
	}

	private String extractDatabase(Map<String, String> env) {
		String database = env.get("MYSQL_DATABASE");
		Assert.state(database != null, "No MYSQL_DATABASE defined");
		return database;
	}

	String getUsername() {
		return this.username;
	}

	String getPassword() {
		return this.password;
	}

	String getDatabase() {
		return this.database;
	}

}
