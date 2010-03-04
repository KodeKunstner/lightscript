proc fib { x } {
    if { < 1 $x } {
        return [ + [fib [- $x 1]] [fib [- $x 2]]]
    } else {
        return 1
    }
}

puts [fib 30]
