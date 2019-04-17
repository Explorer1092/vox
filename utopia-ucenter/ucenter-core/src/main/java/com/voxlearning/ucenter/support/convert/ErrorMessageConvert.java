package com.voxlearning.ucenter.support.convert;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ErrorMessageConvert {

    //FIXME: 从华盛顿拷贝过来的, 功能类似开放平台的更换系统班级

    private static final String RES_RESULT_NO_STUDENT_FOUND_MSG = "加入失败，该学生不存在";
    private static final String RES_RESULT_ABOVE_CLAZZ_QUOTA_MSG = "加入失败，班级人数已满";
    private static final String RES_RESULT_ALREADY_IN_CLASS_MSG = "加入失败，你已在此班级里";
    private static final String RES_RESULT_TEACHER_ACCOUNT_UNUSUAL_MSG = "此老师使用异常，你不能添加Ta为老师！";
    private static final String RES_DIFFERENT_CLAZZ_MSG = "学生已有班级，但选择的班级跟加入过的班级不一致";
    private static final String RES_RESULT_NO_CLASS_FOUND_MSG = "找不到对应班级";
    private static final String RES_RESULT_NO_TEACHER_FOUND_MSG = "找不到老师";
    private static final String RES_RESULT_JOIN_CLAZZ_ERROR_MSG = "班级不能加入，请联系老师!";
    private static final String RES_ALREADY_IN_CLAZZ_MSG = "已在此班级内,如需更换老师,请点击更换班级";


    public static String joinClazzErrorMsg(Map message) {
        String errorType = SafeConverter.toString(message.get("type"));
        if (errorType != null) {
            switch (errorType) {
                case "NO_STUDENT_FOUND":
                    return RES_RESULT_NO_STUDENT_FOUND_MSG;
                case "ABOVE_QUOTA":
                    return RES_RESULT_ABOVE_CLAZZ_QUOTA_MSG;
                case "ALREADY_IN_CLASS":
                    return RES_RESULT_ALREADY_IN_CLASS_MSG;
                case "CHEATING_TEACHER":
                    return RES_RESULT_TEACHER_ACCOUNT_UNUSUAL_MSG;
                case "DIFFERENT_CLAZZ":
                    return RES_DIFFERENT_CLAZZ_MSG;
                case "NO_SUCH_CLASS":
                    return RES_RESULT_NO_CLASS_FOUND_MSG;
                case "NO_TEACHER_FOUND":
                    return RES_RESULT_NO_TEACHER_FOUND_MSG;
                case "CLASS_FREE_JOIN_CLOSED":
                    return RES_RESULT_JOIN_CLAZZ_ERROR_MSG;
                case "MULTI_TEACHER_ONE_SUBJECT":
                    return RES_ALREADY_IN_CLAZZ_MSG;
            }
        }

        log.warn("student join clazz failed, message is {}", JsonUtils.toJson(message));
        return RES_RESULT_JOIN_CLAZZ_ERROR_MSG;
    }
}
