(define fib (lambda (x)
    (if (< 1 x)
        (+ (fib (- x 1)) (fib (- x 2)))
        1)))
(display (fib 30))
(exit)
