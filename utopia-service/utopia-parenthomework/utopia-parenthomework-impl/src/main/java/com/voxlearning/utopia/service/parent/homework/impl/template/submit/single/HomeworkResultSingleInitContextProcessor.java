package com.voxlearning.utopia.service.parent.homework.impl.template.submit.single;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * 初始化上下文
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
@SubType({
        ObjectiveConfigType.EXAM
})
public class HomeworkResultSingleInitContextProcessor implements HomeworkProcessor {

    //Local variables
    @Inject private HomeworkLoader homeworkLoader;

    //Logic
    /**
     * 初始化上下文
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        if(param.getData()==null){
            LoggerUtils.info("HomeworkResultSingleInitContextProcessor.process.dataIsNull", param);
            hc.setMapMessage(MapMessage.errorMessage("data is null"));
            return;
        }
        String homeworkId = param.getHomeworkId();
        Map<String, Object> data = MapUtils.m("userAnswers", param.getData().get("answer"));
        data.putAll(param.getData());
        HomeworkResult hr = JsonUtils.safeConvertMapToObject(data, HomeworkResult.class);
        HomeworkProcessResult hpr = JsonUtils.safeConvertMapToObject(data, HomeworkProcessResult.class);
        //作业
        Homework homework = homeworkLoader.loadHomework(homeworkId);

        //查询题目信息
        HomeworkPractice homeworkPractice = homeworkLoader.loadHomeworkPractice(homeworkId);

        //发送消息
        hc.setHomework(homework);
        hc.setHomeworkResult(hr);
        hc.setHomeworkProcessResults(Lists.newArrayList(hpr));
        hc.setHomeworkPractice(homeworkPractice);
    }

}
