import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-billing-return',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main class="flex min-h-screen items-center justify-center bg-slate-50 p-4 dark:bg-gray-950">
      <section class="w-full max-w-lg rounded-2xl bg-white p-8 text-center shadow-xl dark:bg-gray-900">
        <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Retorno do pagamento</h1>
        <p class="mt-3 text-slate-600 dark:text-gray-300">
          O Mercado Pago está confirmando sua assinatura. A liberação de acesso acontece automaticamente após a confirmação.
        </p>
        <a routerLink="/login" class="mt-6 inline-block rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white">Ir para o login</a>
      </section>
    </main>
  `
})
export class BillingReturnComponent {}
