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

import { AfterViewInit, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { forkJoin, Observable, Subject, timer } from 'rxjs';
import { retry, shareReplay, switchMap, takeUntil } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { ClientCredentialsInfo } from '@shared/models/client-crenetials.model';
import { MqttClientCredentialsService } from '@core/http/mqtt-client-credentials.service';
import { MqttClientSessionService } from '@core/http/mqtt-client-session.service';
import { ClientSessionStatsInfo } from '@shared/models/session.model';
import { CredentialsHomeCardConfig, SessionsHomeCardConfig } from '@shared/models/home-page.model';

@Component({
  selector: 'tb-home-cards-sessions-credentials',
  templateUrl: './home-cards-sessions-credentials.component.html'
})
export class HomeCardsSessionsCredentialsComponent implements OnInit, AfterViewInit, OnDestroy {

  @Input()
  isLoading$: Observable<boolean>;

  private pollData$: Observable<any>;
  private stopPolling = new Subject();

  clientSessionStatsInfo: ClientSessionStatsInfo;
  clientCredentialsInfo: ClientCredentialsInfo;

  sessionConfig = SessionsHomeCardConfig;
  credentialsConfig = CredentialsHomeCardConfig;

  constructor(private dialog: MatDialog,
              private mqttClientCredentialsService: MqttClientCredentialsService,
              private mqttClientSessionService: MqttClientSessionService) {
  }

  ngOnInit(): void {
    this.pollData$ = timer(0, 10000).pipe(
      switchMap(() => forkJoin(
        this.mqttClientSessionService.getClientSessionsStats(),
        this.mqttClientCredentialsService.getClientCredentialsStatsInfo()
      )),
      retry(),
      shareReplay(),
      takeUntil(this.stopPolling)
    );
  }

  ngAfterViewInit(): void {
    this.startPolling();
  }

  ngOnDestroy(): void {
    this.stopPolling.next();
  }

  startPolling() {
    this.pollData$.subscribe(data => {
      this.clientSessionStatsInfo = data[0];
      this.clientCredentialsInfo = data[1];
      this.setConfig();
    });
  }

  private setConfig() {
    this.sessionConfig.map(el => {
      el.value = this.clientSessionStatsInfo ? this.clientSessionStatsInfo[el.key] : 0;
    });
    this.sessionConfig.map(el => {
      el.value = this.clientSessionStatsInfo ? this.clientSessionStatsInfo[el.key] : 0;
    });
  }

  viewDocumentation(page: string) {
    window.open(`https://thingsboard.io/docs/mqtt-broker/${page}`, '_blank');
  }
}
