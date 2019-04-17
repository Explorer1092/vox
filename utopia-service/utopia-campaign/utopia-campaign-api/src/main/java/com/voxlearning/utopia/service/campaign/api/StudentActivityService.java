package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.activity.SudokuUserRecord;
import com.voxlearning.utopia.entity.activity.TangramEntryRecord;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;
import com.voxlearning.utopia.entity.activity.XqbSignUp;
import com.voxlearning.utopia.service.campaign.mapper.StudentParticipated;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for 学生活动
 * Created by haitian.gan on 2017/9/26.
 */
@ServiceVersion(version = "1.8")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface StudentActivityService {

    StudentParticipated allowParticipatedTangram(Long userId, String activityId);

    StudentParticipated allowParticipatedTwentyFour(Long userId, String activityId);

    StudentParticipated allowParticipatedSudoku(Long userId, String activityId, String pattern, Integer limitTime, Integer limitAmount);

    /**
     * 七巧板活动 , 开始比赛
     */
    MapMessage enterTangramArena(Long userId,String code);

    TangramEntryRecord loadTangramEntryRecord(Long userId,String code);

    MapMessage submitTangramScore(Long userId,int score,String code);

    Long loadAllTangramRecordsForStatCount(String code);

    List<TangramEntryRecord> loadAllTangramRecordsForStat(String code);

    /**
     * BackDoor接口，修改学生记录用的
     * @return
     */
    MapMessage editTangramRecordInCheat(TangramEntryRecord record);

    /**
     * 小小铅笔公益活动 ， 提交报名信息
     */
    MapMessage submitXQBSignUpInfo(XqbSignUp signUpInfo);

    /**
     * 给华泰导出线下的用户数据
     * @param pageSize
     * @param pageNum
     * @return
     */
    List<XqbSignUp> loadXQBSignUpForExport(Date endDate, Integer pageSize, Integer pageNum);

    /**
     * 24点游戏 用户开始游戏
     */
    MapMessage enterTwoFourPoint(Long userId,String code);

    /**
     * 24点游戏 根据用户查询记录
     */
    TwoFourPointEntityRecord loadTwoFourPointEntityRecord(Long userId,String code);

    /**
     * 24点游戏 提交分数
     */
    MapMessage submitTwoFourScore(Long userId, int score,String code);


    /**
     * 24点游戏 编辑用户记录
     */
    MapMessage editTwoFourRecordInCheat(TwoFourPointEntityRecord record);

    /**
     * 24点游戏 查询所有用户记录
     */
    List<TwoFourPointEntityRecord> loadAllTwofourRecords(String code);


    MapMessage pullMoreQuestion(Long userId,String code);

    MapMessage randomExpQuestion(Long userId, Integer max);

    MapMessage exitTwofour(Long userId, String code);

    MapMessage resetCountTwofour(Long userId, String code);

    MapMessage skipCountTwofour(Long userId, String code);


    // 数独 start
    List<SudokuUserRecord> loadSudokuRecordByUserId(Long userId, String activityId);

    SudokuUserRecord loadSudokuRecordByUidAid(Long userId, String activityId, String date);

    MapMessage updateSudoCountdown(Long userId, String activityId, Integer time);

    MapMessage enterSudoku(Long userId, String activityId);

    MapMessage submitSudokuScore(Long userId, String activityId, String time, Integer index);

    MapMessage loadSudokuHistory(Long userId, String activityId);

    MapMessage generateSudokuQuestion(String activityId);

    Long loadAllCountByActivityId(String code);

    List<SudokuUserRecord> loadAllSudokuRecords(String code);

    @NoResponseWait
    void exportSudokuScore(String activityId, String email);

    @NoResponseWait
    void exportTwentyFourScore(String activityId, String email);

    @NoResponseWait
    void exportTangramScore(String activityId, String email);
    // 数独 end

    MapMessage addActivityOpportunity(Long userId, Integer activityType);

    MapMessage addActivityOpportunity(String activityId, Long userId);

    MapMessage loadCanParticipateActivity(Long userId);
}
