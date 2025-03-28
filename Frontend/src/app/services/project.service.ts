import { Injectable } from '@angular/core';
import { HttpClient,HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Project, PurchaseOrder} from '../models/project.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'any'
})
export class ProjectService {
  private apiUrl = 'http://localhost:8080/api/project-details';

  constructor(private http: HttpClient,private authService: AuthService) {}

  // Function to get Authorization headers
  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });
  }

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}`,{ headers: this.getAuthHeaders() });
  }

  getProject(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`,{ headers: this.getAuthHeaders() });
  }

  createProject(project: Project): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}`, project);
  }

  updateProject(id: number, project: Project): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${id}`, project);
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getProjectPOs(projectId: number): Observable<PurchaseOrder[]> {
    return this.http.get<PurchaseOrder[]>(`${this.apiUrl}/${projectId}/pos`,{ headers: this.getAuthHeaders() });
  }

  addPOToProject(projectId: number, po: PurchaseOrder): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.apiUrl}/${projectId}/pos`, po);
  }
}
