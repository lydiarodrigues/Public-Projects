#include <iostream>
#include <string>
using namespace std;

/* ============================================================
   Helper functions for row operations
   ============================================================ */

// Return a pointer to row r (mutable)
inline int* rowPtr(int* data, int r, int C) {
    return data + r * C;
}

// Return a pointer to row r (const)
inline const int* rowPtrConst(const int* data, int r, int C) {
    return data + r * C;
}

/* ----------- REQUIRED: Compare two rows A and B lexicographically ------
   Must:
     - Compare column 0, then column 1, … until difference found
     - Return -1, 0, +1 normally
     - Increment cmpCount ONCE per scalar comparison A[col] ? B[col]
   ---------------------------------------------------------------------- */
int compareRows(const int* A, const int* B, int C, long long &cmpCount) {
    for(int i = 0; i < C; ++i) {
        ++cmpCount;
        if(A[i] < B[i]){
            return -1;
        }
        else if(A[i] > B[i]){
            return 1;
        }
    }
    return 0;
}

/* ----------- REQUIRED: Swap rows i and j (physically copy C ints) ------
   Must:
     - Swap row i and row j element-by-element
     - Count ONE exchange per row-level swap
   ---------------------------------------------------------------------- */
void rowSwap(int* data, int i, int j, int C, long long &exchanges) {
    if(i == j){
        return; 
    }
    int* rowI = rowPtr(data, i, C);
    int* rowJ = rowPtr(data, j, C);
    for(int col = 0; col < C; ++col){
        swap(rowI[col], rowJ[col]);   
    }
    exchanges++;
}

/* Copy row src → dst (C ints). No counters incremented. */
void copyRowInto(int* dst, const int* src, int C) {
    for (int c = 0; c < C; ++c) dst[c] = src[c];
}

/* ============================================================
   Quick Sort (Lomuto partition)
   ============================================================ */

// TODO: Students implement partition_lomuto()
// TODO: Students implement quicksort_rows_rec()
// TODO: Students implement quicksort_rows()

int partition_lomuto(int* data, int lo, int hi, int C,
                     long long &cmpCount, long long &exchanges) {
    int* a = rowPtr(data, hi, C);
    int i = lo - 1; 
    for(int j = lo; j < hi; ++j){
        const int* b = rowPtrConst(data, j, C);
        if(compareRows(b, a, C, cmpCount) <= 0){
            i++;
            rowSwap(data, i, j, C, exchanges);
        }
    }
    rowSwap(data, i+1, hi, C, exchanges);
    return i+1;
}

void quicksort_rows_rec(int* data, int lo, int hi, int C,
                        long long &cmpCount, long long &exchanges) {
    if(lo < hi){
        int p = partition_lomuto(data, lo, hi, C, cmpCount, exchanges);
        quicksort_rows_rec(data, lo, p - 1, C, cmpCount, exchanges);
        quicksort_rows_rec(data, p + 1, hi, C, cmpCount, exchanges);
    }

}

void quicksort_rows(int* data, int R, int C,
                    long long &cmpCount, long long &exchanges) {
    if(R>1){                    
        quicksort_rows_rec(data, 0, R - 1, C, cmpCount, exchanges);
    }
}
    


/* ============================================================
   Heap Sort
   ============================================================ */

// TODO: Students implement heapify()
// TODO: Students implement heapsort_rows()

void heapify(int* data, int n, int i, int C,
             long long &cmpCount, long long &exchanges) {
    while(true){
        int largest = i;
        int left  = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n) {
            const int* rowL = rowPtrConst(data, left, C);
            const int* rowLargest = rowPtrConst(data, largest, C);
            int cmp = compareRows(rowL, rowLargest, C, cmpCount);
            if (cmp > 0) largest = left;
        }
        if (right < n) {
            const int* rowR = rowPtrConst(data, right, C);
            const int* rowLargest = rowPtrConst(data, largest, C);
            int cmp = compareRows(rowR, rowLargest, C, cmpCount);
            if (cmp > 0) largest = right;
        }

        if (largest == i) break;
        rowSwap(data, i, largest, C, exchanges);
        i = largest;
    }
}

void heapsort_rows(int* data, int R, int C,
                   long long &cmpCount, long long &exchanges) {
    if(R <= 1){
        return;
    }
    for(int i = R / 2 - 1; i >= 0; --i){
        heapify(data, R, i, C, cmpCount, exchanges);
    }
    for(int j = R - 1; j > 0; --j){
        rowSwap(data, 0, j, C, exchanges);
        heapify(data, j, 0, C, cmpCount, exchanges);
    }
}


/* ============================================================
   LexSort (Stable insertion passes from rightmost → leftmost column)
   ============================================================ */

// TODO: Students implement lexsort_lexpass()

void lexsort_lexpass(int* data, int R, int C,
                     long long &cmpCount, long long &exchanges) {
    if (R <= 1 || C == 0){
        return;
    }
    int* temp = new int[C];
    for (int col = C - 1; col >= 0; --col) {
        for (int i = 1; i < R; ++i) {
            int* rowI = rowPtr(data, i, C);
            copyRowInto(temp, rowI, C);
            int j = i - 1;
            while (j >= 0) {
                int* rowJ = rowPtr(data, j, C);
                ++cmpCount; 
                if (rowJ[col] <= temp[col]) {
                    break;    
                }
                int* point = rowPtr(data, j + 1, C);
                copyRowInto(point, rowJ, C);
                ++exchanges;
                --j;
            } 
            int* point = rowPtr(data, j + 1, C);
            copyRowInto(point, temp, C);
            ++exchanges;
        }
    }
    delete[] temp;
}


/* ============================================================
   Generic dispatcher (DO NOT MODIFY)
   ============================================================ */

enum Alg { QUICK, HEAP, LEX };

void generic_table_sort(int* data, int R, int C, Alg alg,
                        long long &comparisons, long long &exchanges) {
    comparisons = 0;
    exchanges = 0;
    if (R <= 1) return;

    if (alg == QUICK) {
        quicksort_rows(data, R, C, comparisons, exchanges);
    } else if (alg == HEAP) {
        heapsort_rows(data, R, C, comparisons, exchanges);
    } else if (alg == LEX) {
        lexsort_lexpass(data, R, C, comparisons, exchanges);
    }
}


/* ============================================================
   Output block printing (DO NOT MODIFY)
   ============================================================ */

void print_table_block(int t, int R, int C, const string &algName,
                       int* data, long long comps, long long exch) {
    cout << "Table " << t << " (R=" << R << ", C=" << C << ") -- "
         << algName << "\n";
    cout << "Comparisons=" << comps << " Exchanges=" << exch << "\n";

    for (int r = 0; r < R; ++r) {
        const int* row = rowPtrConst(data, r, C);
        for (int c = 0; c < C; ++c) {
            if (c) cout << ' ';
            cout << row[c];
        }
        cout << "\n";
    }
}


/* ============================================================
   Main driver (DO NOT MODIFY)
   Reads T tables, prints Quick / Heap / Lex in correct order
   ============================================================ */

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int T;
    if (!(cin >> T)) return 0;

    for (int t = 1; t <= T; ++t) {
        int R, C;
        cin >> R >> C;

        int* A = (R > 0 && C > 0) ? new int[R*C] : nullptr;
        for (int i = 0; i < R*C; ++i) cin >> A[i];

        int* W = (R > 0 && C > 0) ? new int[R*C] : nullptr;

        // ---------------- QUICK ----------------
        for (int i = 0; i < R*C; ++i) W[i] = A[i];
        long long compQ = 0, exchQ = 0;
        generic_table_sort(W, R, C, QUICK, compQ, exchQ);
        print_table_block(t, R, C, "Quick", W, compQ, exchQ);
        cout << "\n";

        // ---------------- HEAP -----------------
        for (int i = 0; i < R*C; ++i) W[i] = A[i];
        long long compH = 0, exchH = 0;
        generic_table_sort(W, R, C, HEAP, compH, exchH);
        print_table_block(t, R, C, "Heap", W, compH, exchH);
        cout << "\n";

        // ---------------- LEX ------------------
        for (int i = 0; i < R*C; ++i) W[i] = A[i];
        long long compL = 0, exchL = 0;
        generic_table_sort(W, R, C, LEX, compL, exchL);
        print_table_block(t, R, C, "Lex", W, compL, exchL);

        delete[] A;
        delete[] W;

        if (t != T) cout << "\n";
    }

    return 0;
}
