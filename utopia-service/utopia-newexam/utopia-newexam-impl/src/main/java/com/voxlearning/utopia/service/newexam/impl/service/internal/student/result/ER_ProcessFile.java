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

package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamQuestionFile;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.newexam.api.entity.NewExamQuestionFile.FileType.*;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Named
public class ER_ProcessFile extends SpringContainerSupport implements NewExamResultTask {

    public static final String OSS_IMAGE_HOST;
    public static final String OSS_HOST;

    static {
        Map<String, String> configs = ConfigManager.instance().getCommonConfig().getConfigs();
        OSS_IMAGE_HOST = StringUtils.defaultString(configs.get("oss_homework_image_host"));
        OSS_HOST = StringUtils.defaultString(configs.get("oss_homework_host"));
    }

    @Override
    public void execute(NewExamResultContext context) {
        if (CollectionUtils.isEmpty(context.getFileUrls())) return;

        for (List<String> urls : context.getFileUrls()) {
            List<NewExamQuestionFile> files = new ArrayList<>();
            for (String url : urls) {
                NewExamQuestionFile file = createNewExamQuestionFile(url);
                files.add(file);
            }
            context.getFiles().add(files);
        }
    }

    public NewExamQuestionFile createNewExamQuestionFile(String fileUrl) {
        NewExamQuestionFile result = new NewExamQuestionFile();
        result.setFileName(fileUrl.substring(fileUrl.lastIndexOf("/") + 1, fileUrl.lastIndexOf(".")));
        result.setSource(NewExamQuestionFile.StorageSource.loadSource(fileUrl));
        result.setFileType(loadFileType(fileUrl));
        result.setRelativeUrl(StringUtils.removeStartIgnoreCase(StringUtils.removeStartIgnoreCase(fileUrl, OSS_IMAGE_HOST), OSS_HOST));
        return result;
    }

    public static NewExamQuestionFile.FileType loadFileType(String url) {
        String suffix = StringUtils.substringAfterLast(url, ".");
        suffix = StringUtils.lowerCase(StringUtils.trim(suffix));
        if (Arrays.asList("jpg", "jpeg", "gif", "png", "tif", "bmp").contains(suffix)) {
            return IMAGE;
        } else if (Arrays.asList("mp3", "pcm", "lpcm").contains(suffix)) {
            return AUDIO;
        }
        return UNKNOWN;
    }
}
