package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.user.api.UserIntegralService;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/25
 */
@Named
public class CH_IncreaseIntegral extends SpringContainerSupport implements CheckHomeworkTask {
    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    @Override
    public void execute(CheckHomeworkContext context) {
//        CheckHomeworkIntegralDetail detail = context.getDetail();
//        if (detail == null || detail.getTeacherIntegral() <= 0) return;

        // 在正常时间内完成作业的学生数量
//        NewAccomplishment accomplishment = context.getAccomplishment();
//        int count = (int) accomplishment.getDetails().values().stream().filter(d -> !d.isRepairTrue()).count();


//        String integralComment = StringUtils.formatMessage("共{}个{}的学生完成您本周第{}次{}作业，您获得园丁豆",
//                count, context.getClazz().formalizeClazzName(), context.getWeekCheckTime() + 1, context.getHomework().getSubject().getValue());
//        Integer teacherIntegral =  detail.getTeacherIntegral();
//        IntegralHistory integralHistory = new IntegralHistory(context.getTeacherId(),
//                IntegralType.每个学生完成作业老师获得积分, teacherIntegral);
//        integralHistory.setComment(integralComment);
//        integralHistory.setHomeworkUniqueKey(context.getHomeworkType().name(), context.getHomeworkId());
//        integralHistory.setRelationClassId(context.getGroupId());

//        userIntegralService.changeIntegral(integralHistory);
    }
}
