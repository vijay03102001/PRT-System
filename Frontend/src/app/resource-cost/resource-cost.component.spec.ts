import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceCostComponent } from './resource-cost.component';

describe('ResourceCostComponent', () => {
  let component: ResourceCostComponent;
  let fixture: ComponentFixture<ResourceCostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceCostComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResourceCostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
