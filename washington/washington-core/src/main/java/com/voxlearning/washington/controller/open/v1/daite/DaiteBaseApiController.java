package com.voxlearning.washington.controller.open.v1.daite;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.ArtScienceType;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_APP_ERROR_MSG;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_BAD_REQUEST_MSG;

/**
 * 戴特base controller
 */
public class DaiteBaseApiController extends AbstractApiController {

    @Inject protected SchoolExtServiceClient schoolExtServiceClient;

    public static final String userWebSource = "Daite";

    @Override
    public void validateRequestNoSessionKey(String... paramKeys) {
        VendorApps app = getApiRequestApp();
        if (app == null || !Objects.equals(app.getAppKey(), "Daite")) {
            logValidateError("error_app");
            throw new IllegalArgumentException(RES_RESULT_APP_ERROR_MSG);
        }
        // 拿到所有的请求参数，和paramKeys 取交集做sig
        Map<String, String[]> parameterMap = getRequest().getParameterMap();
        String[] filterParamKeys = Arrays.stream(paramKeys).filter(p -> parameterMap.containsKey(p)).toArray(String[] :: new);
        if (!isValidRequest(false, filterParamKeys)) {
            logValidateError("error_sig");
            throw new IllegalArgumentException(RES_RESULT_BAD_REQUEST_MSG);
        }
    }

    /**
     * SCIENCE / ART 哪个多取哪个， 一样多随机， 都没有的话 ARTSCIENCE 不分文理
     * @return
     */
    protected ArtScienceType getScienceType(List<Group> groups) {
        long artCount = groups.stream().filter(group -> !group.getDisabled() && group.getArtScienceType() == ArtScienceType.ART).count();
        long scienceCount = groups.stream().filter(group -> !group.getDisabled() && group.getArtScienceType() == ArtScienceType.SCIENCE).count();
        if (artCount == 0 && scienceCount == 0) {
            return ArtScienceType.ARTSCIENCE;
        } else {
            if (scienceCount >= artCount) {
                return ArtScienceType.SCIENCE;
            } else {
                return ArtScienceType.ART;
            }
        }
    }

    protected boolean isNotDaiteUserSchool(Long userId, UserType userType) {
        boolean isDaiteSchool = false;
        if (UserType.STUDENT == userType) {
            StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
            isDaiteSchool = student.getClazz() == null || !schoolExtServiceClient.isDaiteSchool(student.getClazz().getSchoolId());
        } else if (UserType.TEACHER == userType) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(userId);
            isDaiteSchool = !schoolExtServiceClient.isDaiteSchool(teacherDetail.getTeacherSchoolId());
        }
        return isDaiteSchool;
    }

    protected boolean isNotDaiteSchool(Long schoolId) {
        return !schoolExtServiceClient.isDaiteSchool(schoolId);
    }

    /**
     * 17的学制转成DT
     * @param clazzLevel
     * @return
     * @author zhouwei
     */
    protected String classLevelToDaite(String clazzLevel) {
        Integer level = SafeConverter.toInt(clazzLevel);
        if (level >= 11 && level <= 13) {
            return String.valueOf(level - 1);
        }
        return clazzLevel;
    }

    /**
     * DT的学制转成17
     * @param clazzLevel
     * @return
     * @author zhouwei
     */
    protected String classLevelTo17(String clazzLevel) {
        Integer level = SafeConverter.toInt(clazzLevel);
        if (level >= 10 && level <= 12) {
            return String.valueOf(level + 1);
        }
        return clazzLevel;
    }
}
