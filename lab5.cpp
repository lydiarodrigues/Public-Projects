#include <iostream>
#include <string>
using namespace std;


struct Station {
    string name;
    Station* prev;
    Station* next;
    Station(const string& n) : name(n), prev(NULL), next(NULL) {}
};

class TrainRoute {
public:
    TrainRoute(int n);
    void print() const; // provided for convenience
    int distance(const string& from, const string& to) const;
    void add(const string& name, const string& prevName, const string& nextName);
    void remove(const string& name);

private:
    Station* head;
    Station* find(const string& name) const; // provided for convienience
};


TrainRoute::TrainRoute(int n) {
    head = NULL;

    string name[100], prevName[100], nextName[100];
    Station* nodes[100];

    for (int i = 0; i < n; i++) {
        cin >> name[i] >> prevName[i] >> nextName[i];
        nodes[i] = new Station(name[i]);
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (prevName[i] == name[j]) nodes[i]->prev = nodes[j];
            if (nextName[i] == name[j]) nodes[i]->next = nodes[j];
        }
    }

    for (int i = 0; i < n; i++) {
        if (prevName[i] == "NULL") {
            head = nodes[i];
            break;
        }
    }
}

Station* TrainRoute::find(const string& name) const {
    Station* curr = head;
    while (curr != NULL) {
        if (curr->name == name) return curr;
        curr = curr->next;
    }
    return NULL;
}

void TrainRoute::print() const { // do not modify
    Station* curr = head;
    while (curr != NULL) {
        cout << curr->name;
        if (curr->next != NULL) cout << " <-> ";
        curr = curr->next;
    }
    cout << endl;
}

int TrainRoute::distance(const string& from, const string& to) const {
    Station* start = find(from);
    if (!start) return -1; 

    int dist = 0;
    Station* curr = start;
    while (curr != nullptr) {
        if (curr->name == to) return dist;
        curr = curr->next;
        dist++;
    }
    return -1;

}

void TrainRoute::add(const string& name, const string& prevName, const string& nextName) {
    Station* newStation = new Station(name);
    Station* prevStation = (prevName == "NULL") ? nullptr : find(prevName);
    Station* nextStation = (nextName == "NULL") ? nullptr : find(nextName);

    newStation->prev = prevStation;
    newStation->next = nextStation;

    if (prevStation) prevStation->next = newStation;
    if (nextStation) nextStation->prev = newStation;

    if (!prevStation) head = newStation;
}

void TrainRoute::remove(const string& name) {
    Station* target = find(name);
    if (!target) return; 
    if (target->prev) target->prev->next = target->next;
    if (target->next) target->next->prev = target->prev;

    if( target == head) head = target ->next; 

    delete target;
}


int main() { // do not modify
    int n;
    cin >> n;
    TrainRoute route(n);

    int q;
    cin >> q;
    for (int i = 0; i < q; i++) {
        char cmd;
        cin >> cmd;
        if (cmd == 'P') {
            route.print();
        } else if (cmd == 'D') {
            string a, b;
            cin >> a >> b;
            cout << route.distance(a, b) << endl;
        } else if (cmd == 'A') {
            string name, prev, next;
            cin >> name >> prev >> next;
            route.add(name, prev, next);
        } else if (cmd == 'R') {
            string name;
            cin >> name;
            route.remove(name);
        }
    }
}