package BPTree;

public class BPTree {

    public BPNode root;
    public BPNode leafHeadNode;

    public BPTree() {
        root = new BPNode(true, true);
        leafHeadNode = root;
    }

    public KeyValInNode selectKeyInTree(KeyValInNode searchKey) {
        return this.root.select(searchKey);
    }

    public void insertKey(KeyValInNode key) {
        this.root.insertKv(this, key);
    }

    public void insertLeafList(BPNode node) {
        if (this.leafHeadNode == null) {
            this.leafHeadNode = node;
        }
        BPNode lastNode = leafHeadNode;
        while (lastNode.nextNode != null) {
            lastNode = lastNode.nextNode;
        }
        lastNode.nextNode = node;
        node.prevNode = lastNode;
        node.nextNode = null;
    }
}