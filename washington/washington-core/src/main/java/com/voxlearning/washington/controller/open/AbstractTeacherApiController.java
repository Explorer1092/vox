/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.base.gray.TeacherGrayFunctionManager;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Abstract controller class for Teacher Open Api
 * Created by Shuai Huan on 2014/12/29.
 */
public class AbstractTeacherApiController extends AbstractApiController {
    @Inject protected GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject protected CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject protected CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject protected BadWordCheckerClient badWordCheckerClient;

    public static final String juniorAuthRewardUrl = ProductConfig.getJuniorSchoolUrl() + "/mteacher/page/invite";

    protected static final String flowerUrl = "/view/mobile/teacher/flower_exchange/list";
    protected final static String integralUrl = "/view/mobile/common/integral_history";

    protected List<String> vocationMonthList;

    public AbstractTeacherApiController() {
        super();
        vocationMonthList = Arrays.asList("01", "02", "07", "08");
    }

    public void validateRequest(String... paramKeys) {
        super.validateRequest(paramKeys);

        User curUser = getApiRequestUser();
        // 验证用户身份
        if (curUser.fetchUserType() != UserType.TEACHER) {
            throw new IllegalArgumentException(RES_RESULT_USER_TYPE_ERROR_MSG);
        }
    }

    public TeacherDetail getCurrentTeacher() {
        User curUser = getApiRequestUser();
        TeacherDetail teacher;
        if (curUser instanceof TeacherDetail) {
            teacher = (TeacherDetail) curUser;
        } else {
            teacher = teacherLoaderClient.loadTeacherDetail(curUser.getId());
        }
        return teacher;
    }

    public Boolean validateGroupStudent(Long groupId, Long studentId) {
        Boolean studentRight = false;
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, true);
        List<GroupMapper.GroupUser> studentList = groupMapper.getStudents();
        for (GroupMapper.GroupUser student : studentList) {
            if (student.getId().equals(studentId)) {
                studentRight = true;
                break;
            }
        }
        return studentRight;
    }

    protected Boolean juniorAuthRewardGrey(TeacherDetail teacher) {
        TeacherGrayFunctionManager manager = grayFunctionManagerClient.getTeacherGrayFunctionManager();
        //灰度地区
        if (!manager.isWebGrayFunctionAvailable(teacher, "17Teacher", "authreward"))
            return false;
        //改成 分别匹配对应学科的灰度配置
        if (!manager.isWebGrayFunctionAvailable(teacher, "JMS", "Invitation" + captureName(teacher.getSubject().name().toLowerCase())))
            return false;
        //指定学校不显示
        if (teacher.getTeacherSchoolId().equals(32888L))
            return false;
        //坑,看方法注释
        if (!checkTeacherClazz(teacher))
            return false;
        //没认证不显示
        if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) return false;
        //认证时间为null不显示
        if (teacher.getLastAuthDate() == null) return false;
        //认证时间2016-11-10 00:00:00之后不显示
        if (teacher.getLastAuthDate().after(DateUtils.stringToDate("2016-11-10 00:00:00"))) return false;
        //认证超过30天不成
        if (DateUtils.calculateDateDay(teacher.getLastAuthDate(), 30).before(new Date())) return false;
        return true;
    }

    protected MapMessage reLoginResult(){
        MapMessage mapMessage = new MapMessage();
        mapMessage.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
        mapMessage.add(RES_MESSAGE, "用户信息有变，请重新登录！");
        return mapMessage;
    }

    //首字母大写....
    protected static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }

    protected final static Set<Long> juniorTeacherWhiteNameList = new HashSet<>();

    static {
        juniorTeacherWhiteNameList.add(12373128L);
    }

    /**
     * 老师名下没有班级不行
     * 老师教的班级有9年级的,并且不在白名单里的老师,不行
     *
     * @param teacherDetail
     * @return
     */
    protected boolean checkTeacherClazz(TeacherDetail teacherDetail) {
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherDetail.getId());
        if (CollectionUtils.isEmpty(clazzs))
            return false;
        //redmine http://project.17zuoye.net/redmine/issues/33509
//        if (clazzs.stream().anyMatch(t -> t.getClazzLevel().equals(ClazzLevel.NINTH_GRADE)) && !juniorTeacherWhiteNameList.contains(teacherDetail.getId()))
//            return false;
        return true;
    }

    protected String generateShareClazzUrl(Teacher teacher) {
        if (teacher == null)
            return null;
        String encodeTeacherName;
        String encodeSubject = "";
        try {
            encodeTeacherName = URLEncoder.encode(teacher.fetchRealname(), "utf-8");
            if (teacher.getSubject() != null) {
                encodeSubject = URLEncoder.encode(teacher.getSubject().getValue(), "utf-8");
            } else
                logger.info("generateShareClazzUrl teacher's subject is null ,teacher id = " + teacher.getId());
        } catch (UnsupportedEncodingException e) {
            encodeTeacherName = "";
            encodeSubject = "";
        }

        String messagePattern = "/view/mobile/teacher/share?id={}&name={}&subject={}";
        if (teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) {
            messagePattern = "/view/mobile/teacher/share_junior?id={}&name={}&subject={}";
        }

        return fetchMainsiteUrlByCurrentSchema() + StringUtils.formatMessage(messagePattern, teacher.getId(), encodeTeacherName, encodeSubject);
    }



    /* ======================================================================================
       包班制支持
       一个老师多学科情况,拿到指定学科的老师
       ====================================================================================== */

    protected Teacher getCurrentTeacherBySubject() {
        return getCurrentTeacherBySubject(getCurrentSubject());
    }

    protected Teacher getCurrentTeacherBySubject(Subject subject) {
        Teacher teacher = getCurrentTeacher();
        if (subject == null) {
            return teacher;
        }

        // 多学科支持
        if (teacher != null) {
            if (subject != teacher.getSubject()) {// 此时需要根据学科切换当前老师的班级信息
                Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                teacher = teacherLoaderClient.loadTeacher(id);
            }
        }
        return teacher;
    }

    protected Subject getCurrentSubject() {
        String subjectStr = getRequestString(REQ_SUBJECT);
        return StringUtils.isNotEmpty(subjectStr) ? Subject.of(subjectStr) : null;
    }


    protected Set<Clazz> loadTeacherClazzIncludeMainSub(Long teacherId) {
        if (teacherId == null) {
            return Collections.emptySet();
        }

        Set<Long> teacherIds = new HashSet<>();
        teacherIds.add(teacherId);
        Collection<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(teacherId);
        if (CollectionUtils.isNotEmpty(subTeacherIds)) {
            teacherIds.addAll(subTeacherIds);
        }

        Long mainTeacherIds = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherIds != null) {
            teacherIds.add(mainTeacherIds);
        }

        Map<Long, List<Clazz>> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherIds);
        Set<Clazz> retClazzList = new HashSet<>();
        for (Long tid : teacherClazzs.keySet()) {
            List<Clazz> clazzs = teacherClazzs.get(tid);
            if (CollectionUtils.isEmpty(clazzs)) continue;
            retClazzList.addAll(clazzs);
        }

        return retClazzList;
    }
}
