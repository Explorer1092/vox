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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import com.voxlearning.utopia.business.api.entity.BizStudentVoice;
import com.voxlearning.utopia.entity.activity.StudentLuckyBagRecord;
import com.voxlearning.utopia.service.business.api.entity.ExamAnswer;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "3.0.STABLE")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface BusinessStudentService extends IPingable {

    MapMessage loadStudentIndexData(StudentDetail student);

    Map<String, Object> loadStudentAppIndexData(StudentDetail student, String ver, String sys);

    Map<String, Object> loadStudentIndexDataForSpg(StudentDetail student);

    List<Map<String, Object>> getStudentSelfStudyDefaultBooks(StudentDetail student);

    Map<String, Object> getEnglishSelfStudyBook(Long bookId);

    // ========================================================================
    // StudentClazzService
    // ========================================================================

    @Deprecated
    MapMessage joinClazz_findClazzInfo(Long id, Set<Ktwelve> allowedKtwelve);

//    List<Map<String, Object>> findByStudentNameAndSchoolId(Long schoolId, String studentName);

    // ========================================================================
    // StudentVoiceService
    // ========================================================================

    List<BizStudentVoice> loadClazzStudentVoices(Collection<Long> clazzIds);

    // ========================================================================
    // StudentInviteSendLogService
    // ========================================================================

//    @Deprecated
//    List<StudentInviteSendLog> loadStudentInviteSendLogBySenderIdAndCreateDatetime(Long senderId, Date createDatetime);

//    @Deprecated
//    List<StudentInviteSendLog> loadStudentInviteSendLogByMobileAndCreateDatetime(String mobile, Date createDatetime);

//    @Deprecated
//    Long createStudentInviteSendLog(StudentInviteSendLog studentInviteSendLog);

    // ========================================================================
    // StudentAppInteractiveInfoService
    // ========================================================================

    MapMessage findClazzRank(Long clazzId, Long bookId, Long unitId, Long lessonId, Long practiceId);

    MapMessage saveJuniorAppDetail(Long userId, Long bookId, Long unitId, Long lessonId, Long practiceId, Integer score, Map<String, Object> dataJson);

    // ========================================================================
    // StudentParentRewardServcie
    // ========================================================================

    MapMessage studentMakeWish(Long studentId, WishType wishType, String wish);

    MapMessage studentSendWechatNotice(Long studentId, Long missionId, String template);

    @Deprecated
    MapMessage studentCheckDetail(Long studentId, Long missionId);

    @Deprecated
    boolean updateMissionImg(Long missionId, String filename);

    MapMessage parentSetMission(Long parentId, Long studentId, WishType wishType, String wish,
                                Integer totalCount, String mission, MissionType missionType, Long missionId);

    MapMessage parentUpdateProgress(Long parentId, Long missionId);

    MapMessage parentUpdateComplete(Long parentId, Long missionId);

    boolean isCurrentMonthIntegralMissionArranged(Long studentId);

    // ========================================================================
    // mothers day
    // ========================================================================

    @Deprecated
    MapMessage getMothersDayCard(User student, Boolean dataInclued);

    @Deprecated
    MapMessage giveMothersDayCardAsGift(User student, String image, String voice);

    @Deprecated
    MapMessage shareMothersDayCard(Long studentId);

    @Deprecated
    void updateMothersDayCardSended(Long studentId);


    // ========================================================================
    // flower
    // ========================================================================

    List<Map<String, Object>> findCurrentMonthFlowerRankByTeacherIdAndClazzId(Long teacherId, Long clazzId);

    List<Map<String, Object>> findCurrentMonthFlowerRankBySchoolId(Long schoolId, Subject subject);

    int findLastMonthFlowerRankInSchoolByTeacherId(Long teacherId);

    // ========================================================================
    // o2o扫码提交答案  天津教辅
    // ========================================================================

    /**
     * @deprecated No more references
     */
    @Deprecated
    List<ExamAnswer> getExamAnswerByExamId(String examId);

    String updateExamAnswerRecord(Map<String, Object> record);

    void postParentMessage(Long studentId, Long missionId, String wechatNoticeTemplate);

    MapMessage collectAmbassadorReportFeedback(Long englishId, Long mathId, boolean englishFlag, boolean mathFlag, Long studentId);

    StudentLuckyBagRecord loadLuckyBagByReceiverId(Long studentId);

    Map<Long, StudentLuckyBagRecord> loadLuckyBagByReceiverIds(List<Long> studentIds);

    List<StudentLuckyBagRecord> loadLuckyBagBySenderId(Long studentId);

    Map<String, Object> loadStudentLuckyBagIndexData(Long studentId, Long clazzId);

    MapMessage sendLuckyBag(Long studentId, String receiverIds, Long clazzId);

    MapMessage openLuckyBag(Long studentId);

    MapMessage receiveLuckyBag(Long studentId);

}
