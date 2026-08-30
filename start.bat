@echo off

title Sistema

echo.
echo ==========================================
echo       INICIANDO SISTEMA
echo ==========================================
echo.

docker compose up -d --build

if %errorlevel% neq 0 (
    echo.
    echo ==========================================
    echo ERRO AO INICIAR O SISTEMA
    echo ==========================================
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo SISTEMA INICIADO COM SUCESSO
echo ==========================================
echo.
echo Acesse:
echo http://localhost
echo.

start http://localhost

pause