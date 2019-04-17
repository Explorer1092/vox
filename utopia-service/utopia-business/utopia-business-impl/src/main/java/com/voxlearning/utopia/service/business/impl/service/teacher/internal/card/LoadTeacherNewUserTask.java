package com.voxlearning.utopia.service.business.impl.service.teacher.internal.card;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.business.api.constant.TeacherCardType;
import com.voxlearning.utopia.mapper.TeacherCardMapper;
import com.voxlearning.utopia.service.business.impl.service.BusinessTeacherServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.TeacherNewUserTaskMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tanguohong on 2017/4/17.
 */
@Named
public class LoadTeacherNewUserTask extends AbstractTeacherCardDataLoader {

    @Inject
    private BusinessTeacherServiceImpl businessTeacherService;

    @Override
    protected TeacherCardDataContext doProcess(TeacherCardDataContext context) {
        if (context.getTeacher().fetchCertificationState() == AuthenticationState.SUCCESS) {
            return context;
        }
        TeacherNewUserTaskMapper teacherNewUserTaskMapper = businessTeacherService.getTeacherNewUserTaskMapper(context.getTeacher());
        int i = 0;
        if (teacherNewUserTaskMapper.isNameSetted() && teacherNewUserTaskMapper.isMobileAuthenticated()) {
            i++;
        }
        if (teacherNewUserTaskMapper.isEnoughStudentsBindParentMobile()) {
            i++;
        }
        if (teacherNewUserTaskMapper.isEnoughStudentsFinishedHomework()) {
            i++;
        }
        TeacherCardMapper newUserTaskMapper = new TeacherCardMapper();
        newUserTaskMapper.setCardType(TeacherCardType.AUTHENTICATION);
        newUserTaskMapper.setCardName("认证专享更多福利");
        newUserTaskMapper.setCardDescription("认证专享更多福利");
        newUserTaskMapper.setProgress("进度:" + i + "/3");
        newUserTaskMapper.setImgUrl("resources/app/17teacher/res/certificate_banner.png");
        newUserTaskMapper.setDetailUrl("/view/tasks/certification");
        newUserTaskMapper.setCardDetails(JsonUtils.toJson(generateCardDetails(teacherNewUserTaskMapper, context.getTeacher())));
        newUserTaskMapper.setTeacherId(context.getTeacher().getId());
        newUserTaskMapper.setSubject(context.getTeacher().getSubject());
        context.taskCards.add(newUserTaskMapper);
        return context;
    }

    private Map<String, Object> generateCardDetails(TeacherNewUserTaskMapper teacherNewUserTaskMapper, Teacher teacher) {
        Map<String, Object> cardDetails = new LinkedHashMap<>();
        cardDetails.put("subject", teacher.getSubject());
        cardDetails.put("name", teacherNewUserTaskMapper.getName());
        cardDetails.put("mobile", teacherNewUserTaskMapper.getMobile());
        cardDetails.put("bindMobileCount", teacherNewUserTaskMapper.getBindMobileCount());
        cardDetails.put("finishThreePlusCount", teacherNewUserTaskMapper.getFinishThreePlusCount());
        return cardDetails;
    }
}
