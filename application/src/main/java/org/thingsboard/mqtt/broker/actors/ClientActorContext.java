/**
 * Copyright © 2016-2020 The Thingsboard Authors
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
package org.thingsboard.mqtt.broker.actors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thingsboard.mqtt.broker.actors.client.service.ActorProcessor;
import org.thingsboard.mqtt.broker.actors.client.service.MqttMessageHandler;
import org.thingsboard.mqtt.broker.actors.client.service.connect.ConnectService;
import org.thingsboard.mqtt.broker.actors.client.service.session.SessionClusterManager;
import org.thingsboard.mqtt.broker.actors.client.service.subscription.SubscriptionChangesManager;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class ClientActorContext {
    private final SessionClusterManager sessionClusterManager;
    private final SubscriptionChangesManager subscriptionChangesManager;
    private final ActorProcessor actorProcessor;
    private final ConnectService connectService;
    private final MqttMessageHandler mqttMessageHandler;
}
