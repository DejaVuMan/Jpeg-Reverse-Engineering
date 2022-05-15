package JpegHelpers;

import java.util.HashMap;

public class HuffmanTable {
    private final HashMap<Integer, int[]> lookupTable;
    private final Node root;

    private static class Node { // node in binary tree format
        private int symbol;
        private Node[] children; // [0] = left, [1] = right
        private Node parent;

        private Node() { // root
            symbol = -1; // nodes with -1 symbol have no leaves
        }
        private Node(Node parent){
            this();
            this.parent = parent;
        }
        private void initChildNodes(){
            children = new Node[]{new Node(this), new Node(this)};
        }
    }

    HuffmanTable(HashMap<Integer, int[]> lookupTable){
        this.lookupTable = lookupTable; // HM reference to code with corresponding symbols

        root = new Node(); // root node
        root.initChildNodes(); // initialize root's children
        Node farLeft = root.children[0];
        Node current;

        for(int i = 0; i < lookupTable.size(); i++){
            if(getSymbolCount(i) == 0){
                current = farLeft;
                while(current != null){
                    current.initChildNodes();
                    current = getRightNodeOf(current);
                }
                farLeft = farLeft.children[0];
            } else { // symbols to put into binary tree nodes
                for(int symbol: getSymbols(i)){
                    farLeft.symbol = symbol;
                    farLeft = getRightNodeOf(farLeft);
                }
                farLeft.initChildNodes();
                current = getRightNodeOf(farLeft);
                while(current != null){
                    current.initChildNodes();
                    current = getRightNodeOf(current);
                }
            }
        }
    }

    private int getSymbolCount(int code){
        return lookupTable.get(code)[0];
    }

    private int[] getSymbols(int code){
        return lookupTable.get(code);
    }

    private Node getRightNodeOf(Node node){
        if(node.parent.children[0] == node) return node.parent.children[1];
        int traversalCount = 0;

        while(node.parent != null && node.parent.children[1] != node){
            node = node.parent;
            traversalCount++;
        }
        if(node.parent == null) return null;

        node = node.parent.children[1];

        while (traversalCount > 0) {
            node = node.children[0];
            traversalCount--;
        }
        return node;
    }

    public int getCode(BitStream stream) {
        Node currentNode = root;
        while(currentNode.symbol == -1) {
            int bit = stream.bit();
            if(bit < 0) { // end of bit stream
                return bit; // no more codes to read
            }
            currentNode = currentNode.children[bit];
        }
        return currentNode.symbol;
    }
}
