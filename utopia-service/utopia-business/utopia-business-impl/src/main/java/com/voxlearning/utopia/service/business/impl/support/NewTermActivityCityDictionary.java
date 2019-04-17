package com.voxlearning.utopia.service.business.impl.support;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.business.api.constant.CityLevel;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 老师端活动
 * 城市等级字典
 * Created by Yuechen.Wang on 2017/8/14.
 */
public class NewTermActivityCityDictionary {
    private static final Logger logger = LoggerFactory.getLogger(NewTermActivityCityDictionary.class);

    private static Map<Integer, CityLevel> cityLevelMap;
    private static Map<Long, Set<Integer>> activityCityMap;
    private static List<Long> schoolBlockList;

    private static final String DIR = "/activity/city_level_dict";

    static {
        cityLevelMap = new LinkedHashMap<>();
        activityCityMap = new LinkedHashMap<>();
        try {
            logger.info("Start Initialize Teacher Activity City Dictionary.");
            loadCityDictionary(DIR);
            logger.info("Teacher Activity City Dictionary Load Success, total {} found", cityLevelMap.size());
        } catch (Exception ex) {
            logger.error("Failed initial cityLevelMap Map , Please check it right now");
        }

    }

    @SneakyThrows(IOException.class)
    private static void loadCityDictionary(String file) {
        InputStream resource = null;
        BufferedReader reader = null;
        try {
            resource = NewTermActivityCityDictionary.class.getResourceAsStream(file);
            reader = new BufferedReader(new InputStreamReader(resource, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                if (split.length > 3) continue;

                Integer cityCode = SafeConverter.toInt(split[1]);
                CityLevel level = CityLevel.parse(split[0]);
                if (cityCode > 0L && level != null) cityLevelMap.put(cityCode, level);
                // 如果有第三列，则是城市和活动的对照信息
                if(split.length == 3){
                    Long activityId = SafeConverter.toLong(split[2]);
                    Set<Integer> cityList = Optional.ofNullable(activityCityMap.get(activityId))
                            .orElseGet(() -> {
                                HashSet<Integer> newSet = new HashSet<>();
                                activityCityMap.put(activityId,newSet);
                                return newSet;
                            });

                    cityList.add(cityCode);
                }
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

    public static CityLevel getCityLevel(Integer cityCode,Long activityId) {
        if (cityCode == null || activityId == null) {
            return null;
        }

        Set<Integer> citySet =  activityCityMap.getOrDefault(activityId,Collections.emptySet());
        if(!citySet.contains(cityCode))
            return null;

        return cityLevelMap.getOrDefault(cityCode, null);
    }

}
