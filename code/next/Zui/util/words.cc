#include <stdio.h>
#include <string>
#include <iostream>
#include <map>

using namespace std;
int lowerAlpha(int c) {
    if('A' <= c && c <= 'Z') {
        c += 'a' - 'A';
    }
    if(c < 'a' || 'z' < c) {
        c = 0;
    }
    return c;
}

int main() {
    char str[100];
    int pos = -1;
    int c;
    map<string, int> strcount;
    int count;
    while((c = getchar()) >= 0) {
        c = lowerAlpha(c);
        if(pos > 98) {
            pos--;
        }
        str[++pos] = c;
        if(c == 0 && pos > 0) {
            string s(str);
            count = 0;
            try {
                count = strcount[s];
            } catch(...) {
            }
            strcount[s] = count + 1;
        }
        if(c == 0) {
            pos = -1;
        }
    }
    count = 0;
    map<string,int>::iterator iter;
    for(iter = strcount.begin(); iter != strcount.end(); iter++) {
        cout << iter->second<< " " << iter->first<< endl;
        count += iter->second;
    }
    cerr << count << endl;
    return 0;
}
