/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.wechat.test;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by shiwei.liao on 2015/6/19.
 */
public class O2oAnswer {
    public static void main(String[] args) throws IOException {
        String[] answerKey = new String[]{"EXAM_ID", "ANSWER_ID", "TITLE", "ANSWER_VALUE", "SCORE", "EXAM_TITLE"};
        String excelFilePath = "C:\\Users\\Administrator\\Desktop\\o2o_answer0625.xls";
        String jsonFilePath = "D:\\codeBase\\py-tools\\upload_content\\otoanswerimport\\answer_json.json";
        File file = new File(excelFilePath);
        String[][] result = getData(file, 1);

        List<LinkedHashMap<String, Object>> examMapList = new ArrayList<>();
        int rowLength = result.length;
        for (int i = 0; i < rowLength; i++) {
            LinkedHashMap<String, Object> answerMap = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, Object> examMap = new LinkedHashMap<String, Object>();

            for (int j = 0; j < result[i].length - 1; j++) {
                String aid = result[i][j];
                if (StringUtils.equalsIgnoreCase(answerKey[j], "ANSWER_ID")) {
                    aid = aid + "-00";
                }
                answerMap.put(answerKey[j], aid);
            }
            //取出试卷Id
            String examKey = (String) answerMap.get("EXAM_ID");
            if (examMapList != null && examMapList.size() > 0) {
                //判断list中是否已经存在该试卷的答案
                Integer exist = 0;
                for (LinkedHashMap<String, Object> map : examMapList) {
                    String exam_id = (String) map.get("EXAM");
                    // FIXME: 什么业务，为什么要用这样的比较逻辑？
                    if (compare(examKey, exam_id) == 0) {
                        exist = 1;
                        List<LinkedHashMap<String, Object>> answerMapList = (List) map.get("ANSWER");
                        answerMapList.add(answerMap);
                        map.put("ANSWER", answerMapList);
                        break;
                    }
                }
                //是一个全新的试卷的答案
                if (exist == 0) {
                    List<LinkedHashMap<String, Object>> answerMapList = new ArrayList<>();
                    answerMapList.add(answerMap);
                    examMap.put("EXAM", examKey);
                    examMap.put("ANSWER", answerMapList);
                    examMapList.add(examMap);
                }
            } else {
                examMapList = new ArrayList<>();
                List<LinkedHashMap<String, Object>> answerMapList = new ArrayList<>();
                answerMapList.add(answerMap);
                examMap.put("EXAM", examKey);
                examMap.put("ANSWER", answerMapList);
                examMapList.add(examMap);
            }
        }
        String jsonStr = JsonUtils.toJsonPretty(examMapList);
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            jsonFile.createNewFile();
        } else {
            jsonFile.delete();
            jsonFile.createNewFile();
        }
        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(jsonFile), "UTF-8");
        BufferedWriter writer = new BufferedWriter(write);
        writer.write(jsonStr);
        writer.close();
    }

    /**
     * Extracted from StringUtil
     * 比较两个字符串大小
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return <li>-1：str1小</li>
     * <li>0：两字符串相等</li>
     * <li>1：str2小</li>
     */
    public static int compare(String str1, String str2) {
        if (str1 == null && str2 == null) return 0;
        if (str1 == null) return -1;
        if (str2 == null) return 1;

        int len1 = str1.length();
        int len2 = str2.length();
        int len = Math.min(len1, len2);
        for (int i = 0; i < len; i++) {
            char ch1 = str1.charAt(i);
            char ch2 = str2.charAt(i);
            if (ch1 == ch2) continue;
            return (ch1 < ch2) ? -1 : 1;
        }

        if (len1 == len2) return 0;
        return (len1 < len2) ? -1 : 1;
    }


    /**
     * 读取Excel的内容，第一维数组存储的是一行中格列的值，二维数组存储的是多少个行
     *
     * @param file       读取数据的源Excel
     * @param ignoreRows 读取数据忽略的行数，比喻行头不需要读入 忽略的行数为1
     * @return 读出的Excel中数据的内容
     * @throws IOException
     */

    public static String[][] getData(File file, int ignoreRows)

            throws FileNotFoundException, IOException {

        List<String[]> result = new ArrayList<String[]>();

        int rowSize = 0;

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(

                file));

        // 打开HSSFWorkbook

        POIFSFileSystem fs = new POIFSFileSystem(in);

        HSSFWorkbook wb = new HSSFWorkbook(fs);

        HSSFCell cell = null;

        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {

            HSSFSheet st = wb.getSheetAt(sheetIndex);

            // 第一行为标题，不取

            for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {

                HSSFRow row = st.getRow(rowIndex);

                if (row == null) {

                    continue;

                }

                int tempRowSize = row.getLastCellNum() + 1;

                if (tempRowSize > rowSize) {

                    rowSize = tempRowSize;

                }

                String[] values = new String[rowSize];

                Arrays.fill(values, "");

                boolean hasValue = false;

                for (short columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {

                    String value = "";

                    cell = row.getCell(columnIndex);

                    if (cell != null) {

                        // 注意：一定要设成这个，否则可能会出现乱码

//                        cell.setEncoding(HSSFCell.ENCODING_UTF_16);

                        switch (cell.getCellType()) {

                            case HSSFCell.CELL_TYPE_STRING:

                                value = cell.getStringCellValue();

                                break;

                            case HSSFCell.CELL_TYPE_NUMERIC:

                                if (HSSFDateUtil.isCellDateFormatted(cell)) {

                                    Date date = cell.getDateCellValue();

                                    if (date != null) {

                                        value = new SimpleDateFormat("yyyy-MM-dd")

                                                .format(date);

                                    } else {

                                        value = "";

                                    }

                                } else {

                                    value = new DecimalFormat("0").format(cell

                                            .getNumericCellValue());

                                }

                                break;

                            case HSSFCell.CELL_TYPE_FORMULA:

                                // 导入时如果为公式生成的数据则无值

                                if (!cell.getStringCellValue().equals("")) {

                                    value = cell.getStringCellValue();

                                } else {

                                    value = cell.getNumericCellValue() + "";

                                }

                                break;

                            case HSSFCell.CELL_TYPE_BLANK:

                                break;

                            case HSSFCell.CELL_TYPE_ERROR:

                                value = "";

                                break;

                            case HSSFCell.CELL_TYPE_BOOLEAN:

                                value = (cell.getBooleanCellValue() == true ? "Y"

                                        : "N");

                                break;

                            default:

                                value = "";

                        }

                    }

                    if (columnIndex == 0 && value.trim().equals("")) {

                        break;

                    }

                    values[columnIndex] = rightTrim(value);

                    hasValue = true;

                }


                if (hasValue) {

                    result.add(values);

                }

            }

        }

        in.close();

        String[][] returnArray = new String[result.size()][rowSize];

        for (int i = 0; i < returnArray.length; i++) {

            returnArray[i] = (String[]) result.get(i);

        }

        return returnArray;

    }


    /**
     * 去掉字符串右边的空格
     *
     * @param str 要处理的字符串
     * @return 处理后的字符串
     */

    public static String rightTrim(String str) {

        if (str == null) {

            return "";

        }

        int length = str.length();

        for (int i = length - 1; i >= 0; i--) {

            if (str.charAt(i) != 0x20) {

                break;

            }

            length--;

        }

        return str.substring(0, length);

    }
}
