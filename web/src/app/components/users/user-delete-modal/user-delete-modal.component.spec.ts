import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteUserModalComponent } from './user-delete-modal.component';

describe('UserDeleteModalComponent', () => {
  let component: DeleteUserModalComponent;
  let fixture: ComponentFixture<DeleteUserModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteUserModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeleteUserModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
