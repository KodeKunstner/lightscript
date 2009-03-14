#c++ words.cc && cat `locate --regex "e.txt$"` | ./a.out > /tmp/words.lst
c++ words.cc && cat `find ~/text | grep "\(tex$\|txt$\)"` | ./a.out > /tmp/words.lst
