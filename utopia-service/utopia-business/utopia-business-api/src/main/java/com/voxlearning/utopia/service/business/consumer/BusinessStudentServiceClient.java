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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.business.api.BusinessStudentService;
import com.voxlearning.utopia.business.api.entity.BizStudentVoice;
import com.voxlearning.utopia.entity.activity.StudentLuckyBagRecord;
import com.voxlearning.utopia.service.business.api.entity.ExamAnswer;
import com.voxlearning.utopia.service.business.cache.BusinessCache;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.slf4j.Logger;

import java.util.*;

public class BusinessStudentServiceClient implements BusinessStudentService {
    private static final Logger logger = LoggerFactory.getLogger(BusinessStudentServiceClient.class);

    @ImportService(interfaceClass = BusinessStudentService.class)
    private BusinessStudentService remoteReference;

    public Map loadStudentIndexData2(StudentDetail student) {
        if (student == null || student.getClazz() == null) {
            return Collections.emptyMap();
        }
        MapMessage message;
        try {
            message = remoteReference.loadStudentIndexData(student);
        } catch (Exception ex) {
            logger.error("FAILED TO LOAD STUDENT '{}' INDEX DATA", student.getId(), ex);
            return Collections.emptyMap();
        }
        if (message.isSuccess()) {
            return (Map) message.get("indexData");
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    @Deprecated
    public MapMessage getMothersDayCard(User student, Boolean dataIncluded) {
        return remoteReference.getMothersDayCard(student, dataIncluded);
    }

    @Override
    @Deprecated
    public MapMessage giveMothersDayCardAsGift(User student, String image, String voice) {
        return remoteReference.giveMothersDayCardAsGift(student, image, voice);
    }

    @Override
    @Deprecated
    public MapMessage shareMothersDayCard(Long studentId) {
        return remoteReference.shareMothersDayCard(studentId);
    }

    @Override
    @Deprecated
    public void updateMothersDayCardSended(Long studentId) {
        remoteReference.updateMothersDayCardSended(studentId);
    }

    @Override
    public List<Map<String, Object>> findCurrentMonthFlowerRankByTeacherIdAndClazzId(Long teacherId, Long clazzId) {
        return remoteReference.findCurrentMonthFlowerRankByTeacherIdAndClazzId(teacherId, clazzId);
    }

    @Override
    public List<Map<String, Object>> findCurrentMonthFlowerRankBySchoolId(Long schoolId, Subject subject) {
        return remoteReference.findCurrentMonthFlowerRankBySchoolId(schoolId, subject);
    }

    @Override
    public int findLastMonthFlowerRankInSchoolByTeacherId(Long teacherId) {
        return remoteReference.findLastMonthFlowerRankInSchoolByTeacherId(teacherId);
    }

    /**
     * @deprecated No more references
     */
    @Override
    @Deprecated
    public List<ExamAnswer> getExamAnswerByExamId(String examId) {
        return remoteReference.getExamAnswerByExamId(examId);
    }

    @Override
    public String updateExamAnswerRecord(Map<String, Object> record) {
        return remoteReference.updateExamAnswerRecord(record);
    }

    @Override
    public void postParentMessage(Long studentId, Long missionId, String wechatNoticeTemplate) {
        remoteReference.postParentMessage(studentId, missionId, wechatNoticeTemplate);
    }

    @Override
    public MapMessage collectAmbassadorReportFeedback(Long englishId, Long mathId, boolean englishFlag, boolean mathFlag, Long studentId) {
        return remoteReference.collectAmbassadorReportFeedback(englishId, mathId, englishFlag, mathFlag, studentId);
    }


    public StudentLuckyBagRecord loadLuckyBagByReceiverId(Long studentId) {
        if (studentId == null) {
            return null;
        }
        String cacheKey = StudentLuckyBagRecord.ck_receiverId(studentId);
        StudentLuckyBagRecord luckyBagRecord = BusinessCache.getBusinessCache().load(cacheKey);
        if (luckyBagRecord != null) {
            return luckyBagRecord;
        } else {
            return remoteReference.loadLuckyBagByReceiverId(studentId);
        }
    }

    @Override
    public Map<Long, StudentLuckyBagRecord> loadLuckyBagByReceiverIds(List<Long> studentIds) {
        return remoteReference.loadLuckyBagByReceiverIds(studentIds);
    }

    public List<StudentLuckyBagRecord> loadLuckyBagBySenderId(Long studentId) {
        if (studentId == null) {
            return Collections.emptyList();
        }
        String cacheKey = StudentLuckyBagRecord.ck_senderId(studentId);
        List<StudentLuckyBagRecord> recordList = BusinessCache.getBusinessCache().load(cacheKey);
        if (CollectionUtils.isNotEmpty(recordList)) {
            return recordList;
        } else {
            return remoteReference.loadLuckyBagBySenderId(studentId);
        }
    }

    @Override
    public Map<String, Object> loadStudentLuckyBagIndexData(Long studentId, Long clazzId) {
        return remoteReference.loadStudentLuckyBagIndexData(studentId, clazzId);
    }

    @Override
    public MapMessage sendLuckyBag(Long studentId, String receiverIds, Long clazzId) {
        return remoteReference.sendLuckyBag(studentId, receiverIds, clazzId);
    }

    @Override
    public MapMessage openLuckyBag(Long studentId) {
        return remoteReference.openLuckyBag(studentId);
    }

    @Override
    public MapMessage receiveLuckyBag(Long studentId) {
        return remoteReference.receiveLuckyBag(studentId);
    }

    @Override
    public MapMessage loadStudentIndexData(StudentDetail student) {
        return remoteReference.loadStudentIndexData(student);
    }

    @Override
    public Map<String, Object> loadStudentAppIndexData(StudentDetail student, String ver, String sys) {
        return remoteReference.loadStudentAppIndexData(student, ver, sys);
    }

    @Override
    public Map<String, Object> loadStudentIndexDataForSpg(StudentDetail student) {
        return remoteReference.loadStudentIndexDataForSpg(student);
    }

    @Override
    public List<Map<String, Object>> getStudentSelfStudyDefaultBooks(StudentDetail student) {
        return remoteReference.getStudentSelfStudyDefaultBooks(student);
    }

    @Override
    public Map<String, Object> getEnglishSelfStudyBook(Long bookId) {
        return remoteReference.getEnglishSelfStudyBook(bookId);
    }

    @Override
    @Deprecated
    public MapMessage joinClazz_findClazzInfo(Long id, Set<Ktwelve> allowedKtwelve) {
        return remoteReference.joinClazz_findClazzInfo(id, allowedKtwelve);
    }

    @Override
    public List<BizStudentVoice> loadClazzStudentVoices(Collection<Long> clazzIds) {
        return remoteReference.loadClazzStudentVoices(clazzIds);
    }

//    @Override
//    @Deprecated
//    public List<StudentInviteSendLog> loadStudentInviteSendLogBySenderIdAndCreateDatetime(Long senderId, Date createDatetime) {
//        return remoteReference.loadStudentInviteSendLogBySenderIdAndCreateDatetime(senderId, createDatetime);
//    }
//
//    @Override
//    @Deprecated
//    public List<StudentInviteSendLog> loadStudentInviteSendLogByMobileAndCreateDatetime(String mobile, Date createDatetime) {
//        return remoteReference.loadStudentInviteSendLogByMobileAndCreateDatetime(mobile, createDatetime);
//    }
//
//    @Override
//    @Deprecated
//    public Long createStudentInviteSendLog(StudentInviteSendLog studentInviteSendLog) {
//        return remoteReference.createStudentInviteSendLog(studentInviteSendLog);
//    }

    @Override
    public MapMessage findClazzRank(Long clazzId, Long bookId, Long unitId, Long lessonId, Long practiceId) {
        return remoteReference.findClazzRank(clazzId, bookId, unitId, lessonId, practiceId);
    }

    @Override
    public MapMessage saveJuniorAppDetail(Long userId, Long bookId, Long unitId, Long lessonId, Long practiceId, Integer score, Map<String, Object> dataJson) {
        return remoteReference.saveJuniorAppDetail(userId, bookId, unitId, lessonId, practiceId, score, dataJson);
    }

    @Override
    public MapMessage studentMakeWish(Long studentId, WishType wishType, String wish) {
        return remoteReference.studentMakeWish(studentId, wishType, wish);
    }

    @Override
    public MapMessage studentSendWechatNotice(Long studentId, Long missionId, String template) {
        return remoteReference.studentSendWechatNotice(studentId, missionId, template);
    }

    @Override
    @Deprecated
    public MapMessage studentCheckDetail(Long studentId, Long missionId) {
        return remoteReference.studentCheckDetail(studentId, missionId);
    }

    @Override
    @Deprecated
    public boolean updateMissionImg(Long missionId, String filename) {
        return remoteReference.updateMissionImg(missionId, filename);
    }

    @Override
    public MapMessage parentSetMission(Long parentId, Long studentId, WishType wishType, String wish, Integer totalCount, String mission, MissionType missionType, Long missionId) {
        return remoteReference.parentSetMission(parentId, studentId, wishType, wish, totalCount, mission, missionType, missionId);
    }

    @Override
    public MapMessage parentUpdateProgress(Long parentId, Long missionId) {
        return remoteReference.parentUpdateProgress(parentId, missionId);
    }

    @Override
    public MapMessage parentUpdateComplete(Long parentId, Long missionId) {
        return remoteReference.parentUpdateComplete(parentId, missionId);
    }

    @Override
    public boolean isCurrentMonthIntegralMissionArranged(Long studentId) {
        return remoteReference.isCurrentMonthIntegralMissionArranged(studentId);
    }
}
