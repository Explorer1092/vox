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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理主观题文件信息
 *
 * @author Ruib
 * @author guohong.tan
 * @author xuesong.zhang
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class HR_ProcessFile extends SpringContainerSupport implements HomeworkResultTask {

    @Override
    public void execute(HomeworkResultContext context) {

        Map<String, List<List<NewHomeworkQuestionFile>>> questionFiles = new HashMap<>();
        if (CollectionUtils.isNotEmpty(context.getStudentHomeworkAnswers())) {
            for (StudentHomeworkAnswer studentHomeworkAnswer : context.getStudentHomeworkAnswers()) {
                if (CollectionUtils.isEmpty(studentHomeworkAnswer.getFileUrls())) continue;
                List<List<NewHomeworkQuestionFile>> filesList = new ArrayList<>();
                for (List<String> urls : studentHomeworkAnswer.getFileUrls()) {
                    List<NewHomeworkQuestionFile> files = new ArrayList<>();
                    for (String url : urls) {
                        NewHomeworkQuestionFile file = NewHomeworkQuestionFileHelper.newInstance(url);
                        files.add(file);
                    }
                    filesList.add(files);
                }
                questionFiles.put(studentHomeworkAnswer.getQuestionId(), filesList);
            }
        }
        context.setFiles(questionFiles);
    }
}
