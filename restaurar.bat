@echo off
setlocal EnableExtensions EnableDelayedExpansion

title Restauracao do Banco de Dados

echo.
echo ==========================================
echo       RESTAURACAO DO BANCO DE DADOS
echo ==========================================
echo.

REM ==========================================
REM Verificar Docker
REM ==========================================

docker info >nul 2>&1

if %errorlevel% neq 0 (
    echo ERRO: O Docker Desktop nao esta em execucao.
    echo.
    echo Inicie o Docker Desktop e tente novamente.
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Verificar container PostgreSQL
REM ==========================================

docker inspect sistema_postgres >nul 2>&1

if %errorlevel% neq 0 (
    echo ERRO: O container sistema_postgres nao foi encontrado.
    echo.
    echo Inicie o sistema com:
    echo docker compose up -d
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Verificar se PostgreSQL esta executando
REM ==========================================

for /f "delims=" %%I in ('docker inspect -f "{{.State.Running}}" sistema_postgres') do set POSTGRES_RUNNING=%%I

if /I not "!POSTGRES_RUNNING!"=="true" (
    echo ERRO: O PostgreSQL nao esta em execucao.
    echo.
    echo Inicie o sistema com:
    echo docker compose up -d
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Verificar pasta de backup
REM ==========================================

if not exist "backup" (
    echo ERRO: A pasta "backup" nao existe.
    echo.
    echo Nenhum backup foi encontrado.
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Localizar backups
REM ==========================================

set COUNT=0

for /f "delims=" %%F in ('dir /b /a-d /o-d "backup\*.sql" 2^>nul') do (
    set /a COUNT+=1
    set "FILE!COUNT!=%%F"
)

if !COUNT! EQU 0 (
    echo Nenhum arquivo .sql foi encontrado na pasta:
    echo.
    echo backup\
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Listar backups
REM ==========================================

echo Backups disponiveis:
echo.
echo ------------------------------------------

for /L %%N in (1,1,!COUNT!) do (
    echo [%%N] !FILE%%N!
)

echo ------------------------------------------
echo.

REM ==========================================
REM Selecionar backup
REM ==========================================

set "OPTION="

set /p OPTION="Digite o numero do backup que deseja restaurar: "

if "!OPTION!"=="" (
    echo.
    echo Nenhuma opcao foi selecionada.
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Obter arquivo selecionado
REM ==========================================

set "SELECTED_FILE="

for %%N in (!OPTION!) do (
    set "SELECTED_FILE=!FILE%%N!"
)

if "!SELECTED_FILE!"=="" (
    echo.
    echo Opcao invalida.
    echo.
    pause
    exit /b 1
)

set "BACKUP_PATH=backup\!SELECTED_FILE!"

REM ==========================================
REM Confirmacao
REM ==========================================

echo.
echo ==========================================
echo BACKUP SELECIONADO:
echo !SELECTED_FILE!
echo ==========================================
echo.
echo ATENCAO!
echo.
echo A restauracao ira APAGAR os dados atuais
echo do banco de dados antes de importar o backup.
echo.
echo Todos os dados cadastrados depois da data
echo deste backup serao perdidos.
echo.

set "CONFIRM="

set /p CONFIRM="Deseja realmente continuar? Digite SIM para confirmar: "

if /I not "!CONFIRM!"=="SIM" (
    echo.
    echo Restauracao cancelada.
    echo.
    pause
    exit /b 0
)

REM ==========================================
REM Obter banco e usuario
REM ==========================================

for /f "delims=" %%I in ('docker exec sistema_postgres printenv POSTGRES_USER') do set DB_USER=%%I

for /f "delims=" %%I in ('docker exec sistema_postgres printenv POSTGRES_DB') do set DB_NAME=%%I

if "!DB_USER!"=="" (
    echo.
    echo ERRO: Nao foi possivel obter o usuario do PostgreSQL.
    echo.
    pause
    exit /b 1
)

if "!DB_NAME!"=="" (
    echo.
    echo ERRO: Nao foi possivel obter o nome do banco.
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo Banco de dados : !DB_NAME!
echo Usuario        : !DB_USER!
echo Backup         : !SELECTED_FILE!
echo ==========================================
echo.

REM ==========================================
REM Parar API
REM ==========================================

echo Parando a API...

docker compose stop api

if %errorlevel% neq 0 (
    echo.
    echo AVISO: Nao foi possivel parar a API.
    echo A restauracao sera interrompida por seguranca.
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Limpar banco atual
REM ==========================================

echo.
echo Limpando banco de dados atual...

docker exec sistema_postgres psql -U "!DB_USER!" -d "!DB_NAME!" -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

if %errorlevel% neq 0 (
    echo.
    echo ERRO: Nao foi possivel limpar o banco.
    echo.
    echo Iniciando novamente a API...
    docker compose start api
    pause
    exit /b 1
)

REM ==========================================
REM Restaurar backup
REM ==========================================

echo.
echo Restaurando backup...
echo.

type "!BACKUP_PATH!" | docker exec -i sistema_postgres psql -U "!DB_USER!" -d "!DB_NAME!"

if %errorlevel% neq 0 (
    echo.
    echo ==========================================
    echo ERRO DURANTE A RESTAURACAO
    echo ==========================================
    echo.
    echo O backup pode estar corrompido ou
    echo ser incompatível com o banco atual.
    echo.
    
    echo Iniciando novamente a API...
    docker compose start api

    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Iniciar API
REM ==========================================

echo.
echo Iniciando a API...

docker compose start api

if %errorlevel% neq 0 (
    echo.
    echo AVISO: A API nao foi iniciada automaticamente.
    echo Execute:
    echo docker compose up -d
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Finalizacao
REM ==========================================

echo.
echo ==========================================
echo    RESTAURACAO CONCLUIDA COM SUCESSO
echo ==========================================
echo.
echo Banco restaurado:
echo !DB_NAME!
echo.
echo Backup utilizado:
echo !SELECTED_FILE!
echo.
echo A API foi iniciada novamente.
echo.
echo Sistema:
echo http://localhost
echo.
echo ==========================================
echo.

start http://localhost

pause
endlocal