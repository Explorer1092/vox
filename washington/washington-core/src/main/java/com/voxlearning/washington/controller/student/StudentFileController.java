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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.upload.OSSManageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/student/file/")
public class StudentFileController extends AbstractController {
    /**
     * 学生端上传文件
     */
    @RequestMapping(value = "upload.vpage", method = RequestMethod.POST)
    public void uploadResultFromPC(MultipartFile filedata) throws IOException {
        String result;

        User user = currentUser();
        if (user == null) {
            result = JsonUtils.toJson(MapMessage.errorMessage("请重新登录"));
            getResponse().getWriter().print(result);
            return;
        }
        if (!UserType.STUDENT.equals(user.fetchUserType())) {
            result = JsonUtils.toJson(MapMessage.errorMessage("请用学生账号登录"));
            getResponse().getWriter().print(result);
            return;
        }

        if (filedata == null || filedata.isEmpty()) {
            result = JsonUtils.toJson(MapMessage.errorMessage("没有文件"));
            getResponse().getWriter().print(result);
            return;
        }

        /** 上传文件 begin **/
        String fileName;
        try {
            fileName = atomicLockManager.wrapAtomic(this)
                    .keys(user.getId())
                    .proxy()
                    .doUploadResult(user.getId(), filedata);
        } catch (DuplicatedOperationException ex) {
            logger.warn("上传失败！请不要重复提交");
            result = JsonUtils.toJson(MapMessage.errorMessage("上传失败！请不要重复提交"));
            getResponse().getWriter().print(result);
            return;
        } catch (Exception ex) {
            logger.warn("上传失败", ex);
            result = JsonUtils.toJson(MapMessage.errorMessage("上传失败"));
            getResponse().getWriter().print(result);
            return;
        }

        result = JsonUtils.toJson(MapMessage.successMessage().add("fileName", fileName));
        getResponse().getWriter().print(result);
    }

    public String doUploadResult(Long userId, MultipartFile filedata) {
        return OSSManageUtils.upload(filedata);
    }
}
