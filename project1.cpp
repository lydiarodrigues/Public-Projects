
#include <iostream>
#include <string>

using namespace std; 


class Text2Compress {
protected:
    int _seq[200000]; // stores the current sequence
    int _length; // length of sequence
    int _freq[640][640]; // static matrix 128+k by 128+k
    int _rules[512][3]; // merge rules: a b -> z
    int _ruleCount; // number of rules learned
    int _maxSymbol; // highest assigned symbol ID
    int _numNewRules; // number of new rules to be used in decode
    int _newRules[512][3]; //storing the new rules for the decode
    int _secondSeq[200000]; //storing the sequence to be decoded
    int _secondLength; //length of sequence to get decoded
    
public:
    Text2Compress();
    void initialize(int k, int lines); // read input text
    void train(int k); // perform k merges
    void encode(); // apply learned merges
    void decode(); // optional: expand compressed form
    void displaySequence(); // print current sequence
    void displayRules(); // print learned rules
};


Text2Compress:: Text2Compress(){
    _length = 0; 
    _ruleCount = 0; 
    _maxSymbol = 127; 
    _secondLength = 0;
    _numNewRules = 0;

    //intialize _freq to 0 matrix
    for(int i = 0; i<640; i++){
        for(int j = 0; j<640; j++){
            _freq[i][j] = 0; 
        }
    }
    
}
void Text2Compress:: initialize(int k, int lines){
    
    _length = 0; 
    for(int i = 0; i<lines;i++){
        char c; 
        string line; 
        getline(cin,line);
        for(int index = 0; index<line.length(); index++){
            c =  line[index]; 
            _seq[_length] = (unsigned char) c; // use (unsigned char) if comes out negative
            _length++; 
        }
        _seq[_length] = 10; //ASCII for newline
        _length++;
    } 

    //store 2nd half of the input to be used for the decoding 
    //storing rules
    cin >> _numNewRules;
    for(int i = 0; i<_numNewRules; i++){
        cin >> _newRules[i][0] >> _newRules[i][1] >> _newRules[i][2];
    }

    //storing the already encoded message
    int encodedMessage; 
    while(cin >> encodedMessage){
        _secondSeq[_secondLength] = encodedMessage; 
        _secondLength++; 
    }

     
}

void Text2Compress::train(int k)
{
     for(int i = 0; i<k; i++){
        
        //clear out the freq matrix each time itterating through i for loop
        for(int x = 0; x<640; x++){
            for(int y = 0; y<640; y++){
                _freq[x][y] = 0; 
            }
        }
         

        //go through and find how many times each pair appears and put that number in _freq
         for(int j = 0; j< _length -1; j++){
            int first = _seq[j];
            int  second= _seq[j+1];
            _freq[first][second]++; 
         }

         int mostOften = 0; 
         //index1 and index2 hold the two indeces of _freq where mostOften is (-1 so that it isnt a spot already in _freq)
         int index1 = -1; 
         int index2 = -1; 
         for(int m = 0; m <=_maxSymbol; m++){
            for(int n = 0; n <= _maxSymbol; n++){
              //if the spot mn  has more than the other max, current spot becomes max
                if(_freq[m][n] > mostOften){ 
                    mostOften = _freq[m][n];
                    index1 = m;
                    index2 = n;
                }
            }
         }

         if(mostOften == 0){
            break; 
         }
        
        //storing new rule into _rules
        _rules[_ruleCount][0] = index1; 
        _rules[_ruleCount][1] = index2;
        _rules[_ruleCount][2] = _maxSymbol + 1; //the new symbol is 1 more than the old _maxSymbol
        _ruleCount++;
        _maxSymbol++;

        //update _seq to include the rules
        int newLength = 0; 
        int m = 0;
        while(m < _length){
            if(m < _length -1 && _seq[m] == index1 && _seq[m+1] == index2){
                _seq[newLength] = _maxSymbol; 
                newLength++;
                m+=2; 
            }
            else{
                _seq[newLength] = _seq[m]; 
                newLength++; 
                m++; 
            }
        }
        _length = newLength;

    }
}

void Text2Compress::encode()
{
    //undo the rules
    for(int i = _ruleCount-1; i>=0; i--){
        int a = _rules[i][0]; 
        int b = _rules[i][1]; 
        int z = _rules[i][2]; 

        int copySeq[200000];
        int copyLength = 0;
        //find the spot where the rule (z) occurred and put back a and b
        for(int m = 0; m< _length; m++){
            if(_seq[m] == z){
                copySeq[copyLength] = a; 
                copyLength++;
                copySeq[copyLength] = b; 
                copyLength++;
            }
            else{
                copySeq[copyLength] = _seq[m];
                copyLength++;
            }
        }
        //update _seq
        _length = copyLength;
        for(int i = 0; i< copyLength; i++){
            _seq[i] = copySeq[i];
        }
    }


  
}

void Text2Compress::decode()
{
    //undo the rules
    for (int i = _numNewRules - 1; i >= 0; i--) {
        int a = _newRules[i][0]; 
        int b = _newRules[i][1]; 
        int z = _newRules[i][2]; 

        int copySeq[200000];
        int copyLength = 0;

        //find the spot where the rule (z) occurred and put back a and b
        for (int m = 0; m < _secondLength; m++) {
            if (_secondSeq[m] == z) {
                copySeq[copyLength] = a;
                copyLength++;
                copySeq[copyLength] = b;
                copyLength++;
            } else {
                copySeq[copyLength] = _secondSeq[m];
                copyLength++;
            }
        }

        //update _secondSeq
        for (int m = 0; m < copyLength; m++) {
            _secondSeq[m] = copySeq[m];
        }
        _secondLength = copyLength;
    }

    //print as chars
    for (int i = 0; i < _secondLength; i++) {
        cout << (char)_secondSeq[i];
    }
    cout << endl;
    
}

//print out the encoded _seq
void Text2Compress::displaySequence()
{
    for(int i = 0; i< _length; i++){
        cout << _seq[i]<< " ";
    }
    cout << endl;
}

//print out the rules
void Text2Compress::displayRules()
{
    for(int i = 0; i < _ruleCount; i++){
        cout << _rules[i][0] << " " << _rules[i][1] << " " << _rules[i][2] << endl; 
    }
}

int main() {
    int k, numLines;
    // First row: k and number of lines of input
    cin >> k >> numLines;
    while (cin.peek() == '\r' || cin.peek() == '\n')
    {
        cin.get(); // eat carriage returns and newlines
    }
    // Step 2: Create a Text2Compress object
    Text2Compress compressor;
    // Step 1: Read lines of input text
    // Store each character127) into the sequence array
    compressor.initialize(k, numLines);
    // Step 3: Train with k merges
    compressor.train(k);
    // Step 4: Display the learned rules
    cout << "Rules learned from Compression:" << endl;
    compressor.displayRules();
    // Step 5: Display the compressed sequence
    
    cout << "Compressed sequence:" << endl;
    compressor.displaySequence();
    compressor.encode();
    // Step 6: Process decompression lines (triplets + sequence)
    // Step7: print the compressed text
    cout << "Decompressed Text:" << endl; 
    compressor.decode(); 
    // You will write code to handle that part
    return 0;
}
