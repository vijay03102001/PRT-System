import { Component, OnInit } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-side-nav',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './side-nav.component.html',
  styleUrls: ['./side-nav.component.css']
})
export class SideNavComponent {
  sidebarResponsive = false;
  isResourceMenuOpen = false;

  toggleSidebar() {
    this.sidebarResponsive = !this.sidebarResponsive;
  }

  toggleResourceMenu() {
    this.isResourceMenuOpen = !this.isResourceMenuOpen;
  }
}