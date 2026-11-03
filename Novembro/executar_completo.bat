@echo off
cd /d "%~dp0"
chcp 65001 > nul

javac -encoding UTF-8 -cp . TokenType.java Token.java LispLexer.java TabelaLL1.java ParserLL1.java Main.java
if errorlevel 1 (
    echo Erro na compilacao.
    if not "%1"=="--no-pause" pause
    exit /b 1
)

java -Dfile.encoding=UTF-8 -cp . Main
del /q *.class 2>nul

if not "%1"=="--no-pause" (
    echo.
    pause
)
