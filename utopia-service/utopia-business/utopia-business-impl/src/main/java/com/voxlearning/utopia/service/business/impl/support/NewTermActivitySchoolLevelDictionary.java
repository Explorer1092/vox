package com.voxlearning.utopia.service.business.impl.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.business.api.constant.TeacherNewTermActivityCategory;
import com.voxlearning.utopia.business.api.mapper.ActivitySchoolLevelMap;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 2017秋季开学老师端活动
 * Enhancement #63183
 * 小学老师活动1-英语/语文学科扩充
 * Created by Yuechen.Wang on 2018/03/08.
 */
public class NewTermActivitySchoolLevelDictionary {
    private static final Logger logger = LoggerFactory.getLogger(NewTermActivitySchoolLevelDictionary.class);

    private static Map<Long, ActivitySchoolLevelMap> activitySchoolLevelMap;

    static {
        activitySchoolLevelMap = new HashMap<>();
        // 小学英语
        activitySchoolLevelMap.put(
                TeacherNewTermActivityCategory.PrimaryEnglishStudent.getId(),
                new ActivitySchoolLevelMap(TeacherNewTermActivityCategory.PrimaryEnglishStudent.getId(), "D")
        );
        // 小学语文
        activitySchoolLevelMap.put(
                TeacherNewTermActivityCategory.PrimaryChineseStudent.getId(),
                new ActivitySchoolLevelMap(TeacherNewTermActivityCategory.PrimaryChineseStudent.getId(), "C")
        );
        try {
            logger.info("Start Initialize Teacher Activity School Level Dictionary.");
            // 小学英语
            activitySchoolLevelMap.put(
                    TeacherNewTermActivityCategory.PrimaryEnglishStudent.getId(),
                    loadSchoolLevelDictionary(TeacherNewTermActivityCategory.PrimaryEnglishStudent.getId(), "D")
            );
            // 小学语文
            activitySchoolLevelMap.put(
                    TeacherNewTermActivityCategory.PrimaryChineseStudent.getId(),
                    loadSchoolLevelDictionary(TeacherNewTermActivityCategory.PrimaryChineseStudent.getId(), "C")
            );
        } catch (Exception ex) {
            logger.error("Failed initial schoolLevel Map for Feature #50111 , Please check it right now", ex);
        }

    }

    @SneakyThrows(IOException.class)
    private static ActivitySchoolLevelMap loadSchoolLevelDictionary(Long activityId, String defaultLevel) {
        InputStream resource = null;
        BufferedReader reader = null;
        ActivitySchoolLevelMap map = new ActivitySchoolLevelMap(activityId, defaultLevel);
        // 先读取正常学校
        try {
            resource = NewTermActivityCityDictionary.class.getResourceAsStream(map.genMapFile());
            reader = new BufferedReader(new InputStreamReader(resource, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                if (split.length != 2) continue;
                Long schoolId = SafeConverter.toLong(split[0]);
                String level = SafeConverter.toString(split[1]);
                if (schoolId > 0L && StringUtils.isNotBlank(level)) map.appendSchoolMap(schoolId, level);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (resource != null) {
                resource.close();
            }
        }
        // 再读取黑名单
        try {
            resource = NewTermActivityCityDictionary.class.getResourceAsStream(map.genBalckListFile());
            reader = new BufferedReader(new InputStreamReader(resource, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                Long schoolId = SafeConverter.toLong(line);
                if (schoolId > 0L) map.appendBlackList(schoolId);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (resource != null) {
                resource.close();
            }
        }
        return map;
    }

    public static String getSchoolLevel(Long activityId, Long schoolId) {
        ActivitySchoolLevelMap map = NewTermActivitySchoolLevelDictionary.activitySchoolLevelMap.get(activityId);
        if (map == null) {
            return null;
        }
        if (map.checkBlackList(schoolId)) {
            return null;
        }
        return map.getSchoolLevel(schoolId);
    }
}
