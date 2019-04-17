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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.random.RandomGenerator;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.ClazzService;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.TeacherAlterationService;
import com.voxlearning.utopia.service.user.api.constants.ClazzCreateSourceType;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.ClazzIntegralHistoryPagination;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.entities.log.UserOperatorType;
import com.voxlearning.utopia.service.user.api.mappers.ClassTeacherAlterationInfoMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import com.voxlearning.washington.support.SessionUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.CLAZZ_MANAGEMENT_PREFIX;
import static com.voxlearning.washington.controller.open.OpenApiReturnCode.*;

/**
 * Created by Summer Yang on 2015/7/29.
 * 老师微信 班级管理
 * todo 这个类貌似等着删了，微信的班级管理已经全部迁移到 teacherClazzManagementController了 要删的话，问一下长远
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher/clazz")
@Slf4j
public class WechatTeacherClazzController extends AbstractOpenController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    //获取我的班级列表
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext clazzList(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        if (userId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        try {
            Map<String, Object> data = teacherSystemClazzServiceClient
                    .loadSystemClazzManagementIndexData(teacher.getId());
            openAuthContext.setCode(SUCCESS_CODE);
            openAuthContext.add("teacherClazzList", data.get("teachClazzs"));
            openAuthContext.add("sendApplications", data.get("sendApplications"));
            //是否虚假老师
            boolean isFakeTeacher = false;
            CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacher.getId());
            if (teacherSummary != null && SafeConverter.toBoolean(teacherSummary.getFakeTeacher()) && CrmTeacherFakeValidationType.MANUAL_VALIDATION.getName().equals(teacherSummary.getValidationType())) {
                isFakeTeacher = true;
            }
            openAuthContext.add("isFakeTeacher", isFakeTeacher);
            return openAuthContext;
        } catch (Exception ex) {
            log.error("load teacher clazz list failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("查询老师的班级列表失败");
        }
        return openAuthContext;
    }

    //获取未处理请求数量
    @RequestMapping(value = "applicationcount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext applicationCount(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        if (userId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        try {
            int count = teacherAlterationServiceClient.countPendingApplicationSendIn(teacher.getId());
            openAuthContext.setCode(SUCCESS_CODE);
            openAuthContext.add("count", count);
        } catch (Exception ex) {
            log.error("load teacher applicationcount failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("获取未处理请求数量失败");
        }
        return openAuthContext;
    }

    //获取管理学生 学生列表
    @RequestMapping(value = "clazzdetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext clazzDetail(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long clazzId = ConversionUtils.toLong(jsonMap.get("cid"));
        if (userId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        try {
//            Map<String, Object> detail = businessTeacherServiceClient.loadTeacherClazzDetail(teacher.getId(), clazzId);
            Map<String, Object> detail = teacherSystemClazzServiceClient.loadTeacherClazzDetail(teacher.getId(), clazzId);
            if (MapUtils.isEmpty(detail)) {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError("没有数据");
                return openAuthContext;
            }
            openAuthContext.setCode(SUCCESS_CODE);
            openAuthContext.add("students", detail.get("students"));
        } catch (Exception ex) {
            log.error("load teacher clazz student list failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("查询老师班级学生列表失败");
        }
        return openAuthContext;
    }

    //管理学生 修改密码
    @RequestMapping(value = "resetstudentpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext resetStudentPassword(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();

        Long studentId = ConversionUtils.toLong(jsonMap.get("sid"));
        Long clazzId = ConversionUtils.toLong(jsonMap.get("cid"));
        String password = ConversionUtils.toString(jsonMap.get("p"));
        String confirmPassword = ConversionUtils.toString(jsonMap.get("cp"));
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        if (userId == 0L || studentId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        // 如果学生未绑定手机，老师可以直接重置密码
        // 如果学生绑定了家长手机，则一天只能重置一次密码，且通过给手机发送随机密码的方式重置
        // 统一用一个接口并增加检查的原因是防止用户通过接口直接重置
        String studentOrParentMobile = studentLoaderClient.loadStudentOrParentMobile(studentId, SafeConverter.toString(userId));
        if (StringUtils.isNotBlank(studentOrParentMobile)) {
            // 老师给学生重置密码行为，一天只能一次
            if (!asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherResetBindedStudentPWCacheManager_canResetPw(userId, studentId)
                    .getUninterruptibly()) {
                openAuthContext.setCode(SYSTEM_ERROR_CODE);
                openAuthContext.setError("您已经帮助重置过，如果学生没有收到密码可以联系客服");
                return openAuthContext;
            }

            // 生成随机密码
            confirmPassword = password = RandomGenerator.generatePlainPassword();
        }


        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        try {
            MapMessage message = AtomicLockManager.instance().wrapAtomic(teacherServiceClient)
                    .keyPrefix(ClazzService.MANAGE_CLAZZ_PREFIX)
                    .keys(clazzId)
                    .proxy()
                    .changeClazzStudentPassword(teacher, clazzId, studentId, password, confirmPassword);
            if (!message.isSuccess()) {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            } else {

                // 重置密码后处理
                User student = raikouSystem.loadUser(studentId);

                // 如果是绑定手机的学生，则发送重置密码短信
                if (StringUtils.isNotBlank(studentOrParentMobile)) {
                    String smsPayload = StringUtils.formatMessage(
                            "{}同学好，老师正在帮你重置密码，请用新密码：{}登录做作业（如孩子在学校使用，请尽快将新密码转发给老师）",
                            student.fetchRealname(),
                            password
                    );
                    smsServiceClient.createSmsMessage(studentOrParentMobile)
                            .content(smsPayload)
                            .type(SmsType.TEACHER_RESET_STUDENT_PASSWORD.name())
                            .send();
                }

                openAuthContext.setCode(SUCCESS_CODE);

                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(studentId);
                userServiceRecord.setOperatorId(teacher.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("修改密码");
                userServiceRecord.setComments("老师重置学生密码，操作端wechat");
                userServiceRecord.setAdditions("refer:WechatTeacherClazzController.resetStudentPassword");
                userServiceClient.saveUserServiceRecord(userServiceRecord);

                // 如果学生修改密码，更新学生端sessionkey
                VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", studentId);
                if (vendorAppsUserRef != null) {
                    vendorServiceClient.expireSessionKey(
                            "17Student",
                            studentId,
                            SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), studentId));
                }

                // 老师修改学生密码,需要强制学生修改密码
                asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .unflushable_setUserBehaviorCount(UserBehaviorType.STUDENT_FORCE_RESET_PW, studentId, 1L, 0)
                        .awaitUninterruptibly();
            }
        } catch (Exception ex) {
            log.error("update student password failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("修改学生密码失败");
        }
        return openAuthContext;
    }

    //管理学生 删除学生
    @RequestMapping(value = "batchremovestudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext deleteStudents(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long clazzId = ConversionUtils.toLong(jsonMap.get("cid"));
        String studentIds = ConversionUtils.toString(jsonMap.get("sids"));
        Long teacherId = ConversionUtils.toLong(jsonMap.get("uid"));
        if (clazzId == 0L || StringUtils.isBlank(studentIds)) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        try {
            Clazz c = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            List<Long> studentIdList = StringHelper.toLongList(studentIds);
            MapMessage message = atomicLockManager.wrapAtomic(teacherServiceClient)
                    .keyPrefix(CLAZZ_MANAGEMENT_PREFIX)
                    .keys(clazzId)
                    .proxy()
                    .deleteClazzStudents(teacher, c, studentIdList, null);
            if (!message.isSuccess()) {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            } else {
                Clazz clazz = (Clazz) message.get("clazz");
                String m = "{}老师将你移出了{}班，他/她可能觉得你不是这个班级的学生或者你已经有学号，如有疑问请直接联系老师！";
                m = StringUtils.formatMessage(m, teacher.fetchRealname(), clazz.formalizeClazzName());
                Collection deletedStudentIds = (Collection) message.get("deletedStudentIds");
                for (Object deletedStudentId : deletedStudentIds) {
                    messageCommandServiceClient.getMessageCommandService().sendUserMessage((Long) deletedStudentId, m);
                }
                openAuthContext.setCode(SUCCESS_CODE);
            }
        } catch (Exception ex) {
            log.error("batch delete student failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("批量删除学生失败");
        }
        return openAuthContext;
    }

    //加入班级 获取我的班级列表
    @RequestMapping(value = "loadteacherclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadTeacherClazz(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        if (userId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
            List<Clazz> teachClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());
            //我教的班级
            List<Map<String, Object>> teacheClazzList = new LinkedList<>();
            Map<String, List<Clazz>> levelTeachClazzs = teachClazzs.stream()
                    .collect(Collectors.groupingBy(Clazz::getClassLevel));

            levelTeachClazzs.forEach((k, v) -> {
                List<SystemClazzInfo> list = v.stream()
                        .sorted(new ClazzComparator())
                        .map(c -> new SystemClazzInfo(c.getId(), c.getClassName()))
                        .collect(Collectors.toList());
                Map<String, Object> levelObj = new HashMap<>();
                levelObj.put("clazzLevel", k);
                levelObj.put("clazzs", list);
                teacheClazzList.add(levelObj);
            });

            // ugc info
            // 执教班级数
//            TeacherUGCInfo teacherUGCInfo = teacherUgcServiceClient.getTeacherUgcService()
//                    .loadTeacherUGCInfo(teacher.getId())
//                    .getUninterruptibly();
//            int actualTeachClazzCount = teacherUGCInfo == null ? 0 : teacherUGCInfo.getTeachClazzNum();
//            openAuthContext.add("actualTeachClazzCount", actualTeachClazzCount);

            openAuthContext.add("teachClazzs", teacheClazzList);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error("load teacher clazz list failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("获取我的班级列表失败");
        }
        return openAuthContext;
    }

    //加入班级 获取系统班级列表   todo 长远，目前我指定按照infotype=1的逻辑实现。微信没有别的复杂逻辑
    @RequestMapping(value = "chooseclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext chooseClazz(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Integer clazzLevel = ConversionUtils.toInt(jsonMap.get("cl"));
        if (userId == 0L || clazzLevel == 0) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
            Long schoolId = teacher.getTeacherSchoolId();
            if (schoolId == null) {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError("学校不存在");
                return openAuthContext;
            }
            List<Clazz> teachClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());
            List<Clazz> clazzs = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .enabled()
                    .clazzLevel(ClazzLevel.parse(clazzLevel))
                    .toList();
            Set<Long> teachClazzIds = teachClazzs.stream().map(Clazz::getId).collect(Collectors.toSet());
            List<SystemClazzInfo> clazzList = new LinkedList<>();
            clazzs = clazzs.stream()
                    .filter(Clazz::isSystemClazz)
                    .sorted(new ClazzComparator()).collect(Collectors.toList());
            for (Clazz clazz : clazzs) {
                SystemClazzInfo systemClazzInfo = new SystemClazzInfo(clazz.getId(), clazz.getClassName());
                systemClazzInfo.setChecked(teachClazzIds.contains(clazz.getId()));
                clazzList.add(systemClazzInfo);
            }
            clazzList = clazzList.stream()
                    .collect(Collectors.toList());
            openAuthContext.add("clazzs", clazzList);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error("load teacher join clazz list failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("获取班级列表失败");
        }
        return openAuthContext;
    }

    // 获取已存在老师班级信息
    @RequestMapping(value = "findclazzinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext findClazzInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            Map<String, Object> clazzMap = (Map<String, Object>) jsonMap.get("clazzIds");
            Set<Long> clazzIdSet = new LinkedHashSet<>();
            clazzMap.forEach((k, v) -> {
                List<Object> levelClazzs = (List<Object>) v;
                levelClazzs.forEach(lc -> {
                    Map<String, Object> m = (Map<String, Object>) lc;
                    List<SystemClazzInfo> clazzs = JsonUtils.fromJsonToList(JsonUtils.toJson(m.get("clazzs")), SystemClazzInfo.class);
                    clazzs.forEach(c -> clazzIdSet.add(c.getId()));
                });
            });

            MapMessage message = teacherSystemClazzInfoServiceClient.getNewAddAndAdjustClazzs(teacher.getId(), clazzIdSet, ClazzCreateSourceType.wechat, false);
            openAuthContext.setCode(SUCCESS_CODE);
            openAuthContext.add("newClazzs", message.get("newClazzs"));
            openAuthContext.add("adjustClazzs", message.get("adjustClazzs"));
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher find clazz info failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("获取已存在老师班级信息");
        }
        return openAuthContext;
    }

    // 如果有需要申请的班级 走这个借口
    @RequestMapping(value = "adjustclazzs2.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext adjustClazzs2(HttpServletRequest request) throws IOException {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Teacher curTeacher = teacherLoaderClient.loadTeacher(userId);
        if (curTeacher == null) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
            JsonNode rootNode = mapper.readTree(JsonUtils.toJsonPretty(jsonMap.get("datajson")));
            CollectionType LongListType = mapper.getTypeFactory().constructCollectionType(List.class, Long.class);
            // 处理加入班级已有资源请求
            JsonNode node = rootNode.get("newClazzs");
            List<Long> adjustClazzIds = new LinkedList<>();
            for (JsonNode childNode : node) {
                Long clazzId = childNode.get("clazzId").asLong();
                adjustClazzIds.add(clazzId);// 对于发送请求的加入班级，老师先进班，请求是另外一回事
                List<Long> teacherIds = new LinkedList<>();
                for (JsonNode groupNode : childNode.get("groups")) {
                    for (JsonNode teacherNode : groupNode.get("teachers")) {
                        teacherIds.add(teacherNode.get("id").asLong());
                    }
                }
                Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
                Set<Subject> subjectSet = teachers.values().stream().map(Teacher::getSubject).collect(Collectors.toSet());
                for (Teacher teacher : teachers.values()) {
                    if (teacher.getSubject().equals(curTeacher.getSubject())) {// 同学科，进行接管申请
                        MapMessage message = sendApplication(curTeacher.getId(), clazzId, teacher.getId(),
                                ClazzTeacherAlterationType.REPLACE, "向班级任课教师申请接管学生资源失败", sourceType);
                        // 发送消息通知及弹窗
                        if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                            sendApplicationMessageToRespondent(message, new ApplicationMessageBuilder() {
                                @Override
                                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                                    return StringUtils.formatMessage("{}老师申请接管您任课的{}的学生资源",
                                            applicant.getProfile().getRealname(),
                                            clazz.formalizeClazzName());
                                }
                            }, true, true);
                        }
                    } else {// 不同学科，判断是否有同学科的老师，没有发送关联申请
                        // 或者老师为认证老师，则发送关联请求，一旦关联，之前的关联老师被接管
                        if (teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState()
                                || !subjectSet.contains(curTeacher.getSubject())) {
                            MapMessage message = sendApplication(curTeacher.getId(), clazzId, teacher.getId(),
                                    ClazzTeacherAlterationType.LINK, "向班级任课教师申请关联学生资源失败", sourceType);

                            // 发送消息通知及弹窗
                            if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                                sendApplicationMessageToRespondent(message, new ApplicationMessageBuilder() {
                                    @Override
                                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                                        return StringUtils.formatMessage("{}老师申请关联您任课的{}的学生资源",
                                                applicant.getProfile().getRealname(),
                                                clazz.formalizeClazzName());
                                    }
                                }, true, true);
                            }
                        }
                    }
                }
            }
            // 处理直接加入班级请求
            node = rootNode.get("adjustClazzs");
            adjustClazzIds.addAll(mapper.readValue(node.traverse(), LongListType));
            MapMessage message = MapMessage.successMessage();
            if (CollectionUtils.isNotEmpty(adjustClazzIds)) {
                message = clazzServiceClient.teacherAdjustSystemClazzs(curTeacher.getId(), adjustClazzIds, OperationSourceType.wechat);
            }
            if (message.isSuccess()) {
                Collection<Clazz> clazzs = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(adjustClazzIds);
                for (Clazz clazz : clazzs) {
                    // TODO 需要加个检查，避免重复设定班级教材
                    setClazzBook(curTeacher, clazz.getId(), clazz.getClazzLevel().getLevel());
                }
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }

            // 老师UGC信息 - 执教班级数
//            JsonNode actualTeachClazzCountNode = rootNode.get("actualTeachClazzCount");
//            if (actualTeachClazzCountNode != null) {
//                int actualTeachClazzCount = actualTeachClazzCountNode.asInt();
//                teacherServiceClient.setTeacherUGCTeachClazzCount(curTeacher.getId(), actualTeachClazzCount);
//            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher adjust clazz 2 failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("加入班级失败");
        }
        return openAuthContext;
    }

    //如果班级没有老师 走这个借口
    @RequestMapping(value = "adjustclazzs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext adjustClazzs(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            Map<String, Object> clazzMap = (Map<String, Object>) jsonMap.get("clazzIds");
            Set<Long> clazzIdSet = new LinkedHashSet<>();
            clazzMap.forEach((k, v) -> {
                List<Object> levelClazzs = (List<Object>) v;
                levelClazzs.forEach(lc -> {
                    Map<String, Object> m = (Map<String, Object>) lc;
                    List<SystemClazzInfo> clazzs = JsonUtils.fromJsonToList(JsonUtils.toJson(m.get("clazzs")), SystemClazzInfo.class);
                    clazzs.forEach(c -> clazzIdSet.add(c.getId()));
                });
            });
            MapMessage message = MapMessage.successMessage();
            if (CollectionUtils.isNotEmpty(clazzIdSet)) {
                message = clazzServiceClient.teacherAdjustSystemClazzs(teacher.getId(), clazzIdSet, OperationSourceType.wechat);
            }
            if (message.isSuccess()) {
                Collection<Clazz> clazzs = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(clazzIdSet);
                for (Clazz clazz : clazzs) {
                    // TODO 需要加个检查，避免重复设定班级教材
                    setClazzBook(teacher, clazz.getId(), clazz.getClazzLevel().getLevel());
                }
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher find clazz info failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("获取班级列表失败");
        }
        return openAuthContext;
    }

    /**
     * 教师查看未处理的申请记录
     */
    @RequestMapping(value = "unprocessedapplication.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext pengdingList(HttpServletRequest request) {
        // 拓展为所有主副帐号的申请记录
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));
        try {
            Set<Long> teachers = teacherLoaderClient.loadRelTeacherIds(userId);
            teachers.add(userId);
            List<Map<String, Object>> unprocessedApplicationSendInMaps = new ArrayList<>();
            List<Map<String, Object>> unprocessedApplicationSendOutMaps = new ArrayList<>();
            for (long t : teachers) {
                List<ClassTeacherAlterationInfoMapper> unprocessedApplicationSendIn = new ArrayList<>();
                List<ClassTeacherAlterationInfoMapper> unprocessedApplicationSendOut = new ArrayList<>();
                unprocessedApplicationSendIn.addAll(teacherAlterationServiceClient.findUnprocessedApplicationSendIn(t, sourceType));
                unprocessedApplicationSendOut.addAll(teacherAlterationServiceClient.findUnprocessedApplicationSendOut(t, sourceType));
                // 简化输出
                for (ClassTeacherAlterationInfoMapper classTeacherAlterationInfoMapper : unprocessedApplicationSendIn) {
                    Map<String, Object> map = new HashMap();
                    map.put("recordId", classTeacherAlterationInfoMapper.getRecordId());
                    map.put("classId", classTeacherAlterationInfoMapper.getClassId());
                    map.put("className", classTeacherAlterationInfoMapper.getClassName());
                    map.put("studentCount", classTeacherAlterationInfoMapper.getStudentCount());
                    map.put("datetime", classTeacherAlterationInfoMapper.getDatetime());
                    map.put("message", classTeacherAlterationInfoMapper.getMessage());
                    map.put("type", classTeacherAlterationInfoMapper.getType());
                    map.put("ownerId", t);
                    unprocessedApplicationSendInMaps.add(map);
                }
                for (ClassTeacherAlterationInfoMapper classTeacherAlterationInfoMapper : unprocessedApplicationSendOut) {
                    Map<String, Object> map = new HashMap();
                    map.put("recordId", classTeacherAlterationInfoMapper.getRecordId());
                    map.put("classId", classTeacherAlterationInfoMapper.getClassId());
                    map.put("className", classTeacherAlterationInfoMapper.getClassName());
                    map.put("studentCount", classTeacherAlterationInfoMapper.getStudentCount());
                    map.put("datetime", classTeacherAlterationInfoMapper.getDatetime());
                    map.put("message", classTeacherAlterationInfoMapper.getMessage());
                    map.put("type", classTeacherAlterationInfoMapper.getType());
                    map.put("ownerId", t);
                    unprocessedApplicationSendOutMaps.add(map);
                }
            }
            openAuthContext.add("unprocessedApplicationSendIn", unprocessedApplicationSendInMaps);
            openAuthContext.add("unprocessedApplicationSendOut", unprocessedApplicationSendOutMaps);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher find unprocessedapplication failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("查询换班请求失败");
        }
        return openAuthContext;
    }

    /**
     * 老师取消关联学生申请
     */
    @RequestMapping(value = "cancellinkapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext cancelLinkApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = cancelApplication(userId, recordId, ClazzTeacherAlterationType.LINK, sourceType);
            if (message.isSuccess()) {
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher cancellinkapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("取消关联学生申请失败");
        }
        return openAuthContext;
    }

    /**
     * 老师拒绝其他老师关联学生申请
     */
    @RequestMapping(value = "rejectlinkapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext rejectLinkApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = rejectApplication(userId, recordId, ClazzTeacherAlterationType.LINK, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, false);
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher rejectlinkapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("拒绝关联学生申请失败");
        }
        return openAuthContext;
    }

    /**
     * 老师同意其他老师的关联学生申请
     */
    @RequestMapping(value = "approvelinkapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext approveLinkApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = approveApplication(userId, recordId, ClazzTeacherAlterationType.LINK, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, true);
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher approvelinkapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("同意关联学生申请失败");
        }
        return openAuthContext;
    }

    /**
     * 老师取消接管学生资源申请
     */
    @RequestMapping(value = "cancelreplaceapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext cancelReplaceApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = cancelApplication(userId, recordId, ClazzTeacherAlterationType.REPLACE, sourceType);
            if (message.isSuccess()) {
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher cancelreplaceapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("取消接管学生资源申请失败");
        }
        return openAuthContext;
    }

    /**
     * 老师拒绝其他老师的接管学生申请
     */
    @RequestMapping(value = "rejectreplaceapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext rejectReplaceApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = rejectApplication(userId, recordId, ClazzTeacherAlterationType.REPLACE, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, false);
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher rejectreplaceapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("拒绝接管学生资源申请失败");
        }
        return openAuthContext;
    }

    /**
     * 老师同意其他老师的接管学生申请
     */
    @RequestMapping(value = "approvereplaceapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext approveReplaceApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = approveApplication(userId, recordId, ClazzTeacherAlterationType.REPLACE, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, true);
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher approvereplaceapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("同意接管学生资源申请失败");
        }
        return openAuthContext;
    }

    /**
     * 取消转让给其他老师申请
     */
    @RequestMapping(value = "canceltransferapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext cancelTransferApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = cancelApplication(userId, recordId, ClazzTeacherAlterationType.TRANSFER, sourceType);
            if (message.isSuccess()) {
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher canceltransferapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("取消转让给其他老师申请失败");
        }
        return openAuthContext;
    }

    /**
     * 拒绝其他老师的转让班级申请
     */
    @RequestMapping(value = "rejecttransferapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext rejectTransferApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = rejectApplication(userId, recordId, ClazzTeacherAlterationType.TRANSFER, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}拒绝",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, false);
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher rejecttransferapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("拒绝转让给其他老师申请失败");
        }
        return openAuthContext;
    }

    /**
     * 同意其他老师的转让班级申请
     */
    @RequestMapping(value = "approvetransferapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext approveTransferApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long recordId = ConversionUtils.toLong(jsonMap.get("recordId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || recordId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            MapMessage message = approveApplication(userId, recordId, ClazzTeacherAlterationType.TRANSFER, sourceType);
            if (message.isSuccess()) {
                sendApplicationMessageToApplicant(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}接受",
                                respondent.getProfile().getRealname(),
                                clazz.formalizeClazzName(),
                                respondent.getProfile().getRealname());
                    }
                }, false, true);
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher approvetransferapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("同意转让给其他老师申请失败");
        }
        return openAuthContext;
    }

    /**
     * 转让班级申请
     */
    @RequestMapping(value = "sendtransferapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendTransferApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long clazzId = ConversionUtils.toLong(jsonMap.get("cid"));
        Long respondentId = ConversionUtils.toLong(jsonMap.get("respondentId"));
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null || respondentId == 0L || clazzId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            // 只有当不存在未检查作业时，才可以转让班级
            GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(userId, clazzId, false);
            if (group == null) {//
                logger.error("cannot find group for teacher {} and clazz {}", userId, clazzId);
                openAuthContext.setCode(SYSTEM_ERROR_CODE);
                openAuthContext.setError("invalid data");
                return openAuthContext;
            }

            int uncheckHomeworkNum = (int) newHomeworkLoaderClient.loadGroupHomeworks(group.getId(), Subject.MATH).unchecked().count();

            if (uncheckHomeworkNum > 0) {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError("该班级当前有未检查作业/测验，请检查后再转让班级");
                return openAuthContext;
            }
            MapMessage message = sendApplication(userId, clazzId, respondentId, ClazzTeacherAlterationType.TRANSFER,
                    "向其他教师申请转让学生资源失败", sourceType);
            // 发送消息通知及弹窗
            if (message.isSuccess()) {// 表示发送了申请
                // 直接退出班级
                clazzServiceClient.teacherExitSystemClazz(userId, clazzId, Boolean.FALSE, OperationSourceType.wechat);

                if (message.containsKey("recordId")) {
                    sendApplicationMessageToRespondent(message, new ApplicationMessageBuilder() {
                        @Override
                        String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                            return StringUtils.formatMessage("{}老师申请转让其任课的{}的学生资源",
                                    applicant.getProfile().getRealname(),
                                    clazz.formalizeClazzName());
                        }
                    }, true, true);
                    openAuthContext.setCode(SUCCESS_CODE);
                }
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher sendtransferapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("转让给其他老师申请失败");
        }
        return openAuthContext;
    }

    /**
     * 搜索老师
     */
    @RequestMapping(value = "findlinkteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext findLinkTeacher(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long clazzId = ConversionUtils.toLong(jsonMap.get("cid"));
        String nameOrMobile = ConversionUtils.toString(jsonMap.get("nameormobile"));
        String subject = ConversionUtils.toString(jsonMap.get("subject"));
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
        if (teacher == null || StringUtils.isBlank(nameOrMobile) || clazzId == 0L || StringUtils.isBlank(subject)) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            String mobile = "";
            String name = "";
            if (MobileRule.isMobile(nameOrMobile)) {
                mobile = nameOrMobile;
            } else {
                name = nameOrMobile;
            }
            if ((StringUtils.isBlank(mobile) || !MobileRule.isMobile(mobile)) && StringUtils.isBlank(name)) {
                openAuthContext.setCode(SYSTEM_ERROR_CODE);
                openAuthContext.setError("请填写正确的手机号码或者教师姓名");
                return openAuthContext;
            }
            MapMessage message = teacherSystemClazzServiceClient.handleFindTeacherProcess(
                    name,
                    mobile,
                    Subject.valueOf(subject),
                    teacher.getTeacherSchoolId(),
                    clazzId);
            if (message.isSuccess()) {
                openAuthContext.add("type", message.get("type").toString());
                if ("TEACHER_FOUND".equals(message.get("type").toString())) {
                    openAuthContext.add("teachers", message.get("teachers"));
                }
            } else {
                openAuthContext.add("type", message.get("type").toString());
            }
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error("wechat clazz mgn find link teacher failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("添加老师查询老师失败");
        }
        return openAuthContext;
    }

    /**
     * 添加老师申请
     */
    @RequestMapping(value = "sendlinkapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public OpenAuthContext sendLinkApplication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        Long clazzId = ConversionUtils.toLong(jsonMap.get("cid"));
        Long respondentId = ConversionUtils.toLong(jsonMap.get("respondentId"));
        Teacher curTeacher = teacherLoaderClient.loadTeacher(userId);
        if (curTeacher == null || respondentId == 0L || clazzId == 0L) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }

        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));

        try {
            // 当无同科老师时，认证老师可以直接关联
            if (curTeacher.getAuthenticationState().equals(AuthenticationState.SUCCESS.getState())) {
                Teacher respondent = teacherLoaderClient.loadTeacher(respondentId);

                // 处理老师当前的关联状况
                MapMessage result = clazzServiceClient.handleTeacherLinkOperation(curTeacher, respondent, clazzId, OperationSourceType.wechat);

                Map<Teacher, Teacher> replaceTeachers = (Map<Teacher, Teacher>) result.remove("replaceTeachers");
                Map<Teacher, Teacher> linkTeachers = (Map<Teacher, Teacher>) result.remove("linkTeachers");

                if (MapUtils.isNotEmpty(replaceTeachers) || MapUtils.isNotEmpty(linkTeachers)) {
                    boolean needSendApp = SafeConverter.toBoolean(result.remove("needSendApp"));

                    if (!needSendApp) {// 不需要发送请求的状况，直接接管
                        List<Long> addClazzTeacherIds = (List<Long>) result.remove("addClazzTeacherIds");
                        for (Long teacherId : addClazzTeacherIds) {
                            clazzServiceClient.teacherJoinSystemClazz(teacherId, clazzId, OperationSourceType.wechat);
                        }
                        MapMessage m = MapMessage.successMessage();
                        for (Map.Entry<Teacher, Teacher> entry : replaceTeachers.entrySet()) {
                            Teacher fromT = entry.getKey();
                            Teacher toT = entry.getValue();
                            m = groupServiceClient.replaceTeacherGroupForReplace(fromT.getId(), toT.getId(), clazzId, toT.getId().toString(), UserOperatorType.TEACHER);
                        }
                        for (Map.Entry<Teacher, Teacher> entry : linkTeachers.entrySet()) {
                            Teacher fromT = entry.getKey();
                            Teacher toT = entry.getValue();
                            m = groupServiceClient.shareTeacherGroup(fromT.getId(),
                                    toT.getId(), fromT.getSubject(), toT.getSubject(), clazzId, curTeacher.getId().toString(), UserOperatorType.TEACHER);
                        }
                        openAuthContext.add("added", true);
                        openAuthContext.setCode(SUCCESS_CODE);
                        return openAuthContext;
                    }
                }
            }

            // 发送申请
            MapMessage message = sendApplication(userId, clazzId, respondentId,
                    ClazzTeacherAlterationType.LINK, "向班级任课教师申请关联学生资源失败", sourceType);

            // 发送消息通知及弹窗
            if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                sendApplicationMessageToRespondent(message, new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师申请关联您任课的{}的学生资源",
                                applicant.getProfile().getRealname(),
                                clazz.formalizeClazzName());
                    }
                }, true, true);
                openAuthContext.add("added", false);
                openAuthContext.setCode(SUCCESS_CODE);
            } else {
                openAuthContext.setCode(BUSINESS_ERROR_CODE);
                openAuthContext.setError(message.getInfo());
            }
        } catch (Exception ex) {
            log.error("wechat clazz mgn teacher sendlinkapp failed.", ex.getMessage());
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("添加老师申请失败");
        }
        return openAuthContext;
    }

    // 班级积分历史
    @RequestMapping(value = "integralhistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext integralHistory(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        long groupId = SafeConverter.toLong(openAuthContext.getParams().get("gid"), Long.MIN_VALUE);
        int pageNumber = SafeConverter.toInt(openAuthContext.getParams().get("pn"), 1);
        boolean ge0 = SafeConverter.toBoolean(openAuthContext.getParams().get("ge0"), true);

        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }

        User teacher = raikouSystem.loadUser(teacherId);
        if (teacher == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }

        if (pageNumber < 1) {
            pageNumber = 1;
        }

        // 获取前三个月的历史数据
        ClazzIntegralHistoryPagination pagination = clazzIntegralServiceClient.getClazzIntegralService()
                .loadClazzIntegralHistories(groupId, 3, pageNumber - 1, 5, ge0)
                .getUninterruptibly();
        openAuthContext.add("pagination", pagination);
        openAuthContext.add("currentPage", pageNumber);
        openAuthContext.add("integral", pagination.getTotalIntegral());
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    @NoArgsConstructor
    @RequiredArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class SystemClazzInfo {
        @Getter @Setter @NonNull Long id;
        @Getter @Setter @NonNull String name;
        @Getter @Setter Boolean checked;
    }

    private static class ClazzComparator implements Comparator<Clazz> {
        @Override
        public int compare(Clazz o1, Clazz o2) {
            String n1 = o1.getClassName();
            String n2 = o2.getClassName();

            int p1 = n1.lastIndexOf("班");
            int p2 = n2.lastIndexOf("班");

            if (p1 != -1 && p2 != -1) {
                n1 = n1.substring(0, p1);
                n2 = n2.substring(0, p2);
                try {
                    return Integer.valueOf(n1).compareTo(Integer.valueOf(n2));
                } catch (NumberFormatException e) {
                    // do nothing
                }
            }

            return n1.compareTo(n2);
        }
    }

    /**
     * 发送申请
     *
     * @param applicantId  申请人ID
     * @param clazzId      班级ID
     * @param respondentId 被申请人ID
     * @param type         申请类型
     * @param errMsg       出错信息
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     * @author changyuan.liu
     */
    private MapMessage sendApplication(long applicantId,
                                       long clazzId,
                                       long respondentId,
                                       ClazzTeacherAlterationType type,
                                       String errMsg,
                                       OperationSourceType sourceType) {
        MapMessage msg = teacherAlterationServiceClient.sendApplication(applicantId, respondentId, clazzId, type, sourceType);
        if (!msg.isSuccess() && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)
                && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.NO_STUDENT_GROUP_ERR_MSG)
                && !StringUtils.equals(msg.getInfo(), TeacherAlterationService.UNUSUAL_APPLICATIONS_ERR_MSG)) {
            msg = MapMessage.errorMessage(errMsg);
        }
        return msg;
    }

    /**
     * 给申请发送者发送消息提醒
     *
     * @param message              申请消息
     * @param messageBuilder       发送消息生成器
     * @param appendCheckDetailBtn 是否在消息提醒中添加‘查看详情’按钮
     * @param needPopup            是否弹窗
     * @author changyuan.liu
     */
    private void sendApplicationMessageToRespondent(MapMessage message,
                                                    ApplicationMessageBuilder messageBuilder,
                                                    boolean appendCheckDetailBtn,
                                                    boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.remove("applicant");
            Teacher respondent = (Teacher) message.remove("respondent");
            Clazz clazz = (Clazz) message.remove("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);

            doSendApplicationMessage(respondent, sendMsg, appendCheckDetailBtn, needPopup);

            Map<Long, List<UserWechatRef>> tid_refs_map = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(respondent.getId()));
            List<UserWechatRef> refs = tid_refs_map.get(respondent.getId());
            if (CollectionUtils.isNotEmpty(refs)) {
                //发送微信模版消息，如果applicant绑定了微信的话
                doSendApplicationMessageByWechat(applicant, respondent);
            } else {
                //否则，发送短信提醒消息
                doSendApplicationMessageBySMS(applicant, respondent);
            }
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            log.error("Send application succeed but send message failed.", ex.getMessage());
        }
    }

    /**
     * 给申请接受者发送消息提醒
     *
     * @param message
     * @param messageBuilder
     * @param appendCheckDetailBtn
     * @param needPopup
     * @author changyuan.liu
     */
    private void sendApplicationMessageToApplicant(MapMessage message,
                                                   ApplicationMessageBuilder messageBuilder,
                                                   boolean appendCheckDetailBtn,
                                                   boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.remove("applicant");
            Teacher respondent = (Teacher) message.remove("respondent");
            Clazz clazz = (Clazz) message.remove("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);

            doSendApplicationMessage(applicant, sendMsg, appendCheckDetailBtn, needPopup);
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            log.error("Send application succeed but send message failed.", ex.getMessage());
        }
    }

    private void doSendApplicationMessage(Teacher user,
                                          String message,
                                          boolean appendCheckDetailBtn,
                                          boolean needPopup) {
        // 发送站内通知
        if (appendCheckDetailBtn) {
            // FIXME 查看申请地址的链接应该不用改
            message = StringUtils.formatMessage(
                    message + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【查看详情】</a>",
                    "/teacher/clazz/alteration/unprocessedapplication.vpage?type=someBodyToMe"
            );
            sendMessage(user, message);
        } else {
            sendMessage(user, message);
        }
        if (needPopup) {
            // 发送教师首页通知
            userPopupServiceClient.createPopup(user.getId())
                    .content(message)
                    .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
        }
    }

    //发送微信模版消息通知
    private void doSendApplicationMessageByWechat(Teacher applicant, Teacher respondent) {
        // 查询微信
        Map<String, Object> extensionInfo = MiscUtils.m("applicantId", applicant.getId(),
                "applicantName", applicant.fetchRealname(),
                "respondentId", respondent.getId(),
                "respondentName", respondent.fetchRealname());
        wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.TeacherClazzAlterationNotice,
                respondent.getId(), extensionInfo, WechatType.TEACHER);
    }

    //发送短信通知
    private void doSendApplicationMessageBySMS(Teacher applicant, Teacher respondent) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(respondent.getId());
        if (ua.isMobileAuthenticated()) {
            // 每个手机号，每天最多收到3个类似的班级调整的短信
            String userPhone = sensitiveUserDataServiceClient.loadUserMobile(ua.getId());
            if (smsServiceClient.getSmsService().canSendClazzManagement(userPhone).getUninterruptibly()) {
                String content = "您收到" + applicant.fetchRealname() + "老师的班级请求，点击链接立刻处理，" + "http://www.17zyw.cn/ABvANn";
                userSmsServiceClient.buildSms().to(ua)
                        .content(content)
                        .type(SmsType.CLAZZ_ALTERATION_NOTIFY)
                        .send();
            }
        }
    }

    private void sendMessage(Teacher receiver, String payload) {
        if (StringUtils.isBlank(payload)) {
            return;
        }
        payload = StringUtils.replace(payload, "老师老师", "老师");
        teacherLoaderClient.sendTeacherMessage(receiver.getId(), payload);
    }

    /**
     * 申请消息生成器
     *
     * @author changyuan.liu
     */
    private abstract class ApplicationMessageBuilder {
        /**
         * 生成申请消息
         *
         * @param applicant  申请人
         * @param respondent 被申请人
         * @param clazz      班级
         * @return 申请消息
         */
        abstract String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz);
    }

    /**
     * 同意申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     * @return
     * @author changyuan.liu
     */
    private MapMessage approveApplication(long respondentId,
                                          long recordId,
                                          ClazzTeacherAlterationType type,
                                          OperationSourceType sourceType) {
        MapMessage msg = teacherAlterationServiceClient.approveApplication(respondentId, recordId, type, sourceType);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("批准申请失败");
        }
        return msg;
    }

    /**
     * 取消申请
     *
     * @param applicantId 申请人ID
     * @param recordId    申请ID
     * @param type        申请类型
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage cancelApplication(long applicantId,
                                         long recordId,
                                         ClazzTeacherAlterationType type,
                                         OperationSourceType sourceType) {
        MapMessage msg = teacherAlterationServiceClient.cancelApplication(applicantId, recordId, type, sourceType);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("操作失败");
        }
        return msg;
    }

    /**
     * 拒绝申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage rejectApplication(long respondentId,
                                         long recordId,
                                         ClazzTeacherAlterationType type,
                                         OperationSourceType sourceType) {
        MapMessage msg = teacherAlterationServiceClient.rejectApplication(respondentId, recordId, type, sourceType);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("拒绝申请失败");
        }
        return msg;
    }

    private void setClazzBook(Teacher teacher, long clazzId, int clazzLevel) {
        // TODO 需要加个检查，避免重复设定班级教材
        ExRegion region = userLoaderClient.loadUserRegion(teacher);
        Long bookId = contentLoaderClient.getExtension().initializeClazzBook(
                teacher.getSubject(), clazzLevel, region.getCode(),
                new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
        ChangeBookMapper cbm = new ChangeBookMapper();
        cbm.setType(0);
        cbm.setBooks(String.valueOf(bookId));
        cbm.setClazzs(String.valueOf(clazzId));
        try {
            contentServiceClient.setClazzBook(teacher, cbm);
        } catch (Exception ignored) {
            logger.warn("Failed to set clazz books [bookIds={},clazzIds={}]", cbm.getBooks(), cbm.getClazzs(), ignored);
        }
    }

}
