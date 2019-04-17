package com.voxlearning.utopia.service.business.impl.support;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.business.api.constant.LevelOfSchool;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 2017秋季开学老师端活动
 * Feature #50111 活动一 - 老师带新认证学生
 * 学校等级字典
 * Created by Yuechen.Wang on 2017/8/14.
 */
public class NewTermActivitySchoolDictionary {
    private static final Logger logger = LoggerFactory.getLogger(NewTermActivitySchoolDictionary.class);

    private static Map<Long, LevelOfSchool> schoolLevelMap;
    private static List<Long> schoolBlockList;
    private static List<Long> tuckerSchoolBlackList;

    private static final String LEVEL_A = "/activity/term_2017_school_dict_a";
    private static final String LEVEL_B = "/activity/term_2017_school_dict_b";
    private static final String LEVEL_C_BLOCK = "/activity/term_2017_school_block_dict_c";
    private static final String TUCKER_SCHOOL_BLACK = "/activity/tucker_activity_black_school_dict";

    static {
        schoolLevelMap = new LinkedHashMap<>();
        schoolBlockList = new LinkedList<>();
        tuckerSchoolBlackList = new LinkedList<>();
        try {
            logger.info("Start Initialize Term 2017 Teacher Activity School Dictionary.");
            // 测试环境不加载了,
            if (RuntimeMode.isUsingProductionData()) {
                loadSchoolDictionary(LevelOfSchool.A, LEVEL_A);
                loadSchoolDictionary(LevelOfSchool.B, LEVEL_B);
                loadSchoolBlackList(schoolBlockList, LEVEL_C_BLOCK);
                loadSchoolBlackList(tuckerSchoolBlackList, TUCKER_SCHOOL_BLACK);
            }
            logger.info("Term 2017 Teacher Activity School Dictionary Load Success, total {} found, block : {}, tucker blacklist: {}", schoolLevelMap.size(), schoolBlockList.size(), tuckerSchoolBlackList.size());
        } catch (Exception ex) {
            logger.error("Failed initial schoolLevel Map for Feature #50111 , Please check it right now", ex);
        }

    }

    @SneakyThrows(IOException.class)
    private static void loadSchoolDictionary(LevelOfSchool level, String file) {
        InputStream resource = null;
        BufferedReader reader = null;
        try {
            resource = NewTermActivitySchoolDictionary.class.getResourceAsStream(file);
            reader = new BufferedReader(new InputStreamReader(resource, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                Long schoolId = SafeConverter.toLong(line);
                if (schoolId > 0L) schoolLevelMap.put(schoolId, level);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (resource != null) {
                resource.close();
            }
        }
    }

    @SneakyThrows(IOException.class)
    private static void loadSchoolBlackList(List<Long> list, String file) {
        InputStream resource = null;
        BufferedReader reader = null;
        try {
            resource = NewTermActivitySchoolDictionary.class.getResourceAsStream(file);
            reader = new BufferedReader(new InputStreamReader(resource, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                Long schoolId = SafeConverter.toLong(line);
                if (schoolId > 0L) list.add(schoolId);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (resource != null) {
                resource.close();
            }
        }
    }

    public static LevelOfSchool getSchoolLevel(Long schoolId) {
        if (schoolId == null) {
            return null;
        }
        if (schoolBlockList.contains(schoolId)) {
            return null;
        }
        return schoolLevelMap.getOrDefault(schoolId, LevelOfSchool.C);
    }

    public static boolean checkTuckerSchool(Long schoolId) {
        return schoolId != null && !tuckerSchoolBlackList.contains(schoolId);
    }

}
