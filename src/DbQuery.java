import entity.NewRecordInfo;
import Util.ComUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * according to text ,find record
 */
public class DbQuery {
    private static String selectText;
    private static int perPageLen;
    private static String heapFileName;
    private static int matchCnt = 0;

    public static void main(String[] args) {
        selectText = "";
        for (int i = 0; i < args.length - 1; i++) {
            selectText += args[i] + " ";
        }
        selectText = selectText.substring(0, selectText.length() - 1);
        perPageLen = Integer.parseInt(args[args.length - 1]);
        heapFileName = "heap." + perPageLen;
        File searchFile = new File(heapFileName);
        if (!searchFile.exists() || !searchFile.isFile()) {
            System.err.println(heapFileName + "does not exist. Please try again.");
            return;
        }
        long time1 = System.currentTimeMillis();
        dealFileContext();
        long time2 = System.currentTimeMillis();
        System.out.println("Total number of matches in heap file: " + matchCnt);
        System.out.println("Time taken to finish the process in milliseconds: " + (time2 - time1));
    }

    private static void dealFileContext() {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(heapFileName))) {
            byte[] pageBuf = new byte[perPageLen];
            int len = 0;
            int pageCnt = 0;
            while ((len = inputStream.read(pageBuf)) != -1) {
                if (perPageLen != len) {
                    System.err.println("Not a Page");
                }
                searchTextInPage(pageBuf);
                pageCnt++;
            }
            System.out.println("Total number of pages scanned: " + pageCnt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Searches for similar record in each Page
     */
    private static void searchTextInPage(byte[] pageBuf) {
        int pagePos = 0;
        int byte_Len_For_Int = 4;
        byte[] tmpBRForInt = new byte[byte_Len_For_Int];
        int date_Time_Len = 22;
        byte[] tmpDateTimeBR = new byte[date_Time_Len];
        int min_Len_Record = 38;
        while (pagePos + min_Len_Record < perPageLen) {
            System.arraycopy(pageBuf, pagePos, tmpBRForInt, 0, byte_Len_For_Int);
            int id = ByteBuffer.wrap(tmpBRForInt).getInt();
            pagePos += byte_Len_For_Int;
            if (id == 0) {
                break;
            }
            System.arraycopy(pageBuf, pagePos, tmpBRForInt, 0, byte_Len_For_Int);
            int sensorId = ByteBuffer.wrap(tmpBRForInt).getInt();
            pagePos += byte_Len_For_Int;
            System.arraycopy(pageBuf, pagePos, tmpBRForInt, 0, byte_Len_For_Int);
            int hourly = ByteBuffer.wrap(tmpBRForInt).getInt();
            pagePos += byte_Len_For_Int;
            System.arraycopy(pageBuf, pagePos, tmpDateTimeBR, 0, date_Time_Len);
            String dateTime = new String(tmpDateTimeBR);
            pagePos += date_Time_Len;
            System.arraycopy(pageBuf, pagePos, tmpBRForInt, 0, byte_Len_For_Int);
            int sensorNameLen = ByteBuffer.wrap(tmpBRForInt).getInt();
            byte[] sensorNameBR = new byte[sensorNameLen];
            pagePos += byte_Len_For_Int;
            System.arraycopy(pageBuf, pagePos, sensorNameBR, 0, sensorNameLen);
            String sensorName = new String(sensorNameBR);
            pagePos += sensorNameLen;
            if (sensorNameLen == 0) {
                System.err.println("Error occurred, SensorName is null.");
            }
            NewRecordInfo newRecordInfo = new NewRecordInfo(id, sensorId, hourly, dateTime, sensorNameLen, sensorName);
            if (ComUtil.compareForQuery(selectText, newRecordInfo)) {
                matchCnt++;
                System.out.println(ComUtil.retrieveFullRecord(newRecordInfo));
            }
        }
    }
}
