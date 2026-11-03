@echo off
cd /d "%~dp0"
chcp 65001 > nul
title Compilador Lisp - Mes 2 (Outubro) - Versao Completa (Modular)

echo [1/2] Compilando Analisador Sintatico Recursivo Descendente (Versao Completa)...
javac -cp . -encoding UTF-8 TokenType.java Token.java ScannerSimples.java NoSintatico.java ParserRecursivo.java Main.java
if errorlevel 1 (
    echo [ERRO] Falha na compilacao do codigo Java!
    if /I not "%~1"=="--nopause" if /I not "%~1"=="--no-pause" pause
    exit /b 1
)

echo [2/2] Executando bateria de testes em testes/*.lisp (Versao Completa)...
echo.
java -cp . Main %*
del /q *.class >nul 2>&1

if /I not "%~1"=="--nopause" if /I not "%~1"=="--no-pause" (
    echo.
    pause
)
