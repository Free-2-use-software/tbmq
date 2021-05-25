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
package org.thingsboard.mqtt.broker.service.mqtt.client.event;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.mqtt.broker.cluster.ServiceInfoProvider;
import org.thingsboard.mqtt.broker.common.data.ClientInfo;
import org.thingsboard.mqtt.broker.common.data.SessionInfo;
import org.thingsboard.mqtt.broker.common.util.ThingsBoardThreadFactory;
import org.thingsboard.mqtt.broker.gen.queue.QueueProtos;
import org.thingsboard.mqtt.broker.queue.TbQueueCallback;
import org.thingsboard.mqtt.broker.queue.TbQueueControlledOffsetConsumer;
import org.thingsboard.mqtt.broker.queue.TbQueueMsgMetadata;
import org.thingsboard.mqtt.broker.queue.TbQueueProducer;
import org.thingsboard.mqtt.broker.queue.common.TbProtoQueueMsg;
import org.thingsboard.mqtt.broker.queue.provider.ClientSessionEventQueueFactory;
import org.thingsboard.mqtt.broker.util.BytesUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.thingsboard.mqtt.broker.service.mqtt.client.event.ClientSessionEventConst.REQUEST_ID_HEADER;
import static org.thingsboard.mqtt.broker.service.mqtt.client.event.ClientSessionEventConst.REQUEST_TIME;
import static org.thingsboard.mqtt.broker.service.mqtt.client.event.ClientSessionEventConst.RESPONSE_TOPIC_HEADER;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultClientSessionEventService implements ClientSessionEventService {
    private final ConcurrentMap<UUID, EventFuture> pendingRequests = new ConcurrentHashMap<>();

    private final ExecutorService responseConsumerExecutor = Executors.newSingleThreadExecutor(ThingsBoardThreadFactory.forName("client-session-event-response-consumer"));
    private ScheduledExecutorService cleanupStaleRequestsScheduler;

    private volatile boolean stopped = false;
    private final AtomicLong tickTs = new AtomicLong();
    private final AtomicLong nextCleanupMs = new AtomicLong();
    private final AtomicLong tickSize = new AtomicLong();


    @Value("${queue.client-session-event.max-pending-requests}")
    private long maxPendingRequests;

    @Value("${queue.client-session-event-response.poll-interval}")
    private long pollDuration;
    @Value("${queue.client-session-event-response.cleanup-interval}")
    private long cleanupInterval;
    @Value("${queue.client-session-event-response.max-request-timeout}")
    private long maxRequestTimeout;

    private final ClientSessionEventQueueFactory clientSessionEventQueueFactory;
    private final ServiceInfoProvider serviceInfoProvider;
    private final ClientSessionEventFactory eventFactory;

    private TbQueueProducer<TbProtoQueueMsg<QueueProtos.ClientSessionEventProto>> eventProducer;
    private TbQueueControlledOffsetConsumer<TbProtoQueueMsg<QueueProtos.ClientSessionEventResponseProto>> eventResponseConsumer;

    @PostConstruct
    public void init() {
        this.eventProducer = clientSessionEventQueueFactory.createEventProducer(serviceInfoProvider.getServiceId());
        this.eventResponseConsumer = clientSessionEventQueueFactory.createEventResponseConsumer(serviceInfoProvider.getServiceId());
        startProcessingEventResponses();
        startStaleRequestsCleanup();
    }

    @Override
    public ListenableFuture<Boolean> connect(SessionInfo sessionInfo) {
        if (tickSize.get() > maxPendingRequests) {
            return Futures.immediateFailedFuture(new RuntimeException("Cannot send CONNECTION_REQUEST. Pending request map is full!"));
        }

        return sendEvent(sessionInfo.getClientInfo().getClientId(), eventFactory.createConnectionRequestEventProto(sessionInfo), true);
    }

    @Override
    public void disconnect(ClientInfo clientInfo, UUID sessionId) {
        sendEvent(clientInfo.getClientId(), eventFactory.createDisconnectedEventProto(clientInfo, sessionId), false);
    }

    @Override
    public void tryClear(SessionInfo sessionInfo) {
        sendEvent(sessionInfo.getClientInfo().getClientId(), eventFactory.createTryClearSessionRequestEventProto(sessionInfo), false);
    }

    private ListenableFuture<Boolean> sendEvent(String clientId, QueueProtos.ClientSessionEventProto eventProto, boolean isAwaitingResponse) {
        UUID requestId = UUID.randomUUID();
        TbProtoQueueMsg<QueueProtos.ClientSessionEventProto> eventRequest = generateRequest(clientId, eventProto, requestId);

        SettableFuture<Boolean> future = SettableFuture.create();
        if (isAwaitingResponse) {
            EventFuture eventFuture = new EventFuture(tickTs.get() + maxRequestTimeout, future);
            pendingRequests.putIfAbsent(requestId, eventFuture);
        }
        log.trace("[{}][{}][{}] Sending client session event request.", clientId, eventProto.getEventType(), requestId);
        eventProducer.send(eventRequest, new TbQueueCallback() {
            @Override
            public void onSuccess(TbQueueMsgMetadata metadata) {
                log.trace("[{}][{}][{}] Request sent: {}", clientId, eventProto.getEventType(), requestId, metadata);
            }

            @Override
            public void onFailure(Throwable t) {
                log.debug("[{}][{}][{}] Failed to send request. Reason - {}.", clientId, eventProto.getEventType(), requestId, t.getMessage());
                if (isAwaitingResponse) {
                    pendingRequests.remove(requestId);
                    future.setException(t);
                }
            }
        });
        return future;
    }

    private TbProtoQueueMsg<QueueProtos.ClientSessionEventProto> generateRequest(String clientId, QueueProtos.ClientSessionEventProto clientSessionEventProto, UUID requestId) {
        TbProtoQueueMsg<QueueProtos.ClientSessionEventProto> eventRequest = new TbProtoQueueMsg<>(clientId, clientSessionEventProto);
        eventRequest.getHeaders().put(REQUEST_ID_HEADER, BytesUtil.uuidToBytes(requestId));
        eventRequest.getHeaders().put(REQUEST_TIME, BytesUtil.longToBytes(System.currentTimeMillis()));
        eventRequest.getHeaders().put(RESPONSE_TOPIC_HEADER, BytesUtil.stringToBytes(eventResponseConsumer.getTopic()));
        return eventRequest;
    }


    private void startProcessingEventResponses() {
        eventResponseConsumer.subscribe();
        responseConsumerExecutor.execute(() -> {
            while (!stopped) {
                try {
                    List<TbProtoQueueMsg<QueueProtos.ClientSessionEventResponseProto>> eventResponseList = eventResponseConsumer.poll(pollDuration);
                    if (eventResponseList.size() > 0) {
                        log.trace("Read {} event responses.", eventResponseList.size());
                    } else {
                        continue;
                    }
                    for (TbProtoQueueMsg<QueueProtos.ClientSessionEventResponseProto> eventResponseMsg : eventResponseList) {
                        processEventResponse(eventResponseMsg);
                    }
                    eventResponseConsumer.commit();
                } catch (Throwable e) {
                    processingEventResponseError(e);
                }
            }
        });
    }

    private void processEventResponse(TbProtoQueueMsg<QueueProtos.ClientSessionEventResponseProto> eventResponseMsg) {
        byte[] requestIdBytes = eventResponseMsg.getHeaders().get(REQUEST_ID_HEADER);
        if (requestIdBytes == null) {
            log.error("Missing requestId. Msg - {}.", eventResponseMsg);
            return;
        }
        UUID requestId = BytesUtil.bytesToUuid(requestIdBytes);
        log.trace("[{}] Event response received: {}", requestId, eventResponseMsg);
        EventFuture eventFuture = pendingRequests.remove(requestId);
        if (eventFuture == null) {
            log.debug("[{}] Invalid or stale request.", requestId);
        } else {
            boolean success = eventResponseMsg.getValue().getSuccess();
            eventFuture.future.set(success);
        }
    }

    private void processingEventResponseError(Throwable e) {
        if (stopped) {
            log.debug("Ignoring error {} because service is stopped.", e.getMessage());
            return;
        }
        log.warn("Failed to obtain event responses from queue. Reason - {}.", e.getMessage());
        log.trace("Detailed error: ", e);
        try {
            Thread.sleep(pollDuration);
        } catch (InterruptedException e2) {
            log.trace("Failed to wait until the server has capacity to handle new responses. Reason - {}.", e2.getMessage());
        }
    }

    private void startStaleRequestsCleanup() {
        this.cleanupStaleRequestsScheduler = Executors.newSingleThreadScheduledExecutor(ThingsBoardThreadFactory.forName("client-session-event-response-clean-up-scheduler"));
        cleanupStaleRequestsScheduler.scheduleWithFixedDelay(() -> {
            tickTs.getAndSet(System.currentTimeMillis());
            if (nextCleanupMs.get() < tickTs.get()) {
                cleanup();
            }
            tickSize.getAndSet(pendingRequests.size());
        }, cleanupInterval, cleanupInterval, TimeUnit.MILLISECONDS);
    }

    private void cleanup() {
        pendingRequests.forEach((key, eventFuture) -> {
            if (eventFuture.expTime < tickTs.get()) {
                EventFuture staleRequest = pendingRequests.remove(key);
                if (staleRequest != null) {
                    log.debug("[{}] Request timeout detected, expTime [{}], tickTs [{}]", key, staleRequest.expTime, tickTs);
                    staleRequest.future.setException(new TimeoutException());
                }
            }
        });
        nextCleanupMs.getAndSet(tickTs.get() + maxRequestTimeout);
    }

    @PreDestroy
    public void destroy() {
        stopped = true;
        
        responseConsumerExecutor.shutdownNow();
        if (cleanupStaleRequestsScheduler != null) {
            cleanupStaleRequestsScheduler.shutdownNow();
        }
        
        if (eventProducer != null) {
            eventProducer.stop();
        }
        if (eventResponseConsumer != null) {
            eventResponseConsumer.unsubscribeAndClose();
        }
    }

    @AllArgsConstructor
    private static class EventFuture {
        private final long expTime;
        private final SettableFuture<Boolean> future;
    }
}
