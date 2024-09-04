/**
 * Copyright © 2016-2024 The Thingsboard Authors
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
package org.thingsboard.mqtt.broker.dao.messages;

import org.thingsboard.mqtt.broker.common.data.StringUtils;

import java.io.Serial;
import java.io.Serializable;

public record ClientIdMessagesCacheKey(String clientId, String cachePrefix) implements Serializable {

    @Serial
    private static final long serialVersionUID = 65684921903757140L;

    @Override
    public String toString() {
        String keyBase = "{" + clientId + "}_messages";
        return StringUtils.isBlank(cachePrefix) ? keyBase : cachePrefix + keyBase;
    }

}