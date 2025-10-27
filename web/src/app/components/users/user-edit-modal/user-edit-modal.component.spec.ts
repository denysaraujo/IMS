import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditUserModalComponent } from './user-edit-modal.component';

describe('UserEditModalComponent', () => {
  let component: EditUserModalComponent;
  let fixture: ComponentFixture<EditUserModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditUserModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditUserModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
