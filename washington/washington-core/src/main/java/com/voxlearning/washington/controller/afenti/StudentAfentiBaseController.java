package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.support.AbstractController;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.*;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

/**
 * @author Ruib
 * @since 2016/8/4
 */
public class StudentAfentiBaseController extends AbstractController {

    protected MapMessage currentAfentiStudentDetailWithSubjectCheck() {
        User user = currentUser();
        if (null == user || (!user.isStudent() && !user.isParent()))
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());

        StudentDetail student = __student(user);
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());

        Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
        if (!AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        return MapMessage.successMessage().add("studentDetail", student).add("subject", subject);
    }

    protected MapMessage currentAfentiStudentDetail() {
        User user = currentUser();
        if (null == user || (!user.isStudent() && !user.isParent()))
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());

        StudentDetail student = __student(user);
        if (null == student) return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (student.getClazz() == null)
            return MapMessage.errorMessage(NO_CLAZZ.getInfo()).setErrorCode(NO_CLAZZ.getCode());

        return MapMessage.successMessage().add("studentDetail", student);
    }

    private StudentDetail __student(User user) {
        StudentDetail student;
        if (user.isParent()) {
            Long studentId;
            String json = getRequestString("commonParams");
            if (StringUtils.isBlank(json)) {
                studentId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"), Long.MIN_VALUE);
            } else {
                Map<String, Object> params = JsonUtils.fromJson(json);
                studentId = SafeConverter.toLong(params.getOrDefault("studentId", null), Long.MIN_VALUE);
            }
            if (studentId == Long.MIN_VALUE) return null;
            Set<Long> children = parentLoaderClient.loadParentStudentRefs(user.getId())
                    .stream()
                    .map(StudentParentRef::getStudentId)
                    .collect(Collectors.toSet());
            if (!children.contains(studentId)) return null;
            student = studentLoaderClient.loadStudentDetail(studentId);
        } else {
            student = user instanceof StudentDetail ? (StudentDetail) user : currentStudentDetail();
        }
        return student;
    }

    protected Subject getSubject() {
        Subject subject = Subject.of(getRequestString("subject"));
        if (!AVAILABLE_SUBJECT.contains(subject))
            return null;
        return subject;
    }

    protected OrderProductServiceType getOrderProductServiceType(Subject subject) {
        if (!AVAILABLE_SUBJECT.contains(subject)) return null;

        switch (subject) {
            case ENGLISH:
                return AfentiExam;
            case MATH:
                return AfentiMath;
            case CHINESE:
                return AfentiChinese;
            default:
                return null;
        }
    }

    protected OrderProductServiceType getVideoProductServiceType(Subject subject) {
        if (!AVAILABLE_SUBJECT.contains(subject)) return null;

        switch (subject) {
            case MATH:
                return AfentiMathVideo;
            case ENGLISH:
                return AfentiExamVideo;
            case CHINESE:
                return AfentiChineseVideo;
            default:
                return null;
        }
    }
}
