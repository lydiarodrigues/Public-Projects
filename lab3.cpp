#include <iostream>
using namespace std; 

int alienBunnyBoom(int n){
    if(n <= 2){
        return 1; 
    }
    else{
        return alienBunnyBoom(n-1) + alienBunnyBoom(n-2); 
    }
}

int superPowerChain(int n){
    if(n == 1){
        return 1; 
    }
    else{
        return n * superPowerChain(n-1); 
    }
}

int main() {
    int numHeroes, numMonths;
    cin >> numHeroes >> numMonths;

    cout << "Total Power Of Heroes: " << superPowerChain(numHeroes) << endl;
    cout << "Total Number Of Bunny Pairs: " << alienBunnyBoom(numMonths) << endl;
    return 0;
}