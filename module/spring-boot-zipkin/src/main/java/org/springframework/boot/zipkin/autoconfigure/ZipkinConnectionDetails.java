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

package org.springframework.boot.zipkin.autoconfigure;

import zipkin2.reporter.HttpEndpointSupplier.Factory;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

/**
 * Details required to establish a connection to a Zipkin server.
 * <p>
 * Note: {@linkplain #getSpanEndpoint()} is only read once and passed to a bean of type
 * {@link Factory HttpEndpointSupplier.Factory} which defaults to no-op (constant).
 *
 * @author Moritz Halbritter
 * @since 4.0.0
 */
public interface ZipkinConnectionDetails extends ConnectionDetails {

	/**
	 * The endpoint for the span reporting.
	 * @return the endpoint
	 */
	String getSpanEndpoint();

}
