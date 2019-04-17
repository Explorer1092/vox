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

package com.voxlearning.ucenter.controller.School;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.SchoolType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.CharSet;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.user.api.entities.School;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author changyuan.liu
 * @since 2015.12.16
 */
@Controller
@RequestMapping("/school")
@Slf4j
public class SchoolController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;

    @RequestMapping(value = "areaschool.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findSchoolInSpecifiedRegion() {
        int code = getRequestInt("area");
        if (code == 0) {
            return MapMessage.errorMessage().add("total", 0).add("rows", Collections.emptyList());
        }
        String level = getRequestString("level");
        Ktwelve ktwelve = Ktwelve.of(level);
        if (ktwelve != Ktwelve.PRIMARY_SCHOOL && ktwelve != Ktwelve.JUNIOR_SCHOOL && ktwelve != Ktwelve.INFANT) {
            ktwelve = Ktwelve.PRIMARY_SCHOOL;
        }
        List<School> schoolList = loadAreaSchools(Collections.singleton(code),
                SchoolLevel.safeParse(ktwelve.getLevel()));
        return MapMessage.successMessage().add("total", schoolList.size()).add("rows", schoolList);
    }

    @RequestMapping(value = "areaschoolgbfl.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findSchoolInSpecifiedRegionSplitedByFirstLetter() {
        int code = getRequestInt("area");
        if (code == 0) {
            return MapMessage.errorMessage().add("total", 0).add("rows", Collections.emptyList());
        }
        String level = getRequestString("level");
        Ktwelve ktwelve = Ktwelve.of(level);
        if (ktwelve != Ktwelve.PRIMARY_SCHOOL && ktwelve != Ktwelve.JUNIOR_SCHOOL && ktwelve != Ktwelve.INFANT) {
            ktwelve = Ktwelve.PRIMARY_SCHOOL;
        }
        // FIXME show waiting and success state school
        List<School> schools = loadAreaSchools(Collections.singleton(code),
                SchoolLevel.safeParse(ktwelve.getLevel()));
        List<Map<String, Object>> rows = getSchoolList(schools);
        return MapMessage.successMessage().add("total", rows.size()).add("rows", rows);
    }

    @RequestMapping(value = "areaschoolrs.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findRegionsSchools() {
        String regions = getRequestString("regions");
        Ktwelve ktwelve = Ktwelve.of(getRequestString("level"));
        if (StringUtils.isBlank(regions) || ktwelve == Ktwelve.UNKNOWN)
            return MapMessage.errorMessage().add("total", 0).add("rows", Collections.emptyList());

        Set<Integer> codes = Arrays.asList(StringUtils.split(regions, ",")).stream()
                .map(ConversionUtils::toInt).collect(Collectors.toSet());

        List<School> schools = null;
        if (ktwelve == Ktwelve.SENIOR_SCHOOL) {
            schools = loadAreaSchools(codes, SchoolLevel.HIGH);
        } else {
            schools = loadAreaSchools(codes, SchoolLevel.safeParse(ktwelve.getLevel()));
        }

        List<Map<String, Object>> rows = getSchoolList(schools);
        return MapMessage.successMessage().add("total", rows.size()).add("rows", rows);
    }

    /////////////////////////////////////////Private Methods//////////////////////////////////////////////

    private List<Map<String, Object>> getSchoolList(List<School> schoolList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (School each : schoolList) {
            if (StringUtils.isNotBlank(each.getShortName())) {
                Map<String, Object> school = new HashMap<>();
                school.put("id", each.getId());
                school.put("name", each.getShortName());
                school.put("region", each.getRegionCode());
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

    private List<School> loadAreaSchools(Collection<Integer> regionCodes, SchoolLevel schoolLevel) {
        return raikouSystem.querySchoolLocations(regionCodes)
                .enabled()
                .waitingSuccess()
                .level(schoolLevel)
                .filter(s -> s.getType() != SchoolType.TRAINING.getType() && s.getType() != SchoolType.CONFIDENTIAL.getType())
                .transform()
                .asList();
    }
}
