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

package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by Shuai Huan on 2014/9/16.
 */
public class ZipUploader {

    /**
     * 解压文件，zip文件中含有index.html/index.htm，直接返回该名字，该方法用于解压
     *
     * @param fromFileInputStream: 文件输入流
     * @param toFileFolder         解压后存放的文件夹
     */
    public static String uploadAndUnzip(InputStream fromFileInputStream, File toFileFolder) {
        /**
         * 由fromFileInputStream构建输入文件
         * */
        Date date = new Date();
        FastDateFormat formatter = FastDateFormat.getInstance("yyyyMMddHHmmss");
        String timesuffix = formatter.format(date);
        String zipFileName = "zipfile" + timesuffix + ".zip";
        File zipInputFile = new File(toFileFolder.toString() + File.separatorChar + zipFileName);
        try {
            OutputStream os = new FileOutputStream(zipInputFile);
            int bytesRead;
            byte[] buffer = new byte[1024 * 8];
            while ((bytesRead = fromFileInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fromFileInputStream.close();
        } catch (IOException e) {
        }

        String returnStr = "";
        try {
            ZipFile zipfile = new ZipFile(zipInputFile);
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipInputFile));
            ZipEntry zipEntry;
            int i = 0;
            //存储第一次解压的文件，考虑处理解压后仍有一层文件夹的情况
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                i++;
                String currFileName = zipEntry.getName();
                if (i == 1) {
                    returnStr = zipEntry.getName();
                }
                if (currFileName.equalsIgnoreCase("index.html") || currFileName.equalsIgnoreCase("index.htm")) {
                    returnStr += currFileName;
                }
                File temp = new File(toFileFolder, currFileName);
                if (!temp.getParentFile().exists()) {
                    temp.getParentFile().mkdirs();
                }
                //不对文件夹进行操作，否则文件夹会变成0kb的文件
                if (zipEntry.isDirectory()) {
                    continue;
                }
                OutputStream os = new FileOutputStream(temp);

                // 通过ZipFile的getInputStream方法拿到具体的ZipEntry的输入流
                InputStream is = zipfile.getInputStream(zipEntry);
                int len;
                while ((len = is.read()) != -1)
                    os.write(len);
                os.close();
                is.close();
            }
            zipfile.close();
            zipInputStream.close();
            zipInputFile.delete();//删除上传的zip文件
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnStr;
    }
}
