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
package org.thingsboard.mqtt.broker.service.mqtt.persistence;

import org.thingsboard.mqtt.broker.common.data.ClientInfo;
import org.thingsboard.mqtt.broker.gen.queue.QueueProtos;
import org.thingsboard.mqtt.broker.service.processing.PublishMsgCallback;
import org.thingsboard.mqtt.broker.service.subscription.Subscription;
import org.thingsboard.mqtt.broker.session.ClientSessionCtx;

import java.util.Collection;

public interface MsgPersistenceManager {
    void processPublish(QueueProtos.PublishMsgProto publishMsgProto, Collection<Subscription> persistentSubscriptions, PublishMsgCallback callback);

    void processPersistedMessages(ClientSessionCtx clientSessionCtx);

    void stopProcessingPersistedMessages(ClientInfo clientInfo);

    void clearPersistedMessages(ClientInfo clientInfo);

    void acknowledgePersistedMsgDelivery(int packetId, ClientSessionCtx clientSessionCtx);
}