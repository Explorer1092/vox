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

package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;

import java.util.Arrays;
import java.util.Map;

import static com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile.FileType.*;
import static com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile.StorageSource.ALIYUN;

public class NewHomeworkQuestionFileHelper {

    public static final String OSS_IMAGE_HOST;
    public static final String OSS_HOST;

    static {
        Map<String, String> configs = ConfigManager.instance().getCommonConfig().getConfigs();
        OSS_IMAGE_HOST = StringUtils.defaultString(configs.get("oss_homework_image_host"));
        OSS_HOST = StringUtils.defaultString(configs.get("oss_homework_host"));
    }

    public static NewHomeworkQuestionFile newInstance(String fileUrl) {
        NewHomeworkQuestionFile inst = new NewHomeworkQuestionFile();
        inst.setFileName(fileUrl.substring(fileUrl.lastIndexOf("/") + 1, fileUrl.lastIndexOf(".")));
        inst.setSource(loadSource(fileUrl));
        inst.setFileType(loadFileType(fileUrl));
        inst.setRelativeUrl(StringUtils.removeStartIgnoreCase(StringUtils.removeStartIgnoreCase(fileUrl, OSS_IMAGE_HOST), OSS_HOST));
        return inst;
    }

    // 文件地址 http://image.oss.17zuoye.com/2016/01/16/20160116155033512486.jpg
    public static String getFileUrl(NewHomeworkQuestionFile inst) {
        if (inst.getFileType() == IMAGE) {
            return OSS_IMAGE_HOST + inst.getRelativeUrl();
        } else {
            return OSS_HOST + inst.getRelativeUrl();
        }
    }

    /**
     * 根据完整url判断文件类型
     *
     * @param url 完整的url，例：http://image.oss.17zuoye.com/2016/01/16/20160116155033512486.jpg
     * @return 枚举
     */
    public static NewHomeworkQuestionFile.FileType loadFileType(String url) {
        String suffix = StringUtils.substringAfterLast(url, ".");
        suffix = StringUtils.lowerCase(StringUtils.trim(suffix));
        if (Arrays.asList("jpg", "jpeg", "gif", "png", "tif", "bmp").contains(suffix)) {
            return IMAGE;
        } else if (Arrays.asList("mp3", "pcm", "lpcm").contains(suffix)) {
            return AUDIO;
        }
        return UNKNOWN;
    }

    /**
     * 根据完整url判断文件类型
     *
     * @param url 完整的url，例：http://image.oss.17zuoye.com/2016/01/16/20160116155033512486.jpg
     * @return 枚举
     */
    public static NewHomeworkQuestionFile.StorageSource loadSource(String url) {
        url = (url == null ? "" : url).trim().toLowerCase();
        if (url.contains(OSS_IMAGE_HOST) || url.contains(OSS_HOST)) {
            return ALIYUN;
        }
        return NewHomeworkQuestionFile.StorageSource.UNKNOWN;
    }
}
