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
import { isDefinedAndNotNull } from '@core/utils';
import { AggregationType, FixedWindow } from '@shared/models/time/time.models';
import { Direction } from '@shared/models/page/sort-order';
import { StatsChartType, TimeseriesData } from '@shared/models/chart.model';

@Injectable({
  providedIn: 'root'
})
export class StatsService {

  constructor(private http: HttpClient) {
  }

  public getEntityTimeseries(entityId: string, startTs: number, endTs: number,
                             keys: Array<string> = ["incomingMsgs", "outgoingMsgs", "droppedMsgs", "sessions", "subscriptions"],
                             limit: number = 100, agg: AggregationType = AggregationType.NONE, interval?: number,
                             orderBy: Direction = Direction.DESC, useStrictDataTypes: boolean = false,
                             config?: RequestConfig): Observable<TimeseriesData> {
    let url = `/api/timeseries/${entityId}/values?keys=${keys.join(',')}&startTs=${startTs}&endTs=${endTs}`;
    if (isDefinedAndNotNull(limit)) {
      url += `&limit=${limit}`;
    }
    if (isDefinedAndNotNull(agg)) {
      url += `&agg=${agg}`;
    }
    if (isDefinedAndNotNull(interval)) {
      url += `&interval=${interval}`;
    }
    if (isDefinedAndNotNull(orderBy)) {
      url += `&orderBy=${orderBy}`;
    }
    if (isDefinedAndNotNull(useStrictDataTypes)) {
      url += `&useStrictDataTypes=${useStrictDataTypes}`;
    }
    return this.http.get<TimeseriesData>(url, defaultHttpOptionsFromConfig(config));
  }

  public getLatestTimeseries(entityId: string, keys: Array<string> = ["incomingMsgs", "outgoingMsgs", "droppedMsgs", "sessions", "subscriptions"],
                             useStrictDataTypes: boolean = false, config?: RequestConfig): Observable<TimeseriesData> {
    let url = `/api/timeseries/${entityId}/latest?keys=${keys.join(',')}`;
    if (isDefinedAndNotNull(useStrictDataTypes)) {
      url += `&useStrictDataTypes=${useStrictDataTypes}`;
    }
    return this.http.get<TimeseriesData>(url, defaultHttpOptionsFromConfig(config));
  }

  public getMonitoringInitData(config?: RequestConfig): Observable<any> {
    const dataset: any = {};
    for (const chart in StatsChartType) {
      const data = [];
      let ts = Date.now() - (60 * 60 * 1000 * 10);
      for (let i = 0; i < 10; i++) {
        ts = ts + (60 * 60 * 1000);
        const value = Math.floor(Math.random() * 10) + 5;
        data.push({value, ts});
      }
      dataset[chart] = data;
    }
    return of(dataset);
  }

  public pollMonitoringLatestValue(config?: RequestConfig): Observable<any> {
    const dataset: any = {};
    for (const chart in StatsChartType) {
      const data = [];
      const ts = Date.now() + (60 * 60 * 1000);
      const value = Math.floor(Math.random() * 10) + 5;
      data.push({value, ts});
      dataset[chart] = data;
    }
    return of(dataset);
  }

  public getEntityTimeseriesMock(startTime?: number, endTime?: number, config?: RequestConfig): Observable<any> {
    const dataset: any = {};
    for (const chart in StatsChartType) {
      const data = [];
      let ts = Date.now() - (60 * 1000);
      for (let i = 0; i < 10; i++) {
        ts = ts + (5 * 1000);
        const value = Math.floor(Math.random() * 10) + 5;
        data.push({value, ts});
      }
      dataset[chart] = data;
    }
    return of(dataset);
  }

  public pollEntityTimeseriesMock(config?: RequestConfig): Observable<any> {
    const dataset: any = {};
    for (const chart in StatsChartType) {
      const randomValuesNumber = 1;
      const data = [];
      const ts = Date.now() + (10 * 1000);
      for (let i = 0; i < randomValuesNumber; i++) {
        const value = Math.floor(Math.random() * 10) + 5;
        data.push({value, ts});
      }
      dataset[chart] = data;
    }
    return of(dataset);
  }
}
