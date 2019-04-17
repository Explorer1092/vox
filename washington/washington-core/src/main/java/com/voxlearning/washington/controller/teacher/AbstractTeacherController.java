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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.support.AbstractController;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract teacher controller implementation.
 *
 * @author Xiaohai Zhang
 * @author Rui Bao
 * @since 2013-07-25 10:59
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract public class AbstractTeacherController extends AbstractController {

    protected List<Map<String, Object>> getClazzList(){
        Teacher teacher = getSubjectSpecifiedTeacher();
        return deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId())
                .stream()
                .filter(c -> !c.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("className", c.formalizeClazzName());
                    map.put("classLevel", c.getClassLevel());
                    map.put("groups", Collections.emptyList());
                    return map;
                })
                .collect(Collectors.toList());
    }

    protected boolean hasClazzTeachingPermission(Long teacherId, Long clazzId) {
        return teacherLoaderClient.isTeachingClazz(teacherId, clazzId);
    }

    protected void sendMessage(User receiver, String payload) {
        if (StringUtils.isBlank(payload)) {
            return;
        }
        payload = StringUtils.replace(payload, "老师老师", "老师");
        teacherLoaderClient.sendTeacherMessage(receiver.getId(), payload);
    }

    protected Set<String> getLastNameFirstCapital(String chinese) {
        // 汉语拼音格式输出类
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出设置,大小写,音标方式,V等
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

        Set<String> result = new TreeSet<>();
        char lastNameChar = chinese.toCharArray()[0];

        if (lastNameChar > 128) {
            try {
                String[] temp = PinyinHelper.toHanyuPinyinStringArray(lastNameChar, defaultFormat);
                if (null != temp) {
                    for (String str : temp) {
                        result.add(str.substring(0, 1));
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            result.add(chinese.substring(0, 1).toUpperCase());
        }
        return result;
    }
}
