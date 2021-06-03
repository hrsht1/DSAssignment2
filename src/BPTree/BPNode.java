package BPTree;

import java.util.ArrayList;
import java.util.List;

public class BPNode {

    public boolean leafFlag = true;
    public boolean rootFlag;

    public List<KeyValInNode> keyValNodeList = new ArrayList<>();
    public List<BPNode> childNodeList;

    public BPNode parentNode;
    public BPNode prevNode;
    public BPNode nextNode;

    public List<Integer> childPageIndex;
    public int pageNum;

    public BPNode(boolean leafFlag) {
        if (!leafFlag) {
            this.leafFlag = false;
            childNodeList = new ArrayList<>();
        }
    }

    public BPNode(boolean leafFlag, boolean rootFlag) {
        this.rootFlag = rootFlag;
        if (!leafFlag) {
            this.leafFlag = false;
            childNodeList = new ArrayList<>();
        }

    }

    public KeyValInNode select(KeyValInNode key) {
        if (leafFlag) {
            if (!key.rangeFlag) {
                KeyValInNode resultNode = null;
                for (KeyValInNode keyValInNode : keyValNodeList) {
                    if (0 == key.wholeMatch(keyValInNode)) {
                        resultNode = keyValInNode;
                    }
                }
                return resultNode;
            }
            key.selectRsByRangeList = new ArrayList<>();
            for (KeyValInNode inNode : keyValNodeList) {
                if (inNode.leafNodeMatch(key)) {
                    key.selectRsByRangeList.add(inNode.recordAddr);
                }
            }
            if (key.selectRsByRangeList.isEmpty()) {
                return null;
            }
            BPNode curNode = this.nextNode;
            BPNode curNode0 = this.prevNode;
            w1:
            while (curNode != null) {
                for (KeyValInNode keyValInNode : curNode.keyValNodeList) {
                    if (keyValInNode.leafNodeMatch(key)) {
                        key.selectRsByRangeList.add(keyValInNode.recordAddr);
                    } else {
                        break w1;
                    }
                }
                curNode = curNode.nextNode;
            }
            w2:
            while (null != curNode0) {
                for (int i = curNode0.keyValNodeList.size() - 1; i >= 0; i--) {
                    if (curNode0.keyValNodeList.get(i).leafNodeMatch(key)) {
                        key.selectRsByRangeList.add(curNode0.keyValNodeList.get(i).recordAddr);
                    } else {
                        break w2;
                    }
                }
                curNode0 = curNode0.prevNode;
            }
            return key;
        } else {
            int firstIdx = 0, lastIdx = keyValNodeList.size() - 1;
            if (key.wholeMatch(keyValNodeList.get(firstIdx)) < 0) {
                return childNodeList.get(firstIdx).select(key);
            }
            if (key.wholeMatch(keyValNodeList.get(lastIdx)) >= 0) {
                return childNodeList.get(lastIdx).select(key);
            }
            for (int i = 0; i < (keyValNodeList.size() - 1); i++) {
                if (key.wholeMatch(keyValNodeList.get(i)) >= 0 && key.wholeMatch(keyValNodeList.get(i + 1)) < 0) {
                    return childNodeList.get(i + 1).select(key);
                }
            }
            return null;
        }

    }

    public void insertKv(BPTree tree, KeyValInNode key) {
        if (this.leafFlag) {
            insertKvInLeaf(key, tree);
        } else {
            insertKvNotInLeaf(key, tree);
        }
    }

    public void insertKvInLeaf(KeyValInNode key, BPTree tree) {
        if (!(keyValNodeList.size() >= (BPTreeHeapFileConfig.MaxKvCnt - 1))) {
            InsertNewNode(key);
        } else {
            BPNode left = new BPNode(true);
            BPNode right = new BPNode(true);
            if (null != prevNode) {
                prevNode.nextNode = left;
                left.prevNode = prevNode;
            } else {
                tree.leafHeadNode = left;
            }
            if (null != nextNode) {
                nextNode.prevNode = right;

                right.nextNode = nextNode;
            }
            right.prevNode = left;
            left.nextNode = right;

            InsertNewNode(key);
            int leftSize = upper(keyValNodeList.size());
            for (int i = 0; i < leftSize; i++) {
                left.keyValNodeList.add(keyValNodeList.get(i));
            }
            int rightSize = keyValNodeList.size() - leftSize;
            for (int i = 0; i < rightSize; i++) {
                right.keyValNodeList.add(keyValNodeList.get(leftSize + i));
            }
            if (!rootFlag) {
                int index = parentNode.childNodeList.indexOf(this);

                left.parentNode = parentNode;
                right.parentNode = parentNode;
                // Delete current pointer
                parentNode.childNodeList.remove(this);
                // Add the pointer of the split node to the parent node
                parentNode.childNodeList.add(index, left);
                parentNode.childNodeList.add(index + 1, right);

                parentNode.InsertNewNode(right.keyValNodeList.get(0));
                parentNode.changeNode(tree);

            } else {
                rootFlag = false;
                BPNode rootNode = new BPNode(false, true);
                tree.root = rootNode;
                rootNode.childNodeList.add(left);
                rootNode.childNodeList.add(right);
                left.parentNode = rootNode;
                right.parentNode = rootNode;

                rootNode.InsertNewNode(right.keyValNodeList.get(0));
            }
        }
    }

    public void insertKvNotInLeaf(KeyValInNode key, BPTree tree) {
        int firstIdx = 0, lastIdx = childNodeList.size() - 1;
        if (key.wholeMatch(keyValNodeList.get(firstIdx)) < 0) {
            childNodeList.get(firstIdx).insertKv(tree, key);
        } else if (key.wholeMatch(keyValNodeList.get(keyValNodeList.size() - 1)) >= 0) {
            childNodeList.get(lastIdx).insertKv(tree, key);
        } else {
            int i = 0;
            while (i < keyValNodeList.size() - 1) {
                if (key.wholeMatch(keyValNodeList.get(i)) >= 0) {
                    if (key.wholeMatch(keyValNodeList.get(i + 1)) < 0) {
                        childNodeList.get(i + 1).insertKv(tree, key);
                        break;
                    }
                }
                i += 1;
            }
        }
    }

    private void changeNode(BPTree tree) {
        if (isNodeToSplit()) {
            BPNode left = new BPNode(false);
            BPNode right = new BPNode(false);
            int pLeftSize = upper(childNodeList.size());
            int pRightSize = childNodeList.size() - pLeftSize;   //fix bug
            KeyValInNode keyToParent = keyValNodeList.get(pLeftSize - 1);
            for (int i = 0; i < (pLeftSize - 1); i++) {
                left.keyValNodeList.add(keyValNodeList.get(i));
            }
            for (int i = 0; i < pLeftSize; i++) {
                left.childNodeList.add(childNodeList.get(i));
                left.childNodeList.get(i).parentNode = left;
            }
            for (int i = 0; i < (pRightSize - 1); i++) {
                right.keyValNodeList.add(keyValNodeList.get(pLeftSize + i));
            }
            for (int i = 0; i < pRightSize; i++) {
                right.childNodeList.add(childNodeList.get(pLeftSize + i));
                right.childNodeList.get(i).parentNode = right;
            }
            if (!rootFlag) {// Insert key to parent node of non-leaf node
                int index = parentNode.childNodeList.indexOf(this);
                parentNode.childNodeList.remove(index);
                parentNode.childNodeList.add(index, left);
                parentNode.childNodeList.add(index + 1, right);
                left.parentNode = parentNode;
                right.parentNode = parentNode;
                parentNode.keyValNodeList.add(index, keyToParent);
                parentNode.changeNode(tree);
                keyValNodeList.clear();
                childNodeList.clear();

                keyValNodeList = null;
                childNodeList = null;
                parentNode = null;
            } else {
                rootFlag = false;
                BPNode rootNode = new BPNode(false, true);
                tree.root = rootNode;
                left.parentNode = rootNode;
                right.parentNode = rootNode;
                rootNode.childNodeList.add(left);
                rootNode.childNodeList.add(right);
                childNodeList.clear();
                keyValNodeList.clear();
                childNodeList = null;
                keyValNodeList = null;
                rootNode.keyValNodeList.add(keyToParent);
            }
        }
    }

    private int upper(int x) {
        if (x % 2 == 0) {
            return x / 2;
        } else {
            return (x / 2) + 1;
        }
    }

    private boolean isNodeToSplit() {
        if (childNodeList.size() > BPTreeHeapFileConfig.MaxKvCnt) {
            return true;
        }
        return false;
    }

    public int totalKeyInserted = 0;

    private void InsertNewNode(KeyValInNode key) {
        for (int i = 0; i < keyValNodeList.size(); i++) {
            if (keyValNodeList.get(i).wholeMatch(key) == 0) {
                return;
            } else if (keyValNodeList.get(i).wholeMatch(key) > 0) {
                keyValNodeList.add(i, key);
                totalKeyInserted++;
                if (totalKeyInserted % 1000000 == 0) {
                    System.out.println("");
                }
                return;
            }
        }
        keyValNodeList.add(key);
        totalKeyInserted++;
        if (totalKeyInserted % 1000000 == 0) {
            System.out.println("");
        }
    }


}
