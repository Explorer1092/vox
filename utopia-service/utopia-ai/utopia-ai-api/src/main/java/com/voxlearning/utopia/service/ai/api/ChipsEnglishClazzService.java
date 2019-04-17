package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2018/8/23
 */
@ServiceVersion(version = "20190403")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsEnglishClazzService extends IPingable {
    ChipsEnglishClass loadMyDefaultClass(Long userId);

    List<ChipsEnglishClass> selectAllChipsEnglishClass();

    ChipsEnglishClass selectChipsEnglishClassById(Long clazzId);

    List<ChipsEnglishClass> selectChipsEnglishClassByProductId(String productId);

    List<ChipsEnglishClass> selectChipsEnglishClassByProductIdTeacherName(String productId, String teacherName);

    List<ChipsEnglishUserExt> selectChipsEnglishUserExtByUserIds(Collection<Long> userIdCollection);

    Map<Long, ChipsEnglishUserExtSplit> loadChipsEnglishUserExtSplitByUserIds(Collection<Long> userIdCollection);

    ChipsEnglishUserExt selectChipsEnglishUserExtByUserId(Long userId);

    ChipsEnglishUserExtSplit selectChipsEnglishUserExtSplitByUserId(Long userId);

    List<ChipsEnglishClassUserRef> selectChipsEnglishClassUserRefByClazzId(Long clazzId);

    ChipsEnglishClassUserRef selectChipsEnglishClassUserRefByUserId(Long userId, Long clazzId);

    MapMessage saveOrUpdateChipsEnglishClass(ChipsEnglishClass chipsEnglishClazz);

    MapMessage mergeChipsEnglishClass(Long clazzId, Long aimClazzId);

    List<ChipsEnglishClassStatistics> selectChipsEnglishClassStatisticsByClazzId(Long clazzId);

    List<ChipsClassStatistics> selectChipsClassStatisticsByClazzId(Long clazzId);

    List<ChipsEnglishClassStatisticsLatest> selectChipsEnglishClassStatisticsLatestByClazzId(Long clazzId);

    List<Long> selectAllUserByClazzId(Long clazzId);

    List<ChipsEnglishClassUserRef> selectAllChipsEnglishClassUserRefByUserId(Long userId);

    MapMessage saveUserRefExt(Long clazzId, Long userId, String wechatNumber, Boolean joinedGroup, String duration);

    MapMessage insertOrUpdateUserExt(List<Long> userIdList, boolean showPlay);

    /**
     * 更新是否增加微信字段
     */
    MapMessage insertOrUpdateUserExtWxAddStatus(Long userId, boolean wxAddStatus);

    /**
     * 更新是否增加企业微信字段
     */
    MapMessage insertOrUpdateUserExtEpWxAddStatus(Long userId, boolean epWxAddStatus);


    Long loadClazzIdByUserAndUnit(Long userId, String unitId);

    ChipsEnglishClass loadClazzIdByUserAndProduct(Long userId, String productId);

    //TODO 这个方法太危险了
    MapMessage upsertChipsEnglishUserExtSplit(ChipsEnglishUserExtSplit extSplit);

    List<AiChipsEnglishTeacher> loadAllChipsEnglishTeacher();

    AiChipsEnglishTeacher loadChipsEnglishTeacherById(String id);

    AiChipsEnglishTeacher upsertAiChipsEnglishTeacher(AiChipsEnglishTeacher teacher);

    MapMessage removeAiChipsEnglishTeacher(String id);

    AiChipsEnglishTeacher loadTeacherByUserIdAndBookId(Long userId, String bookId);

    AiChipsEnglishTeacher loadTeacherByUserIdAndClazzId(Long userId, long clazzId);

    MapMessage loadAllChipsClazzCompare(int pageNum);

    MapMessage saveChipsClazzCompare(ChipsClazzCompare clazzCompare);

    MapMessage loadChipsClazzCompareById(String id);

    /**
     * @param id
     * @param type     "1":最新数据，"2":当日数据
     * @param dayIndex 课次
     * @return
     */
    MapMessage buildClazzCompareData(String id, String type, int dayIndex);

    int dayIndexCount(Long clazzId);

    MapMessage removeClazzCompareData(String id);

    List<ChipsEnglishClass> loadAllChipsEnglishClass();

    /**
     * 换购需求更新班级下的product
     *
     * @param clazzId
     * @param productId
     * @return
     */
    MapMessage updateChipsEnglishClassProduct(Long clazzId, String productId);

    MapMessage insertChipsEnglishClassUpdateLog(ChipsEnglishClassUpdateLog log);

    /**
     * 单个用户换班
     * @param userId
     * @param originClazzId
     * @param clazzId
     * @param originProductId
     * @param productId
     * @return
     */
    MapMessage updateUserClazzAndUserCourse(Long userId,Long originClazzId, Long clazzId,String originProductId, String productId);

    List<ChipsUserCourse> loadChipsUserCourseByUserId(Long userId);

    int calRenewCount(ChipsEnglishClass clazz);
}
