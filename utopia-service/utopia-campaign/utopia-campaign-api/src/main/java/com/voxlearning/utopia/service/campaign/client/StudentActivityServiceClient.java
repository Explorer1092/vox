package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.entity.activity.SudokuUserRecord;
import com.voxlearning.utopia.entity.activity.TangramEntryRecord;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;
import com.voxlearning.utopia.entity.activity.XqbSignUp;
import com.voxlearning.utopia.service.campaign.api.StudentActivityService;
import com.voxlearning.utopia.service.campaign.mapper.StudentParticipated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Created by haitian.gan on 2017/9/26.
 */
public class StudentActivityServiceClient {

    private static final Logger log = LoggerFactory.getLogger(StudentActivityServiceClient.class);

    @ImportService(interfaceClass = StudentActivityService.class)
    private StudentActivityService remoteReference;

    public MapMessage enterTangramArena(Long userId,String code){
        return remoteReference.enterTangramArena(userId, code);
    }

    /**
     * 查看是否当天参加过七巧板的比赛，一天只能参加一次
     * @param userId
     * @return
     */
    public StudentParticipated allowParticipatedTangram(Long userId, String code) {
        return remoteReference.allowParticipatedTangram(userId, code);
    }

    public MapMessage submitTangramScore(Long userId, int score, String code) {
        return remoteReference.submitTangramScore(userId, score, code);
    }

    public TangramEntryRecord loadTangramRecord(Long userId,String code){
        return remoteReference.loadTangramEntryRecord(userId, code);
    }

    public List<TangramEntryRecord> loadAllTangramRecordsForStat(String code){
        return remoteReference.loadAllTangramRecordsForStat(code);
    }

    public MapMessage editTangramRecordInCheat(TangramEntryRecord record){
        return remoteReference.editTangramRecordInCheat(record);
    }

    public MapMessage submitXqbSignUp(XqbSignUp signUpInfo){
        return remoteReference.submitXQBSignUpInfo(signUpInfo);
    }

    public List<XqbSignUp> loadXQBSignUpForExport(Date endDate,Integer pageSize, Integer pageNum){
        return remoteReference.loadXQBSignUpForExport(endDate,pageSize,pageNum);
    }

    // 24点游戏 start

    /**
     * 查看是否当天参加过24点游戏一天只能参加一次
     */
    public StudentParticipated allowParticipatedTwentyFour(Long userId, String code) {
        return remoteReference.allowParticipatedTwentyFour(userId, code);
    }

    public MapMessage enterTwoPoint(Long userId, String code) {
        return remoteReference.enterTwoFourPoint(userId, code);
    }

    public MapMessage exitTwofour(Long userId, String code) {
        return remoteReference.exitTwofour(userId, code);
    }


    public MapMessage pullMoreQuestion(Long userId, String code) {
        return remoteReference.pullMoreQuestion(userId, code);
    }

    public MapMessage randomExpQuestion(Long userId, Integer max) {
        return remoteReference.randomExpQuestion(userId, max);
    }

    public MapMessage submitTwoPointScore(Long userId, int score, String code) {
        return remoteReference.submitTwoFourScore(userId, score, code);
    }

    public TwoFourPointEntityRecord loadTwofourPointRecord(Long userId, String code) {
        return remoteReference.loadTwoFourPointEntityRecord(userId, code);
    }

    public MapMessage editTwoFourRecordInCheat(TwoFourPointEntityRecord record) {
        return remoteReference.editTwoFourRecordInCheat(record);
    }

    public List<TwoFourPointEntityRecord> loadAllTwofourRecords(String code) {
        return remoteReference.loadAllTwofourRecords(code);
    }

    public MapMessage resetCountTwofour(Long userId, String code) {
        return remoteReference.resetCountTwofour(userId, code);
    }

    public MapMessage skipCountTwofour(Long userId, String code) {
        return remoteReference.skipCountTwofour(userId, code);
    }

    // 24点游戏 end

    // 数独 start

    /**
     * 不太方便依赖 crm 的 ActivityConfig,分开传递
     *
     * @param userId
     * @param code
     * @param pattern     ActivityTypeEnum 枚举的 name()值
     * @param limitTime   限制时间
     * @param limitAmount 限制题量
     * @return
     */
    public StudentParticipated allowParticipatedSudoku(Long userId, String code, String pattern, Integer limitTime, Integer limitAmount) {
        return remoteReference.allowParticipatedSudoku(userId, code, pattern, limitTime, limitAmount);
    }

    public SudokuUserRecord loadSudokuRecordByUidAidDate(Long userId, String activityId, String date) {
        return remoteReference.loadSudokuRecordByUidAid(userId, activityId, date);
    }

    public MapMessage updateSudoCountdown(Long userId, String activityId, Integer time) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Sudoku:updateSudoCountdown")
                    .keys(userId, activityId)
                    .callback(() -> remoteReference.updateSudoCountdown(userId, activityId, time))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            log.error("Failed to updateSudoCountdown (user={},activity={}): DUPLICATED OPERATION", userId, activityId);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            log.error("Failed to updateSudoCountdown (user={},activity={})", userId, activityId, ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage enterSudoku(Long userId, String activityId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Sudoku:enterSudoku")
                    .keys(userId, activityId)
                    .callback(() -> remoteReference.enterSudoku(userId, activityId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            log.error("Failed to enterSudoku (user={},activity={}): DUPLICATED OPERATION", userId, activityId);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            log.error("Failed to enterSudoku (user={},activity={})", userId, activityId, ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage submitSudokuScore(Long userId, String activityId, String time, Integer index) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Sudoku:SubmitScore")
                    .keys(userId, activityId)
                    .callback(() -> remoteReference.submitSudokuScore(userId, activityId, time, index))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            log.error("Failed to submitSudokuScore (user={},activity={}): DUPLICATED OPERATION", userId, activityId);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            log.error("Failed to submitSudokuScore (user={},activity={})", userId, activityId, ex);
            return MapMessage.errorMessage();
        }
    }

    public MapMessage loadSudokuHistory(Long userId, String activityId) {
        return remoteReference.loadSudokuHistory(userId, activityId);
    }

    public List<SudokuUserRecord> loadAllSudokuRecords(String code) {
        return remoteReference.loadAllSudokuRecords(code);
    }

    // 数独 end

    public MapMessage loadCanParticipateActivity(Long studentId) {
        return remoteReference.loadCanParticipateActivity(studentId);
    }

    public MapMessage addActivityOpportunity(String activityId, Long studentId) {
        return remoteReference.addActivityOpportunity(activityId, studentId);
    }
}
