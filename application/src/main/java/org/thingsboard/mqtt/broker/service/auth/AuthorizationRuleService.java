/**
 * Copyright © 2016-2022 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.mqtt.broker.service.auth;

import org.thingsboard.mqtt.broker.common.data.client.credentials.BasicMqttCredentials;
import org.thingsboard.mqtt.broker.common.data.client.credentials.SslMqttCredentials;
import org.thingsboard.mqtt.broker.exception.AuthenticationException;
import org.thingsboard.mqtt.broker.service.security.authorization.AuthorizationRule;

import java.util.List;

public interface AuthorizationRuleService {
    List<AuthorizationRule> parseSslAuthorizationRule(SslMqttCredentials credentials, String clientCommonName) throws AuthenticationException;

    AuthorizationRule parseBasicAuthorizationRule(BasicMqttCredentials credentials) throws AuthenticationException;

    boolean isAuthorized(String topic, List<AuthorizationRule> authorizationRules);
}
