package JpegHelpers;

import java.util.HashMap;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import javax.swing.*;

public class HuffmanTableDecode {
    private final HashMap<Integer, int[]> lookup;
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

    HuffmanTableDecode(HashMap<Integer, int[]> lookup){
//        Graph graph = new SingleGraph("Huffman Tree");
//        graph.setStrict(false);
//        graph.setAutoCreate(true); // create nodes automagically
        // TODO: Implement graphing and display of Huffman Tree after generating it with post order traversal?
        //int idx = 0;
        this.lookup = lookup; // HM reference to code with corresponding symbols
        root = new Node(); // root node
        root.initChildNodes(); // initialize root's children
        Node farLeft = root.children[0];
        Node current;
        for(int i = 1; i <= lookup.size(); i++){
            if(getSymbolCount(i) == 0){
                current = farLeft;
                while(current != null){
                    current.initChildNodes();
                    current = getRightNodeOf(current); // gets stuck here?
                    //System.out.println("current: " + current);
                }
                farLeft = farLeft.children[0];
            } else { // symbols to put into binary tree nodes
                for(int symbol: getSymbols(i)){
                    farLeft.symbol = symbol;
                    farLeft = getRightNodeOf(farLeft);
                }
                //graph.addEdge(Integer.toString(idx++), farLeft.parent.toString(), farLeft.toString());
                farLeft.initChildNodes();
                current = getRightNodeOf(farLeft);
                farLeft = farLeft.children[0];
                while(current != null){
                    current.initChildNodes();
                    current = getRightNodeOf(current);
                }
            }
        }
//        try{
//            graph.display();
//        } catch(Exception e){
//            System.err.println("Graph display failed: " + e.getLocalizedMessage());
//        }
    }

    private int getSymbolCount(int code){
        return lookup.get(code).length;
    }

    private int[] getSymbols(int code){
        return lookup.get(code);
    }

    private Node getRightNodeOf(Node node){
        if(node.parent.children[0] == node) return node.parent.children[1];
        int traversalCount = 0;

        while(node.parent != null && node.parent.children[1] == node){
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
            try{
                currentNode = currentNode.children[bit];
            } catch (NullPointerException e){
                System.out.println("NullPointerException traversing Huffman Table tree! Trying to return old symbol...");
                return currentNode.symbol;
            }
        }
        return currentNode.symbol;
    }
}
