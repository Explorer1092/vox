package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.data.ActivityBaseRule;
import com.voxlearning.utopia.entity.activity.*;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityDifficultyLevelEnum;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.campaign.api.StudentActivityService;
import com.voxlearning.utopia.service.campaign.impl.dao.*;
import com.voxlearning.utopia.service.campaign.impl.support.CampaignCacheSystem;
import com.voxlearning.utopia.service.campaign.impl.support.SudokuUtils;
import com.voxlearning.utopia.service.campaign.mapper.StudentParticipated;
import com.voxlearning.utopia.service.campaign.mapper.SudokuHistory;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.jsoup.helper.Validate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 学生临时活动
 *
 * 缩写对照:
 * TAM - 泰安数学周
 * XQB - 小小铅笔公益
 *
 * Created by haitian.gan on 2017/9/26.
 */
@Named
@ExposeService(interfaceClass = StudentActivityService.class)
public class StudentActivityServiceImpl extends SpringContainerSupport implements StudentActivityService {

    /** 七巧板题目总数 **/
    private static final int TANGRAM_PUZZLE_NUM = 120;

    @Inject private TangramEntryRecordDao tangramEntryRecordDao;
    @Inject private TwoFourPointEntityRecordDao twoFourPointEntityRecordDao;
    @Inject private XqbSignUpDao xqbSignUpDao;
    @Inject private InternalTwoFourGetQuestionService internalTwoFourGetQuestionService;
    @Inject private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject private SudokuUserRecordDao sudokuUserRecordDao;
    @Inject private SudokuDayQuestionDao sudokuDayQuestionDao;
    @Inject private InternalActivityExportScore internalActivityExportScore;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject protected CampaignCacheSystem campaignCacheSystem;
    private static String NEW_DAY_ERR_INFO = "当前游戏已到结束时间，明天再继续吧~";

    private static final String OPPORTUNITY_PRIVILEGE = "campaign:opportunity:privilege:";

    @Override
    public void afterPropertiesSet(){
    }

    private interface PickQuestionCallback{
        void deal(TangramEntryRecord record);
    }

    private int pickQuestion(Long userId,String code, PickQuestionCallback callback){
        BitSet puzzleSet;
        TangramEntryRecord record = tangramEntryRecordDao.loadByUserId(userId,code);
        if(record == null){
            record = new TangramEntryRecord();
            // 这个功能以后会复用，要区分出不同活动的数据
            record.setActivityCode(code);
            record.setUserId(userId);
            // 初始化答过题的BitSet
            puzzleSet = new BitSet(TANGRAM_PUZZLE_NUM);
            record.setPuzzles(puzzleSet.toLongArray());
        }else {
            puzzleSet = Optional.ofNullable(record.getPuzzles())
                    .map(BitSet::valueOf)
                    .orElse(new BitSet(TANGRAM_PUZZLE_NUM));
        }

        // 如果所有bit都有值了，则clear
        // 每次从随机的地方开始向后选一个clearBit的index，找他后面，如果没有，则反过来找前面
        if(puzzleSet.cardinality() >= TANGRAM_PUZZLE_NUM){
            puzzleSet.clear();
        }

        int randomIndex = RandomUtils.nextInt(0,TANGRAM_PUZZLE_NUM);
        int puzzleIndex = puzzleSet.nextClearBit(randomIndex);
        if( puzzleIndex >= TANGRAM_PUZZLE_NUM)
            puzzleIndex = puzzleSet.previousClearBit(randomIndex);

        puzzleSet.set(puzzleIndex);
        record.setPuzzles(puzzleSet.toLongArray());

        callback.deal(record);
        return randomIndex + 1;
    }

    @Override
    public StudentParticipated allowParticipatedTangram(Long userId, String activityId) {
        TangramEntryRecord record = loadTangramEntryRecord(userId, activityId);

        ActivityConfig config = activityConfigServiceClient.loadById(activityId);
        if (config != null && record != null && config.getRules().getPlayLimit() != null && record.getScoreMap() != null) {
            if (record.getScoreMap().size() >= config.getRules().getPlayLimit()) {
                if (lastPlayNotTody(record.getScoreMap())) {
                    return new StudentParticipated(false, "已达到限制次数");
                }
            }
        }

        if (record == null || record.getStartTime() == null || record.getScoreMap() == null) {
            return new StudentParticipated(true);
        }

        DayRange dayRange = DayRange.current();
        Long maxTimestamp = record.getScoreMap().keySet().stream().max(Comparator.naturalOrder()).orElse(0L);
        StudentParticipated studentParticipated = new StudentParticipated(!dayRange.contains(maxTimestamp));
        if (studentParticipated.isDeny()) {
            if (havePrivilege(userId, activityId)) {
                studentParticipated.setAllow(true);
            } else {
                studentParticipated.setInfo("当天已无机会");
            }
        }
        return studentParticipated;
    }

    @Override
    public StudentParticipated allowParticipatedTwentyFour(Long userId, String activityId) {
        TwoFourPointEntityRecord record = loadTwoFourPointEntityRecord(userId, activityId);

        ActivityConfig config = activityConfigServiceClient.loadById(activityId);
        if (config != null && record != null && config.getRules().getPlayLimit() != null && record.getScoreMap() != null) {
            if (record.getScoreMap().size() >= config.getRules().getPlayLimit()) {
                if (lastPlayNotTody(record.getScoreMap())) {
                    return new StudentParticipated(false, "已达到限制次数");
                }
            }
        }

        if (record == null || record.getStartTime() == null || record.getScoreMap() == null) {
            return new StudentParticipated(true);
        }

        DayRange dayRange = DayRange.current();
        Long maxTimestamp = record.getScoreMap().keySet().stream().max(Comparator.naturalOrder()).orElse(0L);
        StudentParticipated studentParticipated = new StudentParticipated(!dayRange.contains(maxTimestamp));
        if (studentParticipated.isDeny()) {
            if (havePrivilege(userId, activityId)) {
                studentParticipated.setAllow(true);
            } else {
                studentParticipated.setInfo("当天已无机会");
            }
        }
        return studentParticipated;
    }

    private boolean lastPlayNotTody(Map<Long, Integer> scoreMap) {
        // 寻找最大的时间戳、判断是不是当天,如果不是,拒绝掉
        DayRange dayRange = DayRange.current();
        Long maxTimestamp = scoreMap.keySet().stream().max(Comparator.naturalOrder()).orElse(0L);
        return !dayRange.contains(maxTimestamp);
    }

    @Override
    public StudentParticipated allowParticipatedSudoku(Long userId, String activityId, String pattern, Integer limitTime, Integer limitAmount) {
        String curDate = DateFormatUtils.format(new Date(), "yyyyMMdd");
        List<SudokuUserRecord> userRecord = loadSudokuRecordByUserId(userId, activityId);
        SudokuUserRecord curDateRecord = userRecord.stream()
                .filter(i -> Objects.equals(i.getCurDate(), curDate))
                .findFirst().orElse(null);

        ActivityConfig config = activityConfigServiceClient.loadById(activityId);
        if (config != null && config.getRules().getPlayLimit() != null) {
            if (userRecord.size() >= config.getRules().getPlayLimit()) {
                // 这里需要注意,有的模式允许当天重复进入, 当天成绩为空的话(说明此时是超过限制后的某一天)再拒绝进入
                if (curDateRecord == null) return new StudentParticipated(false, "已达到限制次数");
            }
        }

        if (curDateRecord == null) {
            return new StudentParticipated(true);
        } else {
            return new StudentParticipated(false, "当天已无机会");
        }
    }

    @Override
    public MapMessage enterTangramArena(Long userId,String code) {
        try {
            Validate.notNull(userId,"用户id为空!");
            Validate.isTrue(userId > 0,"用户id不存在!");

            TangramEntryRecord record = loadTangramEntryRecord(userId, code);
            if(record != null && record.getStartTime() != null){
                //Date now = DateUtils.getTodayEnd();
                //long hourDiff = DateUtils.hourDiff(now,new Date(record.getStartTime()));

                // Validate.isTrue(hourDiff >= 24,"已经完成比赛，或者中途退出，不能入场!");
            }

            int questionIndex = pickQuestion(userId, code, r -> {
                r.setStartTime(System.currentTimeMillis());
                tangramEntryRecordDao.upsert(r);
            });
            int limitTime = 10;
            if (code.length() == 24) {
                ActivityConfig load = activityConfigServiceClient.getActivityConfigService().load(code);
                limitTime = load.getRules().getLimitTime();
            }

            return MapMessage.successMessage().add("questionIndex", questionIndex).add("time", limitTime);
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e){
            logger.error("TangramActivity error!enter arena failed",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public TangramEntryRecord loadTangramEntryRecord(Long userId,String code) {
        return tangramEntryRecordDao.loadByUserId(userId, code);
    }

    @Override
    public MapMessage submitTangramScore(Long userId,int score,String code) {
        try{
            TangramEntryRecord record = loadTangramEntryRecord(userId, code);
            Validate.notNull(record,"未查询到参赛信息");
            Validate.notNull(record.getStartTime(), "请重新进入!");

            DayRange today = DayRange.current();
            Validate.isTrue(today.contains(record.getStartTime()), NEW_DAY_ERR_INFO);

            int limitTime = 10;
            if (code.length() == 24) { // 新的活动Code是24位的 ObjectId
                ActivityConfig config = activityConfigServiceClient.getActivityConfigService().load(code);
                limitTime = config.getRules().getLimitTime();
            }

            Date now = new Date();
            long minDiff = DateUtils.minuteDiff(now,new Date(record.getStartTime()));
            Validate.isTrue(minDiff <= (limitTime + 1), "比赛已经结束，不能再提交分数!");

            int questionIndex = pickQuestion(userId,code,r -> {
                r.registerScore(new Date(),score);
                tangramEntryRecordDao.replace(r);
            });

            consumePrivilege(userId, code);
            return MapMessage.successMessage().add("questionIndex",questionIndex);
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public Long loadAllTangramRecordsForStatCount(String code) {
        return tangramEntryRecordDao.loadAllCount(code);
    }

    @Override
    public List<TangramEntryRecord> loadAllTangramRecordsForStat(String code) {
        return tangramEntryRecordDao.loadAll(code);
    }

    @Override
    public MapMessage editTangramRecordInCheat(TangramEntryRecord record) {
        try{
            tangramEntryRecordDao.upsert(record);
            return MapMessage.successMessage();
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage submitXQBSignUpInfo(XqbSignUp signUpInfo) {
        try{
            xqbSignUpDao.upsert(signUpInfo);
            return MapMessage.successMessage();
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public List<XqbSignUp> loadXQBSignUpForExport(Date endDate,Integer pageSize, Integer pageNum) {
        return xqbSignUpDao.loadUnderLineInPage(endDate,pageSize,pageNum);
    }

    //24点游戏 start

    @Override
    public MapMessage enterTwoFourPoint(Long userId, String code) {
        try {
            Validate.notNull(userId, "用户id为空!");
            Validate.isTrue(userId > 0, "用户id不存在!");

            TwoFourPointEntityRecord record = loadTwoFourPointEntityRecord(userId, code);

            Integer limitTime = 10;
            if (code.length() == 24) {
                ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(code);
                limitTime = activityConfig.getRules().getLimitTime();
            }

            return internalTwoFourGetQuestionService.getQuestion(userId, code, limitTime);
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("TwoFourPointActivity error!enter arena failed", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }


    public MapMessage exitTwofour(Long userId, String code) {
        TwoFourPointEntityRecord record = loadTwoFourPointEntityRecord(userId, code);
        if (record != null) {
            record.setStartTime(DateUtils.getTodayEnd().getTime());
            twoFourPointEntityRecordDao.upsert(record);
        }
        return MapMessage.successMessage();
    }


    public MapMessage pullMoreQuestion(Long userId, String code) {
        try {
            Validate.notNull(userId, "用户id为空!");
            Validate.isTrue(userId > 0, "用户id不存在!");

            return internalTwoFourGetQuestionService.pullMoreQuestion(userId, code);
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("TwoFourPointActivity error!enter arena failed", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage randomExpQuestion(Long userId, Integer max) {
        List<String> resultList = internalTwoFourGetQuestionService.randomExpQuestion(userId, max);
        return MapMessage.successMessage()
                .add("data", resultList)
                .add("startTime", 10)
                .add("score", 0);
    }

    @Override
    public TwoFourPointEntityRecord loadTwoFourPointEntityRecord(Long userId, String code) {
        return twoFourPointEntityRecordDao.loadByUserId(userId, code);
    }

    @Override
    public MapMessage submitTwoFourScore(Long userId, int score, String code) {
        try {
            TwoFourPointEntityRecord record = loadTwoFourPointEntityRecord(userId, code);
            Validate.notNull(record, "未查询到参赛信息");
            Validate.notNull(record.getStartTime(), "请重新进入!");

            DayRange today = DayRange.current();
            Validate.isTrue(today.contains(record.getStartTime()), NEW_DAY_ERR_INFO);

            Integer limitTime = 10;
            if (code.length() == 24) {
                ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(code);
                limitTime = activityConfig.getRules().getLimitTime();
            }

            Date now = new Date();
            long minDiff = DateUtils.minuteDiff(now, new Date(record.getStartTime()));
            Validate.isTrue(minDiff <= (limitTime + 1), "比赛已经结束，不能再提交分数!");

            record.registerScore(new Date(), score);
            twoFourPointEntityRecordDao.replace(record);

            consumePrivilege(userId, code);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @Override
    public MapMessage editTwoFourRecordInCheat(TwoFourPointEntityRecord record) {
        try {
            twoFourPointEntityRecordDao.upsert(record);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    public Long loadAllTwofourRecordCount(String code) {
        return twoFourPointEntityRecordDao.loadAllCount(code);
    }

    public List<TwoFourPointEntityRecord> loadAllTwofourRecords(String code) {
        return twoFourPointEntityRecordDao.loadAll(code);
    }

    public MapMessage resetCountTwofour(Long userId, String code) {
        TwoFourPointEntityRecord record = twoFourPointEntityRecordDao.loadByUserId(userId, code);
        try {
            Validate.isTrue(record != null, "请重新进入");
            Validate.isTrue(record.getResetCount() != null, "请重新进入");

            twoFourPointEntityRecordDao.resetCountAdd(record);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    public MapMessage skipCountTwofour(Long userId, String code) {
        TwoFourPointEntityRecord record = twoFourPointEntityRecordDao.loadByUserId(userId, code);
        try {
            Validate.isTrue(record != null, "请重新进入");
            Validate.isTrue(record.getSkipCount() != null, "请重新进入");

            twoFourPointEntityRecordDao.skipCountAdd(record);
            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    //24点游戏 end
    @Override
    public List<SudokuUserRecord> loadSudokuRecordByUserId(Long userId, String activityId) {
        return sudokuUserRecordDao.loadByUser(activityId, userId);
    }

    @Override
    public SudokuUserRecord loadSudokuRecordByUidAid(Long userId, String activityId, String date) {
        return loadSudokuRecordByUserId(userId, activityId).stream()
                .filter(i -> Objects.equals(i.getCurDate(), date))
                .findFirst().orElse(null);
    }

    @Override
    public MapMessage updateSudoCountdown(Long userId, String activityId, Integer time) {
        Date now = new Date();
        String curDate = DateFormatUtils.format(now, "yyyyMMdd");

        SudokuUserRecord dayRecord = loadSudokuRecordByUidAid(userId, activityId, curDate);
        if (dayRecord == null) {
            return MapMessage.errorMessage("未找到当日答题记录");
        }

        dayRecord.setCountdown(time);
        sudokuUserRecordDao.upsert(dayRecord);
        return MapMessage.successMessage().add("time", time);
    }

    @Override
    public MapMessage enterSudoku(Long userId, String activityId) {
        Date now = new Date();
        String curDate = DateFormatUtils.format(now, "yyyyMMdd");

        // 获取预先生成的题目
        SudokuDayQuestion sudokuDayQuestion = loadSudokuDayQuestion(activityId, curDate);
        if (sudokuDayQuestion == null || sudokuDayQuestion.getQuestions() == null) {
            return MapMessage.errorMessage("竟然没题 ???");
        }

        // 记录进场时间
        SudokuUserRecord userRecord = loadSudokuRecordByUidAid(userId, activityId, curDate);
        if (userRecord == null) {
            userRecord = new SudokuUserRecord();
            userRecord.setActivityId(activityId);
            userRecord.setCurDate(curDate);
            userRecord.setUserId(userId);
            userRecord.setBeginTime(now);
            userRecord.setCorrectCount(0);

            LinkedList<SudokuUserRecord.QuestionTime> times = new LinkedList<>();
            times.add(new SudokuUserRecord.QuestionTime(now));

            userRecord.setTimes(times);
            sudokuUserRecordDao.insert(userRecord);
        }
        List<SudokuUserRecord.QuestionTime> times = userRecord.getTimes();
        for (SudokuUserRecord.QuestionTime time : times) {
            if (time.getEndTime() == null) {
                time.setBeginTime(now); // 以二次进入的开始时间为准
                break;
            }
        }
        sudokuUserRecordDao.upsert(userRecord);

        List<String> question = sudokuDayQuestion.getQuestions().stream().map(SudokuDayQuestion.DayQuestion::getQuestion).collect(toList());
        MapMessage mapMessage = MapMessage.successMessage().add("question", question).add("progress", userRecord.getCorrectCount());

        // 开发测试时把答案返回到前台
        if (!RuntimeMode.isProduction()) {
            List<String> answer = sudokuDayQuestion.getQuestions().stream().map(SudokuDayQuestion.DayQuestion::getAnswer).collect(toList());
            mapMessage.add("answer", answer);
        }
        return mapMessage;
    }

    @Override
    public MapMessage submitSudokuScore(Long userId, String activityId, String time, Integer index) {
        Date now = new Date();
        String curDate = DateFormatUtils.format(now, "yyyyMMdd");

        SudokuUserRecord userRecord = loadSudokuRecordByUidAid(userId, activityId, curDate);
        if (userRecord == null) {
            return MapMessage.errorMessage(NEW_DAY_ERR_INFO);
        }

        int correctCount = SafeConverter.toInt(index, 0);
        SudokuDayQuestion sudokuDayQuestion = loadSudokuDayQuestion(activityId, curDate);
        int dayQuestionCount = sudokuDayQuestion.getQuestions().size();
        if (correctCount >= dayQuestionCount || userRecord.getEndTime() != null) {
            return MapMessage.successMessage("今日题已答完");
        }

        if (correctCount >= userRecord.getTimes().size()) {
            return MapMessage.errorMessage("请先完成上一题");
        }

        // 修改当前题的完成时间
        SudokuUserRecord.QuestionTime questionTime = userRecord.getTimes().get(correctCount);
        if (questionTime != null) {
            questionTime.setEndTime(now);
            questionTime.setTime(time);
        }

        // 累加答题进度
        correctCount = ++correctCount;
        userRecord.setCorrectCount(correctCount);

        // 如果是最后一题,设置今日完成日期,否则,新建下一题的进度
        if (correctCount >= dayQuestionCount) {
            userRecord.setEndTime(now);
        } else {
            // 可能会重复提交某一提的成绩、不可贸然无限初始化下一题
            if (userRecord.getTimes().size() == correctCount) {
                userRecord.getTimes().add(new SudokuUserRecord.QuestionTime(now));
            }
        }
        sudokuUserRecordDao.upsert(userRecord);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage loadSudokuHistory(Long userId, String activityId) {
        ActivityConfig activity = activityConfigServiceClient.getActivityConfigService().load(activityId);
        Map<String, SudokuDayQuestion> dayQuestionMap = loadSudokuDayQuestion(activityId);
        if (dayQuestionMap.isEmpty()) {
            return MapMessage.errorMessage("竟然没题 ???");
        }

        List<SudokuUserRecord> userRecords = loadSudokuRecordByUserId(userId, activityId);
        Map<String, SudokuUserRecord> userRecordMap = userRecords.stream().collect(Collectors.toMap(SudokuUserRecord::getCurDate, Function.identity(), (o1, o2) -> o2));

        List<String> dayRanges = SudokuUtils.generateDayRangeToDay(activity.getStartTime(), activity.getEndTime()); // 格式: yyyyMMdd

        DayRange curDayRange = DayRange.current();
        List<SudokuHistory> sudokuHistoryList = new ArrayList<>();

        for (String dayString : dayRanges) {
            SudokuUserRecord sudokuUserRecord = userRecordMap.get(dayString);
            SudokuDayQuestion sudokuDayQuestion = dayQuestionMap.get(dayString);
            if (sudokuUserRecord == null) continue;
            try {
                dayString = DateFormatUtils.format(DateUtils.parseDate(dayString, "yyyyMMdd"), "M月d日");
            } catch (Exception ignored) {
            }

            List<SudokuDayQuestion.DayQuestion> dayQuestions = sudokuDayQuestion.getQuestions();

            // 翻转时间
            LinkedList<SudokuUserRecord.QuestionTime> questionTimes = sudokuUserRecord.getTimes();
            questionTimes.sort(Comparator.comparing(SudokuUserRecord.QuestionTime::getBeginTime));

            for (int questionIndex = 0; questionIndex < questionTimes.size(); questionIndex++) {
                SudokuUserRecord.QuestionTime time = questionTimes.get(questionIndex);
                SudokuDayQuestion.DayQuestion dayQuestion = dayQuestions.get(questionIndex);

                SudokuHistory historyId = new SudokuHistory();
                historyId.setDate(dayString);
                historyId.setQuestion(dayQuestion);

                if (time.getBeginTime() != null && time.getEndTime() == null) {
                    historyId.setMark("未完成");
                    historyId.setDate(DateFormatUtils.format(time.getBeginTime(), "MM月dd日 HH:mm"));

                    // 当天没答完的题目不显示答案
                    if (curDayRange.contains(time.getBeginTime())) {
                        dayQuestion.setAnswer(dayQuestion.getQuestion());
                    }
                } else if (time.getBeginTime() != null && time.getEndTime() != null) {
                    historyId.setMark("已完成");
                    historyId.setDate(DateFormatUtils.format(time.getBeginTime(), "MM月dd日 HH:mm"));
                    //long diffSecond = (time.getEndTime().getTime() - time.getBeginTime().getTime()) / 1000;
                    historyId.setTime(time.getTime());
                    historyId.setEndDate(DateFormatUtils.format(time.getEndTime(), "MM月dd日 HH:mm"));
                }
                sudokuHistoryList.add(historyId);
            }
        }
        Collections.reverse(sudokuHistoryList);
        return MapMessage.successMessage().add("data", sudokuHistoryList);
    }

    public Map<String, SudokuDayQuestion> loadSudokuDayQuestion(String activityId) {
        List<SudokuDayQuestion> sudokuDayQuestions = sudokuDayQuestionDao.loadByActivityId(activityId);
        return sudokuDayQuestions.stream()
                .collect(Collectors.toMap(SudokuDayQuestion::getCurDate, Function.identity(), (o1, o2) -> o2));
    }

    public SudokuDayQuestion loadSudokuDayQuestion(String activityId, String curDate) {
        List<SudokuDayQuestion> sudokuDayQuestions = sudokuDayQuestionDao.loadByActivityId(activityId);
        return sudokuDayQuestions.stream()
                .filter(i -> Objects.equals(curDate, i.getCurDate()))
                .findFirst().orElse(null);
    }

    public void saveSudokuDayQuestion(List<SudokuDayQuestion> list) {
        sudokuDayQuestionDao.inserts(list);
    }

    public MapMessage generateSudokuQuestion(String activityId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("Sudoku:generateSudokuQuestion")
                    .keys(activityId)
                    .callback(() -> atomicGenerateSudokuQuestion(activityId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to generateSudokuQuestion (activity={}): DUPLICATED OPERATION", activityId);
            return MapMessage.errorMessage();
        }
    }

    private MapMessage atomicGenerateSudokuQuestion(String activityId) {
        MapMessage configErrorMessage = MapMessage.errorMessage("活动配置错误");

        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(activityId);
        if (activityConfig == null || activityConfig.getType() != ActivityTypeEnum.SUDOKU) {
            return configErrorMessage;
        }
        Map<String, SudokuDayQuestion> question = this.loadSudokuDayQuestion(activityId);
        if (!question.isEmpty()) {
            return MapMessage.errorMessage("不可重复生成");
        }

        Date startTime = activityConfig.getStartTime();
        Date endTime = activityConfig.getEndTime();
        ActivityBaseRule rules = activityConfig.getRules();
        if (startTime == null || endTime == null || rules == null) {
            return configErrorMessage;
        }
        Integer dayQuestionCount = rules.getLimitAmount();
        ActivityDifficultyLevelEnum level = rules.getLevel();
        if (dayQuestionCount == null || level == null) {
            return configErrorMessage;
        }
        SudokuUtils.AbstractQuestionBuild generatorBuild = SudokuUtils.swichGeneratorBuild(activityId, startTime, endTime, level, dayQuestionCount);
        List<SudokuDayQuestion> questions = generatorBuild.build();
        this.saveSudokuDayQuestion(questions);
        return MapMessage.successMessage();
    }

    public Long loadAllCountByActivityId(String code) {
        return sudokuUserRecordDao.loadAllCountByActivityId(code);
    }

    @Override
    public List<SudokuUserRecord> loadAllSudokuRecords(String code) {
        return sudokuUserRecordDao.loadAllByActivityId(code);
    }

    @Override
    public void exportSudokuScore(String code, String email) {
        internalActivityExportScore.exportSudokuScore(code, email);
    }

    @Override
    public void exportTwentyFourScore(String activityId, String email) {
        internalActivityExportScore.exportTwentyFourScore(activityId, email);
    }

    @Override
    public void exportTangramScore(String activityId, String email) {
        internalActivityExportScore.exportTangramScore(activityId, email);
    }

    public MapMessage loadCanParticipateActivity(Long userId) {
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        List<ActivityConfig> canParticipateActivity = activityConfigServiceClient.getCanParticipateActivity(student);
        ActivityConfig clazzConfig = ObjectUtils.get(() -> activityConfigServiceClient.loadClazzActivityConfig(Collections.singleton(student.getClazzId())).get(student.getClazzId()));
        if (clazzConfig != null) {
            canParticipateActivity.add(0, clazzConfig);
        }
        List<Map<String, Object>> result = canParticipateActivity.stream().map(i -> {
            Map<String, Object> item = MapUtils.m(
                    "id", i.getId(),
                    "title", i.getTitle(),
                    "type", i.getType(),
                    "startTime", DateFormatUtils.format(i.getStartTime(), "yyyy-MM-dd"),
                    "endTime", DateFormatUtils.format(i.getEndTime(), "yyyy-MM-dd")
            );
            return item;
        }).collect(toList());
        return MapMessage.successMessage().add("data", result);
    }

    @Override
    @Deprecated
    public MapMessage addActivityOpportunity(Long userId, Integer activityType) {
        return MapMessage.errorMessage("暂不支持,请联系技术处理");
    }

    @Override
    public MapMessage addActivityOpportunity(String activityId, Long userId) {
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(activityId);
        if (activityConfig == null) {
            return MapMessage.errorMessage("活动不存在");
        }

        try {
            if (activityConfig.getType() == ActivityTypeEnum.TANGRAM) {
                TangramEntryRecord record = tangramEntryRecordDao.loadByUserId(userId, activityId);
                if (record == null) return MapMessage.errorMessage("未找到参赛记录");

                Date startTime = new Date(record.getStartTime());
                if (DayRange.current().contains(startTime)) {
                    record.setStartTime(DateUtils.getDayStart(DateUtils.addDays(startTime, -1)).getTime());
                    tangramEntryRecordDao.upsert(record);
                    addPrivilege(userId, activityId);
                } else {
                    return MapMessage.errorMessage("今天还未比赛，不用加次数!");
                }
            } else if (activityConfig.getType() == ActivityTypeEnum.TWENTY_FOUR) {
                TwoFourPointEntityRecord record = twoFourPointEntityRecordDao.loadByUserId(userId, activityId);
                if (record == null) return MapMessage.errorMessage("未找到参赛记录");

                Date startTime = new Date(record.getStartTime());
                if (DayRange.current().contains(startTime)) {
                    record.setStartTime(DateUtils.getDayStart(DateUtils.addDays(startTime, -1)).getTime());
                    twoFourPointEntityRecordDao.upsert(record);
                    addPrivilege(userId, activityId);
                } else {
                    return MapMessage.errorMessage("今天还未比赛，不用加次数!");
                }
            } else if (activityConfig.getType() == ActivityTypeEnum.SUDOKU) {
                List<SudokuUserRecord> recordList = sudokuUserRecordDao.loadByUser(activityId, userId);
                if (recordList.isEmpty()) return MapMessage.errorMessage("未找到参赛记录");
                SudokuUserRecord sudokuUserRecord = recordList.get(recordList.size() - 1);

                sudokuUserRecordDao.remove(sudokuUserRecord.getId());
                logger.info("竞速模式-添加数独机会：userId:{} day:{} activityId:{}", userId, sudokuUserRecord.getCurDate(), sudokuUserRecord.getActivityId());
            }
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    private void addPrivilege(Long userId, String activityId) {
        campaignCacheSystem.CBS.flushable.set(genOpportunityPrivilegeKey(userId, activityId), DateUtils.getCurrentToDayEndSecond(), "1");
    }

    private void consumePrivilege(Long userId, String activityId) {
        campaignCacheSystem.CBS.flushable.delete(genOpportunityPrivilegeKey(userId, activityId));
    }

    private boolean havePrivilege(Long userId, String activityId) {
        CacheObject<Object> objectCacheObject = campaignCacheSystem.CBS.flushable.get(genOpportunityPrivilegeKey(userId, activityId));
        return objectCacheObject.containsValue();
    }

    private String genOpportunityPrivilegeKey(Long userId, String activityId) {
        return CacheKeyGenerator.generateCacheKey(OPPORTUNITY_PRIVILEGE, new String[]{"UID", "AID"}, new Object[]{userId, activityId});
    }
}
