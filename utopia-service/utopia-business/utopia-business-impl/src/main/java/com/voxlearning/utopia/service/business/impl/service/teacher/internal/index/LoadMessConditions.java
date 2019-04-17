/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.BusinessTeacherServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.teacher.TeacherCertificationServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.TeacherApplicationMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherAlterationServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherSystemClazzServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.TEACHER_ACTIVATE_TEACHER;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-10-30
 */
@Named
public class LoadMessConditions extends AbstractTeacherIndexDataLoader {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;

    @Inject private AsyncBusinessCacheServiceImpl asyncBusinessCacheService;

    @Inject private BusinessCacheSystem businessCacheSystem;
    @Inject
    private TeacherCertificationServiceImpl teacherCertificationServiceImpl;
    @Inject
    private TeacherAlterationServiceClient teacherAlterationServiceClient;
    @Inject
    private TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject
    private BusinessTeacherServiceImpl businessTeacherService;
    @Inject
    private VendorLoaderClient vendorLoaderClient;

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        FlightRecorder.dot("LMC_START");

        if (!context.isSkipNextAll()) {
            Teacher teacher = context.getTeacher();
            if (teacher.fetchCertificationState() == SUCCESS) {
                // 未处理的换班请求数量
                context.getParam().put("pendingApplicationCount", teacherAlterationServiceClient.countPendingApplication(teacher.getId()));
                // 老师唤醒卡片  认证老师才查询
                List<ActivateInfoMapper> activateInfoMappers = businessCacheSystem.CBS.flushable
                        .wrapCache(businessTeacherService)
                        .expiration(3600)
                        .keyPrefix(TEACHER_ACTIVATE_TEACHER)
                        .keys(teacher.getId())
                        .proxy()
                        .getPotentialTeacher(teacherLoaderClient.loadTeacherDetail(teacher.getId()));
                if (CollectionUtils.isNotEmpty(activateInfoMappers)) {
                    context.getParam().put("showActivateCard", true);
                }

            } else {
                // 老师班级列表
                context.getParam().put("clazzList", getTeacherClazzs(context.getClazzList()));
            }

            context.getParam().put("mobile", context.getMobile());

            context.getParam().putAll(teacherCertificationServiceImpl.getFinishCount(teacher));

            // 用户所在学校是否有同科校园大使
            AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), context.getSchool().getId());
            if (ref != null) {
                context.getParam().put("schoolAmbassadorExist", true);
                context.getParam().put("schoolAmbassadorName", teacherLoaderClient.loadTeacher(ref.getAmbassadorId()).fetchRealname());
            } else {
                context.getParam().put("schoolAmbassadorExist", false);
            }

            // 教师首页快捷添加班级
            context.getParam().put("isNewCreateTeacher", DateUtils.dayDiff(new Date(), teacher.getCreateTime()) < 15);

            // 教师首页下载学生名单
            context.getParam().put("downloadClazzs", getBatchNameClazz(teacher, context.getClazzList()));
            String method = asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherMobileOrAccountCacheManager_getMethod(teacher.getId(), StringUtils.isNotBlank(context.getMobile()))
                    .getUninterruptibly();
            context.getParam().put("mobileoraccount", StringUtils.equals("ACCOUNT", method) ? "ACCOUNT" : "MOBILE");

            // 判断一个老师是否绑定微信
            context.getParam().put("wechatBinded", wechatLoaderClient.isBinding(teacher.getId(), WechatType.TEACHER.getType()));
            // 判断一个老师是否绑定了app
            context.getParam().put("appBinded", vendorLoaderClient.loadVendorAppUserRef("17Teacher", teacher.getId()) != null);

            //判断一个老师是否绑定大使微信
            context.getParam().put("ambassadorWechatBinded", wechatLoaderClient.isBinding(teacher.getId(), WechatType.AMBASSADOR.getType()));

            // 是否显示布置作业引导
            context.getParam().put("showHomeworkGuide", DateUtils.dayDiff(new Date(), teacher.getCreateTime()) < 7);

            // 换班弹窗
            context.getParam().put("capl", JsonUtils.toJson(getPending(teacher.getId())));

            // 学生app推广动态
            context.getParam().put("showSapl", showStudentAppPromotionLatest(teacher.getId()));

        }
        FlightRecorder.dot("LMC_END");
        return context;
    }

    private List<TeacherApplicationMapper> getPending(Long teacherId) {
        if (!asyncBusinessCacheService.TeacherClazzAlterationCacheManager_needPopup(teacherId).getUninterruptibly()) { // 如果今天已经弹过了，就返回空列表
            return Collections.emptyList();
        }
        // 一天只第一次登陆的时候查询一次
        asyncBusinessCacheService.TeacherClazzAlterationCacheManager_record(teacherId).awaitUninterruptibly();
        Map<String, List<TeacherApplicationMapper>> map = teacherSystemClazzServiceClient
                .processTeacherPendingApplications(teacherId);
        return map.get("receive");
    }

    private List<Map<String, Object>> getTeacherClazzs(List<Clazz> clazzs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Clazz clazz : clazzs) {
            Map<String, Object> clazzMap = new HashMap<>();
            clazzMap.put("id", clazz.getId());
            clazzMap.put("name", clazz.formalizeClazzName());
            result.add(clazzMap);
        }
        return result;
    }

    // 新建无名单班级后导入学生姓名
    private List<Map<String, Object>> getBatchNameClazz(Teacher teacher, List<Clazz> clazzs) {
        if (clazzs.size() > 0 && clazzs.get(0).isSystemClazz()) {// 系统自建班级
            Map<Long, Clazz> idClazzMap = clazzs.stream().collect(Collectors.toMap(Clazz::getId, Function.<Clazz>identity()));
            Map<Long, List<Long>> clazzStuIds = userAggregationLoaderClient.loadTeacherStudentIdsByClazzIds(new ArrayList<>(idClazzMap.keySet()), teacher.getId());

            List<Map<String, Object>> result = new ArrayList<>();
            clazzStuIds.forEach((k, v) -> {
                if (v.size() < 5) {
                    Map<String, Object> map = new HashMap<>();
                    Clazz clazz = idClazzMap.get(k);
                    map.put("clazzId", clazz.getId());
                    map.put("clazzName", clazz.formalizeClazzName());
                    result.add(map);
                }
            });
            return result;
        } else {
            // 如果老师是认证的或者班级为系统班级，查询所有班级中最近一个月创建的并且登录学生小于等于1人
            if (teacher.fetchCertificationState() == SUCCESS) {
                clazzs = clazzs.stream().filter(source ->
                        DateUtils.dayDiff(new Date(), source.getCreateTime()) < 30
                ).collect(Collectors.toList());
            } else { // 非系统班级且如果老师没有认证，查询所有该老师创建的班级中最近一个月创建的并且登录学生小于等于1人
                List<Long> teacherClazzIds = teacherLoaderClient.loadTeacherClazzIds(teacher.getId());
                final List<Long> createClazzIds = new ArrayList<>();
                for (Long teacherClazzId : teacherClazzIds) {
                    createClazzIds.add(teacherClazzId);
                }
                clazzs = clazzs.stream().filter(source ->
                        createClazzIds.contains(source.getId()) && DateUtils.dayDiff(new Date(), source.getCreateTime()) < 30
                ).collect(Collectors.toList());
            }

            if (clazzs.isEmpty()) {
                return Collections.emptyList();
            }

            List<Map<String, Object>> result = new ArrayList<>();
            List<Long> clazzIdList = clazzs.stream()
                    .filter(e -> e != null && e.getId() != null)
                    .map(Clazz::getId)
                    .collect(Collectors.toList());

            Map<Long, List<Long>> clazzStudentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzIds(clazzIdList);
            Set<Long> studentIds = clazzStudentIds.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);

            for (Clazz clazz : clazzs) {
                boolean add = true;
                if (clazzStudentIds.containsKey(clazz.getId())) {
                    List<User> students = clazzStudentIds.get(clazz.getId())
                            .stream()
                            .map(userMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

//                    add = !userLoaderClient.validateLoginUserCount(students, 1);
                    add = !userLoginServiceClient.validateLoginUserCount(
                            students.stream().map(User::getId).collect(Collectors.toList()), 1);
                }
                if (add) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("clazzId", clazz.getId());
                    map.put("clazzName", clazz.formalizeClazzName());
                    result.add(map);
                }
            }
            return result;
        }
    }

    private boolean showStudentAppPromotionLatest(Long teacherId) {
        Date onlineDate = DateUtils.stringToDate("2015-10-26 00:00:00");
        Date now = new Date();
        if (DateUtils.dayDiff(now, onlineDate) <= 30) return true;
        List<GroupTeacherTuple> refs = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByTeacherId(teacherId);
        if (CollectionUtils.isNotEmpty(refs)) {
            for (GroupTeacherTuple ref : refs) {
                if (DateUtils.dayDiff(now, ref.getCreateTime()) <= 14) return true;
            }
        }
        return false;
    }
}
