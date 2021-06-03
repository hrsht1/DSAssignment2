import BPTree.BPTreeCreateIndex;

import java.util.List;

public class TreeQuery {
    public static String SDTNAME;

    public static void main(String[] args) {
        long beginTime;
        long endTime;

        StringBuilder sb = new StringBuilder();
        int pageSize = Integer.parseInt(args[args.length - 1]);
        for (int i = 0; i < args.length - 1; i++) {
            sb.append(args[i]).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        SDTNAME = sb.toString();
        beginTime = System.currentTimeMillis();
        List<String> recordByIndex = BPTreeCreateIndex.selectRecordByIndex(SDTNAME, pageSize);
        int recordResultCnt = 0;
        if (recordByIndex != null) {
            recordResultCnt = recordByIndex.size();
            for (String recordStr : recordByIndex) {
                System.out.println(recordStr);
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Total time taken to process the query and fetch result in milliseconds: " + (endTime - beginTime));
        System.out.println("Total number of records found matching: " + recordResultCnt);
    }
}
