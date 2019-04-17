/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.parkour.misc;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by Sadi.Wan on 2014/8/26.
 */
public class ParkouMiscUtil {
    /**
     * 直辖市
     */
    private static Set<Integer> customRegion = new HashSet<>(Arrays.asList(110000, 120000, 310000, 500000));

    public static LinkedHashMap<Integer,KeyValuePair<String,String>> getStudentRankRegion(StudentDetail sd){

        Integer provinceCode = sd.getRootRegionCode();
        String provinceRankName = "全省排名";
        Integer cityCode = sd.getCityCode();
        String cityRankName = "全市排名";
        if(customRegion.contains(provinceCode)){
            provinceRankName = "全市排名";
            cityCode = sd.getStudentSchoolRegionCode();//直辖市的取全区排名
            if(cityCode.equals(0)){
                cityCode = provinceCode;
            }
            cityRankName = "全区排名";
        }
        LinkedHashMap<Integer,KeyValuePair<String,String>> rtn = new LinkedHashMap<>();
        rtn.put(-1,new KeyValuePair<>("nationalRank","全国排名"));
        rtn.put(provinceCode,new KeyValuePair<>("provinceRank",provinceRankName));
        rtn.put(cityCode,new KeyValuePair<>("cityRank",cityRankName));
        return rtn;
    }
}
