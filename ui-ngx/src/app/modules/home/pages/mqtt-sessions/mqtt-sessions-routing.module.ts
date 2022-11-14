///
/// Copyright © 2016-2022 The Thingsboard Authors
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

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Authority } from '@shared/models/authority.enum';
import { EntitiesTableComponent } from '@home/components/entity/entities-table.component';
import { MqttSessionsTableConfigResolver } from '@home/pages/mqtt-sessions/mqtt-sessions-table-config.resolver';

const routes: Routes = [
  {
    path: 'sessions',
    data: {
      title: 'mqtt-client-session.sessions',
      breadcrumb: {
        label: 'mqtt-client-session.sessions',
        icon: 'mdi:account-supervisor',
      }
    },
    children: [
      {
        path: '',
        component: EntitiesTableComponent,
        data: {
          auth: [Authority.SYS_ADMIN],
          title: 'mqtt-client-session.sessions'
        },
        resolve: {
          entitiesTableConfig: MqttSessionsTableConfigResolver
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [
    MqttSessionsTableConfigResolver
  ]
})

export class MqttSessionsRoutingModule { }
