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

package org.springframework.boot.test.autoconfigure.override;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.cassandra.autoconfigure.CassandraAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;

/**
 * Example {@link SpringBootApplication @SpringBootApplication} for use with
 * {@link OverrideAutoConfiguration @OverrideAutoConfiguration} tests.
 *
 * @author Andy Wilkinson
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = CassandraAutoConfiguration.class)
public class OverrideAutoConfigurationSpringBootApplication {

}
