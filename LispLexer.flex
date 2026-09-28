// Autores: Paulo Fernando P Junior e Diogo Siqueira

%%

// Configuracoes
%class LispLexer
%public
%unicode
%line
%column
%type Token

// Macros
Digito = [0-9]
Numero = [+-]?{Digito}+(\.{Digito}+)?
Letra = [a-zA-Z]
Operador = [\+\-\*\/\=\<\>\!\?]
Simbolo = ({Letra}|{Operador})({Letra}|{Digito}|{Operador})*
String = \"[^\"]*\"
Comentario = ;.*
Espaco = [ \t\r\n]+

%%

// Regras
"("             { return new Token(TokenType.LPAREN, yytext(), yyline + 1, yycolumn + 1); }
")"             { return new Token(TokenType.RPAREN, yytext(), yyline + 1, yycolumn + 1); }

"T" | "NIL" | "#t" | "#f" { return new Token(TokenType.BOOLEAN, yytext(), yyline + 1, yycolumn + 1); }

{Numero}        { return new Token(TokenType.NUMBER, yytext(), yyline + 1, yycolumn + 1); }
{String}        { return new Token(TokenType.STRING, yytext(), yyline + 1, yycolumn + 1); }
{Simbolo}       { return new Token(TokenType.SYMBOL, yytext(), yyline + 1, yycolumn + 1); }

{Espaco}        { }
{Comentario}    { }

[^]             { return new Token(TokenType.ERROR, yytext(), yyline + 1, yycolumn + 1); }
<<EOF>>         { return new Token(TokenType.EOF, "<EOF>", yyline + 1, yycolumn + 1); }
