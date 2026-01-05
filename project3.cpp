//Lydia Rodrigues Project 3 Nested BST
#include <iostream>
#include <vector>
#include <algorithm> 
#include <set>
#include <functional>
using namespace std;

template <class DT>
class NestedBST
{
public:
    DT value;             // value of the node
    vector<int> keys;     // vector of keys associated with this value (node)
    int dimension;        // dimension of this node
    NestedBST *left;      // pointer to left child
    NestedBST *right;     // pointer to right child
    NestedBST *innerTree; // pointer to nested BST (next dimension)
    NestedBST();                // default constructor
    NestedBST(DT val, int dim); // parameterized constructor
    void insert(int key, const vector<DT> &values); // insert or update a key-value tuple
    void find(const vector<DT> &pattern);           // find keys matching a pattern with wildcards
    void display(int indent = 0);                   // print tree structure for verification
};

template <class DT>
NestedBST<DT>::NestedBST(){
    value = DT();
    dimension = 0;
    left = nullptr;
    right = nullptr;
    innerTree = nullptr;
};

template <class DT>
NestedBST<DT>::NestedBST(DT val, int dim){
    value = val;
    dimension = dim;
    left = nullptr;
    right = nullptr;
    innerTree = nullptr;
}

template <class DT>
void NestedBST<DT>::find(const vector<DT> &pattern){
    const DT WILDCARD = -999999;
    vector<pair<vector<DT>, int>> results;
    set<pair<vector<DT>, int>> seen;
    function<void(NestedBST<DT> *, vector<DT>)> dfs = [&](NestedBST<DT> *node, vector<DT> current){
        if (!node) return;
        if (current.size() <= node->dimension){
            current.push_back(node->value);
        }
        else current[node->dimension] = node->value;
        bool matches = (pattern[node->dimension] == WILDCARD || pattern[node->dimension] == node->value);
        if (matches){
            if (node->dimension == static_cast<int>(pattern.size()) - 1){
                for (int k : node->keys){
                    auto p = make_pair(current, k);
                    if (seen.find(p) == seen.end()){
                        results.push_back(p);
                        seen.insert(p);
                    }
                }
            }
            else if (node->innerTree){
                dfs(node->innerTree, current);
            }
        }
        if (pattern[node->dimension] == WILDCARD){
            dfs(node->left, current);
            dfs(node->right, current);
        }
        else{
            if (pattern[node->dimension] < node->value)
                dfs(node->left, current);
            else if (pattern[node->dimension] > node->value)
                dfs(node->right, current);
        }
    };
    dfs(this, {});
    if (results.empty()){
        cout << "EMPTY" << endl;
        return;
    }
    sort(results.begin(), results.end(), [](auto &a, auto &b){ return a.first < b.first || (a.first == b.first && a.second < b.second); });
    for (auto &p : results){
        cout << "key=" << p.second << " for (";
        for (size_t i = 0; i < p.first.size(); i++)
        {
            cout << p.first[i];
            if (i < p.first.size() - 1)
                cout << ",";
        }
        cout << ")" << endl;
    }
}

template <class DT>
void NestedBST<DT>::insert(int key, const vector<DT> &values){
    if (left == nullptr && right == nullptr && innerTree == nullptr && keys.empty() && value == DT()){
        value = values[dimension];
        if (dimension == static_cast<int>(values.size()) - 1){
            keys.push_back(key);
            cout << "Inserted key=" << key << " for (";
            for (size_t i = 0; i < values.size(); i++){
                cout << values[i];
                if (i < values.size() - 1) cout << ",";
            }
            cout << ")" << endl;
            return;
        }
        innerTree = new NestedBST(values[dimension + 1], dimension + 1);
        innerTree->insert(key, values);
        return;
    }
    if (values[dimension] < value){
        if (!left)
            left = new NestedBST(values[dimension], dimension);
        left->insert(key, values);
    }
    else if (values[dimension] > value){
        if (!right)
            right = new NestedBST(values[dimension], dimension);
        right->insert(key, values);
    }
    else{
        if (dimension == static_cast<int>(values.size()) - 1){
            if (keys.empty()) {
                keys.push_back(key);
                cout << "Inserted key=" << key << " for (";
            }
            else{
                auto it = std::find(keys.begin(), keys.end(), key);
                if (it != keys.end())
                {

                    cout << "Unchanged key=" << key << " for (";
                }
                else
                {

                    keys[0] = key; 
                    cout << "Updated key=" << key << " for (";
                }
            }
            for (size_t i = 0; i < values.size(); i++){
                cout << values[i];
                if (i < values.size() - 1)
                    cout << ",";
            }
            cout << ")" << endl;
            return;
        }

        if (!innerTree) innerTree = new NestedBST(values[dimension + 1], dimension + 1);

        innerTree->insert(key, values);
    }
}

template <class DT>
void NestedBST<DT>::display(int indent){
    if (!this) return;
    cout << std::string(indent, ' ');
    cout << "[dim " << dimension << "] value=" << value;
    if (!keys.empty()) cout << "  key=";
    if (!keys.empty()){
        for (size_t i = 0; i < keys.size(); i++){
            cout << keys[i];
            if (i < keys.size() - 1)
                cout << ",";
        }
    }
    else if (innerTree){
        int candidates = 0;
        function<void(NestedBST<DT> *)> countKeys = [&](NestedBST<DT> *node){
            if (!node) return;
            candidates += node->keys.size();
            countKeys(node->left);
            countKeys(node->right);
            if (node->innerTree) countKeys(node->innerTree);
        };
        countKeys(innerTree);
        cout << "  (candidates=" << candidates << ")";
    }
    cout << endl;

    if (innerTree){
        cout << std::string(indent + 2, ' ') << "-> dim " << innerTree->dimension << endl;
        innerTree->display(indent + 4);
    }
    if (left) left->display(indent);
    if (right) right->display(indent);
}

int main()
{

    int numDimensions; // number of value dimensions
    cin >> numDimensions;

    // Create the root tree (dimension 0)
    NestedBST<int> *root = new NestedBST<int>();
    root->dimension = 0;

    int numCommands;
    cin >> numCommands;

    char command;
    for (int i = 0; i < numCommands; i++)
    {
        cin >> command;
        switch (command)
        {
        case 'I':
        { // Insert
            int key;
            cin >> key;
            vector<int> values(numDimensions);
            for (int d = 0; d < numDimensions; d++)
            {
                cin >> values[d];
            }
            root->insert(key, values);
            break;
        }

        case 'F':
        {
            vector<int> pattern(numDimensions);
            string token;
            for (int d = 0; d < numDimensions; d++)
            {
                cin >> token;
                if (token == "*")
                    pattern[d] = -999999;
                else
                    pattern[d] = stoi(token);
            }
            root->find(pattern);
            break;
        }

        case 'D':
        { // Display
            cout << "NestedBST Structure:" << endl;
            root->display();
            cout << endl;
            break;
        }

        default:
            cout << "Unknown command: " << command << endl;
            break;
        }
    }
    // Clean up
    delete root;
    return 0;
}