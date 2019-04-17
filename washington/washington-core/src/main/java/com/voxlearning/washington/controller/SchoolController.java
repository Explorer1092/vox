/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.SchoolType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.CharSet;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * School controller implementation.
 *
 * @author Jingwei Dong
 * @author Xiaohai Zhang
 * @author Rui Bao
 * @since 2011-08-05
 */
@Controller
@RequestMapping("/school")
@NoArgsConstructor
@Slf4j
public class SchoolController extends AbstractController {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @RequestMapping(value = "areaschool.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findSchoolInSpecifiedRegion() {
        int code = getRequestInt("area");
        if (code == 0) {
            return MapMessage.errorMessage().add("total", 0).add("rows", Collections.emptyList());
        }
        List<School> schoolList = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolLoaderClient.getSchoolLoader()
                        .querySchoolLocations(code)
                        .getUninterruptibly()
                        .stream()
                        .filter(e -> !e.isDisabled())
                        .filter(e -> e.match(SchoolLevel.JUNIOR))
                        .filter(e -> e.match(AuthenticationState.SUCCESS) || e.match(AuthenticationState.WAITING))
                        .map(School.Location::getId)
                        .collect(Collectors.toSet()))
                .getUninterruptibly()
                .values()
                .stream()
                .sorted(Comparator.comparing(School::getId))
                .collect(Collectors.toList());
        return MapMessage.successMessage().add("total", schoolList.size()).add("rows", schoolList);
    }

    @RequestMapping(value = "schoolgbfl.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findSchoolInSpecifiedRegionSplitedByFirstLetter(@RequestParam("area") String area) {
        int code = SafeConverter.toInt(area, -1);
        if (code < 0) {
            return MapMessage.errorMessage().add("total", 0).add("rows", Collections.emptyList());
        }

        List<School> schools = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolLoaderClient.getSchoolLoader()
                        .querySchoolLocations(code)
                        .getUninterruptibly()
                        .stream()
                        .filter(e -> !e.isDisabled())
                        .filter(e -> e.match(AuthenticationState.SUCCESS) || e.match(AuthenticationState.WAITING))
                        .filter(e -> e.match(SchoolLevel.JUNIOR))
                        .filter(e -> !e.match(SchoolType.CONFIDENTIAL))// 不显示虚拟学校
                        .map(School.Location::getId)
                        .collect(Collectors.toSet()))
                .getUninterruptibly()
                .values()
                .stream()
                .sorted(Comparator.comparing(School::getId))
                .collect(Collectors.toList());

        List<Map<String, Object>> rows = getSchoolList(schools);
        return MapMessage.successMessage().add("total", rows.size()).add("rows", rows);
    }

    // private method

    private List<Map<String, Object>> getSchoolList(List<School> schoolList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (School each : schoolList) {
            if (StringUtils.isNotBlank(each.getShortName())) {
                Map<String, Object> school = new HashMap<>();
                school.put("id", each.getId());
                school.put("name", each.getShortName());
                List<String> letters = new ArrayList<>();
                Set<String> firstCapitalList = getSchoolFirstCapital(each.getShortName());
                for (String firstCapial : firstCapitalList) {
                    if (CharSet.ASCII_ALPHA_UPPER.contains(firstCapial.charAt(0))) {
                        letters.add(firstCapial);
                    }
                }
                school.put("letters", letters);
                result.add(school);
            }
        }
        return result;
    }

    private Map<String, List<School>> getSchoolMap(List<School> schoolList) {
        Map<String, List<School>> schoolMap = buildNeonatalMap();

        for (School each : schoolList) {
            schoolMap.get("ALL").add(each);
            if (StringUtils.isBlank(each.getShortName())) {
                schoolMap.get("OTHER").add(each);
            } else {
                Set<String> firstCapitalList = getSchoolFirstCapital(each.getShortName());
                for (String firstCapial : firstCapitalList) {
                    if (CharSet.ASCII_ALPHA_UPPER.contains(firstCapial.charAt(0))) {
                        schoolMap.get(firstCapial).add(each);
                    } else {
                        schoolMap.get("OTHER").add(each);
                    }
                }
            }

        }
        return schoolMap;
    }

    private Map<String, List<School>> buildNeonatalMap() {
        Map<String, List<School>> result = new LinkedHashMap<>();
        List<String> alpha = Arrays.asList("ALL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "OTHER");
        for (String each : alpha) {
            result.put(each, new ArrayList<School>());
        }
        return result;
    }

    private Set<String> getSchoolFirstCapital(String chinese) {
        // 汉语拼音格式输出类
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出设置,大小写,音标方式,V等
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

        Set<String> result = new TreeSet<>();
        char schoolNameChar = chinese.toCharArray()[0];

        if (schoolNameChar > 128) {
            try {
                String[] temp = PinyinHelper.toHanyuPinyinStringArray(schoolNameChar, defaultFormat);
                if (null != temp) {
                    for (String str : temp) {
                        result.add(str.substring(0, 1));
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                log.error(e.getMessage(), e);
            }
        } else {
            result.add(chinese.substring(0, 1).toUpperCase());
        }
        return result;
    }
}
