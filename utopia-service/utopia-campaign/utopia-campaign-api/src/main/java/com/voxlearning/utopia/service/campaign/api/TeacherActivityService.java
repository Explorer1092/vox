package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.entity.activity.MathActivityRecord;
import com.voxlearning.utopia.service.campaign.api.entity.*;
import com.voxlearning.utopia.service.campaign.api.mapper.CourseInvitation;
import com.voxlearning.utopia.service.campaign.api.mapper.MathActivityConfig;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181119")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TeacherActivityService {

    String YQJT_ALL_RAW_CACHE_KEY = "YQJT_RESOURCE_RAW_NEW:20180918";

    int TAM_RANK_STUN_LIMIT = (RuntimeMode.isProduction()) ? 10 : 2;
    int TAM_RANK_FINN_LIMIT = (RuntimeMode.isProduction()) ? 10 : 2;
    int TOTAL_RANK_PHASE = 100;

    int QIYIJI_PRICE = 1000; // 一起讲堂视频价格

    // 2017 寒假抽奖活动 -----------------------------

    TeacherVocationLottery loadTeacherVocationLottery(Long teacherId);

    MapMessage updateTeacherVocationLottery(TeacherVocationLottery lotteryRecord);

    MapMessage incTVLRecordFields(Long teacherId, Map<String,Object> fieldDeltaMap);

    // ------------------- 泰安 - 数学活动趣味周 ------------------------------
    List<MathActivityRecord> loadTeacherRecords(Long teacherId);

    int gupSelectedClazz(Long teacherId, Integer clazz);

    MathActivityConfig loadActivityConfig();

    int getCurrentPhase();

    List<MathActivityRecord> loadActivityRank(int phase,int clazz);

    MapMessage upsertMathMatchRecord(MathActivityRecord record);

    MapMessage flushMathMatchRank(int phase, int clazz);

    // ----------------------- 一起讲堂 -----------------------------

    MapMessage load17JTUserCourse(Long teacherId,Long courseId);

    Date loadCourseBuyTime(Long teacherId, Long courseId);

    /**
     * 测试或者是调试的时候用
     * @param userId
     * @return
     */
    Map<String,Object> load17JTUserData(Long userId);

    List<YiqiJTCourse> load17JTCourseList();

    MapMessage buy17JTCourse(Long userId,Long courseId);

    MapMessage unlockJTCourse(Long userId,Long courseId);

    MapMessage fixJTData(Long userId);

    String wrapAuth(String url, Date expTime);

    YiqiJTCourse loadCourseById(long courseId);

    YiqiJTCourse upsertCourse(YiqiJTCourse course);

    void addCourseCatalog(YiqiJTCourseCatalog courseCatalog);

    boolean delCourseCatalog(long catalogId);

    List<YiqiJTCourseCatalog> getCourseCatalogsByCourseId(long courseId);

    public YiqiJTCourseCatalog getCourseCatalogById(long id);

    List<YiqiJTCourseCatalog> loadCourseCatalogList();

    void upsertCourseSubject(List<YiqiJTCourseSubject> subjects);

    void upsertCourseGeade(List<YiqiJTCourseGrade> grades);

    void upsertCourseChoiceNote(YiqiJTChoiceNote choiceNote);

    boolean delCourseChoiceNote(long noteid);

    List<YiqiJTChoiceNote> getCourseNotesByCourseId(long courseId);

    YiqiJTChoiceNote getCourseNoteById(long id);

    List<YiqiJTChoiceNote> loadCourseNoteList();

    List<YiqiJTCourseGrade> getAllGrade();

    List<YiqiJTCourseGrade> getGradesByCourdeId(long coueseId);

    List<YiqiJTCourseSubject> getAllSubject();

    List<YiqiJTCourseSubject> getSubjectsByCourseId(long coueseId);

    int updateCourseTopNum(long courseId, int topNum);

    int updateCourseStatus(long courseId, int status);

    List<YiqiJTCourseOuterchain> getCourseOuterchainsByCourseId(long coueseId);

    YiqiJTCourseOuterchain getCourseOuterchainById(long id);

    void addCourseOuterchain(YiqiJTCourseOuterchain courseOuterchain);

    boolean delCourseOuterchain(long id);

    List<YiqiJTCourse> select17JTCourseList(String courseName, List<Long> gradeList, List<Long> subjectList);

    MapMessage add17JTReadCount(Long id);

    MapMessage add17JTCollectCount(Long id, Long incrValue);

    MapMessage startCourseInvitation(Long teacherId, Long courseId);

    MapMessage cancelCourseInvitation(Long teacherId, Long courseId);

    CourseInvitation loadCourseInvitation(Long teacherId, Long courseId);

    MapMessage helperCourseInvitation(String openId, String nickName, String imgUrl, Long teacherId, Long courseId);
}
