@echo off
cd /d "%~dp0"
chcp 65001 > nul
javac -encoding UTF-8 -cp . TokenType.java Token.java LispLexer.java Main.java
if errorlevel 1 exit /b 1
java -cp . Main
del /q *.class 2>nul
if /i not "%~1"=="--nopause" pause
