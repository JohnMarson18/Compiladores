; Teste 4: Casos adicionais da gramatica
; Operadores matematicos e relacionais: <=, *, -
(defun fatorial (n)
  (if (<= n 1)
    1
    (* n (fatorial (- n 1)))))

; Todos os booleanos da gramatica: T, NIL, #t, #f
(print "Testando booleanos:")
(#t #f T NIL)
