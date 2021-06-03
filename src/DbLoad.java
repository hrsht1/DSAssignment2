import entity.NewRecordInfo;
import Util.ComUtil;

import java.io.*;

/**
 * DbLoad class is used to load csv records into memory and create a heap file
 */
public class DbLoad {
    private static int perPageLength = 0;
    private static byte[] pageBuffer;
    private static String csvFileName;
    private static int totalNumberOfRecords = 0;
    private static int totalPagesConsumed = 0;
    private final static String Split_Char = ",";
    private static int pageBufferPosition = 0;
    public static RandomAccessFile raf;

    /**
     * Read and Load all records from CSV
     */
    public static void dealRecordFromCSV() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFileName)))) {
            String row;
            String header = bufferedReader.readLine();
            while ((row = bufferedReader.readLine()) != null) {
                String[] colArr = row.split(Split_Char);
                NewRecordInfo newRecordInfo = new NewRecordInfo(colArr);
                putNewRecordIntoPageBuf(ComUtil.convertRecordsIntoByteArray(newRecordInfo));
                totalNumberOfRecords++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * To put each record from byte array into page buffer
     */
    public static void putNewRecordIntoPageBuf(byte[] recordBR) {
        if (perPageLength < pageBufferPosition + recordBR.length) {
            if (0 == pageBufferPosition) {
                return;
            }
            try {
                raf.seek((long) totalPagesConsumed * perPageLength);
                raf.write(pageBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            totalPagesConsumed++;
            pageBuffer = new byte[perPageLength];
            pageBufferPosition = 0;
        }
        System.arraycopy(recordBR, 0, pageBuffer, pageBufferPosition, recordBR.length);
        pageBufferPosition += recordBR.length;
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 3) {
            System.out.println("Too few arguments, please enter in this format: -p [pagesize] [datafile]");
            return;
        }
        perPageLength = Integer.parseInt(args[1]);
        pageBuffer = new byte[perPageLength];
        String heapFileName = "heap." + perPageLength;
        csvFileName = args[2];
        File lastFile = new File(heapFileName);
        lastFile.delete();
        raf = new RandomAccessFile(heapFileName, "rw");
        long time1;
        long time2;
        time1 = System.currentTimeMillis();
        dealRecordFromCSV();
        time2 = System.currentTimeMillis();
        System.out.println("\rTotal number of records loaded \t\t\t\t\t:" + totalNumberOfRecords);
        System.out.println("Total number of pages consumed in the process \t:" + totalPagesConsumed);
        System.out.println("Time taken to finish the process in milliseconds:" + (time2 - time1));
    }
}
