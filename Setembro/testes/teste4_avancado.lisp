; Teste 4: Casos adicionais da gramatica
; Operadores matematicos e relacionais: <=, *, -
(defun fatorial (n)
  (if (<= n 1)
    1
    (* n (fatorial (- n 1)))))

(print "Testando booleanos:")
(#t #f T NIL)
