///
/// Copyright © 2016-2023 The Thingsboard Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Injectable } from '@angular/core';
import { defaultHttpOptionsFromConfig, RequestConfig } from './http-utils';
import { Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { PageLink } from '@shared/models/page/page-link';
import { PageData } from '@shared/models/page/page-data';
import { KafkaBroker, KafkaConsumerGroup, KafkaTopic } from '@shared/models/kafka.model';

@Injectable({
  providedIn: 'root'
})
export class KafkaService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getKafkaBroker(pageLink: PageLink, config?: RequestConfig): Observable<PageData<KafkaBroker>> {
    const data: PageData<KafkaBroker> = {
      data: [],
      totalPages: pageLink.pageSize,
      totalElements: 100,
      hasNext: true
    };
    for (let i = 1; i < 11; i++) {
      data.data.push({
        id: 'id_' + i,
        address: 'localhost',
        size: i * 1000,
      });
    }
    return of(data);
    // return this.http.get<PageData<KafkaBroker>>(`/api/mqtt/kafka/brokers${pageLink.toQuery()}`,
    //   defaultHttpOptionsFromConfig(config));
  }

  public getKafkaTopics(pageLink: PageLink, config?: RequestConfig): Observable<PageData<KafkaTopic>> {
    const data: PageData<KafkaTopic> = {
      data: [],
      totalPages: pageLink.pageSize,
      totalElements: 100,
      hasNext: true
    };
    for (let i = 1; i < 1000; i++) {
      data.data.push({
        name: 'client_session_' + i,
        partitions: i,
        replicas: i * 5,
        size: i * 1000
      });
    }
    return of(data);
    // return this.http.get<PageData<KafkaTopic>>(`/api/mqtt/kafka/topics${pageLink.toQuery()}`,
    //   defaultHttpOptionsFromConfig(config));
  }

  public getKafkaConsumerGroups(pageLink: PageLink, config?: RequestConfig): Observable<PageData<KafkaConsumerGroup>> {
    const data: PageData<KafkaConsumerGroup> = {
      data: [],
      totalPages: pageLink.pageSize,
      totalElements: 100,
      hasNext: true
    };
    for (let i = 1; i < 1000; i++) {
      data.data.push({
        state: 'stable',
        id: 'id' + i,
        members: i,
        lag: i
      });
    }
    return of(data);
    // return this.http.get<PageData<KafkaConsumerGroup>>(`/api/mqtt/kafka/consumerGroups${pageLink.toQuery()}`,
    //   defaultHttpOptionsFromConfig(config));
  }

}
