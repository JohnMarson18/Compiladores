@echo off
chcp 65001 > nul
cls

echo Compilando LispLexer.java...
javac -encoding UTF-8 LispLexer.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha na compilacao do LispLexer.java!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilacao concluida com sucesso.
echo.
java -cp . LispLexer
echo.
pause
