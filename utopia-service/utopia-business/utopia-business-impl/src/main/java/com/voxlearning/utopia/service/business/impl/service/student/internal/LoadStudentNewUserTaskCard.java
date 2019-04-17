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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.business.impl.service.user.UserTaskService;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.user.api.constants.AccountType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.UserTag;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.StudentNewUserTaskMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.temp.StudentBindClazzList;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学生首页新手任务
 *
 * @author Rui.Bao
 * @since 2014-08-08 11:47 AM
 */
@Named
public class LoadStudentNewUserTaskCard extends AbstractStudentIndexDataLoader {

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private UserTaskService userTaskService;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private GlobalTagServiceClient globalTagServiceClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail student = context.getStudent();
        Set<Long> groupIds = context.__groupIds;
        boolean displayNoviceCard = false; // 是否展示新手任务卡
        GroupMapper groupMapper = context.__studentGroups.stream().findAny().orElse(null);
        if (groupMapper == null) {
            return context;
        }
        StudentNewUserTaskMapper mapper = userTaskService.getStudentNewUserTaskMapper(student);
        if (mapper != null) {
            // 判断老师是否重置了该学生的密码
            boolean pwdReseted = pwdReseted(student);
            if (pwdReseted) mapper.setPasswordModified(false); // 强制用户修改密码

            // 强制绑定手机
            if (!mapper.isMobileVerfied()) {
                if (StudentBindClazzList.getClazzMap().containsKey(groupMapper.getClazzId())) {
                    // 在重名班级中 进行处理
                    context.getParam().put("bmpopup", true);
                    if (!context.__newExamExist) {
                        context.getParam().put("force", true);
                    }
                } else {

                    // 如果该学生是CRM批量注册的，或者有需要绑定手机的标签，或者是认证学生，或者密码被老师重置过，需要绑定手机
                    boolean bmpopup = (student.isBatchUser() || pwdReseted || tagExisted(student)) && !isVirtual(student.getId()) && !isXuebaStudent(student);
                    context.getParam().put("bmpopup", bmpopup);
                    // 如果需要绑定手机，判断是否需要强制绑定
                    boolean force = bmpopup && !context.__newExamExist;
                    context.getParam().put("force", force);
                }
            }

            // 如果学生在重名班级列表 并且已经绑定了手机
            if (StudentBindClazzList.getClazzMap().containsKey(groupMapper.getClazzId()) && mapper.isMobileVerfied()) {
                // 看看是否需要验证
                long count = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .persistence_getUserBehaviorCount(UserBehaviorType.STUDENT_VALIDATION_MOBILE, student.getId())
                        .getUninterruptibly();
                if (count <= 0) {
                    String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(student.getId());
                    context.getParam().put("mobile", mobile);
                    context.getParam().put("vmpopup", true);
                    if (!context.__newExamExist) {
                        context.getParam().put("force", true);
                    }
                }
            }
            if (!mapper.taskFinished()) displayNoviceCard = true;
        }

        context.getParam().put("displayNoviceCard", displayNoviceCard);
        if (displayNoviceCard) context.getParam().put("taskMapper", mapper);

        // 如果有家长帐号就不用强绑APP了
        boolean noBindWindow = CollectionUtils.isNotEmpty(studentLoaderClient.loadStudentParentRefs(student.getId()));
        if (noBindWindow) {
            context.getParam().put("noBindWindow", true);
        } else {
            // 获取PC强绑APP是否黑名单
            List<GlobalTag> globalTagList = globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.NoAppBindWindowUsers.name());
            if (CollectionUtils.isNotEmpty(globalTagList)) {
                List<Long> grayList = globalTagList.stream().map(g -> SafeConverter.toLong(g.getTagValue())).collect(Collectors.toList());
                if (grayList.contains(student.getId())) {
                    context.getParam().put("noBindWindow", true);
                }
            }
        }
        return context;
    }

    // 是否体验账号
    private boolean isVirtual(Long studentId) {
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        return studentExtAttribute != null && studentExtAttribute.getAccountType() == AccountType.VIRTUAL;
    }

    // 是否是翻转课堂学生
    private boolean isXuebaStudent(Student student) {
        return StringUtils.equals(student.getWebSource(), UserWebSource.xueba.getSource());
    }

    private boolean pwdReseted(StudentDetail student) {
        Long userBehaviorCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(UserBehaviorType.STUDENT_FORCE_RESET_PW, student.getId())
                .getUninterruptibly();
        return userBehaviorCount != 0;// 如果缓存没有该键值，则返回0，所以判断是否是0即可
    }

    private boolean tagExisted(StudentDetail student) {
        UserTag userTag = userTagLoaderClient.loadUserTag(student.getId());
        return userTag != null && userTag.hasTag(UserTagType.MOBILE_FORCE_BIND.name());
    }

}
