package Util;

import entity.NewRecordInfo;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ComUtil {
    public static byte[] convertRecordsIntoByteArray(NewRecordInfo record) {

        List<byte[]> byteArrList = new ArrayList<>();
        int recordLength = 0;
        int byte_Len_For_Int = 4;
        byte[] idBR = ByteBuffer.allocate(byte_Len_For_Int).putInt(record.id).array();
        recordLength += idBR.length;
        byteArrList.add(idBR);
        byte[] sensorIDBR = ByteBuffer.allocate(byte_Len_For_Int).putInt(record.getSensor_ID()).array();
        recordLength += sensorIDBR.length;
        byteArrList.add(sensorIDBR);
        byte[] hourlyCountBR = ByteBuffer.allocate(byte_Len_For_Int).putInt(record.getHourly_Counts()).array();
        recordLength += hourlyCountBR.length;
        byteArrList.add(hourlyCountBR);
        byte[] dateTimeBR = record.getDate_Time().getBytes();
        recordLength += dateTimeBR.length;
        byteArrList.add(dateTimeBR);
        if (record.sensor_Name_len == 0) {
            System.out.println("Sensor Name null");
        }
        byte[] sensorNameLenBR = ByteBuffer.allocate(byte_Len_For_Int).putInt(record.sensor_Name_len).array();
        recordLength += sensorNameLenBR.length;
        byteArrList.add(sensorNameLenBR);
        byte[] sensorNameBR = record.getSensor_Name().getBytes();
        recordLength += sensorNameBR.length;
        byteArrList.add(sensorNameBR);
        byte[] buffer = new byte[recordLength];
        int nextPosition = 0;
        for (byte[] tmpBR : byteArrList) {
            System.arraycopy(tmpBR, 0, buffer, nextPosition, tmpBR.length);
            nextPosition += tmpBR.length;
        }
        return buffer;
    }

    /**
     * compare text and newRecordInfo info
     */
    public static boolean compareForQuery(String text, NewRecordInfo newRecordInfo) {
        String combinedStr = newRecordInfo.getSensor_ID() + "-" + newRecordInfo.getDate_Time();
        return combinedStr.contains(text);
    }

    /**
     * recover newRecordInfo to origin recordInfo
     */
    public static String retrieveFullRecord(NewRecordInfo newRecordInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(newRecordInfo.getId()).append(',');
        sb.append(newRecordInfo.Date_Time).append(',');
        int[] otherFiled = resolveDay(newRecordInfo.Date_Time);
        sb.append(otherFiled[0]).append(',');
        sb.append(allMonths[otherFiled[1]]).append(',');
        sb.append(otherFiled[2]).append(',');
        sb.append(allDays[otherFiled[3]]).append(',');
        sb.append(otherFiled[4]).append(',');
        sb.append(newRecordInfo.getSensor_ID()).append(',');
        sb.append(newRecordInfo.getSensor_Name()).append(',');
        sb.append(newRecordInfo.getHourly_Counts());
        return sb.toString();
    }

    /**
     * resolve year month mday day time from date_time
     * example 11/01/2019 05:00:00 PM
     */
    private static int[] resolveDay(String dateTime) {
        int[] result = new int[5];
        String[] str1 = dateTime.split(" ");
        boolean isPM = str1[2].equals("PM");
        String[] ymd = str1[0].split("/");
        result[0] = Integer.parseInt(ymd[2]);
        result[1] = Integer.parseInt(ymd[0]);
        result[2] = Integer.parseInt(ymd[1]);
        result[3] = retrieveWeekFromDate(str1[0]);
        String[] timeString = str1[1].split(":");
        int baseHour = Integer.parseInt(timeString[0]);
        if (!isPM) {
            if (baseHour != 12) {
                result[4] = baseHour;
            } else {
                result[4] = 0;
            }
        } else {
            if (baseHour != 12) {
                result[4] = baseHour + 12;
            } else {
                result[4] = 12;
            }
        }
        return result;
    }

    private static final String[] allDays = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final String[] allMonths = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    public static int retrieveWeekFromDate(String datetime) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(datetime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int weekToDay;
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            weekToDay = 7;
        } else {
            weekToDay = c.get(Calendar.DAY_OF_WEEK) - 1;
        }
        return weekToDay;
    }

    public static void main(String[] args) {
        System.out.println(retrieveWeekFromDate("11/01/2019"));
    }

}
