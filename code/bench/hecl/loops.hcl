set x 0
while {< $x 1000} {
    set x [+ $x 1]
    set y 0
    while {< $y 10000} {
        set y [+ $y 1]
    }
}

puts [+ $x $y]
