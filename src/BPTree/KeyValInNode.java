package BPTree;

import java.util.List;

public class KeyValInNode {
    public boolean rangeFlag;
    public List<Integer> selectRsByRangeList;
    public String key;
    public Integer recordAddr;

    public KeyValInNode(String key) {
        this.key = key;
    }

    public int wholeMatch(KeyValInNode compareKv) {
        int flag = key.compareTo(compareKv.key);
        if (flag < 0) {
            if (compareKv.key.indexOf(key) == 0) {
                return 0;
            }
            return -1;
        } else if (flag == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public boolean leafNodeMatch(KeyValInNode compareKv) {
        return this.key.indexOf(compareKv.key) == 0;
    }
}
