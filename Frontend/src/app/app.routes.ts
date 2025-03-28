import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { HomeComponent } from './home/home.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProjectsComponent } from './projects/projects.component';
import { ResourcesComponent } from './resources/resources.component';
import { TimesheetsComponent } from './timesheets/timesheets.component';
import { ReportsComponent } from './reports/reports.component';
import { ResourceCostComponent } from './resource-cost/resource-cost.component';
import { ResourceDetailsComponent } from './resource-details/resource-details.component';
import { AllocationUtilizationComponent } from './allocation-utilization/allocation-utilization.component';
import {LoginComponent} from './login/login.component'

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'dashboard',
    component: DashboardComponent
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'projects',
    component: ProjectsComponent
  },

  {
    path: 'resources',
    component: ResourcesComponent
  },

  {
    path: 'resource-cost',
    component: ResourceCostComponent
  },

  {
    path: 'resource-details',
    component: ResourceDetailsComponent
  },

  { path: 'allocation-utilization',
    component: AllocationUtilizationComponent
  },

  {
    path: 'timesheets',
    component: TimesheetsComponent
  },
  {
    path: 'reports',
    component: ReportsComponent
  },
  {
    path: '**',
    redirectTo: '' // Redirect to home for any undefined routes
  }
];
