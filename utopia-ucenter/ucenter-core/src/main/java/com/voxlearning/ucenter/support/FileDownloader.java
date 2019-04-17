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

package com.voxlearning.ucenter.support;

import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 临时实现类，用于下载文件。
 *
 * @author Xiaohai Zhang
 * @version 0.1
 * @since 13-1-23
 */
abstract public class FileDownloader {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloader.class);

    private FileDownloader() {
    }

    public static void downloadText(String filename, byte[] content, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        filename = UtopiaHttpRequestContext.attachmentFilenameEncoding(filename, request);
        response.reset();
        response.addHeader("Content-Disposition", "attachment;filename=" + filename);
        response.setContentType("application/octet-stream");

        Writer writer = null;
        try {
            String contentString = new String(content, "UTF-8");
            writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
            writer.write(contentString);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            logger.error("Download text file failed", ex);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
