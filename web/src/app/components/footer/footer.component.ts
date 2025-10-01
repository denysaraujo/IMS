import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  imports: [CommonModule, RouterModule]
})
export class FooterComponent {
  currentYear = new Date().getFullYear();
  appVersion = '1.0.0';
  companyInfo = 'Duo Technology';
}