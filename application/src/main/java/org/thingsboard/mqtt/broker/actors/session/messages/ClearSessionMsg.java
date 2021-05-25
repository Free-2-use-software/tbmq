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
package org.thingsboard.mqtt.broker.actors.session.messages;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.thingsboard.mqtt.broker.actors.TbActorId;
import org.thingsboard.mqtt.broker.actors.msg.MsgType;

import java.util.UUID;

@Slf4j
@Getter
public class ClearSessionMsg extends CallbackMsg {
    private final UUID sessionId;

    public ClearSessionMsg(ClientSessionCallback callback, UUID sessionId) {
        super(callback);
        this.sessionId = sessionId;
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.CLEAR_SESSION_MSG;
    }

    @Override
    public void onTbActorStopped(TbActorId actorId) {
        super.onTbActorStopped(actorId);
        log.debug("[{}] Actor was stopped before processing {},sessionId - {}.", actorId, getMsgType(), sessionId);
    }
}
