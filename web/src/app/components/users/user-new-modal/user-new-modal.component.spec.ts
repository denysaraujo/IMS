import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserNewModalComponent } from './user-new-modal.component';

describe('UserNewModalComponent', () => {
  let component: UserNewModalComponent;
  let fixture: ComponentFixture<UserNewModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserNewModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserNewModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
