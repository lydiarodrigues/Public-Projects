#include <iostream>
using namespace std;

class Solution{
public:
    int letterToNumber(char c);
    int romanToInt(string str); 
};

int Solution::letterToNumber(char c){
    switch(c){
        case 'I': return 1;
        case 'V': return 5;
        case 'X': return 10;
        case 'L': return 50;
        case 'C': return 100;
        case 'D': return 500;
        case 'M': return 1000;
        default: return 0;
    }
}

int Solution::romanToInt(string str)
{
    int total = 0;
    for(int i = 0; i< str.length()-1; i++){
        if(letterToNumber(str[i]) < letterToNumber(str[i+1])){
            total -= letterToNumber(str[i]);
        }
        else{
            total += letterToNumber(str[i]);
        }
    }
    return total + letterToNumber(str[str.length()-1]);
}

int main() {
    string roman;
    cin >> roman;

    Solution sol;
    cout << "Int Value Of " << roman << " is " << sol.romanToInt(roman) << endl;
    return 0;
}