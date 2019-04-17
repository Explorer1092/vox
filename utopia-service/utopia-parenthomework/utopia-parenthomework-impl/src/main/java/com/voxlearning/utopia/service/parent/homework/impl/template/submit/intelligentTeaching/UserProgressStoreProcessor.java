package com.voxlearning.utopia.service.parent.homework.impl.template.submit.intelligentTeaching;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserProgress;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SupportType;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 保存用户进度
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Named("IntelliagentTeaching.UserProgressProcessor")
@SupportType(bizType = "INTELLIGENT_TEACHING", op="submit")
public class UserProgressStoreProcessor implements HomeworkProcessor {

    @Inject
    private HomeworkUserProgressService homeworkUserProgressService;

    //Logic

    /**
     * exec
     * @param hc args
     */
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        HomeworkResult homeworkResult = hc.getHomeworkResult();
        if(hc.getMapMessage() != null || !homeworkResult.getBizType().equals("INTELLIGENT_TEACHING")){
            return;
        }
        //已完成
        if(ObjectUtils.get(()->homeworkResult.getFinished(), Boolean.FALSE)){
            String bookId = (String)homeworkResult.getAdditions().get("bookId");
            String unitId = (String)homeworkResult.getAdditions().get("unitId");
            UserProgress userProgress = new UserProgress();
            userProgress.setBookId(bookId);
            userProgress.setUnitId(unitId);
            userProgress.setCourse("*");
            userProgress.setSectionId("*");
            userProgress.setExtInfo(MapUtils.m("homeworkId", homeworkResult.getHomeworkId()));
            homeworkUserProgressService.save(param.getStudentId(), homeworkResult.getBizType(), userProgress);
        }
    }
}
