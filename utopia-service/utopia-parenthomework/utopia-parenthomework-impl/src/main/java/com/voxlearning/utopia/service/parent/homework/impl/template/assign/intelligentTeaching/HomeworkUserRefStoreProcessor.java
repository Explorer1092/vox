package com.voxlearning.utopia.service.parent.homework.impl.template.assign.intelligentTeaching;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserRef;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserRefDao;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 保存作业和用户的关系
 * @author chongfeng.qi
 * @date 20181120
 */
@Named("IntelliagentTeaching.HomeworkUserRefStoreProcessor")
public class HomeworkUserRefStoreProcessor implements HomeworkProcessor {

    @Inject
    private HomeworkUserRefDao homeworkUserRefDao;

    @Override
    public void process(HomeworkContext hc) {
        Homework homework = hc.getHomework();
        HomeworkParam param = hc.getHomeworkParam();
        Long studentId = param.getStudentId();
        boolean assigned = homeworkUserRefDao.loadHomeworkUserRef(studentId).stream().anyMatch(u->StringUtils.equals(u.getHomeworkId(), homework.getId()));
        if(assigned){
            hc.setMapMessage(MapMessage.successMessage());
            return;
        }
        HomeworkUserRef homeworkUserRef = new HomeworkUserRef();
        homeworkUserRef.setHomeworkId(homework.getId());
        homeworkUserRef.setUserId(studentId);
        homeworkUserRef.setId(HomeworkUtil.generatorID(homework.getId(), studentId));
        homeworkUserRefDao.insert(homeworkUserRef);
    }
}
