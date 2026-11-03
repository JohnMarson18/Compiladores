; Funcao fatorial recursiva com condicional e operadores relacionais
(defun fatorial (n)
  (if (<= n 1)
    1
    (* n (fatorial (- n 1)))))
