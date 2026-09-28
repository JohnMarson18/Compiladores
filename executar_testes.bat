@echo off
chcp 65001 > nul

echo Gerando analisador com JFlex...
java -jar jflex.jar LispLexer.flex
if exist LispLexer.java~ del LispLexer.java~

echo Compilando codigo Java...
javac *.java

echo Executando testes...
java Main

pause
