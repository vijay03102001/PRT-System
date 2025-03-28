import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllocationUtilizationComponent } from './allocation-utilization.component';

describe('AllocationUtilizationComponent', () => {
  let component: AllocationUtilizationComponent;
  let fixture: ComponentFixture<AllocationUtilizationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AllocationUtilizationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AllocationUtilizationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
