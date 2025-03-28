import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  logoPath = 'assets/images/logo.jpeg';
  avatarPath = 'assets/images/user-avatar.jpg';

  currentTime: string = '';

  constructor() {}

  ngOnInit(): void {
    // Update current time
    this.updateCurrentTime();
    setInterval(() => this.updateCurrentTime(), 1000);
  }

  updateCurrentTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  toggleSidebar(): void {
    // Placeholder for sidebar toggle functionality
    console.log('Sidebar toggle clicked');
  }
}
