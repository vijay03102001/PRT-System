import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectDetails } from '../models/project.model';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'any'
})
export class ProjectDetailsService {
    private apiUrl = 'http://localhost:8080/api/project-details';

    constructor(private http: HttpClient,private authService: AuthService) { }
    private getAuthHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        return new HttpHeaders({
          Authorization: `Bearer ${token}`,
        });
      }

      getProjectDetails(): Observable<ProjectDetails[]> {
        return this.http.get<ProjectDetails[]>(this.apiUrl,{ headers: this.getAuthHeaders() });
    }
}
