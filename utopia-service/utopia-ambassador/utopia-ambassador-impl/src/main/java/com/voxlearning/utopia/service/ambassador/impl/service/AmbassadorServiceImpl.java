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

package com.voxlearning.utopia.service.ambassador.impl.service;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType;
import com.voxlearning.utopia.api.constant.AmbassadorRecordType;
import com.voxlearning.utopia.service.ambassador.api.AmbassadorService;
import com.voxlearning.utopia.service.ambassador.api.document.*;
import com.voxlearning.utopia.service.ambassador.impl.loader.AmbassadorLoaderImpl;
import com.voxlearning.utopia.service.ambassador.impl.persistence.*;
import com.voxlearning.utopia.service.ambassador.impl.support.AmbassadorCacheSystem;
import com.voxlearning.utopia.service.ambassador.impl.support.AmbassadorUtils;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserTag;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserTagQueueClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType.*;

/**
 * Default exposable service implementation of {@link AmbassadorService}.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@Named
@ExposeService(interfaceClass = AmbassadorService.class)
public class AmbassadorServiceImpl implements AmbassadorService {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private AmbassadorAcademyRecordDao ambassadorAcademyRecordDao;
    @Inject private AmbassadorCacheSystem ambassadorCacheSystem;
    @Inject private AmbassadorCompetitionDao ambassadorCompetitionDao;
    @Inject private AmbassadorCompetitionDetailDao ambassadorCompetitionDetailDao;
    @Inject private AmbassadorLevelDetailDao ambassadorLevelDetailDao;
    @Inject private AmbassadorLevelHistoryDao ambassadorLevelHistoryDao;
    @Inject private AmbassadorLoaderImpl ambassadorLoader;
    @Inject private AmbassadorReportInfoDao ambassadorReportInfoDao;
    @Inject private AmbassadorReportStudentFeedbackDao ambassadorReportStudentFeedbackDao;
    @Inject private AmbassadorSchoolRefDao ambassadorSchoolRefDao;
    @Inject private AmbassadorScoreHistoryDao ambassadorScoreHistoryDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserTagLoaderClient userTagLoaderClient;
    @Inject private UserTagQueueClient userTagQueueClient;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    @Override
    public void recordAmbassadorMentor(Long teacherId, Map<UserTagType, UserTagEventType> params) {
        if (teacherId == null || MapUtils.isEmpty(params)) {
            return;
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            return;
        }
        //不是认证老师 不记录
        if (AuthenticationState.SUCCESS != teacher.fetchCertificationState()) {
            return;
        }
        //没有同科校园大使不记录
        AmbassadorSchoolRef ref = ambassadorLoader.findSameSubjectAmbassadorInSchool(teacher.getSubject(), teacher.getTeacherSchoolId());
        if (ref == null) {
            return;
        }

        int newCount = 0;
        UserTag nowTags = userTagLoaderClient.loadUserTag(teacherId);
        AmbassadorLevelDetail ambassadorLevelDetail = ambassadorLevelDetailDao.findByAmbassadorId(ref.getAmbassadorId());
        for (Map.Entry<UserTagType, UserTagEventType> entry : params.entrySet()) {
            //处理校园大使新mentor体系
            UserTag.Tag tag = nowTags == null ? null : nowTags.fetchTag(entry.getKey().name());
            if (tag == null) {
                //没有点亮过这个图标  第一次点亮
                userTagQueueClient.create(entry.getValue())
                        .user(teacher)
                        .addition("isFirst", true)
                        .addition("useTime", new Date().getTime())
                        .post();
                newCount += 1;
            } else {
                String dataMapString = tag.getValue();
                Map<String, Object> dataMap = JsonUtils.fromJson(dataMapString);
                Long longDate = (Long) dataMap.get("monthFirstDate");
                Date monthFirstDate = new Date(longDate);
                // temp fix npe when detail is null, need check with others for the logic
                if (ambassadorLevelDetail != null && !AmbassadorUtils.getDateRange(ref, ambassadorLevelDetail.getLevel()).contains(monthFirstDate)) {
                    //记录
                    userTagQueueClient.create(entry.getValue())
                            .user(teacher)
                            .addition("isFirst", false)
                            .addition("useTime", new Date().getTime())
                            .post();
                    newCount += 1;
                }
            }
        }
    }

    @Override
    public void addCompetitionScore(Long teacherId, Long targetUserId, AmbassadorCompetitionScoreType scoreType) {
        if (teacherId == null || scoreType == null) {
            return;
        }
        AmbassadorCompetition competition = ambassadorCompetitionDao.loadByTeacherId(teacherId);
        if (competition == null) {
            return;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return;
        }
        // 部分积分需要判断当前月总和
        EnumSet<AmbassadorCompetitionScoreType> supported = EnumSet.of(SMART_CLAZZ, BBS, WRITE_COMMENT, XXT);
        if (supported.contains(scoreType)) {
            Date beginDate = MonthRange.current().getStartDate();
            List<AmbassadorCompetitionDetail> histories = ambassadorCompetitionDetailDao.loadByTeacherId(teacherId);
            if (CollectionUtils.isNotEmpty(histories)) {
                histories = histories.stream()
                        .filter(h -> h.getCreateDatetime().after(beginDate))
                        .filter(h -> h.getScoreType() == scoreType).collect(Collectors.toList());
                // 计算总分
                int totalCount = 0;
                for (AmbassadorCompetitionDetail history : histories) {
                    totalCount = totalCount + history.getScore();
                }
                int limit = 5;
                if (scoreType == SMART_CLAZZ) {
                    limit = 10;
                }
                if (totalCount >= limit) {
                    return;
                }
            }
        }
        //记录明细
        AmbassadorCompetitionDetail detail = new AmbassadorCompetitionDetail();
        detail.setTeacherId(teacherId);
        detail.setScore(scoreType.getScore());
        detail.setScoreType(scoreType);
        detail.setTargetUserId(targetUserId);
        detail.setSchoolId(teacherDetail.getTeacherSchoolId());
        detail.setSubject(teacherDetail.getSubject());
        ambassadorCompetitionDetailDao.insert(detail);
    }

    @Override
    public void addAmbassadorScore(Long ambassadorId, Long targetUserId, AmbassadorCompetitionScoreType scoreType) {
        if (ambassadorId == null || scoreType == null) {
            return;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(ambassadorId);
        if (teacherDetail == null || !teacherDetail.isSchoolAmbassador()) {
            return;
        }
        // 部分积分需要判断当前月总和
        EnumSet<AmbassadorCompetitionScoreType> supported = EnumSet.of(SMART_CLAZZ, BBS, WRITE_COMMENT, XXT);
        if (supported.contains(scoreType)) {
            Date beginDate = MonthRange.current().getStartDate();
            List<AmbassadorScoreHistory> histories = ambassadorScoreHistoryDao.loadScoreHistory(ambassadorId, beginDate)
                    .stream()
                    .filter(h -> h.getScoreType() == scoreType)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(histories)) {
                // 计算总分
                int totalCount = histories.stream()
                        .filter(e -> e.getScore() != null)
                        .mapToInt(AmbassadorScoreHistory::getScore)
                        .sum();
                int limit = scoreType == SMART_CLAZZ ? 10 : 5;
                if (totalCount >= limit) {
                    return;
                }
            }
        }
        //记录明细
        AmbassadorScoreHistory history = new AmbassadorScoreHistory();
        history.setAmbassadorId(ambassadorId);
        history.setScore(scoreType.getScore());
        history.setScoreType(scoreType);
        history.setTargetUserId(targetUserId);
        ambassadorScoreHistoryDao.insert(history);
    }

    @Override
    public void addScoreOncePerDay(Long teacherId, Long targetUserId, AmbassadorCompetitionScoreType scoreType) {
        if (teacherId == null || scoreType == null) return;
        String cacheKey = CacheKeyGenerator.generateCacheKey("AmbassadorService.addScoreOncePerDay",
                new String[]{"teacherId", "scoreType"},
                new Object[]{teacherId, scoreType});
        int expiration = DateUtils.getCurrentToDayEndSecond();
        long count = SafeConverter.toLong(ambassadorCacheSystem.CBS.persistence.incr(cacheKey, 1, 1, expiration));
        if (count == 1) {
            addAmbassadorScore(teacherId, targetUserId, scoreType);
            addCompetitionScore(teacherId, targetUserId, scoreType);
        }
    }

    @Override
    public MapMessage saveUpdateAmbassadorAcademyRecord(Long ambassadorId, AmbassadorRecordType recordType) {
        AmbassadorAcademyRecord academyRecord = ambassadorAcademyRecordDao.findByAmbassadorId(ambassadorId);
        if (academyRecord == null) {
            if (recordType != AmbassadorRecordType.START_SCHOOL) {
                return MapMessage.errorMessage("请先完成入学考试后再进行下一个阶段的答题");
            }
            academyRecord = new AmbassadorAcademyRecord();
            academyRecord.setAmbassadorId(ambassadorId);
            academyRecord.setRecordType(recordType.getType());
            ambassadorAcademyRecordDao.insert(academyRecord);
            //奖励10金币
            IntegralHistory integralHistory = new IntegralHistory(ambassadorId, IntegralType.校园大使完成大使学院考试奖励金币_产品运营, 100);
            integralHistory.setComment("校园大使完成大使学院考试奖励园丁豆");
            userIntegralService.changeIntegral(integralHistory);
            return MapMessage.successMessage("操作成功");
        } else {
            int type = academyRecord.getRecordType();
            AmbassadorRecordType nextType = AmbassadorRecordType.typeOf(type + 1);
            if (nextType != recordType) {
                return MapMessage.errorMessage("请按顺序完成答题，不可跨越阶段进行答题");
            }
            academyRecord.setRecordType(nextType.getType());
            academyRecord.setUpdateDatetime(new Date());
            ambassadorAcademyRecordDao.replace(academyRecord);
            int amount = 10;
            if (recordType == AmbassadorRecordType.SENIOR) {//大四题目 奖励100
                amount = 100;
            }
            IntegralHistory integralHistory = new IntegralHistory(ambassadorId, IntegralType.校园大使完成大使学院考试奖励金币_产品运营, amount * 10);
            integralHistory.setComment("校园大使完成大使学院考试奖励园丁豆");
            userIntegralService.changeIntegral(integralHistory);
            return MapMessage.successMessage("操作成功");
        }
    }

    @Override
    public MapMessage changeAmbassadorSchool(Subject subject, AmbassadorSchoolRef sourceRef, Long targetSchoolId) {
        School targetSchool = schoolLoaderClient.getSchoolLoader()
                .loadSchool(targetSchoolId)
                .getUninterruptibly();
        if (sourceRef == null || targetSchoolId == null || targetSchool == null) {
            return MapMessage.errorMessage("参数错误");
        }
        // 修改原学校的预备大使到新学校
        ambassadorCompetitionDao.updateCompetitionSchool(subject, sourceRef.getSchoolId(), targetSchoolId);
        // 修改原学校的预备大使积分到新学校
        ambassadorCompetitionDetailDao.updateCompetitionDetailSchool(subject, sourceRef.getSchoolId(), targetSchoolId);
        // 修改学校
        sourceRef.setSchoolId(targetSchoolId);
        sourceRef = ambassadorSchoolRefDao.replace(sourceRef);

        // 修改等级
        AmbassadorLevelDetail levelDetail = ambassadorLevelDetailDao.findByAmbassadorId(sourceRef.getAmbassadorId());
        levelDetail.setSchoolId(targetSchoolId);
        levelDetail.setUpdateDatetime(new Date());
        ambassadorLevelDetailDao.replace(levelDetail);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage disableAmbassador(Subject subject, AmbassadorSchoolRef sourceRef, Long targetSchoolId) {
        School targetSchool = schoolLoaderClient.getSchoolLoader()
                .loadSchool(targetSchoolId)
                .getUninterruptibly();
        if (sourceRef == null || targetSchoolId == null || targetSchool == null) {
            return MapMessage.errorMessage("参数错误");
        }
        // 删除校园大使
        ambassadorSchoolRefDao.disabledByAmbassadorId(sourceRef.getAmbassadorId());
        // 删除校园大使积分
        ambassadorScoreHistoryDao.disableAmbassadorScore(sourceRef.getAmbassadorId());
        // 删除校园大使级别
        AmbassadorLevelDetail levelDetail = ambassadorLevelDetailDao.findByAmbassadorId(sourceRef.getAmbassadorId());
        ambassadorLevelDetailDao.disabled(levelDetail.getId());
        // 修改原学校的预备大使到新学校
        ambassadorCompetitionDao.updateCompetitionSchool(subject, sourceRef.getSchoolId(), targetSchoolId);
        // 修改原学校的预备大使积分到新学校
        ambassadorCompetitionDetailDao.updateCompetitionDetailSchool(subject, sourceRef.getSchoolId(), targetSchoolId);
        return MapMessage.successMessage();
    }

    // ========================================================================
    // dollar methods
    // ========================================================================

    @Override
    public AmbassadorSchoolRef $insertAmbassadorSchoolRef(AmbassadorSchoolRef document) {
        ambassadorSchoolRefDao.insert(document);
        return document;
    }

    @Override
    public AmbassadorSchoolRef $replaceAmbassadorSchoolRef(AmbassadorSchoolRef document) {
        return ambassadorSchoolRefDao.replace(document);
    }

    @Override
    public void $disableAmbassadorSchoolRef(Long ambassadorId) {
        ambassadorSchoolRefDao.disabledByAmbassadorId(ambassadorId);
    }

    @Override
    public void $disableAmbassadorScoreHistories(Long ambassadorId) {
        ambassadorScoreHistoryDao.disableAmbassadorScore(ambassadorId);
    }

    @Override
    public AmbassadorCompetition $insertAmbassadorCompetition(AmbassadorCompetition document) {
        if (document == null) return null;
        ambassadorCompetitionDao.insert(document);
        return document;
    }

    @Override
    public boolean $disableAmbassadorCompetition(Long id) {
        return id != null && ambassadorCompetitionDao.disabled(id) > 0;
    }

    @Override
    public AmbassadorLevelDetail $insertAmbassadorLevelDetail(AmbassadorLevelDetail document) {
        if (document == null) return null;
        ambassadorLevelDetailDao.insert(document);
        return document;
    }

    @Override
    public AmbassadorLevelDetail $replaceAmbassadorLevelDetail(AmbassadorLevelDetail document) {
        if (document == null) return null;
        return ambassadorLevelDetailDao.replace(document);
    }

    @Override
    public boolean $disableAmbassadorLevelDetail(Long id) {
        return id != null && ambassadorLevelDetailDao.disabled(id) > 0;
    }

    @Override
    public AmbassadorReportInfo $insertAmbassadorReportInfo(AmbassadorReportInfo document) {
        if (document == null) return null;
        ambassadorReportInfoDao.insert(document);
        return document;
    }

    @Override
    public AmbassadorReportInfo $replaceAmbassadorReportInfo(AmbassadorReportInfo document) {
        if (document == null) return null;
        return ambassadorReportInfoDao.replace(document);
    }

    @Override
    public boolean $disableAmbassadorReportInfo(Long id) {
        return id != null && ambassadorReportInfoDao.deleteById(id) > 0;
    }

    @Override
    public AmbassadorReportStudentFeedback $insertAmbassadorReportStudentFeedback(AmbassadorReportStudentFeedback document) {
        if (document == null) return null;
        ambassadorReportStudentFeedbackDao.insert(document);
        return document;
    }

    @Override
    public AmbassadorLevelHistory $insertAmbassadorLevelHistory(AmbassadorLevelHistory document) {
        if (document == null) return null;
        ambassadorLevelHistoryDao.insert(document);
        return document;
    }
}
