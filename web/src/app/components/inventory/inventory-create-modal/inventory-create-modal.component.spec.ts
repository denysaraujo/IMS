import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InventoryCreateModalComponent } from './inventory-create-modal.component';

describe('InventoryCreateModalComponent', () => {
  let component: InventoryCreateModalComponent;
  let fixture: ComponentFixture<InventoryCreateModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventoryCreateModalComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryCreateModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('emits the inventory fields on submit', () => {
    const inventory = {
      name: 'Estoque da loja',
      location: 'Loja principal',
      category: 'Geral',
      description: 'Produtos para venda'
    };
    spyOn(component.inventorySaved, 'emit');
    component.inventory = inventory;

    component.onSubmit();

    expect(component.inventorySaved.emit).toHaveBeenCalledWith(inventory);
  });

  it('emits cancel when the modal is dismissed', () => {
    spyOn(component.cancel, 'emit');

    component.onCancel();

    expect(component.cancel.emit).toHaveBeenCalled();
  });
});