import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.css']
})
export class ProjectsComponent {
  projects: any[] = [];
  purchaseOrders: any[] = [];
  selectedProject: any = null;
  showProjectModal: boolean = false;
  showPOModal: boolean = false;
  editingProject: boolean = false;
  editingPO: boolean = false;
  projectForm: FormGroup;
  poForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.projectForm = this.fb.group({
      projectName: [''],
      projectDescription: [''],
      projectBudget: [0]
    });

    this.poForm = this.fb.group({
      poNumber: [''],
      value: [0]
    });
  }

  openNewProjectModal() {
    this.editingProject = false;
    this.projectForm.reset();
    this.showProjectModal = true;
  }

  editProject(project: any) {
    this.editingProject = true;
    this.projectForm.patchValue(project);
    this.showProjectModal = true;
  }

  deleteProject(projectId: number) {
    this.projects = this.projects.filter(p => p.projectId !== projectId);
  }

  backToList() {
    this.selectedProject = null;
  }

  openNewPOModal() {
    this.editingPO = false;
    this.poForm.reset();
    this.showPOModal = true;
  }

  editPO(po: any) {
    this.editingPO = true;
    this.poForm.patchValue(po);
    this.showPOModal = true;
  }

  deletePO(poId: number) {
    this.purchaseOrders = this.purchaseOrders.filter(po => po.poId !== poId);
  }

  saveProject() {
    if (this.editingProject) {
      const index = this.projects.findIndex(p => p.projectName === this.projectForm.value.projectName);
      this.projects[index] = this.projectForm.value;
    } else {
      this.projects.push(this.projectForm.value);
    }
    this.closeProjectModal();
  }

  closeProjectModal() {
    this.showProjectModal = false;
  }

  savePO() {
    if (this.editingPO) {
      const index = this.purchaseOrders.findIndex(po => po.poNumber === this.poForm.value.poNumber);
      this.purchaseOrders[index] = this.poForm.value;
    } else {
      this.purchaseOrders.push(this.poForm.value);
    }
    this.closePOModal();
  }

  closePOModal() {
    this.showPOModal = false;
  }

  viewProject(project: any) {
    this.selectedProject = project;
  }
}
