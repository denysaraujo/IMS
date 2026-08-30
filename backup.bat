@echo off
setlocal EnableExtensions

title Backup do Banco de Dados

echo.
echo ==========================================
echo        BACKUP DO BANCO DE DADOS
echo ==========================================
echo.

REM ==========================================
REM Verificar se o Docker esta executando
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
REM Verificar se o container PostgreSQL existe
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
REM Verificar se o PostgreSQL esta executando
REM ==========================================

docker inspect -f "{{.State.Running}}" sistema_postgres >nul 2>&1

if %errorlevel% neq 0 (
    echo ERRO: Nao foi possivel verificar o PostgreSQL.
    echo.
    pause
    exit /b 1
)

for /f "delims=" %%I in ('docker inspect -f "{{.State.Running}}" sistema_postgres') do set POSTGRES_RUNNING=%%I

if /I not "%POSTGRES_RUNNING%"=="true" (
    echo ERRO: O PostgreSQL nao esta em execucao.
    echo.
    echo Inicie o sistema com:
    echo docker compose up -d
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Criar pasta de backup
REM ==========================================

if not exist "backup" (
    mkdir "backup"
)

REM ==========================================
REM Obter usuario e banco do PostgreSQL
REM ==========================================

for /f "delims=" %%I in ('docker exec sistema_postgres printenv POSTGRES_USER') do set DB_USER=%%I

for /f "delims=" %%I in ('docker exec sistema_postgres printenv POSTGRES_DB') do set DB_NAME=%%I

if "%DB_USER%"=="" (
    echo ERRO: Nao foi possivel obter o usuario do PostgreSQL.
    echo.
    pause
    exit /b 1
)

if "%DB_NAME%"=="" (
    echo ERRO: Nao foi possivel obter o nome do banco de dados.
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Criar nome do arquivo
REM ==========================================

for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HH-mm-ss"') do set TIMESTAMP=%%I

set BACKUP_FILE=backup\%DB_NAME%_%TIMESTAMP%.sql

echo Banco de dados : %DB_NAME%
echo Usuario        : %DB_USER%
echo Arquivo        : %BACKUP_FILE%
echo.
echo Realizando backup...
echo.

REM ==========================================
REM Executar pg_dump
REM ==========================================

docker exec sistema_postgres pg_dump -U "%DB_USER%" -d "%DB_NAME%" > "%BACKUP_FILE%"

if %errorlevel% neq 0 (
    echo.
    echo ==========================================
    echo ERRO AO REALIZAR O BACKUP
    echo ==========================================
    echo.
    
    if exist "%BACKUP_FILE%" del "%BACKUP_FILE%"
    
    pause
    exit /b 1
)

REM ==========================================
REM Verificar arquivo
REM ==========================================

if not exist "%BACKUP_FILE%" (
    echo.
    echo ERRO: O arquivo de backup nao foi criado.
    echo.
    pause
    exit /b 1
)

for %%A in ("%BACKUP_FILE%") do set BACKUP_SIZE=%%~zA

if "%BACKUP_SIZE%"=="0" (
    echo.
    echo ERRO: O arquivo de backup esta vazio.
    echo.
    del "%BACKUP_FILE%"
    pause
    exit /b 1
)

REM ==========================================
REM Sucesso
REM ==========================================

echo.
echo ==========================================
echo       BACKUP REALIZADO COM SUCESSO
echo ==========================================
echo.
echo Banco:
echo %DB_NAME%
echo.
echo Arquivo:
echo %BACKUP_FILE%
echo.
echo Tamanho:
echo %BACKUP_SIZE% bytes
echo.
echo ==========================================
echo.

pause
endlocal