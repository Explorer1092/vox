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
public class WinterWorkConfigUtils extends SpringContainerSupport {

    @Override
    public void afterPropertiesSet() throws Exception {
        initRewardMap();
        initSchool();
    }

    private static Map<Integer, WinterWorkReward> rewardMap = new HashMap<>();
    private static Set<Integer> school = new HashSet<>();

    public static WinterWorkReward getWinterWorkReward(Integer schoolId) {
        WinterWorkReward defaultValue = new WinterWorkReward(schoolId, 0);
        if (schoolId == null) return defaultValue;
        return rewardMap.getOrDefault(schoolId, defaultValue);
    }

    public static boolean contentCity(Integer schoolId) {
        if (schoolId == null) return false;
        return school.contains(schoolId);
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
                startTime = DateUtils.parseDate("2018-12-17 00:00:00", parsePattern);
                endTime = DateUtils.parseDate("2019-02-10 23:59:59", parsePattern);
            } else {
                startTime = DateUtils.parseDate("2018-12-17 00:00:00", parsePattern);
                endTime = DateUtils.parseDate("2019-02-10 23:59:59", parsePattern);
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

    private void initRewardMap() {
        try {
            String model = RuntimeMode.ge(Mode.STAGING) ? "production" : "staging";
            List<String> lines = loadFileToList("/activity/winter_work/" + model + "/reward/1.txt");
            for (String line : lines) {
                if (StringUtils.isEmpty(line)) continue;
                String[] split = line.split(",");
                if (split.length != 2) continue;

                WinterWorkReward finalReviewReward = new WinterWorkReward(
                        SafeConverter.toInt(split[0]),
                        SafeConverter.toInt(split[1])
                );

                rewardMap.put(finalReviewReward.getCityId(), finalReviewReward);
            }

            rewardMap = ImmutableMap.<Integer, WinterWorkReward>builder().putAll(rewardMap).build();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private void initSchool() {
        try {
            String model = RuntimeMode.ge(Mode.STAGING) ? "production" : "staging";
            List<String> lines = loadFileToList("/activity/winter_work/" + model + "/city/winter_work_city.txt");
            for (String line : lines) {
                if (StringUtils.isEmpty(line)) continue;
                school.add(SafeConverter.toInt(line));
            }

            school = ImmutableSet.<Integer>builder().addAll(school).build();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private List<String> loadFileToList(String file) throws IOException {
        List<String> list = new ArrayList<>();
        InputStream in = WinterWorkConfigUtils.class.getResourceAsStream(file);
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
    public static class WinterWorkReward {
        private Integer cityId;
        private Integer winterWork;
    }
}
