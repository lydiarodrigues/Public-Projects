#include <iostream>
using namespace std;
#include <string> 

class timeValueNode { // node in a singly-linked list of time/value pairs
    public:
        int value; // numeric value stored at this cell
        int timeFrame; // integer timeframe when 'value' becomes effective
        timeValueNode* _next; // next node in ascending time order
       
        //contructors and destructors
        timeValueNode(int val, int tFrame); // constructor
        ~timeValueNode(); // destructor
        void display(); // display the entire list
        friend ostream& operator<<(ostream& os, const timeValueNode& node); // overload << operator for easy printing
        int size(); // return the number of nodes in the list
        void remove(int tFrame); // remove node with given timeFrame
        int find(int tFrame); // find value at given timeFrame
        void addAtEnd(int val, int tFrame); // add a new node at the end of the list
        // other helper functions as needed
};

timeValueNode::timeValueNode(int val, int tFrame)
{
    value = val;
    timeFrame = tFrame;
    _next = NULL;
}

timeValueNode::~timeValueNode()
{
    delete _next;
}

void timeValueNode::display()
{
    timeValueNode* current = this;
        while (current != NULL && (*current).timeFrame != -1) {
            cout << "(" << (*current).value << ", " << (*current).timeFrame << ") ";
            current = (*current)._next;
        } 
         cout<<endl; 
}

ostream& operator<<(ostream& os, const timeValueNode& node)
{
    const timeValueNode* current = &node;
    while(current != NULL){
        os << "(" << (*current).value << ", " << (*current).timeFrame << ") ";
        current = (*current)._next;
    }
    return os;
}

int timeValueNode::size()
{
    timeValueNode* current = this;
    int count = 0; 
    while(current != NULL && (*current).timeFrame != -1){
        count++; 
        current = (*current)._next;
    }
    return count;
}
void timeValueNode::remove(int tFrame)
{
    timeValueNode* current = this;
    timeValueNode* previous = NULL;
    while (current != NULL && (*current).timeFrame != tFrame) {
        previous = current;
        current = (*current)._next;
    }

    if (current == NULL) {
        return; // nothing in location to remove
    }

    if(previous == NULL){
        
        if((*current)._next != NULL){
            value = (*(*current)._next).value;
            timeFrame = (*(*current)._next).timeFrame;
            timeValueNode* after = (*current)._next;
            _next = (*after)._next;
            (*after)._next = NULL; 
            delete after;
        } else {
            value = 0; 
            timeFrame = -1; 
        }
    }
    else {
        (*previous)._next = (*current)._next;
        (*current)._next = NULL;
        delete current;
    }
}
void timeValueNode::addAtEnd(int val, int tFrame)
{
    timeValueNode* current = this; 
    timeValueNode* prev = NULL;

    if((*current).timeFrame == tFrame){
        (*current).value = val;
        return;
    }
    if((*current).timeFrame > tFrame || (*current).timeFrame == -1){
        timeValueNode* newNode = new timeValueNode(val, tFrame);
        (*newNode)._next = (*current)._next;
        (*current).value = val; 
        (*current).timeFrame = tFrame;
        (*current)._next = (*newNode)._next;
        delete newNode;
        return;
    }
    
    while(current != NULL && (*current).timeFrame < tFrame){
        prev = current;
        current = (*current)._next;
    }
    if(current != NULL && (*current).timeFrame == tFrame){
        (*current).value = val;
        return;
    }
    else{
        timeValueNode* newNode = new timeValueNode(val, tFrame);
        (*prev)._next = newNode;
        (*newNode)._next = current;
    }
}
int timeValueNode :: find(int tFrame){
    timeValueNode* current = this;
    int last = 0; 
    while(current && (*current).timeFrame <= tFrame && (*current).timeFrame !=-1){
        last = (*current).value; 
        current = (*current)._next;
    }
    return last;
}

class sparseRow { // represents a row in a sparse matrix
    public:
        int rowIndex; // index of the row
        int colIndex; // index of the column
        timeValueNode* head; // head of the linked list of time/value pairs
        sparseRow(); // default constructor set rowIndex and colIndex to -1 and head to NULL
        sparseRow (int rIndex, int cIndex); // constructor set rowIndex and colIndex to given values and head to NULL
        ~sparseRow(); // destructor to clean up the linked list
        void display(); // display the row information and its time/value pairs
        // overload << operator for easy printing - print row index, col index, and all time/value pairs in the linked list
        friend ostream& operator<<(ostream& os, const sparseRow& row);
        int size(); // return the number of time/value pairs in the linked list
        // other helper functions as needed
};


sparseRow::sparseRow()
{
    rowIndex = -1;
    colIndex = -1;
    head = NULL;
}

sparseRow::sparseRow(int rIndex, int cIndex)
{
    rowIndex = rIndex;
    colIndex = cIndex;
    head = NULL;
}

sparseRow::~sparseRow()
{
    delete head;
}

void sparseRow::display()
{
    if(head != NULL){
        timeValueNode* current = head;
        if((*head).size() > 0){
            cout << "Row: " << rowIndex << ", Col: " << colIndex << " -> " ;
            (*head).display();
            cout << endl;
         }
    }
}

ostream& operator<<(ostream& os, const sparseRow& row)
{
    if(row.head != NULL && (*(row.head)).timeFrame !=-1 && (*row.head).size() > 0){
        os<< "Row: " << row.rowIndex << ", Col: " << row.colIndex << " -> ";
        (*row.head).display();
    }
    return os; 
}

int sparseRow::size()
{
    if(head != NULL){
        return (*head).size();
    }
    return 0;
}


class TemporalSparseMatrix {
    public:
        // Matrix dimensions
        int rows; // total number of rows in the matrix
        int cols; // total number of columns in the matrix
       
        // Capacity & usage
        int MAX_NONZERO; // maximum number of non-zero entries allowed
        int currNZ; // number of slots currently in use (0..maxNZ)
        
        // Storage for sparse entries
        sparseRow* entries; // array of sparseRow entries
        
        // Optional bookkeeping fields (students decide how to use them)
        int highestTimeSeen; // max timeframe observed so far (can be -1 initially)
        TemporalSparseMatrix(int numRows, int numCols, int maxNZ); // constructor
        ~TemporalSparseMatrix(); //
        
        // insert a value at given row, column, and timeframe if found in the entries array, update the linked list at that entry,
        // if not found, create a new sparseRow entry if there is capacity and insert the time/value pair in the linked list also
        // increment currNZ if a new sparseRow is created
        void insert(int rIndex, int cIndex, int val, int tFrame);
       
       
        // remove the value at given row, column, and timeframe if found in the entries array,
        // if the linked list at that entry becomes empty after removal, remove the sparseRow entry also
        // decrement currNZ if a sparseRow is removed and shift the remaining entries up one to fill the gap
        // if not found, do nothing
        void remove(int rIndex, int cIndex, int tFrame);
       
       
        // find the value at given row, column, and timeframe if found in the entries array,
        // otherwise return 0
        int find(int rIndex, int cIndex, int tFrame); // find the value at given row, column, and timeframe
       
        void display(); // display the entire matrix
       
        int size(); // return the number of non-zero entries in the matrix
       
        friend ostream& operator<<(ostream& os, const TemporalSparseMatrix& matrix); // overload << operator for easy printing
        // other helper functions as needed
};

TemporalSparseMatrix::TemporalSparseMatrix(int numRows, int numCols, int maxNZ)
{
    rows = numRows;
    cols = numCols;
    MAX_NONZERO = maxNZ;
    currNZ = 0;
    entries = new sparseRow[MAX_NONZERO];
    highestTimeSeen = -1;
}
TemporalSparseMatrix::~TemporalSparseMatrix()
{
    delete[] entries;
}
void TemporalSparseMatrix::insert(int rIndex, int cIndex, int val, int tFrame)
{
    if(rIndex < 0 || rIndex >= rows || cIndex < 0 || cIndex >= cols || val == 0 || tFrame < 0){
        return; 
    }

    highestTimeSeen = max(highestTimeSeen, tFrame);
    for(int i = 0; i < currNZ; i++){
        if(entries[i].rowIndex == rIndex && entries[i].colIndex == cIndex){
            if(entries[i].head == NULL){
                entries[i].head = new timeValueNode(val, tFrame);
            }
            else{
                (*entries[i].head).addAtEnd(val, tFrame);
            }
            return;
        }
    }
    if(currNZ < MAX_NONZERO){
        entries[currNZ] = sparseRow(rIndex, cIndex);
        entries[currNZ].head = new timeValueNode(val, tFrame);
        currNZ++;
    }
}
void TemporalSparseMatrix::remove(int rIndex, int cIndex, int tFrame)
{
    for(int i = 0; i < currNZ; i++){
        if(entries[i].rowIndex == rIndex && entries[i].colIndex == cIndex){
            if(entries[i].head != NULL){
                (*entries[i].head).remove(tFrame);
                if((*entries[i].head).size() == 0){
                    delete entries[i].head;
                    entries[i].head = NULL;
                    for(int j = i; j < currNZ - 1; j++){
                        entries[j] = entries[j + 1];
                    }
                    currNZ--;
                    entries[currNZ] = sparseRow();
                }
                return;
            }
            
        }
    }
}
int TemporalSparseMatrix::find(int rIndex, int cIndex, int tFrame)
{
    for(int i = 0; i<currNZ; i++){
        if(entries[i].rowIndex == rIndex && entries[i].colIndex == cIndex && entries[i].head != NULL){
            return (*entries[i].head).find(tFrame);
        }
    }
    return 0;
}
void TemporalSparseMatrix::display()
{
    for(int i = 0; i < currNZ; i++){
        if(entries[i].rowIndex != -1 && entries[i].head != NULL){
            cout << entries[i];
            
        }
    }
}
int TemporalSparseMatrix::size()
{
    return currNZ;
}
ostream& operator<<(ostream& os, const TemporalSparseMatrix& matrix)
{
    for(int i = 0; i<matrix.currNZ; i++){
        if(matrix.entries[i].rowIndex != -1 && matrix.entries[i].head != NULL){
            os << matrix.entries[i];
        }
    }
    return os;
}

int main () {
    int numRows, numCols, maxNZ;
    cin >> numRows >> numCols >> maxNZ;
    TemporalSparseMatrix tsm(numRows, numCols, maxNZ);
    int numCommands; // number of commands to process
    char command; // command type
    cin >> numCommands;
    // Process each command and this is a suggested format, students can modify as needed; not guanrteed to be correct.
    // Your responsibility
    for (int i = 0; i < numCommands; i++){
        cin >> command;
        switch (command) {
        case 'I': { // Insert
            int rIndex, cIndex, val, tFrame;
            cin >> rIndex >> cIndex >> val >> tFrame;
            tsm.insert(rIndex, cIndex, val, tFrame);
            cout << "Inserted value " << val << " at (" << rIndex << ", " << cIndex << ") for timeframe " << tFrame << endl;
            break;
        }
        case 'R': { // Remove
            int rIndex, cIndex, tFrame;
            cin >> rIndex >> cIndex >> tFrame;
            tsm.remove(rIndex, cIndex, tFrame);
            cout << "Removed value at (" << rIndex << ", " << cIndex << ") for timeframe " << tFrame << endl;
            break;
        }
        case 'F': { // Find
            int rIndex, cIndex, tFrame;
            cin >> rIndex >> cIndex >> tFrame;
            int foundValue = tsm.find(rIndex, cIndex, tFrame);
            cout << "Found value at (" << rIndex << ", " << cIndex << ") for timeframe " << tFrame << ": " << foundValue << endl;
            break;
        }
        case 'D': { // Display
            cout << "Temporal Sparse Matrix:" << endl;
            tsm.display();
            break;
        }
        case 'S': { // Size
            int currentSize = tsm.size();
            cout << "Current number of non-zero entries: " << currentSize << endl;
            break;
        }
        default:
            cout << "Unknown command: " << command << endl;
            break;
        }
    }
    // make sure you clean up and free any allocated memory
    return 0;
}


