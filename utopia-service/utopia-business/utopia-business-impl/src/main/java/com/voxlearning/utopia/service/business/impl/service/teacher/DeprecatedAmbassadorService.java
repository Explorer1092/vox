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
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.ambassador.api.document.*;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.ambassador.client.SchoolAmbassadorServiceClient;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.impl.dao.TeacherActivateTeacherHistoryDao;
import com.voxlearning.utopia.service.business.impl.service.BusinessTeacherServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.MiscServiceImpl;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.UserActivity;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.UserTag;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;

@Deprecated
@Named
public class DeprecatedAmbassadorService extends BusinessServiceSpringBean {

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private BusinessTeacherServiceImpl businessTeacherService;
    @Inject private MiscServiceImpl miscService;
    @Inject private TeacherActivateTeacherHistoryDao teacherActivateTeacherHistoryDao;
    @Inject private UserTagLoaderClient userTagLoaderClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject private SchoolAmbassadorServiceClient schoolAmbassadorServiceClient;


    public void changeActivationType(Long teacherId, boolean flag) {
        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviterIds(Collections.singleton(teacherId))
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> !SafeConverter.toBoolean(t.getOver()))
                .collect(Collectors.toList());
        for (TeacherActivateTeacherHistory history : histories) {
            ActivationType target = history.getActivationType();
            if (flag) {
                switch (target) {
                    case TEACHER_ACTIVATE_TEACHER_LEVEL_ONE:
                        target = ActivationType.SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_ONE;
                        break;
                    case TEACHER_ACTIVATE_TEACHER_LEVEL_TWO:
                        target = ActivationType.SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_TWO;
                        break;
                    case TEACHER_ACTIVATE_TEACHER_LEVEL_THREE:
                        target = ActivationType.SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_THREE;
                        break;
                    default:
                        break;
                }
            } else {
                switch (target) {
                    case SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_ONE:
                        target = ActivationType.TEACHER_ACTIVATE_TEACHER_LEVEL_ONE;
                        break;
                    case SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_TWO:
                        target = ActivationType.TEACHER_ACTIVATE_TEACHER_LEVEL_TWO;
                        break;
                    case SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_THREE:
                        target = ActivationType.TEACHER_ACTIVATE_TEACHER_LEVEL_THREE;
                        break;
                    default:
                        break;
                }
            }
            if (history.getActivationType() != target) {
                teacherActivateTeacherHistoryDao.updateActivationType(history.getId(), target);
            }
        }
    }

    //老师是否预备大使
    public boolean isJoinCompetition(Long teacherId) {
        return ambassadorLoaderClient.getAmbassadorLoader().loadTeacherAmbassadorCompetition(teacherId) != null;
    }

    // 获取本校同科预备大使最近90天的积分明细列表
    private List<Map<String, Object>> loadSchoolSameSubjectCompetitionDetailList(Long schoolId, Subject subject) {
        List<AmbassadorCompetition> competitions = ambassadorLoaderClient.getAmbassadorLoader().findSchoolAmbassadorCompetitions(schoolId);
        competitions = competitions.stream().filter(c -> c.getSubject() == subject).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(competitions)) {
            return Collections.emptyList();
        }
        // 所有预备大使
        List<Long> teacherIds = competitions.stream().map(AmbassadorCompetition::getTeacherId).collect(Collectors.toList());
        Map<Long, TeacherDetail> teacherMap = teacherLoaderClient.loadTeacherDetails(teacherIds);
        Map<Long, TeacherExtAttribute> teacherExtAttributeMap = teacherLoaderClient.loadTeacherExtAttributes(teacherIds);
        Map<Long, List<AmbassadorCompetitionDetail>> dataMap = new HashMap<>();
        Date beginDate = DateUtils.calculateDateDay(new Date(), -90);
        for (Long tid : teacherIds) {
            // 过滤大使
            if (teacherMap.get(tid).isSchoolAmbassador()) {
                continue;
            }
            List<AmbassadorCompetitionDetail> details = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorCompetitionDetails(tid);
            dataMap.put(tid, details.stream().filter(d -> d.getCreateDatetime().after(beginDate)).collect(Collectors.toList()));
        }
        // 获取我的排名信息  以及努力值明细
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<Long, List<AmbassadorCompetitionDetail>> entry : dataMap.entrySet()) {
            Map<String, Object> teacherScoreMap = new HashMap<>();
            List<AmbassadorCompetitionDetail> details = entry.getValue();
            int score = 0;
            for (AmbassadorCompetitionDetail detail : details) {
                score += detail.getScore();
            }
            details = details.stream().sorted((o1, o2) ->
                    Long.compare(o2.getCreateDatetime().getTime(), o1.getCreateDatetime().getTime())).collect(Collectors.toList());
            teacherScoreMap.put("score", score);
            teacherScoreMap.put("scoreDetails", details);
            teacherScoreMap.put("teacherId", entry.getKey());
            teacherScoreMap.put("teacherLevelScore",teacherExtAttributeMap.containsKey(entry.getKey()) ? SafeConverter.toInt(teacherExtAttributeMap.get(entry.getKey()).getLevelValue()) : 0);
            data.add(teacherScoreMap);
        }
        data = data.stream().sorted((o1, o2) -> {
            int s1 = SafeConverter.toInt(o1.get("score"));
            int s2 = SafeConverter.toInt(o2.get("score"));
            if (s2 == s1) {
                int l1 = SafeConverter.toInt(o1.get("teacherLevelScore"));
                int l2 = SafeConverter.toInt(o2.get("teacherLevelScore"));
                return Integer.compare(l2, l1);
            } else {
                return Integer.compare(s2, s1);
            }
        }).collect(Collectors.toList());
        return data;
    }

    //获取我的排名信息和努力值明细 只统计最近3个月的努力值
    public Map<String, Object> loadAmbassadorCompetitiondDetailRank(Long teacherId, Long schoolId, Subject subject) {
        List<Map<String, Object>> data = loadSchoolSameSubjectCompetitionDetailList(schoolId, subject);
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyMap();
        }
        Map<String, Object> myRankMap = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            if (Objects.equals(SafeConverter.toLong(data.get(i).get("teacherId")), teacherId)) {
                myRankMap = data.get(i);
                myRankMap.put("rank", i + 1);
                break;
            }
        }
        return myRankMap;
    }

    //报名参与 预备大使
    public MapMessage joinAmbassadorCompetition(TeacherDetail teacherDetail) {
        if (teacherDetail == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        AmbassadorCompetition competition = new AmbassadorCompetition();
        competition.setTeacherId(teacherDetail.getId());
        competition.setSchoolId(teacherDetail.getTeacherSchoolId());
        competition.setSubject(teacherDetail.getSubject());
        competition.setTotalScore(0);
        ambassadorServiceClient.getAmbassadorService().$insertAmbassadorCompetition(competition);
        return MapMessage.successMessage();
    }

    public MapMessage setAmbassador(TeacherDetail teacher) {
        if (teacher == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        AmbassadorSchoolRef ref = new AmbassadorSchoolRef();
        ref.setSchoolId(teacher.getTeacherSchoolId());
        ref.setAmbassadorId(teacher.getId());
        ref = ambassadorServiceClient.getAmbassadorService().$insertAmbassadorSchoolRef(ref);
        // 插入大使级别 如果以前有记录， 删除
        AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(teacher.getId());
        if (levelDetail != null) {
            ambassadorServiceClient.getAmbassadorService().$disableAmbassadorLevelDetail(levelDetail.getId());
        }
        levelDetail = new AmbassadorLevelDetail();
        levelDetail.setSchoolId(teacher.getTeacherSchoolId());
        levelDetail.setAmbassadorId(teacher.getId());
        levelDetail.setLevel(AmbassadorLevel.SHI_XI);
        ambassadorServiceClient.getAmbassadorService().$insertAmbassadorLevelDetail(levelDetail);
        // 如果有预备大使记录  删除
        AmbassadorCompetition competition = ambassadorLoaderClient.getAmbassadorLoader().loadTeacherAmbassadorCompetition(teacher.getId());
        if (competition != null) {
            ambassadorServiceClient.getAmbassadorService().$disableAmbassadorCompetition(competition.getId());
        }
        asyncUserServiceClient.getAsyncUserService()
                .evictUserCache(teacher.getId())
                .awaitUninterruptibly();
        // 将新老师的激活请求改为校园大使的
        businessTeacherService.changeActivationType(teacher.getId(), true);
        return MapMessage.successMessage();
    }

    public MapMessage applySchoolAmbassador(SchoolAmbassador schoolAmbassador) {
        try {
            if (schoolAmbassadorServiceClient.getSchoolAmbassadorService()
                    .loadSchoolAmbassadorByUserId(schoolAmbassador.getUserId())
                    .getUninterruptibly() != null) {
                return MapMessage.successMessage();
            }
            schoolAmbassador = schoolAmbassadorServiceClient.getSchoolAmbassadorService()
                    .insertSchoolAmbassador(schoolAmbassador)
                    .getUninterruptibly();
            return MapMessage.successMessage().add("ambassador", schoolAmbassador);
        } catch (Exception ex) {
            logger.error("Failed to apply school ambassador", ex);
            return MapMessage.errorMessage("申请失败");
        }
    }

    // 实习大使 首页显示信息
    public Map<String, Object> loadAmbassadorSHXInfo(TeacherDetail ambassador) {
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(ambassador.getId())
                .stream().findFirst().orElse(null);
//        List<Teacher> teachers = loadCurrentSchoolSameSubjectAuthticationTeachers(ambassador, ref.getCreateDatetime());
//        List<Map<String, Object>> allTeacherList = new LinkedList<>();
//        Map<Long, UserTag> userTagMap = userTagLoaderClient.loadUserTags(teachers.stream().map(Teacher::getId).collect(Collectors.toList()));
        Map<String, Object> dataMap = new HashMap<>();
//        // 本月已点亮图标人数
//        int finishCount = 0;
//        for (Teacher teacher : teachers) {
//            if (null == teacher) continue;
//            //过滤掉暂停的老师
//            if (teacher.getPending() != null && teacher.getPending() == 1) {
//                continue;
//            }
//            Map<String, Object> each = new HashMap<>();
//            each.put("name", StringUtils.defaultString(teacher.getProfile().getRealname()));
//            each.put("userId", teacher.getId());
//            each.put("img", teacher.fetchImageUrl());
//            //查询tag
//            getLightIconInfo(each, userTagMap.get(teacher.getId()), ref, AmbassadorLevel.SHI_XI);
//            if (each.get("lightCount") != null && SafeConverter.toInt(each.get("lightCount")) >= 5) {
//                finishCount++;
//            }
//            allTeacherList.add(each);
//        }
        // 处理大使自己的信息
        Map<String, Object> ambassadorInfo = getAmbassadorInfoSX(ambassador, ref);
        dataMap.put("myInfo", ambassadorInfo);
//        allTeacherList = allTeacherList.stream().sorted((o1, o2) ->
//                Integer.compare(SafeConverter.toInt(o1.get("lightCount")), SafeConverter.toInt(o2.get("lightCount")))).collect(Collectors.toList());
//        dataMap.put("teacherList", allTeacherList);
//        dataMap.put("finishCount", finishCount);
        // 获取本学科预备大使数量
        List<AmbassadorCompetition> competitions = ambassadorLoaderClient.getAmbassadorLoader().findSchoolAmbassadorCompetitions(ambassador.getTeacherSchoolId());
        dataMap.put("competitionCount", 0);
        if (CollectionUtils.isNotEmpty(competitions)) {
            competitions = competitions.stream().filter(c -> c.getSubject() == ambassador.getSubject()).collect(Collectors.toList());
            dataMap.put("competitionCount", competitions.size());
        }
        // 查询积分（总分本月）
        int totalScore = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorTotalScore(ambassador.getId(), MonthRange.current().getStartDate());
        dataMap.put("totalScore", totalScore);
        return dataMap;
    }

    private Map<String, Object> getAmbassadorInfoSX(TeacherDetail ambassador, AmbassadorSchoolRef ref) {
        UserTag userTag = userTagLoaderClient.loadUserTag(ambassador.getId());
        Map<String, Object> each = new HashMap<>();
        each.put("name", StringUtils.defaultString(ambassador.getProfile().getRealname()));
        each.put("userId", ambassador.getId());
        each.put("img", ambassador.fetchImageUrl());
        //查询tag
        getLightIconInfo(each, userTag, ref, AmbassadorLevel.SHI_XI);
        return each;
    }

    // 正式大使 首页显示信息
    public Map<String, Object> loadAmbassadorZSInfo(TeacherDetail ambassador) {
        List<Teacher> teachers = loadCurrentSchoolSameSubjectAuthticationTeachers(ambassador, MonthRange.current().getStartDate());
        List<Map<String, Object>> allTeacherList = new LinkedList<>();
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(ambassador.getId())
                .stream().findFirst().orElse(null);
        List<Long> allTeacherIds = teachers.stream().map(Teacher::getId).collect(Collectors.toList());
        Map<Long, UserTag> userTagMap = userTagLoaderClient.loadUserTags(allTeacherIds);
        Map<String, Object> dataMap = new HashMap<>();
        Map<Long, Long> effectiveMap = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCounts(UserBehaviorType.TEACHER_MONTH_EFFECTIVE_HW_COUNT, allTeacherIds)
                .getUninterruptibly();
        int finishCount = 0;
        int effTotalCount = 0;
        for (Teacher teacher : teachers) {
            if (null == teacher) continue;
//            // 过滤掉暂停的老师
//            if (teacher.getPending() != null && teacher.getPending() == 1) {
//                if (effectiveMap.get(teacher.getId()) != null) {
//                    effectiveMap.remove(teacher.getId());
//                }
//                continue;
//            }
            Map<String, Object> each = new HashMap<>();
            each.put("name", StringUtils.defaultString(teacher.getProfile().getRealname()));
            each.put("userId", teacher.getId());
            each.put("img", teacher.fetchImageUrl());
            //查询tag
            getLightIconInfo(each, userTagMap.get(teacher.getId()), ref, AmbassadorLevel.JIN_PAI);
            // 查询老师本月有效作业数量
            int effCount = effectiveMap.get(teacher.getId()) == null ? 0 : effectiveMap.get(teacher.getId()).intValue();
            if (effCount > 0) {
                effTotalCount++;
            }
            each.put("effCount", effCount);
            if (each.get("lightCount") != null && SafeConverter.toInt(each.get("lightCount")) >= 5) {
                finishCount++;
            }
            allTeacherList.add(each);
        }
        // 大使本人信息
        dataMap.put("myInfo", getAmbassadorInfoZS(ambassador, ref, effectiveMap));
        allTeacherList = allTeacherList.stream().sorted((o1, o2) ->
                Integer.compare(SafeConverter.toInt(o1.get("lightCount")), SafeConverter.toInt(o2.get("lightCount")))).collect(Collectors.toList());
        dataMap.put("teacherList", allTeacherList);
        // 获取本学科预备大使数量
        List<AmbassadorCompetition> competitions = ambassadorLoaderClient.getAmbassadorLoader().findSchoolAmbassadorCompetitions(ambassador.getTeacherSchoolId());
        dataMap.put("competitionCount", 0);
        if (CollectionUtils.isNotEmpty(competitions)) {
            competitions = competitions.stream().filter(c -> c.getSubject() == ambassador.getSubject())
                    .filter(c -> !Objects.equals(c.getTeacherId(), ambassador.getId())).collect(Collectors.toList());
            dataMap.put("competitionCount", competitions.size());
        }
        // 本月已点亮图标人数
        dataMap.put("finishCount", finishCount);
        // 查询积分（总分本月）
        int totalScore = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorTotalScore(ambassador.getId(), MonthRange.current().getStartDate());
//        AmbassadorLevelDetail levelDetail = loadAmbassadorLevelDetail(ambassador.getId());
//        if (levelDetail != null) {
//            if (levelDetail.getLevel() == AmbassadorLevel.YIN_PAI) {
//                totalScore = new BigDecimal(totalScore).multiply(new BigDecimal(1.2)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
//            }
//            if (levelDetail.getLevel() == AmbassadorLevel.JIN_PAI) {
//                totalScore = new BigDecimal(totalScore).multiply(new BigDecimal(1.5)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
//            }
//        }
        dataMap.put("totalScore", totalScore);
        // 任务相关
//        dataMap.put("missionMap", getMissionMap(effectiveMap, ambassador.getId()));
        // 本月已布置有效作业老师 加入大使自己的信息
        Long effCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCount(UserBehaviorType.TEACHER_MONTH_EFFECTIVE_HW_COUNT, ambassador.getId(), 0)
                .getUninterruptibly();
        if (effCount > 0) {
            effTotalCount += 1;
        }
        dataMap.put("effTotalCount", effTotalCount);
        return dataMap;
    }

    private Map<String, Object> getAmbassadorInfoZS(TeacherDetail ambassador, AmbassadorSchoolRef ref, Map<Long, Long> effectiveMap) {
        UserTag userTag = userTagLoaderClient.loadUserTag(ambassador.getId());
        Long effCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCount(UserBehaviorType.TEACHER_MONTH_EFFECTIVE_HW_COUNT, ambassador.getId(), 0)
                .getUninterruptibly();
        Map<String, Object> each = new HashMap<>();
        each.put("name", StringUtils.defaultString(ambassador.getProfile().getRealname()));
        each.put("userId", ambassador.getId());
        each.put("img", ambassador.fetchImageUrl());
        each.put("effCount", effCount);
        each.put("overCount", effectiveMap.values().stream().filter(e -> effCount < (e == null ? 0 : e)).collect(Collectors.toList()).size());
        //查询tag
        getLightIconInfo(each, userTag, ref, AmbassadorLevel.JIN_PAI);
        return each;
    }

    // 获取当前老师图标点亮情况
    private void getLightIconInfo(Map<String, Object> each, UserTag tag, AmbassadorSchoolRef ref, AmbassadorLevel level) {
        if (tag == null) {
            return;
        }
        List<String> mentorTagKey = Arrays.asList(UserTagType.AMBASSADOR_MENTOR_BBS.name(),
                UserTagType.AMBASSADOR_MENTOR_COMMENT.name(),
                UserTagType.AMBASSADOR_MENTOR_DO_LOTTERY.name(),
                UserTagType.AMBASSADOR_MENTOR_HOMEWORK.name(),
                UserTagType.AMBASSADOR_MENTOR_QUIZ.name(),
                UserTagType.AMBASSADOR_MENTOR_READING.name(),
                UserTagType.AMBASSADOR_MENTOR_REWARD_ORDER.name(),
                UserTagType.AMBASSADOR_MENTOR_REWARD_STAR.name(),
                UserTagType.AMBASSADOR_MENTOR_WECHAT_HOMEWORK.name(),
                UserTagType.AMBASSADOR_MENTOR_SMART_CLAZZ.name(),
                UserTagType.AMBASSADOR_BIND_WECHAT.name());
        Map<String, UserTag.Tag> tagMap = tag.getTags();
        int count = 0;
        for (String key : tagMap.keySet()) {
            if (mentorTagKey.contains(key)) {
                String mentorJson = tagMap.get(key).getValue();
                Map<String, Object> dataMap = JsonUtils.fromJson(mentorJson);
                Long longDate = (Long) dataMap.get("monthFirstDate");
                Date monthFirstDate = new Date(longDate);
                boolean isFirst = (boolean) dataMap.get("isFirst");
                if (getDateRange(ref, level).contains(monthFirstDate)) {
                    if (isFirst) {
                        each.put(key, "F_Y");
                    } else {
                        each.put(key, "Y");
                    }
                    count++;
                } else {
                    each.put(key, "N");
                }
            }
        }
        each.put("lightCount", count);
    }

    // 根据不同的大使级别 获取对应的 图标统计时间
    public DateRange getDateRange(AmbassadorSchoolRef ref, AmbassadorLevel level) {
        if (level == AmbassadorLevel.SHI_XI) {
            // 实习大使统计时间 是 大使的实习期
            return new DateRange(ref.getCreateDatetime(), DateUtils.calculateDateDay(ref.getCreateDatetime(), 30));
        } else {
            return MonthRange.current();
        }
    }

    // 记录大使每个月邀请成功人数
    public void addInviteCountMonth(Long ambassadorId) {
        // 不是大使不记录
        AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorSchoolRefs(ambassadorId)
                .stream().findFirst().orElse(null);
        if (ref == null) {
            return;
        }
        // 实习大使不记录
        AmbassadorLevelDetail level = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(ambassadorId);
        if (level == null || level.getLevel() == AmbassadorLevel.SHI_XI) {
            return;
        }
        asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_incUserBehaviorCount(UserBehaviorType.AMBASSADOR_MONTH_INVITE_COUNT, ambassadorId, 1L, MonthRange.current().getEndDate())
                .awaitUninterruptibly();
    }

    // 一键提醒老师
    public MapMessage remindTeacherForEffectHw(TeacherDetail ambassador) {
        List<Teacher> teachers = loadCurrentSchoolSameSubjectAuthticationTeachers(ambassador, MonthRange.current().getStartDate());
        List<Long> allTeacherIds = teachers.stream().map(Teacher::getId).collect(Collectors.toList());
        Map<Long, Long> effectiveMap = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCounts(UserBehaviorType.TEACHER_MONTH_EFFECTIVE_HW_COUNT, allTeacherIds)
                .getUninterruptibly();
        Map<Long, Long> remindMap = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCounts(UserBehaviorType.AMBASSADOR_MONTH_REMIND_TEACHER_COUNT, allTeacherIds)
                .getUninterruptibly();
        int remindCount = 0;
        int noEffCount = 0;
        for (Map.Entry<Long, Long> entry : effectiveMap.entrySet()) {
            Long userId = entry.getKey();
            int effCount = entry.getValue() == null ? 0 : entry.getValue().intValue();
            if (effCount == 0) {
                noEffCount++;
                if (remindMap.get(userId) == null || remindMap.get(userId) == 0) {
                    // 提醒
                    String content = "校园大使温馨提醒您：嗨，这个月还没布置作业，或者布置了但完成的学生不多？是不是有什么问题，我愿意为你解答！";
                    userPopupServiceClient.createPopup(userId).content(content).type(PopupType.AMBASSADOR_REMIND_TEACHER_FOR_EFFECT_HW).category(LOWER_RIGHT).create();
                    // 记录
                    asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                            .persistence_incUserBehaviorCount(UserBehaviorType.AMBASSADOR_MONTH_REMIND_TEACHER_COUNT, userId, 1L, MonthRange.current().getEndDate())
                            .awaitUninterruptibly();
                    remindCount++;
                }
            }
        }
        if (remindCount > 0) {
            return MapMessage.successMessage("提醒成功");
        } else if (noEffCount > 0) {
            return MapMessage.errorMessage("本月已经提醒过所有老师了。");
        } else {
            return MapMessage.errorMessage("本月所有老师都已布置有效作业，您已打败95%的校园大使！");
        }
    }

    // 一键点赞老师
    public MapMessage praiseTeacherForEffectHw(TeacherDetail ambassador) {
        List<Teacher> teachers = loadCurrentSchoolSameSubjectAuthticationTeachers(ambassador, MonthRange.current().getStartDate());
        List<Long> allTeacherIds = teachers.stream().map(Teacher::getId).collect(Collectors.toList());
        Map<Long, Long> effectiveMap = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCounts(UserBehaviorType.TEACHER_MONTH_EFFECTIVE_HW_COUNT, allTeacherIds)
                .getUninterruptibly();
        Map<Long, Long> priaseMap = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .persistence_getUserBehaviorCounts(UserBehaviorType.AMBASSADOR_MONTH_PRIASE_TEACHER_COUNT, allTeacherIds)
                .getUninterruptibly();
        for (Map.Entry<Long, Long> entry : effectiveMap.entrySet()) {
            Long userId = entry.getKey();
            int effCount = entry.getValue() == null ? 0 : entry.getValue().intValue();
            if (effCount > 0) {
                if (priaseMap.get(userId) == null || priaseMap.get(userId) == 0) {
                    // 表扬
                    String content = "校园大使赞了您：这个月布置作业辛苦了，有付出就会有回报！";
                    userPopupServiceClient.createPopup(userId).content(content).type(PopupType.AMBASSADOR_PRIASE_TEACHER_FOR_EFFECT_HW).category(LOWER_RIGHT).create();
                    // 记录
                    asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                            .persistence_incUserBehaviorCount(UserBehaviorType.AMBASSADOR_MONTH_PRIASE_TEACHER_COUNT, userId, 1L, MonthRange.current().getEndDate())
                            .awaitUninterruptibly();
                }
            }
        }
        return MapMessage.successMessage("点赞成功");
    }

    // 辞任校园大使
    public MapMessage resignationAmbassador(TeacherDetail detail) {
        // 删除校园大使
        ambassadorServiceClient.getAmbassadorService().$disableAmbassadorSchoolRef(detail.getId());
        // 删除校园大使积分
        ambassadorServiceClient.getAmbassadorService().$disableAmbassadorScoreHistories(detail.getId());
        // 删除校园大使级别
        AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(detail.getId());
        ambassadorServiceClient.getAmbassadorService().$disableAmbassadorLevelDetail(levelDetail.getId());
        asyncUserServiceClient.getAsyncUserService()
                .evictUserCache(detail.getId())
                .awaitUninterruptibly();

        List<Map<String, Object>> data = loadSchoolSameSubjectCompetitionDetailList(detail.getTeacherSchoolId(), detail.getSubject());
        if (CollectionUtils.isNotEmpty(data)) {
            // 取第一名
            Map<String, Object> firstTeacher = MiscUtils.firstElement(data);
            TeacherDetail ambassador = teacherLoaderClient.loadTeacherDetail(SafeConverter.toLong(firstTeacher.get("teacherId")));
            // 设置为大使
            MapMessage message = setAmbassador(ambassador);
            if (message.isSuccess()) {
                // 提醒 短信 右下角
                String comment = StringUtils.formatMessage("原校园大使已卸任，恭喜您成为新的校园大使。马上到<a href='/ambassador/center.vpage'>『校园大使』</a>页面看看吧！");
                userPopupServiceClient.createPopup(ambassador.getId())
                        .content(comment)
                        .type(PopupType.AMBASSADOR_REMIND)
                        .category(PopupCategory.LOWER_RIGHT)
                        .create();
                UserAuthentication authentication = userLoaderClient.loadUserAuthentication(ambassador.getId());
                if (authentication != null && authentication.isMobileAuthenticated()) {
                    // 发短信
                    userSmsServiceClient.buildSms().to(authentication)
                            .content("原校园大使已卸任，恭喜您成为新的校园大使。马上到『校园大使』页面看看吧！")
                            .type(SmsType.AMBASSADOR_REMIND_SMS)
                            .send();
                }
            }
        }
        return MapMessage.successMessage("您已卸任实习大使，谢谢您对一起作业的支持。");
    }

    // 获取大使积分明细 本月
    public List<Map<String, Object>> loadAmbassadorScoreHistory(Long ambassadorId) {
        List<AmbassadorScoreHistory> histories = ambassadorLoaderClient.getAmbassadorLoader().loadScoreHistory(ambassadorId, MonthRange.current().getStartDate());
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(histories)) {
            return dataList;
        }
        histories = histories.stream().sorted((o1, o2) -> {
            long t1 = o1.getCreateDatetime().getTime();
            long t2 = o2.getCreateDatetime().getTime();
            return Long.compare(t2, t1);
        }).collect(Collectors.toList());
        for (AmbassadorScoreHistory history : histories) {
            Map<String, Object> data = new HashMap<>();
            data.put("createDatetime", DateUtils.dateToString(history.getCreateDatetime(), "yyyy-MM-dd"));
            data.put("score", history.getScore());
            data.put("scoreType", history.getScoreType().getDescription());
            dataList.add(data);
        }
        return dataList;
    }

    public List<Teacher> loadCurrentSchoolSameSubjectAuthticationTeachers(TeacherDetail teacherDetail, Date endDate) {
        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(teacherDetail.getTeacherSchoolId());
        // 过滤 同学科老师  以及 本月之前认证的老师 不包括大使
        teachers = teachers.stream().filter(t -> t.getSubject() == teacherDetail.getSubject())
                .filter(t -> t.fetchCertificationState() == AuthenticationState.SUCCESS)
                .filter(t -> !Objects.equals(teacherDetail.getId(), t.getId()))
                .filter(t -> t.getLastAuthDate() == null || t.getLastAuthDate().before(endDate)).collect(Collectors.toList());
        return teachers;
    }

    public MapMessage reportTeacher(TeacherDetail teacher, Long teacherId, String teacherName, String reason, AmbassadorReportType type) {
        try {
            // 处理举报非认证老师流程
            if (type == AmbassadorReportType.REPORT_UN_AUTH_TEACEHR) {
                // 不是假老师
                if (!teacherLoaderClient.isFakeTeacher(teacherId)) {
                    // 先看是否为模型判假
                    CrmTeacherSummary summary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);
                    if (summary != null && summary.getValidationType() != null && summary.getValidationType().equals(CrmTeacherFakeValidationType.FAKE_BY_MODEL.name())) {
                        // 如果是模型判假的老师 直接判假
                        MapMessage message = crmSummaryServiceClient.updateTeacherFakeType(teacherId, CrmTeacherFakeValidationType.MANUAL_VALIDATION, "老师举报判假");
                        if (message.isSuccess()) {
                            // 发送申诉消息
                            miscService.sendFakeAppealMessage(teacherId);
                        }
                        // 记录USER_RECORD
                        UserServiceRecord userServiceRecord = new UserServiceRecord();
                        userServiceRecord.setUserId(teacherId);
                        userServiceRecord.setOperatorId("system");
                        userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
                        userServiceRecord.setOperationContent("假老师模型判定");
                        userServiceRecord.setComments("该老师被举报，举报人ID（" + teacher.getId() + "），举报原因：" + reason);
                        userServiceClient.saveUserServiceRecord(userServiceRecord);
                        return MapMessage.successMessage("申请成功");
                    } else {
                        // 查询15天是否作业活跃
                        List<UserActivity> activityList = userActivityServiceClient.getUserActivityService()
                                .findUserActivities(teacherId)
                                .getUninterruptibly();
                        UserActivity activity = activityList.stream().filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                                .filter(a -> a.getActivityTime().after(DateUtils.calculateDateDay(new Date(), -15))).findFirst().orElse(null);
                        if (activity == null) {
                            // 15天没有检查过作业
                            MapMessage message = crmSummaryServiceClient.updateTeacherFakeType(teacherId, CrmTeacherFakeValidationType.MANUAL_VALIDATION, "老师举报判假");
                            if (message.isSuccess()) {
                                // 发送申诉消息
                                miscService.sendFakeAppealMessage(teacherId);
                            }
                            // 记录USER_RECORD
                            UserServiceRecord userServiceRecord = new UserServiceRecord();
                            userServiceRecord.setUserId(teacherId);
                            userServiceRecord.setOperatorId("system");
                            userServiceRecord.setOperationType(UserServiceRecordOperationType.老师判假.name());
                            userServiceRecord.setOperationContent("非活跃老师判定");
                            userServiceRecord.setComments("该老师被举报，举报人ID（" + teacher.getId() + "），举报原因：" + reason);
                            userServiceClient.saveUserServiceRecord(userServiceRecord);
                            return MapMessage.successMessage("申请成功");
                        } else {
                            TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(teacherId);
                            // 推送到假老师审核
                            CrmTeacherFake fake = new CrmTeacherFake();
                            fake.setTeacherId(teacherId);
                            fake.setTeacherName(detail.fetchRealname());
                            fake.setSchoolName(detail.getTeacherSchoolName());
                            fake.setFakeNote("（" + teacher.getId() + "）" + teacher.fetchRealname() + "举报，原因：" + reason);
                            fake.setReviewStatus(ReviewStatus.WAIT);
                            crmSummaryServiceClient.saveCrmTeacherFake(fake);
                            return MapMessage.successMessage("申请成功");
                        }
                    }
                }
            }
            if (type == AmbassadorReportType.APPLY_CANCLE_TEACHER_AUTH) {
                // 发短信
                if ("转校/转校区".equals(reason) || "同校换科目".equals(reason)) {
                    UserAuthentication authentication = userLoaderClient.loadUserAuthentication(teacherId);
                    if (authentication != null && authentication.isMobileAuthenticated()) {
                        String content = "校园大使申请取消您的认证老师资格，原因是您已转校/转校区。如在新学校继续使用一起作业账号，请联系客服400-160-1717；否则3日后取消认证。";
                        if ("同校换科目".equals(reason)) {
                            content = "校园大使申请取消您的认证老师资格，原因是您已换科目。如继续使用一起作业账号，请联系客服400-160-1717；否则3日后取消认证。";
                        }
                        userSmsServiceClient.buildSms().to(authentication)
                                .type(SmsType.AMBASSADOR_REPORT_NOTICE)
                                .content(content)
                                .send();
                    }
                }
                // 记录
                AmbassadorReportInfo info = new AmbassadorReportInfo();
                info.setTeacherName(teacherName);
                info.setReason(reason);
                info.setTeacherId(teacherId);
                info.setReportId(teacher.getId());
                info.setType(type.getType());
                info.setStatus(AmbassadorReportStatus.REPORTING);
                ambassadorServiceClient.getAmbassadorService().$insertAmbassadorReportInfo(info);
            }
            return MapMessage.successMessage("申请成功");
        } catch (Exception ex) {
            logger.error("report teacher error, error is " + ex);
            return MapMessage.errorMessage("申请失败");
        }
    }

    public AmbassadorReportStudentFeedback loadAmbassadorStudentFeedBack(Long studentId, Long teacherId, Date reportDate) {
        List<AmbassadorReportStudentFeedback> feedbacks = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorReportStudentFeedbacks(teacherId, studentId);
        if (CollectionUtils.isEmpty(feedbacks)) {
            return null;
        }
        return feedbacks.stream().filter(k -> k.getCreateDatetime().after(reportDate)).findFirst().orElse(null);
    }
}
