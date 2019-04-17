package com.voxlearning.washington.mapper.specialteacher.base;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.ClazzType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;

import java.util.*;
import java.util.stream.Stream;

import static com.voxlearning.utopia.api.constant.Subjects.ALL_SUBJECTS;

/**
 * 教务老师相关常量
 *
 * @author yuechen.wang
 * @since 2017-7-11
 **/
public class SpecialTeacherConstants {

    /**
     * 有效的年级
     */
    private static final List<ClazzLevel> validGrades = Arrays.asList(
            ClazzLevel.FIRST_GRADE,
            ClazzLevel.SECOND_GRADE,
            ClazzLevel.THIRD_GRADE,
            ClazzLevel.FOURTH_GRADE,
            ClazzLevel.FIFTH_GRADE,
            ClazzLevel.SIXTH_GRADE,
            ClazzLevel.SEVENTH_GRADE,
            ClazzLevel.EIGHTH_GRADE,
            ClazzLevel.NINTH_GRADE,
            ClazzLevel.SENIOR_ONE,
            ClazzLevel.SENIOR_TWO,
            ClazzLevel.SENIOR_THREE
    );

    /**
     * 学科中文名对应学科映射
     */
    public static final Map<String, Subject> subjectNameMap;

    /**
     * 年级中文名对应年级映射
     */
    public static final Map<String, ClazzLevel> gradeNameMap;

    /**
     * 下载Excel文件的文件后缀
     */
    public static final String XLS_SUFFIX = ".xls";

    /**
     * Excel文件通用时间戳格式
     */
    public static final String TIME_PATTERN = "yyyyMMddHHmmss";

    private static final String nameRegex = "^[\u2E80-\uFE4F]+([·•][\u2E80-\uFE4F]+)*$";

    static {
        subjectNameMap = new HashMap<>();
        for (Subject subject : ALL_SUBJECTS) {
            subjectNameMap.put(subject.getValue(), subject);
        }
        // 还有一些特殊的兼容
        subjectNameMap.put("语", Subject.CHINESE);
        subjectNameMap.put("数", Subject.MATH);
        subjectNameMap.put("外", Subject.ENGLISH);
        subjectNameMap.put("英", Subject.ENGLISH);
        subjectNameMap.put("物", Subject.PHYSICS);
        subjectNameMap.put("化", Subject.CHEMISTRY);
        subjectNameMap.put("生", Subject.BIOLOGY);
        subjectNameMap.put("政", Subject.POLITICS);
        subjectNameMap.put("史", Subject.HISTORY);
        subjectNameMap.put("地", Subject.GEOGRAPHY);
        subjectNameMap.put("信", Subject.INFORMATION);
        subjectNameMap.put("通", Subject.GENERIC_TECHNOLOGY);

        gradeNameMap = new HashMap<>();
        for (ClazzLevel clazzLevel : validGrades) {
            gradeNameMap.put(clazzLevel.getDescription(), clazzLevel);
        }
        // 还有一些特殊的兼容
        Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13).forEach(level -> gradeNameMap.put(level + "年级", ClazzLevel.parse(level)));
    }

    public static List<Subject> validSubjects() {
        return ALL_SUBJECTS;
    }

    public static Subject parseSubjectOfChinese(String subjectName) {
        if (StringUtils.isBlank(subjectName)) {
            return Subject.UNKNOWN;
        }
        return subjectNameMap.getOrDefault(subjectName, Subject.UNKNOWN);
    }

    public static List<ClazzLevel> validGrades() {
        return validGrades;
    }

    public static ClazzLevel parseGradeOfChinese(String gradeName) {
        if (StringUtils.isBlank(gradeName)) {
            return null;
        }
        return gradeNameMap.getOrDefault(gradeName, null);
    }

    public static ClazzType parseImportType(String importTypeName) {
        if (StringUtils.isBlank(importTypeName)) {
            return null;
        }
        switch (importTypeName) {
            case "行政班":
                return ClazzType.PUBLIC;
            case "教学班":
                return ClazzType.WALKING;
            default:
                return null;
        }
    }

    public static boolean checkChineseName(String name) {
        return checkChineseName(name, 10);
    }

    public static boolean checkChineseName(String name, int maxLength) {
        return name != null && name.length() <= maxLength && name.matches(nameRegex);
    }

    public static boolean checkDigitNumber(String number, int maxLength) {
        String regex = String.format("^[0-9]{1,%d}$", maxLength);
        return number != null && number.matches(regex);
    }

    public static String generatePassword(long schoolId) {
        return schoolId + "17zy";
    }

    public static String generateSmsContent(String name, String password) {
        return StringUtils.formatMessage(
                "老师您好，教务{}老师统一为校内老师注册了一起作业账号，您可通过手机号+密码{}登录17zuoye.com",
                name, password
        );
    }

    public static String generateXlsFileName(String name) {
        if (StringUtils.isBlank(name)) {
            return "下载文件";
        }
        return name + DateUtils.dateToString(new Date(), TIME_PATTERN) + XLS_SUFFIX;
    }

    public static String parseClassKeyToGradeName(String classKey) {
        try {
            String[] array = classKey.split("_");
            return ClazzLevel.getDescription(ConversionUtils.toInt(array[0]));
        } catch (Exception ignored) {
            return "";
        }
    }

    public static String parseClassKeyToClassName(String classKey) {
        try {
            String[] array = classKey.split("_");
            return ClazzLevel.getDescription(ConversionUtils.toInt(array[0])) + array[1];
        } catch (Exception ignored) {
            return "";
        }
    }

    public static boolean isAllBlank(final CharSequence... css) {
        if (css == null || css.length == 0) {
            return true;
        }
        for (final CharSequence cs : css) {
            if (StringUtils.isNotBlank(cs)) {
                return false;
            }
        }
        return true;
    }

}
