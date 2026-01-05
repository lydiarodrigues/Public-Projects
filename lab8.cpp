#include <iostream>
#include <vector>
#include <cmath>
using namespace std;

class MinMaxHeap {
private:
    vector<int> heap;

    int parent(int i) const {
        return (i - 1) / 2;
    }

    int left(int i) const {
        return 2 * i + 1;
    }

    int right(int i) const {
        return 2 * i + 2;
    }

    void swapNodes(int a, int b) {
        int temp = heap[a];
        heap[a] = heap[b];
        heap[b] = temp;
    }

    bool isMinLevel(int i) const {
        int level = 0;
        while (i > 0) {
            i = parent(i);
            level++;
        }
        return (level % 2 == 0);
    }


    void bubbleUpMin(int index) {
        while (index > 2) {
            int gp = parent(parent(index));
            if (heap[index] < heap[gp]) {
                swapNodes(index, gp);
                index = gp;
            } else break;
        }
    }

    void bubbleUpMax(int index) {
        while (index > 2) {
            int gp = parent(parent(index));
            if (heap[index] > heap[gp]) {
                swapNodes(index, gp);
                index = gp;
            } else break;
        }
    }

    void bubbleUp(int index) {
        if(index == 0) return;
        int p = parent(index);
        if (isMinLevel(index)) {
            if (heap[index] > heap[p]) {
                swapNodes(index, p);
                bubbleUpMax(p);
            } else {
                bubbleUpMin(index);
            }
        }
        else{
            if (heap[index] < heap[p]) {
                swapNodes(index, p);
                bubbleUpMin(p);
            } else {
                bubbleUpMax(index);
            }
        }
    }

public:
    MinMaxHeap() {}
    ~MinMaxHeap() {}

    void insert(int value) {
        heap.push_back(value);
        bubbleUp(heap.size() - 1);
    }

    void display() const {
        if (heap.empty()) {
            cout << "Heap is empty.\n";
            return;
        }

        cout << "\nMin-Max Heap (level by level):\n";

        int count = 0;
        int itemsOnLevel = 1;

        for (int i = 0; i < (int)heap.size(); i++) {
            cout << heap[i] << " ";
            count++;

            if (count == itemsOnLevel) {
                cout << "\n";
                count = 0;
                itemsOnLevel *= 2;
            }
        }
        cout << "\n";
    }
};

int main() {
    MinMaxHeap h;

    int x;
    while (cin >> x) {
        h.insert(x);
    }

    h.display();

    return 0;
}
