package BPTree;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class BPTreeCreator {

    public static void createBPTree(int pageSize, BPTree bpTree) {
        BPNode root = bpTree.root;
        int nextPageNum = 0;
        Queue<BPNode> BPNodeQueue = new LinkedList<>();
        BPNodeQueue.offer(root);
        root.pageNum = nextPageNum;
        nextPageNum += 1;
        while (!BPNodeQueue.isEmpty()) {
            int size = BPNodeQueue.size();
            for (int i = 0; i < size; i++) {
                BPNode node = BPNodeQueue.poll();
                if (node != null && !node.leafFlag) {
                    for (BPNode childNode : node.childNodeList) {
                        BPNodeQueue.add(childNode);
                        childNode.pageNum = nextPageNum;
                        nextPageNum++;
                    }
                }
            }
        }
        try (RandomAccessFile raf = new RandomAccessFile("tree." + pageSize, "rw")) {
            //level scan
            BPNodeQueue = new LinkedList<>();
            BPNodeQueue.offer(root);
            while (!BPNodeQueue.isEmpty()) {
                int size = BPNodeQueue.size();
                for (int i = 0; i < size; i++) {
                    BPNode node = BPNodeQueue.poll();
                    assert node != null;
                    short kSize = (short) node.keyValNodeList.size();

                    int nodeByteSize = BPTreeHeapFileConfig.ByteSizeOfShort + BPTreeHeapFileConfig.ByteSizeOfChar +
                            kSize * BPTreeHeapFileConfig.ByteSizeOfKey + BPTreeHeapFileConfig.ByteSizeOfInt * (kSize + 1);
                    byte[] nodeBr = new byte[nodeByteSize];

                    int nextOffset = 0;

                    byte[] kSizeBr = ByteBuffer.allocate(BPTreeHeapFileConfig.ByteSizeOfShort).putShort(kSize).array();
                    System.arraycopy(kSizeBr, 0, nodeBr, nextOffset, kSizeBr.length);
                    nextOffset += BPTreeHeapFileConfig.ByteSizeOfShort;

                    boolean leafFlag = node.leafFlag;
                    byte[] isLeafBr = ByteBuffer.allocate(BPTreeHeapFileConfig.ByteSizeOfChar).putChar((char) (leafFlag ? 1 : 0)).array();
                    System.arraycopy(isLeafBr, 0, nodeBr, nextOffset, isLeafBr.length);
                    nextOffset += BPTreeHeapFileConfig.ByteSizeOfChar;

                    int keyIdx = 0;
                    for (KeyValInNode key : node.keyValNodeList) {
                        //save value
                        if (!node.leafFlag) {
                            BPNodeQueue.offer(node.childNodeList.get(keyIdx));
                            byte[] pageIdxBr = ByteBuffer.allocate(BPTreeHeapFileConfig.ByteSizeOfInt)
                                    .putInt(node.childNodeList.get(keyIdx).pageNum).array();
                            System.arraycopy(pageIdxBr, 0, nodeBr, nextOffset, pageIdxBr.length);
                        } else {
                            byte[] dataPtr = ByteBuffer.allocate(BPTreeHeapFileConfig.ByteSizeOfInt)
                                    .putInt(node.keyValNodeList.get(keyIdx).recordAddr).array();
                            System.arraycopy(dataPtr, 0, nodeBr, nextOffset, dataPtr.length);
                        }
                        nextOffset += BPTreeHeapFileConfig.ByteSizeOfInt;
                        //save key
                        byte[] keyBr = ByteBuffer.allocate(BPTreeHeapFileConfig.ByteSizeOfKey).put(key.key.getBytes()).array();
                        System.arraycopy(keyBr, 0, nodeBr, nextOffset, keyBr.length);
                        nextOffset += BPTreeHeapFileConfig.ByteSizeOfKey;
                        keyIdx++;
                    }
                    //if not leaf node ,need to save last value
                    if (!node.leafFlag) {
                        BPNodeQueue.offer(node.childNodeList.get(keyIdx));
                        byte[] pageIdxBr = ByteBuffer.allocate(BPTreeHeapFileConfig.ByteSizeOfInt)
                                .putInt(node.childNodeList.get(keyIdx).pageNum).array();
                        System.arraycopy(pageIdxBr, 0, nodeBr, nextOffset, pageIdxBr.length);
                    }
                    //write file
                    raf.seek((long) node.pageNum * pageSize);
                    raf.write(nodeBr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readBpTree(int pageSize, BPTree bpTree) {

        BPNode newRootNode = new BPNode(false, true);
        byte[] kSizeBr, leafFlagBr, valueBr, keyBr;
        short kSize;

        try (RandomAccessFile raf = new RandomAccessFile("tree." + pageSize, "rw")) {
            byte[] pageContent = new byte[pageSize];
            int offsetP = 0;
            raf.seek(offsetP);
            raf.read(pageContent);
            kSizeBr = new byte[BPTreeHeapFileConfig.ByteSizeOfShort];
            System.arraycopy(pageContent, offsetP, kSizeBr, 0, BPTreeHeapFileConfig.ByteSizeOfShort);
            offsetP += BPTreeHeapFileConfig.ByteSizeOfShort;
            kSize = ByteBuffer.wrap(kSizeBr).getShort();
            byte[] rootIsLeafBr = new byte[BPTreeHeapFileConfig.ByteSizeOfChar];
            System.arraycopy(pageContent, offsetP, rootIsLeafBr, 0, BPTreeHeapFileConfig.ByteSizeOfChar);
            offsetP += BPTreeHeapFileConfig.ByteSizeOfChar;
            newRootNode.leafFlag = ByteBuffer.wrap(rootIsLeafBr).getChar() != 0;

            newRootNode.childPageIndex = new ArrayList<>();
            for (int i = 0; i < kSize; i++) {
                if (!newRootNode.leafFlag) {
                    valueBr = new byte[BPTreeHeapFileConfig.ByteSizeOfInt];
                    System.arraycopy(pageContent, offsetP, valueBr, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
                    offsetP += BPTreeHeapFileConfig.ByteSizeOfInt;
                    newRootNode.childPageIndex.add(ByteBuffer.wrap(valueBr).getInt());
                } else {
                    valueBr = new byte[BPTreeHeapFileConfig.ByteSizeOfInt];
                    System.arraycopy(pageContent, offsetP, valueBr, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
                    offsetP += BPTreeHeapFileConfig.ByteSizeOfInt;
                    newRootNode.keyValNodeList.get(i).recordAddr = ByteBuffer.wrap(valueBr).getInt();
                }
                keyBr = new byte[BPTreeHeapFileConfig.ByteSizeOfKey];
                System.arraycopy(pageContent, offsetP, keyBr, 0, BPTreeHeapFileConfig.ByteSizeOfKey);
                offsetP += BPTreeHeapFileConfig.ByteSizeOfKey;
                KeyValInNode values = new KeyValInNode(new String(keyBr).trim());
                newRootNode.keyValNodeList.add(values);
            }

            if (!newRootNode.leafFlag) {
                byte[] pageIdxBr = new byte[BPTreeHeapFileConfig.ByteSizeOfInt];
                System.arraycopy(pageContent, offsetP, pageIdxBr, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
                newRootNode.childPageIndex.add(ByteBuffer.wrap(pageIdxBr).getInt());
            }

            bpTree.root = newRootNode;
            bpTree.leafHeadNode = null;

            if (bpTree.root.leafFlag) {
                bpTree.leafHeadNode = bpTree.root;
                return;
            }
            Queue<BPNode> BPNodeQueue = new LinkedList<>();
            BPNodeQueue.offer(newRootNode);
            while (!BPNodeQueue.isEmpty()) {
                int size = BPNodeQueue.size();

                for (int i = 0; i < size; i++) {
                    BPNode parentNode = BPNodeQueue.poll();

                    assert parentNode != null;
                    if (parentNode.leafFlag) {
                        bpTree.insertLeafList(parentNode);
                        continue;
                    }

                    for (int j = 0; j < parentNode.childPageIndex.size(); j++) {
                        try {
                            raf.seek((long) parentNode.childPageIndex.get(j) * pageSize);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        pageContent = new byte[pageSize];
                        raf.read(pageContent);

                        offsetP = 0;

                        kSizeBr = new byte[BPTreeHeapFileConfig.ByteSizeOfShort];
                        System.arraycopy(pageContent, offsetP, kSizeBr, 0, BPTreeHeapFileConfig.ByteSizeOfShort);
                        offsetP += BPTreeHeapFileConfig.ByteSizeOfShort;
                        kSize = ByteBuffer.wrap(kSizeBr).getShort();

                        leafFlagBr = new byte[BPTreeHeapFileConfig.ByteSizeOfShort];
                        System.arraycopy(pageContent, offsetP, leafFlagBr, 0, BPTreeHeapFileConfig.ByteSizeOfShort);
                        offsetP += BPTreeHeapFileConfig.ByteSizeOfShort;
                        boolean isLeaf = ByteBuffer.wrap(leafFlagBr).getChar() != 0;
                        BPNode newNode = new BPNode(isLeaf, false);

                        newNode.childPageIndex = new ArrayList<>();
                        for (int k = 0; k < kSize; k++) {
                            valueBr = new byte[BPTreeHeapFileConfig.ByteSizeOfInt];
                            System.arraycopy(pageContent, offsetP, valueBr, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
                            offsetP += BPTreeHeapFileConfig.ByteSizeOfInt;
                            //read key
                            keyBr = new byte[BPTreeHeapFileConfig.ByteSizeOfKey];
                            try {
                                System.arraycopy(pageContent, offsetP, keyBr, 0, BPTreeHeapFileConfig.ByteSizeOfKey);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            offsetP += BPTreeHeapFileConfig.ByteSizeOfKey;
                            KeyValInNode values = new KeyValInNode(new String(keyBr).trim());
                            newNode.keyValNodeList.add(values);

                            if (!newNode.leafFlag) {

                                newNode.childPageIndex.add(ByteBuffer.wrap(valueBr).getInt());
                            } else {

                                newNode.keyValNodeList.get(k).recordAddr = ByteBuffer.wrap(valueBr).getInt();
                            }
                        }
                        if (!newNode.leafFlag) {

                            valueBr = new byte[BPTreeHeapFileConfig.ByteSizeOfInt];
                            System.arraycopy(pageContent, offsetP, valueBr, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
                            newNode.childPageIndex.add(ByteBuffer.wrap(valueBr).getInt());
                        }
                        parentNode.childNodeList.add(newNode);
                        newNode.parentNode = parentNode;
                        BPNodeQueue.add(newNode);
                    }
                    parentNode.childPageIndex = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
