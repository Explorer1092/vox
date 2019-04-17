package com.voxlearning.utopia.service.business.impl.support;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Named
public class FinalReviewConfigUtils extends SpringContainerSupport {


    @Override
    public void afterPropertiesSet() throws Exception {
        initFinalReviewRewardMap();
        initBaseReviewSchool();
        initTermReviewSchool();
    }

    private static Map<Long, FinalReviewReward> finalReviewRewardMap = new HashMap<>();
    private static Set<Long> baseReviewSchool = new HashSet<>();
    private static Set<Long> termReviewSchool = new HashSet<>();

    public static FinalReviewReward getFinalReviewReward(Long schoolId) {
        FinalReviewReward defaultValue = new FinalReviewReward(schoolId, 0, 0);
        if (schoolId == null) return defaultValue;
        return finalReviewRewardMap.getOrDefault(schoolId, defaultValue);
    }

    /**
     * 学校是否有基础必过任务
     */
    public static boolean isBaseReviewSchool(Long schoolId) {
        if (schoolId == null) return false;
        return baseReviewSchool.contains(schoolId);
    }

    /**
     * 学校是否有重点复习任务
     */
    public static boolean isTermReviewSchool(Long schoolId) {
        if (schoolId == null) return false;
        return termReviewSchool.contains(schoolId);
    }

    @Getter
    private static Date startTime;
    @Getter
    private static Date endTime;

    private static DayRange dayRange;

    static {
        String parsePattern = "yyyy-MM-dd HH:mm:ss";
        try {
            if (RuntimeMode.ge(Mode.STAGING)) {
                startTime = DateUtils.parseDate("2018-12-10 00:00:00", parsePattern);
                endTime = DateUtils.parseDate("2019-01-10 23:59:59", parsePattern);
            } else {
                startTime = DateUtils.parseDate("2018-12-01 00:00:00", parsePattern);
                endTime = DateUtils.parseDate("2018-12-08 23:59:59", parsePattern);
            }
            dayRange = new DayRange(startTime.getTime(), endTime.getTime());
        } catch (Exception ignore) {
        }
    }

    /**
     * 是否在活动时间范围
     */
    public static boolean timeScope() {
        return dayRange.contains(new Date());
    }

    private void initFinalReviewRewardMap() {
        try {
            String model = RuntimeMode.ge(Mode.STAGING) ? "production" : "staging";
            List<String> lines = loadFileToList("/activity/final_review/" + model + "/reward/1.txt");
            for (String line : lines) {
                if (StringUtils.isEmpty(line)) continue;
                String[] split = line.split(",");
                if (split.length != 3) continue;

                FinalReviewReward finalReviewReward = new FinalReviewReward(
                        SafeConverter.toLong(split[0]),
                        SafeConverter.toInt(split[1]),
                        SafeConverter.toInt(split[2])
                );

                finalReviewRewardMap.put(finalReviewReward.getSchoolId(), finalReviewReward);
            }

            finalReviewRewardMap = ImmutableMap.<Long, FinalReviewReward>builder().putAll(finalReviewRewardMap).build();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private void initTermReviewSchool() {
        try {
            String model = RuntimeMode.ge(Mode.STAGING) ? "production" : "staging";
            List<String> lines = loadFileToList("/activity/final_review/" + model + "/school/basic_review_school.txt");
            for (String line : lines) {
                if (StringUtils.isEmpty(line)) continue;
                baseReviewSchool.add(SafeConverter.toLong(line));
            }

            baseReviewSchool = ImmutableSet.<Long>builder().addAll(baseReviewSchool).build();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private void initBaseReviewSchool() {
        try {
            String model = RuntimeMode.ge(Mode.STAGING) ? "production" : "staging";
            List<String> lines = loadFileToList("/activity/final_review/" + model + "/school/term_review_school.txt");
            for (String line : lines) {
                if (StringUtils.isEmpty(line)) continue;
                termReviewSchool.add(SafeConverter.toLong(line));
            }

            termReviewSchool = ImmutableSet.<Long>builder().addAll(termReviewSchool).build();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private List<String> loadFileToList(String file) throws IOException {
        List<String> list = new ArrayList<>();
        InputStream in = FinalReviewConfigUtils.class.getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            list.add(line.trim());
        }
        reader.close();
        return list;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinalReviewReward {
        private Long schoolId;
        private Integer basicReview;
        private Integer termReview;
    }
}
