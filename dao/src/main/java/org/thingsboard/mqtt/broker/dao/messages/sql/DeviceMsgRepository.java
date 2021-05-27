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
package org.thingsboard.mqtt.broker.dao.messages.sql;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.mqtt.broker.dao.model.sql.DevicePublishMsgCompositeKey;
import org.thingsboard.mqtt.broker.dao.model.sql.DevicePublishMsgEntity;

import java.util.List;

import static org.thingsboard.mqtt.broker.dao.model.ModelConstants.DEVICE_PUBLISH_MSG_CLIENT_ID_PROPERTY;
import static org.thingsboard.mqtt.broker.dao.model.ModelConstants.DEVICE_PUBLISH_MSG_COLUMN_FAMILY_NAME;
import static org.thingsboard.mqtt.broker.dao.model.ModelConstants.DEVICE_PUBLISH_MSG_SERIAL_NUMBER_PROPERTY;
import static org.thingsboard.mqtt.broker.dao.model.ModelConstants.DEVICE_PUBLISH_MSG_TIME_PROPERTY;

public interface DeviceMsgRepository extends CrudRepository<DevicePublishMsgEntity, DevicePublishMsgCompositeKey> {
    @Query(value = "SELECT * FROM " + DEVICE_PUBLISH_MSG_COLUMN_FAMILY_NAME + " pubMsg " +
            "WHERE pubMsg." + DEVICE_PUBLISH_MSG_CLIENT_ID_PROPERTY + " = :clientId " +
            "ORDER BY pubMsg." + DEVICE_PUBLISH_MSG_SERIAL_NUMBER_PROPERTY + " DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<DevicePublishMsgEntity> findByClientIdReversed(@Param("clientId") String clientId,
                                                        @Param("limit") int limit);

    @Query(value = "SELECT * FROM " + DEVICE_PUBLISH_MSG_COLUMN_FAMILY_NAME + " pubMsg " +
            "WHERE pubMsg." + DEVICE_PUBLISH_MSG_CLIENT_ID_PROPERTY + " = :clientId " +
            "ORDER BY pubMsg." + DEVICE_PUBLISH_MSG_SERIAL_NUMBER_PROPERTY + " DESC " +
            "OFFSET :offset " +
            "LIMIT 1",
            nativeQuery = true)
    DevicePublishMsgEntity findEntityByClientIdAfterOffset(@Param("clientId") String clientId,
                                                           @Param("offset") int offset);

    @Transactional
    void removeAllByClientId(String clientId);

    @Transactional
    void removeAllByClientIdAndPacketId(String clientId, int packetId);

    @Transactional
    int removeAllByTimeLessThan(long earliestAcceptableTime);

    @Transactional
    int removeAllByClientIdAndSerialNumberLessThan(String clientId, long earliestAcceptableSerialNumber);
}
