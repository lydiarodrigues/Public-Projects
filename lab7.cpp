#include <iostream>
#include <string>
using namespace std;


class Node{
    private:
        char info;
        Node* left;
        Node* right;
    public:
        Node (char val, Node* l = NULL, Node* r = NULL){
            info = val;
            left = l;
            right = r;
        }
        Node* getLeft() const { return left; }
        Node* getRight() const { return right; }
        char   getInfo() const { return info; }

        void setRight(Node* r) {
            this->right = r;
        }

        void setLeft(Node* l) {
            this->left = l;
        }
};

class BinaryTree{
    private:
        Node* root;
    public:
        BinaryTree(){ 
            string input;
            cin >> input;
            int node_count = 0; //track the number of nodes created
            // start with the root
            root = new Node(input[0], new Node(input[2*0+1]), new Node(input[2*0+2])); // left -> 2i+1, right -> 2i+2
            node_count += 3; // three nodes created root, left and right.

            for (int i =1; node_count < input.length(); ++i){
                Node* temp = search(input[i], root);
                temp->setLeft(new Node(input[2*i+1]));
                temp->setRight(new Node(input[2*i+2]));
                node_count += 2; // two nodes created left and right.
            }
        }

        ~BinaryTree(){
            root->setLeft(NULL);
            root->setRight(NULL);
            root = NULL;
            delete root; 
        }

        Node* search (char a, Node* current){ 
            if(current == NULL || current->getInfo() == 'N'){
                return NULL;
            }
            else if((*current).getInfo() == a){
                return current;
            }
            else{
                Node* temp;
                if(current->getLeft() != NULL){
                    temp = search(a, (*current).getLeft());
                    if(temp != NULL){
                        return temp;
                    }
                }
                if(current->getRight() != NULL){
                    temp = search(a, (*current).getRight());
                    if(temp != NULL){
                        return temp;
                    }
                }
                return NULL;
            }
        }

        int height(){
            int sum = 1; 
            Node* current = root;
            while((*current).getLeft() != NULL){
                sum++;
                current = current->getLeft();
            }
            return sum; 
        }

        void postfixExpression(Node* node){
            if(node == NULL){
                return;
            }
            postfixExpression((*node).getLeft());
            postfixExpression((*node).getRight());
            if((*node).getInfo() != 'N'){
                cout << (*node).getInfo() << " ";
            }
        }

        Node* getRoot(){
            return root;
        }

};

int main(){
    BinaryTree tree;
    int height = tree.height();
    cout << "Height of tree: " << tree.height() << endl;
    tree.postfixExpression(tree.getRoot());
    return 0;
}