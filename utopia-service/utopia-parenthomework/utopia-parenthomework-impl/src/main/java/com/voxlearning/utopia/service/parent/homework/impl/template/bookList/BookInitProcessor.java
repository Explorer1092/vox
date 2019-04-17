package com.voxlearning.utopia.service.parent.homework.impl.template.bookList;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import javax.inject.Named;

/**
 * 获取教材流程初始化
 * @author chongfeng.qi
 * @data 20190110
 */
@Named
public class BookInitProcessor implements HomeworkProcessor {

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        Long studentId = homeworkParam.getStudentId();
        if (studentId == null) {
            hc.setMapMessage(MapMessage.errorMessage("学生id为空"));
            return;
        }
        Integer clazzLevel = ObjectUtils.get(() -> SafeConverter.toInt(homeworkParam.getData().get("clazzLevel")));
        if (clazzLevel == null || clazzLevel == 0 || clazzLevel > 6) {
            hc.setMapMessage(MapMessage.errorMessage("该功能对小学生开放"));
            return;
        }
        if (StringUtils.isBlank(homeworkParam.getBizType())) {
            homeworkParam.setBizType(ObjectiveConfigType.EXAM.name());
        }
    }
}
