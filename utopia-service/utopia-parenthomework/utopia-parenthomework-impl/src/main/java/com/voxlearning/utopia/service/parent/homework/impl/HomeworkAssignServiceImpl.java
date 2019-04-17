package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignService;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SupportType;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkAssignTemplate;
import com.voxlearning.utopia.service.parent.homework.util.SubjectUtils;
import lombok.extern.log4j.Log4j;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * 作业服务实现：主要提供布置作业功能
 *
 * @author Wenlong Meng
 * @since 2018-11-21
 */
@Named
@ExposeService(interfaceClass = HomeworkAssignService.class)
@Log4j
public class HomeworkAssignServiceImpl extends SpringContainerSupport implements HomeworkAssignService {

    //local variables

    /**
     * 布置作业
     *
     * @param homeworkParam 参数
     * @return 成功或错误信息
     */
    @Override
    public MapMessage assignHomework(HomeworkParam homeworkParam) {
        if (homeworkParam == null) {
            return MapMessage.errorMessage();
        }
        Long userId = homeworkParam.getStudentId();
        Long formUserId = homeworkParam.getCurrentUserId();
        String source = homeworkParam.getSource();
        if (ObjectUtils.anyBlank(userId, formUserId, source)) {
            return MapMessage.errorMessage("参数错误");
        }
        String subject = homeworkParam.getSubject();
        if (StringUtils.isBlank(subject)) {
            homeworkParam.setSubject(SubjectUtils.BASIC_SUBJECTS.get(0).name());
        }

        //布置
        return getAssignTemplate(homeworkParam.getBizType()).assign(homeworkParam);
    }

    /**
     * 初始化模板
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        applicationContext.getBeansOfType(HomeworkAssignTemplate.class).values()
            .forEach(p -> {
                SupportType annotation = p.getClass().getAnnotation(SupportType.class);
                if (annotation != null) {
                    processorTemplateMap.put(annotation.bizType(), p);
                }
            }
        );
    }
    private static Map<String, HomeworkAssignTemplate> processorTemplateMap = new HashMap<>();
    private HomeworkAssignTemplate getAssignTemplate(String bizType){
        return processorTemplateMap.getOrDefault(bizType, processorTemplateMap.get("*"));
    }

}