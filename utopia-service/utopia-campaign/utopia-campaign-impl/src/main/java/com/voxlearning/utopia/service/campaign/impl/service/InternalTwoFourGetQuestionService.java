package com.voxlearning.utopia.service.campaign.impl.service;

import com.google.common.collect.ImmutableList;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.QuestionType;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.TwenTyFourExtent;
import com.voxlearning.utopia.service.campaign.impl.dao.TwoFourPointEntityRecordDao;
import com.voxlearning.utopia.service.campaign.impl.support.EternalLifeIterator;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Named("com.voxlearning.utopia.service.campaign.impl.service.IntegralTwoFourGetQuestionService")
public class InternalTwoFourGetQuestionService extends SpringContainerSupport {

    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private TwoFourPointEntityRecordDao twoFourPointEntityRecordDao;
    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;

    /**
     * 各方案题库
     */
    private static List<String> PLAN_13_A = new ArrayList<>();
    private static List<String> PLAN_13_B = new ArrayList<>();
    private static List<String> PLAN_13_C = new ArrayList<>();
    private static List<String> PLAN_13_D = new ArrayList<>();
    private static List<String> PLAN_13_E = new ArrayList<>(); // 只有加减,给一二年级学生做

    private static List<String> HIGH_LEVEL_PLAN = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        initTwoFourPoint();
    }

    private void initTwoFourPoint() {
        try {
            loadPlanFile("/24game/13_plan_a.txt", PLAN_13_A);
            loadPlanFile("/24game/13_plan_b.txt", PLAN_13_B);
            loadPlanFile("/24game/13_plan_c.txt", PLAN_13_C);
            loadPlanFile("/24game/13_plan_d.txt", PLAN_13_D);
            loadPlanFile("/24game/13_plan_e.txt", PLAN_13_E);

            Collections.shuffle(PLAN_13_A);
            Collections.shuffle(PLAN_13_B);
            Collections.shuffle(PLAN_13_C);
            Collections.shuffle(PLAN_13_D);
            Collections.shuffle(PLAN_13_E);

            // 不可变集合,线程安全
            PLAN_13_A = ImmutableList.copyOf(PLAN_13_A);
            PLAN_13_B = ImmutableList.copyOf(PLAN_13_B);
            PLAN_13_C = ImmutableList.copyOf(PLAN_13_C);
            PLAN_13_D = ImmutableList.copyOf(PLAN_13_D);
            PLAN_13_E = ImmutableList.copyOf(PLAN_13_E);

            HIGH_LEVEL_PLAN.addAll(PLAN_13_A);
            HIGH_LEVEL_PLAN.addAll(PLAN_13_B);
            HIGH_LEVEL_PLAN.addAll(PLAN_13_C);
            HIGH_LEVEL_PLAN.addAll(PLAN_13_D);

            Collections.shuffle(HIGH_LEVEL_PLAN);
            HIGH_LEVEL_PLAN = ImmutableList.copyOf(HIGH_LEVEL_PLAN);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private void loadPlanFile(String file, List<String> list) throws IOException {
        InputStream in = StudentActivityServiceImpl.class.getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            list.add(line);
        }
        reader.close();
    }

    /**
     * 中年级：10道题内，标签1：3道题（3/10概率）；标签2：5道题（5/10）;标签3：2道题（2/10）
     * 高年级：10道题内，标签2：3道题（3/10概率）；标签3：5道题（5/10）;标签4：2道题（2/10）
     */
    public MapMessage getQuestion(Long userId, String code,Integer limitTime) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        TwoFourPointEntityRecord recore = twoFourPointEntityRecordDao.loadByUserId(userId, code);
        if (recore == null) {
            recore = new TwoFourPointEntityRecord();
            recore.setUserId(userId);
            recore.setCode(code);
            initQuestionTypeOffset(recore);
            recore.setResetCount(0L);
            recore.setSkipCount(0L);
            recore.setStartTime(System.currentTimeMillis());
        }
        recore.setStartTime(System.currentTimeMillis());

        List<String> resultList = getStudentQuestion(code, studentDetail, recore);
        twoFourPointEntityRecordDao.upsert(recore);
        return MapMessage.successMessage().add("data", resultList).add("startTime", recore.getStartTime()).add("score", getScore(recore)).add("time", limitTime);
    }

    public MapMessage pullMoreQuestion(Long userId, String code) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        TwoFourPointEntityRecord recore = twoFourPointEntityRecordDao.loadByUserId(userId, code);
        if (recore == null || recore.getStartTime() == null) {
            return MapMessage.errorMessage("请重新开始");
        }

        List<String> resultList = getStudentQuestion(code, studentDetail, recore);

        twoFourPointEntityRecordDao.upsert(recore);
        return MapMessage.successMessage().add("data", resultList).add("startTime", recore.getStartTime()).add("score", getScore(recore));
    }

    private List<String> getStudentQuestion(String code, StudentDetail studentDetail, TwoFourPointEntityRecord recore) {
        List<String> resultList;
        int max = 10;
        // 新的配置平台采用 ObjectID 24 位 ID 做活动 Code, 老活动的 Code 格式类似 320600_1_10
        if (code.length() == 24) {
            ActivityConfig config = activityConfigServiceClient.getActivityConfigService().load(code);
            if (config != null && config.getRules() != null) {
                TwenTyFourExtent extent = config.getRules().getExtent();
                if (extent == TwenTyFourExtent.ONE_TO_TEN) {
                    max = 10;
                } else {
                    max = 13;
                }
            }
        } else {
            try {
                String[] split = code.split("_");
                max = Integer.valueOf(split[2]);
            } catch (Exception e) {

            }
        }
        if (studentDetail.getClazzLevelAsInteger() == 1 || studentDetail.getClazzLevelAsInteger() == 2) {
            resultList = genLowQuestion(recore, max);
        } else if (studentDetail.getClazzLevelAsInteger() == 3 || studentDetail.getClazzLevelAsInteger() == 4) {
            resultList = genMediumQuestion(recore, max);
        } else {
            resultList = genHighQuestion(recore, max);
        }
        return resultList;
    }

    private List<String> genLowQuestion(TwoFourPointEntityRecord record, int max) {
        List<String> history = new ArrayList<>();
        return getQuestion(PLAN_13_E, QuestionType.TYPE_13_E, max, 10, record, history);
    }

    /*
        中年级：10道题内，标签1：3道题（3/10概率）；标签2：5道题（5/10）;标签3：2道题（2/10）
     */
    private List<String> genMediumQuestion(TwoFourPointEntityRecord record, int max) {
        List<String> question = new ArrayList<>();
        List<String> history = new ArrayList<>();

        question.addAll(getQuestion(PLAN_13_A, QuestionType.TYPE_13_A, max, 3, record, history));
        question.addAll(getQuestion(PLAN_13_B, QuestionType.TYPE_13_B, max, 5, record, history));
        question.addAll(getQuestion(PLAN_13_C, QuestionType.TYPE_13_C, max, 2, record, history));

        return question;
    }

    /*
        高年级：10道题内，标签2：3道题（3/10概率）；标签3：5道题（5/10）;标签4：2道题（2/10））
     */
    private List<String> genHighQuestion(TwoFourPointEntityRecord record, int max) {
        List<String> question = new ArrayList<>();
        List<String> history = new ArrayList<>();

        question.addAll(getQuestion(PLAN_13_B, QuestionType.TYPE_13_B, max, 3, record, history));
        question.addAll(getQuestion(PLAN_13_C, QuestionType.TYPE_13_C, max, 5, record, history));
        question.addAll(getQuestion(PLAN_13_D, QuestionType.TYPE_13_D, max, 2, record, history));

        return question;
    }

    /**
     * @param list            备选集合
     * @param questionTypeKey 偏移量 key
     * @param max             每行数字的最大值 一般为 10 或 13
     * @param size            需要返回大小
     * @param record          学生参与记录实体
     * @param history         需要排除的问题
     * @return
     */
    private List<String> getQuestion(List<String> list, QuestionType questionTypeKey, int max, int size, TwoFourPointEntityRecord record, List<String> history) {
        Map<QuestionType, Integer> questionTypeOffset = record.getQuestionTypeOffset();
        int offset = questionTypeOffset.getOrDefault(questionTypeKey, 0);

        List<String> result = new ArrayList<>();

        //过大重置
        if (offset >= list.size()) {
            offset = RandomUtils.nextInt(0, list.size());
        }

        for (int i = offset; i < list.size(); i++) {
            String line = list.get(i);
            offset++;
            if (isPerfect(max, line, history)) {
                result.add(line);
                history.add(line);
                if (result.size() == size) {
                    questionTypeOffset.put(questionTypeKey, offset);
                    record.setQuestionTypeOffset(questionTypeOffset);
                    return result;
                }
            }
        }

        //如果找到结尾还没有符合要求的, 就从头开始找
        offset = 0;
        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            offset++;
            if (isPerfect(max, line, history)) {
                result.add(line);
                history.add(line);
                if (result.size() == size) {
                    questionTypeOffset.put(questionTypeKey, offset);
                    record.setQuestionTypeOffset(questionTypeOffset);
                    return result;
                }
            }
        }
        return result;
    }

    private static boolean isPerfect(int max, String line, List<String> history) {
        if (history.contains(line)) {
            return false;
        }
        String[] split = line.split(",");
        for (String s : split) {
            if (Integer.valueOf(s) > max) {
                return false;
            }
        }
        return true;
    }

    private void initQuestionTypeOffset(TwoFourPointEntityRecord recore) {
        Map<QuestionType, Integer> questionTypeMap = new LinkedHashMap<>();
        questionTypeMap.put(QuestionType.TYPE_13_A, RandomUtils.nextInt(0, PLAN_13_A.size()));
        questionTypeMap.put(QuestionType.TYPE_13_B, RandomUtils.nextInt(0, PLAN_13_B.size()));
        questionTypeMap.put(QuestionType.TYPE_13_C, RandomUtils.nextInt(0, PLAN_13_C.size()));
        questionTypeMap.put(QuestionType.TYPE_13_D, RandomUtils.nextInt(0, PLAN_13_D.size()));
        questionTypeMap.put(QuestionType.TYPE_13_E, RandomUtils.nextInt(0, PLAN_13_E.size()));
        recore.setQuestionTypeOffset(questionTypeMap);
    }

    private int getScore(TwoFourPointEntityRecord record) {
        if (record == null || record.getScoreMap() == null) {
            return 0;
        }

        DayRange dayRange = DayRange.current();
        for (Map.Entry<Long, Integer> entry : record.getScoreMap().entrySet()) {
            Date date = new Date(entry.getKey());
            if (dayRange.contains(date)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public List<String> randomExpQuestion(Long userId, int max) {
        StudentDetail studentDetail = null;
        if (userId != null) {
            studentDetail = studentLoaderClient.loadStudentDetail(userId);
        }

        if (studentDetail == null || studentDetail.getClazz() == null
                || studentDetail.getClazz().getClazzLevel().getLevel() == 1
                || studentDetail.getClazz().getClazzLevel().getLevel() == 2) {
            return randomQuestion(PLAN_13_E, max);
        } else {
            return randomQuestion(HIGH_LEVEL_PLAN, max);
        }
    }

    private static List<String> randomQuestion(List<String> plan, int max) {
        EternalLifeIterator<String> iterator = new EternalLifeIterator<>(Collections.singletonList(plan), true);

        List<String> result = new ArrayList<>();

        int i = 1;
        while (i <= 10) {
            String next = iterator.next();
            if (limitMax(max, next)) {
                result.add(next);
                i++;
            }
        }
        return result;
    }

    private static boolean limitMax(int max, String string) {
        String[] split = string.split(",");
        for (String s : split) {
            int test = Integer.valueOf(s);
            if (test > max) {
                return false;
            }
        }
        return true;
    }

}
