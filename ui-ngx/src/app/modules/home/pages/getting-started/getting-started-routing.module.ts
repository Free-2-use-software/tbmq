///
/// Copyright © 2016-2025 The Thingsboard Authors
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
import { GettingStartedGuideComponent } from '@home/pages/getting-started/getting-started-guide.component';
import { BreadCrumbConfig, BreadCrumbLabelFunction } from '@shared/components/breadcrumb';
import { gettingStartedGuideTitle } from '@shared/models/getting-started.model';

export const gettingStartedBreadcumbLabelFunction: BreadCrumbLabelFunction<GettingStartedGuideComponent>
  = ((route, translate, component) => {
    const id = route.params?.guideId;
    return translate.instant(gettingStartedGuideTitle(id));
});

const routes: Routes = [
  {
    path: 'getting-started',
    data: {
      breadcrumb: {
        label: 'getting-started.getting-started',
        icon: 'school'
      }
    },
    children: [
      {
        path: '',
        loadComponent: () => import('@home/pages/getting-started/getting-started.component').then(m => m.GettingStartedComponent),
        data: {
          auth: [Authority.SYS_ADMIN],
          title: 'getting-started.getting-started'
        }
      },
      {
        path: ':guideId',
        loadComponent: () => import('@home/pages/getting-started/getting-started-guide.component').then(m => m.GettingStartedGuideComponent),
        data: {
          auth: [Authority.SYS_ADMIN],
          breadcrumb: {
            labelFunction: gettingStartedBreadcumbLabelFunction,
            icon: 'menu_book'
          } as BreadCrumbConfig<GettingStartedGuideComponent>
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GettingStartedRoutingModule {
}
