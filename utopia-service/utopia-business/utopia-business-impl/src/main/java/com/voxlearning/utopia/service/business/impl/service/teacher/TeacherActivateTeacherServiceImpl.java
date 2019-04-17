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

package com.voxlearning.utopia.service.business.impl.service.teacher;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ActivationType;
import com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.mapper.ActivateMapper;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;
import com.voxlearning.utopia.service.business.impl.dao.TeacherActivateTeacherHistoryDao;
import com.voxlearning.utopia.service.business.impl.loader.TeacherTaskLoaderImpl;
import com.voxlearning.utopia.service.business.impl.service.user.internal.ActivatableTeacherFinder;
import com.voxlearning.utopia.service.business.impl.service.user.internal.ActivatedTeacherFinder;
import com.voxlearning.utopia.service.business.impl.service.user.internal.ActivatingTeacherFinder;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.constants.TeacherLevelValueType;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.*;
import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.*;

@Named
@Slf4j
public class TeacherActivateTeacherServiceImpl extends BusinessServiceSpringBean {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private AmbassadorServiceClient ambassadorServiceClient;

    @Inject private BusinessCacheSystem businessCacheSystem;

    @Inject private TeacherActivateTeacherHistoryDao teacherActivateTeacherHistoryDao;
    @Inject private ActivatableTeacherFinder activatableTeacherFinder;
    @Inject private ActivatingTeacherFinder activatingTeacherFinder;
    @Inject private ActivatedTeacherFinder activatedTeacherFinder;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private TeacherTaskLoaderImpl teacherTaskLoader;

    public MapMessage personalStatisticOfTeacherActivateTeacher(Long teacherId) {
        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviterIds(Collections.singleton(teacherId))
                .values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (histories.isEmpty()) {
            return MapMessage.successMessage().add("pcount", 0).add("icount", 0);
        }
        histories = histories.stream()
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> SafeConverter.toBoolean(t.getOver()))
                .collect(Collectors.toList());
        int icount = 0;
        for (TeacherActivateTeacherHistory history : histories) {
            icount += activationIntegralCalculator(history.getActivationType()) / 10;
        }
        return MapMessage.successMessage().add("pcount", histories.size()).add("icount", icount);
    }

    public List<ActivateInfoMapper> getPotentialTeacher(TeacherDetail teacher) {
        return activatableTeacherFinder.find(teacher);
    }

    public int getActivatingCount(Long teacherId) {
        if (null == teacherId) {
            return 0;
        }
        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviterIds(Collections.singleton(teacherId))
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> !SafeConverter.toBoolean(t.getOver()))
                .collect(Collectors.toList());

        List<Long> inviteeIds = new ArrayList<>();
        for (TeacherActivateTeacherHistory history : histories) {
            inviteeIds.add(history.getInviteeId());
        }
        Map<Long, Teacher> inviteeMap = teacherLoaderClient.loadTeachers(inviteeIds);
        int count = 0;
        for (TeacherActivateTeacherHistory history : histories) {
            if (inviteeMap.containsKey(history.getInviteeId())
                    && inviteeMap.get(history.getInviteeId()).getSubject() != null
                    && !inviteeMap.get(history.getInviteeId()).isDisabledTrue()) {
                count++;
            }
        }
        return count;
    }

    public MapMessage activateTeacher(TeacherDetail inviter, ActivateMapper mapper) {
        if (inviter == null || mapper == null || mapper.getUserList().isEmpty()) {
            return MapMessage.errorMessage("发送邀请失败，请重新选择教师");
        }
        ActivateInfoMapper candidate = mapper.getUserList().get(0);//默认只有一个
        Long inviteeId = candidate.getUserId();
        ActivationType type = candidate.getType();
        if (inviteeId == null || type == null) {
            return MapMessage.errorMessage("发送邀请失败，请重新选择教师");
        }

        // 目标激活人不能在被人的激活列表中
        List<TeacherActivateTeacherHistory> temp = teacherActivateTeacherHistoryDao
                .findByInviteeIds(Collections.singleton(inviteeId))
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> !SafeConverter.toBoolean(t.getOver()))
                .collect(Collectors.toList());

        if (!temp.isEmpty()) {
            String key = CacheKeyGenerator.generateCacheKey(TEACHER_ACTIVATE_TEACHER, null, new Object[]{inviter.getId()});
            businessCacheSystem.CBS.flushable.delete(key);
            return MapMessage.errorMessage("该老师正在被其他老师唤醒，您不能唤醒此老师");
        }

        // 最多可以激活三个老师
        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviterIds(Collections.singleton(inviter.getId()))
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> !SafeConverter.toBoolean(t.getOver()))
                .collect(Collectors.toList());
        List<Long> inviteeIds = new ArrayList<>();
        for (TeacherActivateTeacherHistory history : histories) {
            inviteeIds.add(history.getInviteeId());
        }
        Map<Long, Teacher> inviteeMap = teacherLoaderClient.loadTeachers(inviteeIds);
        List<TeacherActivateTeacherHistory> candidates = new ArrayList<>();
        for (TeacherActivateTeacherHistory history : histories) {
            if (inviteeMap.containsKey(history.getInviteeId())
                    && inviteeMap.get(history.getInviteeId()).getSubject() != null
                    && !inviteeMap.get(history.getInviteeId()).isDisabledTrue()) {
                candidates.add(history);
            }
        }

        if (candidates.size() >= 3) { // 普通老师只能激活三个
            return MapMessage.errorMessage("最多可同时唤醒3名老师哦~");
        }
        final Map<Long, TeacherActivateTeacherHistory> map = candidates.stream()
                .filter(e -> e != null && e.getInviteeId() != null)
                .collect(Collectors.groupingBy(TeacherActivateTeacherHistory::getInviteeId))
                .values().stream()
                .map(e -> e.iterator().next())
                .collect(Collectors.toMap(TeacherActivateTeacherHistory::getInviteeId, Function.identity()));

        // 创建或正更新
        TeacherActivateTeacherHistory history = map.get(candidate.getUserId());
        if (history == null) {
            history = new TeacherActivateTeacherHistory();
            history.setInviterId(inviter.getId());
            history.setInviteeId(candidate.getUserId());
            history.setActivationType(candidate.getType());
            history.setSuccess(Boolean.TRUE);
            teacherActivateTeacherHistoryDao.insert(history);
            // 发送站内信
            User invitee = userLoaderClient.loadUser(candidate.getUserId());
            if (invitee != null) {
                String content = StringUtils.formatMessage("亲爱的{}老师，{}老师诚邀您为自己的班级布置作业。<br />" +
                        "只需3步获得50园丁豆奖励。<br />1.布置作业<br />2.同一班级至少8人完成作业<br />" +
                        "3.检查作业<br />行动起来吧！", invitee.fetchRealname(), inviter.fetchRealname());
                teacherLoaderClient.sendTeacherMessage(invitee.getId(), content);
            }
        } else {
            if (history.getActivationType() != candidate.getType()) {
                teacherActivateTeacherHistoryDao.updateActivationType(history.getId(), candidate.getType());
            }
        }

        // 清除待唤醒老师列表的缓存以及正在唤醒老师列表缓存
        Set<String> keys = new LinkedHashSet<>();
        keys.add(CacheKeyGenerator.generateCacheKey(TEACHER_ACTIVATE_TEACHER, null, new Object[]{inviter.getId()}));
        keys.add(CacheKeyGenerator.generateCacheKey(ACTIVATING_TEACHER_LIST, null, new Object[]{inviter.getId()}));
        businessCacheSystem.CBS.flushable.delete(keys);
        return MapMessage.successMessage();
    }

    public List<ActivateInfoMapper> getActivatingTeacher(Long teacherId) {
        return activatingTeacherFinder.find(teacherId);
    }

    public List<ActivateInfoMapper> getActivatedTeacher(Long teacherId) {
        return activatedTeacherFinder.find(teacherId);
    }

    public MapMessage deleteTeacherActivateTeacherHistory(Long inviterId, String historyId) {
        TeacherActivateTeacherHistory history = teacherActivateTeacherHistoryDao.load(historyId);
        if (history == null) {
            return MapMessage.errorMessage("删除失败，请重新操作");
        }
        if (!inviterId.equals(history.getInviterId())) {
            return MapMessage.errorMessage("您不是邀请人，删除失败");
        }
        teacherActivateTeacherHistoryDao.delete(historyId);
        // 清除待唤醒老师列表的缓存以及正在唤醒老师列表缓存
        Set<String> keys = new LinkedHashSet<>();
        keys.add(CacheKeyGenerator.generateCacheKey(TEACHER_ACTIVATE_TEACHER, null, new Object[]{inviterId}));
        keys.add(CacheKeyGenerator.generateCacheKey(ACTIVATING_TEACHER_LIST, null, new Object[]{inviterId}));
        businessCacheSystem.CBS.flushable.delete(keys);
        return MapMessage.successMessage();
    }

    public Set<Long> getActivatingInviter(Long inviteeId){
        return teacherActivateTeacherHistoryDao.findByInviteeIds(Collections.singleton(inviteeId))
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> !SafeConverter.toBoolean(t.getOver()))
                .map(t -> t.getInviterId())
                .collect(Collectors.toSet());
    }

    public void teacherActivateTeacherFinish(User invitee) {
        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviteeIds(Collections.singleton(invitee.getId()))
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> !SafeConverter.toBoolean(t.getOver()))
                .collect(Collectors.toList());
        for (TeacherActivateTeacherHistory history : histories) {
            final TeacherDetail inviter = teacherLoaderClient.loadTeacherDetail(history.getInviterId());
            teacherActivateTeacherHistoryDao.updateOver(history.getId());
            // 清除待唤醒老师列表的缓存以及正在唤醒老师列表缓存以及唤醒成功的老师列表
            Set<String> keys = new LinkedHashSet<>();
            keys.add(CacheKeyGenerator.generateCacheKey(TEACHER_ACTIVATE_TEACHER, null, new Object[]{inviter.getId()}));
            keys.add(CacheKeyGenerator.generateCacheKey(ACTIVATING_TEACHER_LIST, null, new Object[]{inviter.getId()}));
            keys.add(CacheKeyGenerator.generateCacheKey(ACTIVATED_TEACHER_LIST, null, new Object[]{inviter.getId()}));
            businessCacheSystem.CBS.flushable.delete(keys);
        }
    }

    public MapMessage findUnauthenticatedTeacherInTheSameSchoolByIdOrName(Long schoolAmbassadorId, String token) {
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(schoolAmbassadorId)
                .getUninterruptibly();
        Long schoolId = school == null ? null : school.getId();

        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(schoolId);
        teachers = new LinkedList<>(teachers);
        for (Iterator<Teacher> it = teachers.iterator(); it.hasNext(); ) {
            Teacher teacher = it.next();
            if (Objects.equals(teacher.getId(), schoolAmbassadorId)) {
                it.remove();
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Teacher teacher : teachers) {
            if (StringUtils.equals(teacher.getProfile().getRealname(), token) || (StringUtils.isNumeric(token) && Objects.equals(teacher.getId(), conversionService.convert(token, Long.class)))) {
                Map<String, Object> map = new HashMap<>();
                map.put("teacherId", teacher.getId());
                map.put("createDatetime", DateUtils.dateToString(teacher.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
                map.put("teacherName", StringUtils.defaultString(teacher.getProfile().getRealname()));
                map.put("authenticationState", teacher.fetchCertificationState());
                result.add(map);
            }
        }
        return result.isEmpty() ? MapMessage.errorMessage("您查找的教师不存在。") : MapMessage.successMessage().add("teacherList", result);
    }

    public MapMessage recommendTeacherAuthentication(Long schoolAmbassadorId, Long recommendedTeacherId) {
        User recommendedTeacher = userLoaderClient.loadUser(recommendedTeacherId);
        if (null == recommendedTeacher) {
            return MapMessage.errorMessage("您推荐的教师不存在。");
        }
        if (recommendedTeacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage("您推荐的教师已经通过认证。");
        }
        MapMessage message;
        try {
            message = userAttributeServiceClient.setExtensionAttribute(recommendedTeacherId,
                    UserExtensionAttributeKeyType.SCHOOL_AMBASSADOR_RECOMMEND_TEACHER_AUTHENTICATION.name(),
                    schoolAmbassadorId.toString());
        } catch (Exception ex) {
            message = MapMessage.errorMessage();
        }
        return message;
    }

    private int activationIntegralCalculator(ActivationType type) {
        switch (type) {
            case SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_ONE:
                return 500;
            case SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_TWO:
                return 1000;
            case SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_THREE:
                return 1500;
            case TEACHER_ACTIVATE_TEACHER_LEVEL_ONE:
                return 500;
            case TEACHER_ACTIVATE_TEACHER_LEVEL_TWO:
                return 1000;
            case TEACHER_ACTIVATE_TEACHER_LEVEL_THREE:
                return 1500;
            default:
                return 0;
        }
    }

    public Map<String, Object> getActivateIntegralPopupContent(Long teacherId) {
        if (null == teacherId) {
            throw new IllegalArgumentException("invalid teacher id ");
        }

        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviterIds(Collections.singleton(teacherId))
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> SafeConverter.toBoolean(t.getOver()))
                .filter(t -> null == t.getExtensionAttributes()
                        || t.getExtensionAttributes().get("showpop") == null
                        || t.getExtensionAttributes().get("showpop").toString().equals("1"))
                .collect(Collectors.toList());
        if (histories.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, Object> content = new HashMap<>();
        List<String> userNames = new ArrayList<>();
        int integral = 0;
        for (TeacherActivateTeacherHistory history : histories) {
            User user = userLoaderClient.loadUser(history.getInviteeId());
            if (null != user) {
                userNames.add(user.getProfile().getRealname());
            }
            integral += activationIntegralCalculator(history.getActivationType());
            Map<String, Object> attributes = history.getExtensionAttributes();
            if (null == attributes) {
                attributes = new HashMap<>();
            }
            attributes.put("showpop", "0"); //对每个被唤醒成功的用户，弹窗只弹一次，此处置0下次就不会查出来了
            teacherActivateTeacherHistoryDao.updateExtensionAttributes(history.getId(), attributes);
        }
        content.put("integral", integral);
        content.put("users", userNames);
        return content;
    }
}
