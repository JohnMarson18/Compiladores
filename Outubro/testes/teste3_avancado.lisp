; Funcao recursiva fatorial e todos os booleanos da gramatica
(defun fatorial (n)
  (if (<= n 1)
    1
    (* n (fatorial (- n 1)))))

(print "Testando booleanos:")
(#t #f T NIL)
