package com.voxlearning.utopia.service.parent.homework.util;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 科目
 * @author chongfeng.qi
 * @date 2018-11-07
 */
@Getter
@Setter
public class SubjectUtils implements Serializable {
    private static final long serialVersionUID = -3773765521796336377L;

    public static List<Subject> BASIC_SUBJECTS = Arrays.asList(
            Subject.CHINESE, Subject.MATH, Subject.ENGLISH
    );

    public static boolean isValid(String name) {
        return BASIC_SUBJECTS.stream().anyMatch(s -> s.name().equals(name));
    }
}
