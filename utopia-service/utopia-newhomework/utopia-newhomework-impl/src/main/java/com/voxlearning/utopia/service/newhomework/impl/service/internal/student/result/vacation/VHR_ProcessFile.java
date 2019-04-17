package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.mapper.StudentHomeworkAnswer;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理主观题目文件信息
 *
 * @author guoqiang.li
 * @since 2017/5/26
 */
@Named
public class VHR_ProcessFile extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Override
    public void execute(VacationHomeworkResultContext context) {
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
