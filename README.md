# IMS
# Inventory Management System - Sistema de Gerenciamento de Estoque

O Sistema de Gerenciamento de Estoque é uma solução tecnológica que ajuda empresas a monitorar, controlar e organizar os seus estoques de forma eficiente. Ele funciona como um registro centralizado e automatizado que permite rastrear todas as movimentações de produtos ou materiais dentro de um negócio, desde a entrada no armazém até a entrega ao cliente final.

# Principais Funcionalidades do Sistema

* Cadastro de Produtos:

O sistema permite o registro detalhado dos itens, incluindo informações como código, descrição, categoria, quantidade mínima e máxima, local de armazenamento e fornecedores.

* Controle de Entradas e Saídas:

Registra automaticamente todas as movimentações de entrada (compras ou devoluções) e saída (vendas, transferências ou perdas), garantindo uma visão clara e atualizada do estoque.

* Inventário em Tempo Real:

Com o uso de tecnologias como códigos de barras ou RFID, é possível saber em tempo real a quantidade disponível de cada item, reduzindo erros e otimizando a reposição de produtos.

* Alertas de Estoque:

O sistema gera notificações quando um item atinge o limite mínimo ou quando há excesso, ajudando a evitar rupturas ou sobrecarga no estoque.

* Relatórios e Análises:
  
Ferramentas de relatórios oferecem insights sobre o desempenho do estoque, como itens mais vendidos, produtos com baixa rotatividade e sazonalidade, auxiliando na tomada de decisões estratégicas.

* Integração com Outros Sistemas:
  
Muitos sistemas de gerenciamento de estoque podem ser integrados com plataformas de vendas, ERP, ou sistemas de contabilidade, permitindo um fluxo de informações mais dinâmico e reduzindo o retrabalho.

# Benefícios do Sistema de Gerenciamento de Estoque

* Eficiência Operacional: Reduz erros manuais e o tempo gasto em tarefas como contagem de estoque e pedidos de reposição.
  
* Redução de Custos: Evita compras desnecessárias e perdas por vencimento ou obsolescência de produtos.

* Satisfação do Cliente: Garante a disponibilidade de produtos, evitando atrasos ou indisponibilidade no momento da venda.
  
* Tomada de Decisões Baseada em Dados: Oferece informações estratégicas para otimizar a gestão do negócio.

Esse sistema é essencial para empresas de qualquer porte ou segmento que buscam uma gestão eficiente, reduzindo perdas e melhorando a produtividade geral.

## Recuperação de senha

Para habilitar o envio de tokens de recuperação no Docker, configure as variáveis SMTP no arquivo `.env`:

```dotenv
SPRING_MAIL_HOST=smtp.seu-provedor.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=seu-usuario
SPRING_MAIL_PASSWORD=sua-senha-ou-app-password
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_STARTTLS=true
```

O token enviado por e-mail expira em 15 minutos e pode ser usado uma única vez.

## Isolamento multi-tenant

Cada empresa (`Company`) é o tenant da aplicação. O backend obtém o tenant do usuário autenticado e o Hibernate restringe automaticamente as consultas e gravações de usuários, clientes, inventários, itens, vendas e locações a essa empresa.

Ao aplicar a migração `V5`, os registros existentes são associados à primeira empresa cadastrada. Se ainda não houver uma empresa, é criada uma empresa legada para preservar os dados atuais. Códigos e documentos que precisam ser únicos são únicos dentro do tenant, não globalmente.

## Planos, cadastro e Mercado Pago

A migração `V6` cria o catálogo de funcionalidades, planos, assinaturas e eventos de cobrança; a `V7` adiciona período de avaliação, limites por plano e mantém as empresas existentes em um plano legado sem cobrança. O cadastro público (`/signup`) cria a empresa e o administrador com período de avaliação de 14 dias (configurável por `SAAS_TRIAL_DAYS`). Após o trial, o acesso às funcionalidades depende de uma assinatura ativa.

A primeira configuração do serviço deve:

1. Definir `MERCADOPAGO_ACCESS_TOKEN`, `MERCADOPAGO_WEBHOOK_SECRET`, `MERCADOPAGO_NOTIFICATION_URL` e `MERCADOPAGO_BACK_URL` no ambiente do backend. A URL de notificação deve apontar para `/api/billing/webhook`; use credenciais de teste do Mercado Pago para validar antes de produção.
2. Definir `PLATFORM_ADMIN_USERNAME` e `PLATFORM_ADMIN_PASSWORD` (mínimo de 12 caracteres) para criar uma conta global de administração. Essa conta é criada uma vez; remova as variáveis depois do bootstrap.
3. Entrar em `/saas-admin`, criar planos, definir limites opcionais de usuários/clientes/itens de estoque e associar funcionalidades. O auto cadastro fica disponível em `/signup` quando houver planos ativos.

O webhook valida a assinatura HMAC do Mercado Pago, consulta o estado da assinatura diretamente à API do provedor e deduplica eventos. O painel `/saas-admin` permite administrar empresas, ver indicadores e uso por tenant, suspender/reativar acesso, gerenciar planos e funcionalidades e consultar assinaturas, pagamentos e eventos. O administrador de cada empresa pode consultar e cancelar a assinatura em `/billing`.

Credenciais não devem ser incluídas no repositório nem no `.env` enviado ao controle de versão. Os arquivos `.example-env` contêm apenas os nomes das variáveis.
