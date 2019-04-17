package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkDoService;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkDoTemplate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 作业服务实现：主要提供做作业功能
 *
 * @author Wenlong Meng
 * @version 20181111
 * @since 2018-11-21
 */
@Named
@ExposeService(interfaceClass = HomeworkDoService.class)
@Slf4j
public class HomeworkDoServiceImpl extends SpringContainerSupport implements HomeworkDoService {

    //local variables
    @Inject private HomeworkDoTemplate homeworkDoTemplate;

    /**
     * 做作业
     *
     * @param param
     * @return
     */
    @Override
    public MapMessage dos(HomeworkParam param) {
        try{
            String command = param.getCommand();
            switch (command){
                case "index":
                    return homeworkDoTemplate.index(param);
                case "do":
                    return homeworkDoTemplate.od(param);
                case "questions":
                    return homeworkDoTemplate.questions(param);
                case "answers":
                    return homeworkDoTemplate.answers(param);
                case "submit":
                    return homeworkDoTemplate.submit(param);
                default:
                    return MapMessage.successMessage().setInfo("command[" + command + "] isn't supported");
            }
        }catch (Exception e){
            log.error("dos:{}", JsonUtils.toJson(param),e);
            return MapMessage.errorMessage();
        }

    }
}