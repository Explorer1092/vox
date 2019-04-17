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

package com.voxlearning.utopia.service.business.impl.service.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.Sets;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableObject;
import com.voxlearning.utopia.data.CertificationCondition;
import com.voxlearning.utopia.entity.ucenter.CertificationApplication;
import com.voxlearning.utopia.entity.ucenter.CertificationApplicationOperatingLog;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification.TeacherCertificationContext;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.certification.TeacherCertificationPostProcessor;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.certification.client.CertificationServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkStat;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.utopia.service.user.api.constants.UserTagType.ACTIVATION_TIME;

@Named
@Slf4j
public class TeacherCertificationServiceImpl extends BusinessServiceSpringBean {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private TeacherCertificationPostProcessor processor;

    @Inject private CertificationServiceClient certificationServiceClient;

    @Inject private ParentLoaderClient parentLoaderClient;

    public static void attachUserShippingAddress(CertificationApplication certificationApplication, UserShippingAddress userShippingAddress) {
        if (userShippingAddress == null) {
            return;
        }
        certificationApplication.setSchoolName(userShippingAddress.getSchoolName());
        certificationApplication.setProvinceCode(com.voxlearning.alps.lang.convert.ConversionServiceProvider.instance().getConversionService().convert(userShippingAddress.getProvinceCode(), Integer.class));
        certificationApplication.setProvinceName(userShippingAddress.getProvinceName());
        certificationApplication.setCityCode(com.voxlearning.alps.lang.convert.ConversionServiceProvider.instance().getConversionService().convert(userShippingAddress.getCityCode(), Integer.class));
        certificationApplication.setCityName(userShippingAddress.getCityName());
        certificationApplication.setCountyCode(com.voxlearning.alps.lang.convert.ConversionServiceProvider.instance().getConversionService().convert(userShippingAddress.getCountyCode(), Integer.class));
        certificationApplication.setCountyName(userShippingAddress.getCountyName());
        certificationApplication.setAddress(userShippingAddress.getDetailAddress());
        certificationApplication.setPostCode(userShippingAddress.getPostCode());
    }

    public CertificationCondition getCertificationCondition(Long userId) {
        Validate.notNull(userId, "User id must not be null");
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        Validate.notNull(teacher, "User id %s not found", userId);

        CertificationCondition condition = new CertificationCondition();
        condition.setTeacherId(userId);
        condition.setExtensionAttributes(new HashMap<>());
        condition.getExtensionAttributes().put("teacher", teacher);
        condition.setEnoughStudentsFinishedHomework(hasEnoughStudentsFinishedHomework(userId));
        condition.setEnoughStudentsBindParentMobile(hasEnoughStudentsBindParentMobileOrBindSelfMobile(userId));
        condition.setMobileAuthenticated(checkMobileAuthenticated(userId, condition));
        return condition;
    }

    public Map<Long, CertificationCondition> batchGetCertificationCondition(List<Long> teacherIds) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptyMap();
        }
        Map<Long, CertificationCondition> dataMap = new HashMap<>();
        for (Long teacherId : teacherIds) {
            dataMap.put(teacherId, getCertificationCondition(teacherId));
        }
        return dataMap;
    }

    public MapMessage startCertificationApplication(final Long userId) {
        CertificationCondition condition;
        try {
            condition = getCertificationCondition(userId);
            condition.validate();
        } catch (Exception ex) {
            logger.error("User {} failed passing certification condition checking", userId);
            return MapMessage.errorMessage("User " + userId + " failed passing certification condition checking");
        }

        final UserShippingAddress userShippingAddress = (UserShippingAddress) condition.getExtensionAttributes().get("userShippingAddress");
        final String authenticatedSensitiveMobile = (String) condition.getExtensionAttributes().get("authenticatedSensitiveMobile");
        final User teacher = (User) condition.getExtensionAttributes().get("teacher");

        final MutableObject<String> errorMessage = new MutableObject<>(null);

        CertificationApplication certificationApplication = certificationServiceClient.getRemoteReference()
                .findCertificationApplication(userId).getUninterruptibly();
        if (certificationApplication != null) {
            // don't rollback transaction here for legacy data automatic migration
            if (certificationApplication.fetchCertificationState() == SUCCESS) {
                errorMessage.setValue("User " + userId + "'s certification application already approved");
                return MapMessage.errorMessage(errorMessage.getValue());
            } else if (certificationApplication.fetchCertificationState() != AuthenticationState.AGAIN) {
                errorMessage.setValue("User " + userId + "'s certification application is pending");
                return MapMessage.errorMessage(errorMessage.getValue());
            }
        } else {
            certificationApplication = new CertificationApplication();
        }
        certificationApplication.setUserId(userId);
        certificationApplication.setRealname(teacher.getProfile().getRealname());
        attachUserShippingAddress(certificationApplication, userShippingAddress);
        certificationApplication.setSensitivePhone(authenticatedSensitiveMobile);
        certificationApplication.setCertificationState(AuthenticationState.WAITING.getState());
        if (certificationApplication.getId() == null) {
            try {
                certificationServiceClient.getRemoteReference()
                        .insertCertificationApplication(certificationApplication).awaitUninterruptibly();
            } catch (Exception ignored) {
            }
        } else {
            certificationServiceClient.getRemoteReference()
                    .updateCertificationApplication(certificationApplication.getId(), certificationApplication).awaitUninterruptibly();
        }

        if (errorMessage.getValue() != null) {
            return MapMessage.errorMessage(errorMessage.getValue());
        }
        return MapMessage.successMessage();
    }

    public void changeUserAuthenticationState(final Long userId, final AuthenticationState authenticationState, final Long operatorId, final String operatorName) {
        Validate.notNull(userId, "User id must not be null");
        Validate.notNull(authenticationState, "Certification state must not be null");
        Teacher user = teacherLoaderClient.loadTeacher(userId);
        if (user == null) {
            logger.warn("User {} not found, ignore", userId);
            return;
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(userId)
                .getUninterruptibly();
        if (school == null) {
            logger.warn("User {} school not found, ignore", userId);
            return;
        }

        // update user authentication state
        AuthenticationState originalState = user.fetchCertificationState();
        if (originalState != authenticationState) {
            logger.debug("User: {} -> {}", originalState.name(), authenticationState.name());
            userServiceClient.updateAuthenticationState(userId, authenticationState.getState());
        }

        // write operating log
        CertificationApplicationOperatingLog operatingLog = new CertificationApplicationOperatingLog();
        operatingLog.setOperatorId(operatorId);
        operatingLog.setOperatorName(operatorName);
        operatingLog.setApplicantId(userId);
        operatingLog.setCertificationState(authenticationState.getState());
        operatingLog.setComment(originalState.name() + " -> " + authenticationState.name());
        certificationServiceClient.getRemoteReference()
                .insertCertificationApplicationOperatingLog(operatingLog)
                .awaitUninterruptibly();

        // 老师认证上下文，用于后处理
        TeacherCertificationContext teacherCertificationContext = TeacherCertificationContext.newInstance(user);

        // 副账号记录认证日志
        List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(userId);
        if (CollectionUtils.isNotEmpty(subTeacherIds)) {
            subTeacherIds.forEach(sid -> {
                operatingLog.setId(null);
                operatingLog.setApplicantId(sid);
                certificationServiceClient.getRemoteReference()
                        .insertCertificationApplicationOperatingLog(operatingLog)
                        .awaitUninterruptibly();
            });
            teacherCertificationContext.setSubTeacherIds(subTeacherIds);
        }

        // post processor
        if (authenticationState.getState() == 1) {
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    processor.process(teacherCertificationContext);
                } catch (Exception ignored) {
                }
            });
        }
    }

    public boolean hasEnoughStudentsFinishedHomework(Long teacherId) {
        return studentsFinishedHomeworkCount(teacherId, null) >= 8;
    }

    // 此方法在 CRM中使用
    public Map<Long, Boolean> hasEnoughStudentsFinishHomeworkByTeacherIds(Collection<Long> teacherIds) {
        Map<Long, Boolean> teacherMap = new HashMap<>();
        teacherIds.stream().forEach(teacherId -> {
            List<StudentHomeworkStat.DataMapper> statList = newHomeworkLoaderClient.getStudentHomeworkStatByTeacherId(teacherId);
            if (CollectionUtils.isEmpty(statList)) {
                teacherMap.put(teacherId, false);
            } else {
                long studentCount = statList.stream().filter(s -> s.getNormalHomeworkCount() >= 3).count();
                if (studentCount >= 8) {
                    teacherMap.put(teacherId, true);
                } else {
                    teacherMap.put(teacherId, false);
                }
            }
        });
        return teacherMap;
    }

    public boolean hasEnoughStudentsBindParentMobileOrBindSelfMobile(Long teacherId) {
        return studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(teacherId, null).size() >= 3;
    }

    public Map<Long, Boolean> hasEnoughStudentsBindParentMobileOrStudentsBindSelfMobile(Collection<Long> teacherIds) {
        Map<Long, Integer> studentBindMobileCount = studentsBindParentMobileOrStudentsBindSelfMobileCount(teacherIds);
        Map<Long, Boolean> teacherMap = new HashMap<>();
        teacherIds.stream().forEach(teacherId -> {
            if (studentBindMobileCount.get(teacherId) >= 3) {
                teacherMap.put(teacherId, true);
            } else {
                teacherMap.put(teacherId, false);
            }
        });
        return teacherMap;
    }

    public int studentsFinishedHomeworkCount(Long teacherId, Date start) {
        // 查询所有班级学生做作业的情况，寻找完成三次作业的学生中注册时间在教师注册时间之后或者学生首次登陆时间在老师首次登陆时间之后的学生
        Set<Long> sids = newHomeworkLoaderClient.getStudentHomeworkStatByTeacherId(teacherId)
                .stream()
                .filter(source -> source.getNormalHomeworkCount() >= 3)
                .map(StudentHomeworkStat.DataMapper::getStudentId)
                .collect(Collectors.toSet());

        if (sids.isEmpty()) return 0;
        if (start == null) return sids.size();

        Map<Long, User> students = userLoaderClient.loadUsers(sids);
        Set<Long> all = new HashSet<>(sids);
        all.add(teacherId);
        Map<Long, UserTag> userTags = userTagLoaderClient.loadUserTags(all);

        UserTag tut = userTags.get(teacherId);
        final Date tat;
        if (tut != null && tut.hasTag(ACTIVATION_TIME.name())) {
            tat = DateUtils.stringToDate(tut.fetchTag(ACTIVATION_TIME.name()).getValue());
        } else {
            tat = start;
        }

        return (int) students.values().stream()
                .filter(user -> {
                    if (user.getCreateTime().after(start) || user.getCreateTime().equals(start)) return true;
                    UserTag sut = userTags.get(user.getId());
                    Date sat = user.getCreateTime();
                    if (sut != null && sut.hasTag(ACTIVATION_TIME.name())) {
                        sat = DateUtils.stringToDate(sut.fetchTag(ACTIVATION_TIME.name()).getValue());
                    }
                    return sat.after(tat) || sat.equals(tat);
                })
                .count();
    }

    // 这个方法不要随便调用，这个方法用于教师的认证奖励，不能用于认证
    public Map<String, Long> getFinishCount(Teacher teacher) {
        Map<String, Long> result = new HashMap<>();
        long count3 = 0; // 用于老的认证奖励，在教师之后的学生完成3次作业的人数
        long count6 = 0; // 用于新的认证奖励，完成6次作业的人数

        if (teacher != null) {
            // 查询所有班级学生做作业的情况
            List<StudentHomeworkStat.DataMapper> stats = newHomeworkLoaderClient.getStudentHomeworkStatByTeacherId(teacher.getId());

            // 计算count6
            count6 = stats.stream().filter(source -> source.getNormalHomeworkCount() >= 6).count();

            // 计算count3，一个月后可以删除
            Set<Long> sids = stats.stream()
                    .filter(source -> source.getNormalHomeworkCount() >= 3)
                    .map(StudentHomeworkStat.DataMapper::getStudentId)
                    .collect(Collectors.toSet());
            Map<Long, User> students = userLoaderClient.loadUsers(sids);

            Set<Long> all = new HashSet<>(sids);
            all.add(teacher.getId());
            Map<Long, UserTag> userTags = userTagLoaderClient.loadUserTags(all);

            UserTag tut = userTags.get(teacher.getId());
            Date tat = teacher.getCreateTime();
            Date tct = teacher.getCreateTime();
            if (tut != null && tut.hasTag(ACTIVATION_TIME.name())) {
                tat = DateUtils.stringToDate(tut.fetchTag(ACTIVATION_TIME.name()).getValue());
            }

            for (StudentHomeworkStat.DataMapper stat : stats) {
                if (!students.containsKey(stat.getStudentId())) continue;
                long count = stat.getNormalHomeworkCount();
                if (count < 3) continue;

                User student = students.get(stat.getStudentId());
                UserTag sut = userTags.get(stat.getStudentId());
                Date sat = student.getCreateTime();
                Date sct = student.getCreateTime();
                if (sut != null && sut.hasTag(ACTIVATION_TIME.name())) {
                    sat = DateUtils.stringToDate(sut.fetchTag(ACTIVATION_TIME.name()).getValue());
                }
                if (sct.before(tct) && sat.before(tat)) continue;
                count3++;
            }
        }

        result.put("count3", count3);
        result.put("count6", count6);
        return result;
    }

    public List<Long> studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(Long teacherId, Collection<Long> clazzIds) {
        if (teacherId == null) return Collections.emptyList();

        if (CollectionUtils.isEmpty(clazzIds)) {
            List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
            clazzIds = clazzs.stream()
                    .filter(e -> e != null && e.getId() != null)
                    .map(Clazz::getId)
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(clazzIds)) return Collections.emptyList();

        List<Long> studentIds = userAggregationLoaderClient.loadTeacherStudentIdsBySystemClazzIds(clazzIds, teacherId)
                .values().stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(e -> e != null)
                .distinct()
                .collect(Collectors.toList());
        if (studentIds.isEmpty()) return Collections.emptyList();

        return new ArrayList<>(filterSelfOrParentBindMobileUsers(studentIds));
    }

    private boolean checkMobileAuthenticated(Long teacherId, CertificationCondition condition) {
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(teacherId);
        if (userAuthentication.isMobileAuthenticated()) {
            condition.getExtensionAttributes().put("authenticatedSensitiveMobile", userAuthentication.getSensitiveMobile());
            return true;
        }
        return false;
    }

    private Map<Long, Integer> studentsBindParentMobileOrStudentsBindSelfMobileCount(Collection<Long> teacherIds) {
        Map<Long, List<GroupMapper>> teacherGroupsByTeacherId = groupLoaderClient.loadTeacherGroupsByTeacherId(teacherIds, true);
        Set<Long> studentIds = new HashSet<>();
        Map<Long, Integer> resultMap = new HashMap<>();
        Map<Long, Set<Long>> teacherStudentsMap = new HashMap<>();
        teacherGroupsByTeacherId.keySet().stream().forEach(p -> {
            //循环获取每个老师的组
            List<GroupMapper> groupMapperList = teacherGroupsByTeacherId.get(p);
            Set<Long> studentsOfTeacher = new HashSet<>();
            //循环获取每个组的学生
            groupMapperList.stream().forEach(group -> {
                List<GroupMapper.GroupUser> groupUserList = group.getStudents();
                //循环组内的每个学生
                Set<Long> groupStudentIds = groupUserList.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(groupStudentIds)) {
                    studentIds.addAll(groupStudentIds);
                    studentsOfTeacher.addAll(groupStudentIds);
                }
            });
            teacherStudentsMap.put(p, studentsOfTeacher);
        });
        if (CollectionUtils.isEmpty(studentIds)) {
            teacherIds.stream().forEach(teacherId -> resultMap.put(teacherId, 0));
            return resultMap;
        }

        Set<Long> studentIdsSetBindMobile = filterSelfOrParentBindMobileUsers(studentIds);

        teacherIds.stream().forEach(teacherId -> {
            Set<Long> teacherStudents = teacherStudentsMap.get(teacherId);
            if (CollectionUtils.isEmpty(teacherStudents)) {
                resultMap.put(teacherId, 0);
            }
            //没有绑定手机的学生set
            Set<Long> studentsNotBingMobile = Sets.__difference_PleaseMakeSureYouKnowWhatAreYouDoing(teacherStudents, studentIdsSetBindMobile);
            if (CollectionUtils.isEmpty(studentsNotBingMobile)) {
                resultMap.put(teacherId, teacherStudents.size());
            } else {
                resultMap.put(teacherId, teacherStudents.size() - studentsNotBingMobile.size());
            }
        });
        return resultMap;
    }

    private Set<Long> filterSelfOrParentBindMobileUsers(Collection<Long> studentIds) {
        Set<Long> retIdList = new HashSet<>();

        List<Long> needCheckParentIds = new ArrayList<>();
        needCheckParentIds.addAll(studentIds);

        // 绑定了手机的学生账号
        Map<Long, UserAuthentication> userAuthentications = userLoaderClient.loadUserAuthentications(studentIds);
        for (UserAuthentication userAuthentication : userAuthentications.values()) {
            if (userAuthentication.isMobileAuthenticated() && !retIdList.contains(userAuthentication.getId())) {
                retIdList.add(userAuthentication.getId());
                needCheckParentIds.remove(userAuthentication.getId());
            }
        }

        // 家长绑定了手机号的学生账号
        if (CollectionUtils.isEmpty(needCheckParentIds)) {
            return retIdList;
        }

        Map<Long, List<StudentParent>> parentRefs = parentLoaderClient.loadStudentParents(needCheckParentIds);
        for (Long studentId : parentRefs.keySet()) {
            List<StudentParent> parents = parentRefs.get(studentId);
            Set<Long> keyParentIds = parents.stream()
                    .filter(p -> p.isKeyParent() && p.getParentUser() != null)
                    .map(p -> p.getParentUser().getId())
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(keyParentIds)) {
                continue;
            }

            Map<Long, UserAuthentication> parentAuthentications = userLoaderClient.loadUserAuthentications(keyParentIds);
            UserAuthentication parentAuthentication = parentAuthentications.values().stream().findFirst().orElse(null);
            if (parentAuthentication != null) {
                retIdList.add(studentId);
            }
        }

        return retIdList;
    }
}
