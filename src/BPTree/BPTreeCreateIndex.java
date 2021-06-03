package BPTree;

import entity.NewRecordInfo;
import Util.ComUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BPTreeCreateIndex {

    private static KeyValInNode createKeyValuePairs(String key, int value) {
        KeyValInNode keyValInNode = new KeyValInNode(key);
        keyValInNode.recordAddr = value;
        return keyValInNode;
    }

    public static KeyValInNode createKeyValuePairs(String key) {
        return new KeyValInNode(key);
    }

    public static void constructBpIndex(int pageSize, BPTree bpTree) {
        dealFileContext(pageSize, bpTree);
        BPTreeCreator.createBPTree(pageSize, bpTree);
    }

    private static void dealFileContext(int pageSize, BPTree bpTree) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream("heap." + pageSize))) {
            byte[] pageBuf = new byte[pageSize];
            int len;
            int pageIdx = 0;
            while ((len = inputStream.read(pageBuf)) != -1) {
                if (pageSize != len) {
                    System.err.println("no a page");
                }
                searchTextInPage(pageBuf, pageSize, pageIdx, bpTree);
                pageIdx += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final int Date_Time_Len = 22;

    private static void searchTextInPage(byte[] pageBuf, int pageSize, int pageIdx, BPTree bpTree) {
        int pagePos = 0;
        byte[] tmpBRForInt = new byte[BPTreeHeapFileConfig.ByteSizeOfInt];
        byte[] tmpDateTimeBR = new byte[Date_Time_Len];
        int min_Len_Record = 38;
        while (pagePos + min_Len_Record < pageSize) {
            int originPagePos = pagePos;
            System.arraycopy(pageBuf, pagePos, tmpBRForInt, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
            int id = ByteBuffer.wrap(tmpBRForInt).getInt();
            pagePos += BPTreeHeapFileConfig.ByteSizeOfInt;
            if (id == 0) {
                break;
            }
            System.arraycopy(pageBuf, pagePos, tmpBRForInt, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
            int sensorId = ByteBuffer.wrap(tmpBRForInt).getInt();
            pagePos += BPTreeHeapFileConfig.ByteSizeOfInt;
            System.arraycopy(pageBuf, pagePos, tmpBRForInt, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
            int hourly = ByteBuffer.wrap(tmpBRForInt).getInt();
            pagePos += BPTreeHeapFileConfig.ByteSizeOfInt;
            System.arraycopy(pageBuf, pagePos, tmpDateTimeBR, 0, Date_Time_Len);
            String dateTime = new String(tmpDateTimeBR);
            pagePos += Date_Time_Len;
            System.arraycopy(pageBuf, pagePos, tmpBRForInt, 0, BPTreeHeapFileConfig.ByteSizeOfInt);
            int sensorNameLen = ByteBuffer.wrap(tmpBRForInt).getInt();
            byte[] sensorNameBR = new byte[sensorNameLen];
            pagePos += BPTreeHeapFileConfig.ByteSizeOfInt;
            System.arraycopy(pageBuf, pagePos, sensorNameBR, 0, sensorNameLen);
            String sensorName = new String(sensorNameBR);
            pagePos += sensorNameLen;
            if (sensorNameLen == 0) {
                System.err.println("sensorName is null");
            }
            NewRecordInfo newRecordInfo = new NewRecordInfo(id, sensorId, hourly, dateTime, sensorNameLen, sensorName);
            String combinStr = newRecordInfo.getSensor_ID() + "-" + newRecordInfo.getDate_Time();
            KeyValInNode keyValInNode = createKeyValuePairs(combinStr, pageIdx * pageSize + originPagePos);
            bpTree.insertKey(keyValInNode);
        }
    }

    public static String readRecordByAddress(int address, int pageSize) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile("heap." + pageSize, "rw");
            raf.seek(address);
            byte[] tmpBRForInt = new byte[BPTreeHeapFileConfig.ByteSizeOfInt];
            byte[] tmpDateTimeBR = new byte[Date_Time_Len];
            raf.read(tmpBRForInt);
            int id = ByteBuffer.wrap(tmpBRForInt).getInt();
            raf.read(tmpBRForInt);
            int sensorId = ByteBuffer.wrap(tmpBRForInt).getInt();
            raf.read(tmpBRForInt);
            int hourly = ByteBuffer.wrap(tmpBRForInt).getInt();
            raf.read(tmpDateTimeBR);
            String dateTime = new String(tmpDateTimeBR);
            raf.read(tmpBRForInt);
            int sensorNameLen = ByteBuffer.wrap(tmpBRForInt).getInt();
            byte[] sensorNameBR = new byte[sensorNameLen];
            raf.read(sensorNameBR);
            String sensorName = new String(sensorNameBR);
            if (sensorNameLen == 0) {
                System.err.println("Sensor Name is null");
            }
            NewRecordInfo newRecordInfo = new NewRecordInfo(id, sensorId, hourly, dateTime, sensorNameLen, sensorName);
            return ComUtil.retrieveFullRecord(newRecordInfo);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<String> selectRecordByIndex(String conditionText, int pageSize) {

        BPTree tree = new BPTree();
        tree.root = null;
        tree.leafHeadNode = null;
        long bt = System.currentTimeMillis();
        BPTreeCreator.readBpTree(pageSize, tree);
        long et = System.currentTimeMillis();
        System.out.println("read tree = " + (et - bt));

        KeyValInNode searchKeyValInNode = BPTreeCreateIndex.createKeyValuePairs(conditionText);

        if (conditionText.length() < BPTreeHeapFileConfig.LengthOfKey) {
            searchKeyValInNode.rangeFlag = true;
        }
        if (conditionText.length() == BPTreeHeapFileConfig.LengthOfKey) {
            searchKeyValInNode.rangeFlag = conditionText.split("-")[1].length() < 22;
        }

        KeyValInNode keyValInNode = tree.selectKeyInTree(searchKeyValInNode);

        if (keyValInNode == null) return null;
        List<String> results = new ArrayList<>();

        if (!searchKeyValInNode.rangeFlag) {
            results.add(readRecordByAddress(keyValInNode.recordAddr, pageSize));
        } else {
            for (Integer pos : keyValInNode.selectRsByRangeList) {
                results.add(readRecordByAddress(pos, pageSize));
            }
        }
        return results;
    }
}
