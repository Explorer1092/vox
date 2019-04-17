/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

// $Id: ClazzController.java 16268 2013-01-23 15:29:05Z xinqiang.wang $


package com.voxlearning.washington.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.data.DownloadContent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.FileDownloader;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Clazz controller implementation.
 *
 * @author Jingwei Dong
 * @author Guohong Tan
 * @author Yaoheng Wu
 * @author Rui Bao
 * @author Xinqiang Wang
 * @author Xiaohai Zhang
 * @since 2011-08-05
 */
@Controller
@RequestMapping("/clazz")
@Slf4j
@NoArgsConstructor
public class ClazzController extends AbstractController {

    @RequestMapping(value = "fetchaccount.vpage", method = RequestMethod.GET)
    @ResponseBody
    public void downloadPassword(HttpServletResponse response) {
        User user = currentUser();
        if (user == null) {
            int code = 403;
            String message = "not allowed to get account";
            response.setStatus(code);
            try {
                response.getWriter().write(code + ":" + message);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return;
        }

        MapMessage message = sensitiveUserDataServiceClient.downloadPassword(user);
        if (!message.isSuccess()) {
            logger.error("Failed to download user '{}' password", user.getId());
        } else {
            byte[] content = ((DownloadContent) message.get("password")).getContent();
            String filename = user.fetchRealname() + "的一起作业账号.txt";
            try {
                FileDownloader.downloadText(filename, content, getRequest(), response);
            } catch (Exception ex) {
                logger.error("Failed to download user '{}' password, error: {}", user.getId(), ex.getMessage());
            }
        }
    }

    @RequestMapping(value = "downloadletter.vpage", method = RequestMethod.GET)
    public void down(HttpServletRequest request, HttpServletResponse response) {
        try {
            FileDownloader.downloadSpecificFile(request, response, "/public/downloadtemplate/letter_to_parents_17zuoye.doc", "致家长的一封信.doc");
        } catch (IOException ex) {
            log.error("downloadletter Error", ex.getMessage());
        }
    }

}
