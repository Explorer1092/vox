package com.voxlearning.utopia.service.campaign.impl.support;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.entity.activity.SudokuDayQuestion;
import com.voxlearning.utopia.enums.ActivityDifficultyLevelEnum;
import com.voxlearning.utopia.service.campaign.impl.service.StudentActivityServiceImpl;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SudokuUtils {

    private static final Logger log = LoggerFactory.getLogger(SudokuUtils.class);

    private static final ZoneId zoneId = ZoneId.systemDefault();
    private static final DateTimeFormatter dateTimeFormatterYear = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 根据起止时间生成 yyyyMMdd 格式的日期字符串
     *
     * @param start 开始时间
     * @param end   结束时间 如果当天时间早于结束时间, 以当天时间为准
     * @return
     */
    public static List<String> generateDayRangeToDay(Date start, Date end) {
        LocalDate nowDate = LocalDate.now();
        ArrayList<String> result = new ArrayList<>();

        LocalDate startLocalDate = start.toInstant().atZone(zoneId).toLocalDate();
        LocalDate endLocalDate = end.toInstant().atZone(zoneId).toLocalDate();
        if (nowDate.isBefore(endLocalDate)) {
            endLocalDate = nowDate;
        }

        LocalDate item = startLocalDate;
        do {
            result.add(dateTimeFormatterYear.format(item));
            item = item.plusDays(1);
        } while (!(item.compareTo(endLocalDate) > 0));
        return result;
    }

    public static List<String> generateDayRange(Date start, Date end) {
        ArrayList<String> result = new ArrayList<>();

        LocalDate startLocalDate = start.toInstant().atZone(zoneId).toLocalDate();
        LocalDate endLocalDate = end.toInstant().atZone(zoneId).toLocalDate();

        LocalDate item = startLocalDate;
        do {
            result.add(dateTimeFormatterYear.format(item));
            item = item.plusDays(1);
        } while (!(item.compareTo(endLocalDate) > 0));
        return result;
    }

    public static String secondToTime(long time) {
        String timeString = null;
        long hour = 0;
        long minute = 0;
        long second = 0;
        if (time <= 0)
            return "00:00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeString = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99) return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeString = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeString;
    }

    private static String unitFormat(long i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Long.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    private static List<SudokuQuestion> four = new ArrayList<>();
    private static List<SudokuQuestion> six = new ArrayList<>();
    private static List<SudokuQuestion> nine = new ArrayList<>();

    static {
        try {
            InputStream in = StudentActivityServiceImpl.class.getResourceAsStream("/sudoku/sudoku_question.json");
            String json = IOUtils.toString(in, Charset.defaultCharset());

            List<SudokuQuestion> questionList = JSON.parseArray(json, SudokuQuestion.class);
            for (SudokuQuestion item : questionList) {
                if (item.getGenre() == 4) four.add(item);
                if (item.getGenre() == 6) six.add(item);
                if (item.getGenre() == 9) nine.add(item);
            }

            Collections.shuffle(four);
            Collections.shuffle(six);
            Collections.shuffle(nine);

            four = ImmutableList.copyOf(four);
            six = ImmutableList.copyOf(six);
            nine = ImmutableList.copyOf(nine);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static AbstractQuestionBuild swichGeneratorBuild(String activityId, Date startTime, Date endTime, ActivityDifficultyLevelEnum levelEnum, Integer dayQuestionCount) {
        AbstractQuestionBuild question = null;

        if (levelEnum == ActivityDifficultyLevelEnum.EASY) {
            question = new Low();
        } else if (levelEnum == ActivityDifficultyLevelEnum.NORMAL) {
            question = new Secondary();
        } else {
            question = new High();
        }

        question.setActivityId(activityId);
        question.setDayQuestionCount(dayQuestionCount);
        question.setStartTime(startTime);
        question.setEndTime(endTime);
        return question;
    }

    interface QuestionBuild {
        List<SudokuDayQuestion> build();
    }

    @Setter
    public abstract static class AbstractQuestionBuild implements QuestionBuild {
        protected String activityId;
        protected Date startTime;
        protected Date endTime;
        protected Integer dayQuestionCount;

        private List<String> getDayString() {
            return SudokuUtils.generateDayRange(startTime, endTime);
        }

        abstract List<Collection<SudokuQuestion>> getQuestionList();

        public List<SudokuDayQuestion> build() {
            List<SudokuDayQuestion> result = new ArrayList<>();
            EternalLifeIterator<SudokuQuestion> iterator = new EternalLifeIterator<>(getQuestionList());
            for (String day : getDayString()) {
                SudokuDayQuestion dayResult = new SudokuDayQuestion();
                dayResult.setCurDate(day);
                dayResult.setActivityId(activityId);

                List<SudokuDayQuestion.DayQuestion> dayQuestionList = new ArrayList<>();
                for (int i = 0; i < dayQuestionCount; i++) {
                    SudokuQuestion next = iterator.next();
                    SudokuDayQuestion.DayQuestion dayQuestion = new SudokuDayQuestion.DayQuestion(next.getGameDesc(), next.getAnswerDesc());
                    dayQuestionList.add(dayQuestion);
                }
                dayResult.setQuestions(dayQuestionList);
                result.add(dayResult);
            }
            return result;
        }
    }

    /**
     * 四宫格
     */
    static class Low extends AbstractQuestionBuild {
        @Override
        List<Collection<SudokuQuestion>> getQuestionList() {
            return Arrays.asList(four);
        }
    }

    /**
     * 六宫格
     */
    static class Secondary extends AbstractQuestionBuild {
        @Override
        List<Collection<SudokuQuestion>> getQuestionList() {
            return Arrays.asList(six);
        }
    }

    /**
     * 九宫格
     */
    static class High extends AbstractQuestionBuild {
        @Override
        List<Collection<SudokuQuestion>> getQuestionList() {
            return Arrays.asList(nine);
        }
    }
}

