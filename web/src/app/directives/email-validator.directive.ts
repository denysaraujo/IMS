import { Directive, ElementRef, HostListener, Input } from '@angular/core';
import { NgControl } from '@angular/forms';
import { UtilsService } from '../services/utils.service';

@Directive({
  selector: '[appEmailValidator]',
  standalone: true
})
export class EmailValidatorDirective {
  @Input() showValidation: boolean = true;

  constructor(
    private elementRef: ElementRef<HTMLInputElement>,
    private control: NgControl,
    private utils: UtilsService
  ) {}

  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = this.utils.formatEmail(input.value);
    
    // Atualiza o valor formatado
    if (this.control.control) {
      this.control.control.setValue(value, { emitEvent: false });
    }
  }

  @HostListener('blur')
  onBlur(): void {
    if (this.showValidation && this.control.control) {
      const value = this.control.value;
      
      if (value && !this.utils.isValidEmail(value)) {
        // Adiciona classe de erro
        this.elementRef.nativeElement.classList.add('border-red-500');
        
        // Opcional: setar erro no control
        this.control.control.setErrors({ invalidEmail: true });
      } else {
        // Remove classe de erro
        this.elementRef.nativeElement.classList.remove('border-red-500');
        
        if (this.control.control.hasError('invalidEmail')) {
          this.control.control.setErrors(null);
        }
      }
    }
  }
}