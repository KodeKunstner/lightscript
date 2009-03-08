(define (ack m n)
    (if (= m 0)
        (+ n 1)
        (if (= n 0)
            (ack (- m 1) 1)
            (ack (- m 1) (ack m (- n 1))))))

(define (fib n)
    (if (< n 2)
        1
        (+ (fib (- n 2)) (fib (- n 1)))))

(define (tak x y z) 
    (if (<= x y)
        z
        (tak
            (tak (- x 1) y z)
            (tak (- y 1) z x)
            (tak (- z 1) x y))))

(define i 3)
(let loop ()
    (display (ack 3 i))
    (display " ")
    (display (fib (+ 17 i)))
    (display " ")
    (display (tak (+ (* 3 i) 3) (+ (* 2 i) 2) (+ i 1)))
    (display " ")
    (set! i (+ i 1))
    (if (<= i 7)
        (loop)
        ()))

(exit)
