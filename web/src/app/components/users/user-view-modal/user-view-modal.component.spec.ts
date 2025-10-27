import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewUserModalComponent } from './user-view-modal.component';

describe('UserViewModalComponent', () => {
  let component: ViewUserModalComponent;
  let fixture: ComponentFixture<ViewUserModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewUserModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewUserModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
