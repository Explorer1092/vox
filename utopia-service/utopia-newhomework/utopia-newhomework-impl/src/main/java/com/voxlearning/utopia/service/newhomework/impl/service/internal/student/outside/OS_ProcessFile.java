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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author majianxin
 */
@Named
public class OS_ProcessFile extends SpringContainerSupport implements OutsideReadingResultTask {

    @Override
    public void execute(OutsideReadingContext context) {

        StudentHomeworkAnswer homeworkAnswer = context.getStudentHomeworkAnswer();
        if (CollectionUtils.isEmpty(homeworkAnswer.getFileUrls())) {
            return;
        }
        List<List<NewHomeworkQuestionFile>> filesList = new ArrayList<>();
        for (List<String> urls : homeworkAnswer.getFileUrls()) {
            List<NewHomeworkQuestionFile> files = new ArrayList<>();
            for (String url : urls) {
                NewHomeworkQuestionFile file = NewHomeworkQuestionFileHelper.newInstance(url);
                files.add(file);
            }
            filesList.add(files);
        }

        Map<String, List<List<NewHomeworkQuestionFile>>> questionFiles = new HashMap<>();
        questionFiles.put(homeworkAnswer.getQuestionId(), filesList);
        context.setFiles(questionFiles);
    }
}
