import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

// Interfaces for dropdown data
interface Project {
  id: number;
  name: string;
}

interface Resource {
  id: number;
  name: string;
  designation: string;
}

interface PurchaseOrder {
  id: number;
  poNumber: string;
}

interface ResourceAllocation {
  projectId: number;
  resourceId: number;
  poId: number;
  allocatedHours: number;
}

@Component({
  selector: 'app-resources',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    ReactiveFormsModule,
    HttpClientModule
  ],
  templateUrl: './resources.component.html',
  styleUrls: ['./resources.component.css']
})
export class ResourcesComponent {
}