package com.voxlearning.utopia.service.crm.impl.service.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.entity.crm.constants.SchoolOperationType;
import com.voxlearning.utopia.entity.crm.schoolrecord.SchoolServiceRecord;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmSchoolExtInfoCheckService;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceRecordServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.ClazzServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by yaguang.wang
 * on 2017/10/18.
 */

@Named
@Service(interfaceClass = CrmSchoolExtInfoCheckService.class)
@ExposeService(interfaceClass = CrmSchoolExtInfoCheckService.class)
public class CrmSchoolExtInfoCheckServiceImpl implements CrmSchoolExtInfoCheckService {

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private SchoolServiceRecordServiceClient schoolServiceRecordService;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public MapMessage beforeUpdateSchoolExtInfoEduSystem(Long schoolId, EduSystemType eduSystem) {
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("无效的学校信息").setErrorCode("002");
        }
        if (eduSystem == null) {
            return MapMessage.errorMessage("学制不能为空").setErrorCode("005");
        }
        String oldEduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
//        if (StringUtils.equals(eduSystem.name(), oldEduSystem)) {
//            return MapMessage.successMessage();
//        }
        MapMessage result = new MapMessage();
        result.add("needUpdate", true);
        result.add("school", school);
        result.add("oldEduSystem", oldEduSystem);
        //Set<Long> activeGroup;
        if (school.isPrimarySchool()) {
            if (eduSystem != EduSystemType.P6 && eduSystem != EduSystemType.P5) {
                return MapMessage.errorMessage("不支持修改成此学制").setErrorCode("003");
            }
            // 如果是小学六年制改五年制, 需要检查六年级有没有活跃老师
            if (StringUtils.equals(EduSystemType.P6.name(), oldEduSystem) && EduSystemType.P5 == eduSystem) {
                //activeGroup = findActiveGroup(schoolId, ClazzLevel.SIXTH_GRADE);
                //result.add("activeGroup", activeGroup);
                if (findActiveGroup(schoolId, ClazzLevel.SIXTH_GRADE)) {
                    result.add("needEmail", true);
                    return result.setSuccess(false).setInfo("学校还存在六年级活跃的老师，修改后这些老师的六年级班级将不可见，确认修改吗？").setErrorCode("004");
                }
            }
        } else if (school.isMiddleSchool()) {
            if (eduSystem != EduSystemType.J3 && eduSystem != EduSystemType.J4) {
                return MapMessage.errorMessage("不支持修改成此学制").setErrorCode("003");
            }
            // 如果是初中四年制改三年制, 需要检查六年级有没有活跃老师
            if (StringUtils.equals(EduSystemType.J4.name(), oldEduSystem) && EduSystemType.J3 == eduSystem) {
                //activeGroup = ;
                //result.add("activeGroup", activeGroup);
                if (findActiveGroup(schoolId, ClazzLevel.SIXTH_GRADE)) {
                    result.add("needEmail", true);
                    return result.setSuccess(false).setInfo("学校还存在六年级活跃的老师，修改后这些老师的六年级班级将不可见，确认修改吗？").setErrorCode("004");
                }
            }
        } else if (school.isSeniorSchool()) {
            if (eduSystem != EduSystemType.S3 && eduSystem != EduSystemType.S4) {
                return MapMessage.errorMessage("不支持修改成此学制").setErrorCode("003");
            }
            // 如果是高中四年制改三年制, 需要检查十年级有没有活跃老师
            if (StringUtils.equals(EduSystemType.S4.name(), oldEduSystem) && EduSystemType.S3 == eduSystem) {
                //activeGroup = ;
                //result.add("activeGroup", activeGroup);
                if (findActiveGroup(schoolId, ClazzLevel.NINTH_GRADE)) {
                    result.add("needEmail", true);
                    return result.setSuccess(false).setInfo("学校还存在活跃的老师，修改后这些老师的班级将不可见，确认修改吗？").setErrorCode("004");
                }
            }
        } else {
            return MapMessage.errorMessage("不支持的学制").setErrorCode("003");
        }
        return result.setSuccess(true);
    }

    @Override
    public MapMessage updateSchoolExtInfoEduSystem(Long schoolId, EduSystemType eduSystem, String desc, String modifier) {
        MapMessage msg = beforeUpdateSchoolExtInfoEduSystem(schoolId, eduSystem);
        if (msg.get("needUpdate") == null) {
            return msg;
        }
        String oldEduSystem = (String) msg.get("oldEduSystem");
        // 先改学校的学制
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();

        if (schoolExtInfo == null) {
            schoolExtInfo = new SchoolExtInfo();
            schoolExtInfo.setId(schoolId);
        }
        schoolExtInfo.setEduSystem(eduSystem.name());
        schoolExtInfo.setSchoolSize(schoolExtInfo.getSchoolSize());
        schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfo(schoolExtInfo)
                .getUninterruptibly();

        return afterUpdateSchoolExtInfoEduSystem(schoolId, oldEduSystem, eduSystem, desc, modifier, msg.get("needEMail") != null);
    }

    @Override
    public MapMessage afterUpdateSchoolExtInfoEduSystem(Long schoolId, String oldEduSystem, EduSystemType newEduSystem, String desc, String modifier, Boolean needEMail) {
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("无效的学校信息").setErrorCode("002");
        }
        // 然后更新这个学校下面的所有的班级的学制
        updateSchoolClazzByEdySystem(schoolId, newEduSystem);
        // 发邮件通知
        if (needEMail) {
            sendModifySchoolEduSystemNotify(school, oldEduSystem, newEduSystem, desc, modifier);
        }

        EduSystemType oldEdu = EduSystemType.of(oldEduSystem);

        SchoolServiceRecord ssr = new SchoolServiceRecord();
        ssr.setSchoolId(schoolId);
        ssr.setSchoolName(school.getShortName());
        ssr.setOperatorId(modifier);
        ssr.setSchoolOperationType(SchoolOperationType.UPDATE_SCHOOL_INFO);
        ssr.setOperationContent(String.format("学制修改，原学制：%s，修改后为：%sf，描述：%s",
                oldEdu == null ? "" : oldEdu.getDescription(),
                newEduSystem.getDescription(), desc));

        schoolServiceRecordService.addSchoolServiceRecord(ssr);

        return MapMessage.successMessage();
    }


    private void sendModifySchoolEduSystemNotify(School school,
                                                 String oldEduSystem,
                                                 EduSystemType eduSystem,
                                                 String desc,
                                                 //Set<Long> activeGroup,
                                                 String modifier) {
        String subject = "学校" + school.getShortName() + "(" + school.getId() + ")" + "【学制修改】执行结果(来自：" + RuntimeMode.getCurrentStage() + "环境)";
        List<String> schoolInfo = Arrays.asList(
                "学校ID ： " + school.getId(),
                "学校名称 ： " + school.loadSchoolFullName(),
                "学校级别 ： " + SchoolLevel.safeParse(school.getLevel()).getDescription(),
                "修改前学校学制 ： " + (EduSystemType.of(oldEduSystem) == null ? "未知" : EduSystemType.of(oldEduSystem).getDescription()),
                "修改后学校学制 ： " + eduSystem.getDescription(),
                //"活跃班组ID ： " + (CollectionUtils.isEmpty(activeGroup) ? "无" : JsonUtils.toJson(activeGroup)),
                "修改人 ： " + modifier,
                "修改时间 ： " + DateUtils.dateToString(new Date()),
                "修改原因 ： " + desc
        );
        try {
            List<String> mailList = Stream.of(commonConfigServiceClient.getCommonConfigBuffer()
                    .loadCommonConfigValue(ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "school_change_edusystem_notify")
                    .split(","))
                    .filter(StringUtils::isNotBlank)
                    .map(addr -> addr + "@17zuoye.com")
                    .collect(Collectors.toList());
            emailServiceClient.createPlainEmail()
                    .to(StringUtils.join(mailList, ";"))
                    .subject(subject)
                    .body(StringUtils.join(schoolInfo, "\r\n"))
                    .send();
        } catch (Exception ignore) {
        }
    }


    private void updateSchoolClazzByEdySystem(Long schoolId, EduSystemType eduSystem) {
        raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .toList()
                .stream()
                .map(Clazz::getId)
                .collect(Collectors.toSet())
                .forEach(clazzId -> clazzServiceClient.updateClazzEduSystem(clazzId, eduSystem));
    }

    private boolean findActiveGroup(Long schoolId, ClazzLevel clazzLevel) {
        // 学校下所有该年级的班级
        Set<Long> clazzIds = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .clazzLevel(clazzLevel)
                .toList()
                .stream()
                .map(Clazz::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(clazzIds)) {
            return false;
        }

        // 这些班级下的所有班组
        Set<Long> groupIds = deprecatedGroupLoaderClient.loadClazzGroups(clazzIds)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(GroupMapper::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(groupIds)) {
            return false;
        }
        long timeMills = DateUtils.addDays(new Date(), -30).getTime();
        Set<Long> unActiveStudentIds = new HashSet<>();
        for (Long groupId : groupIds) {
            Map<Long, List<Long>> groupStudents = studentLoaderClient.loadGroupStudentIds(Collections.singleton(groupId));
            if (MapUtils.isEmpty(groupStudents) || !groupStudents.containsKey(groupId)) {
                continue;
            }
            List<Long> studentList = groupStudents.get(groupId);
            if (CollectionUtils.isEmpty(studentList)) {
                continue;
            }
            int activeStudentCount = 0;
            for (Long studentId : studentList) {
                if (!unActiveStudentIds.contains(studentId) && isActiveStudent(studentId, timeMills)) {
                    activeStudentCount++;
                } else {
                    unActiveStudentIds.add(studentId);
                }
                if (activeStudentCount >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isActiveStudent(Long studentId, long timeMills) {
        return userActivityServiceClient.getUserActivityService()
                .findUserActivities(studentId)
                .getUninterruptibly()
                .stream()
                .filter(activity -> UserActivityType.LAST_HOMEWORK_TIME == activity.getActivityType())
                .anyMatch(activity -> {
                    Date activityTime = activity.getActivityTime();
                    return activityTime != null && activityTime.getTime() >= timeMills;
                });
    }
}
