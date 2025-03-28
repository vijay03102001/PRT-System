import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SideNavComponent } from './side-nav/side-nav.component';
import { HeaderComponent } from './header/header.component';
import { filter } from 'rxjs/operators';
import { MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, SideNavComponent, HeaderComponent,MatSnackBarModule],
  template: `
    <app-header *ngIf="!isHeaderHidden"></app-header>
    <div class="main-content">
      <app-side-nav *ngIf="!isSideNavHidden"></app-side-nav>
      <router-outlet (activate)="onRouteActivated($event)"></router-outlet>
    </div>
  `,
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  isHeaderHidden = false;
  isSideNavHidden = false;

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      const currentUrl = this.router.url;
        this.isHeaderHidden = currentUrl === '/' || currentUrl === '/login';
        this.isSideNavHidden = currentUrl === '/' || currentUrl === '/login';
    });
  }

  onRouteActivated(component: any) {
    // You can keep this method for additional logic if needed
  }
}
