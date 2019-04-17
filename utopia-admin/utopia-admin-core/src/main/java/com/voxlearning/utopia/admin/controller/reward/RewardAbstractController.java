package com.voxlearning.utopia.admin.controller.reward;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import com.voxlearning.utopia.service.reward.constant.RewardUserVisibilityType;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by XiaoPeng.Yang on 14-7-16.
 */
abstract public class RewardAbstractController extends AbstractAdminSystemController {

    /**
     * uploader
     */
    @Inject
    protected CrmImageUploader crmImageUploader;

    /**
     * 与运算得到的用户可见性转为字符串，前端用
     * @param visibleInt
     * @return
     */
    protected String getVisibleStr(Integer visibleInt) {
        if (Objects.equals(visibleInt, 0)) {
            return "0";
        }
        String visible = null;
        if (RewardUserVisibilityType.isPrimaryTeacherFlag(visibleInt)) {
            if (StringUtils.isBlank(visible)) {
                visible = RewardUserVisibilityType.PRIMARY_TEACHER.intType() + "";
            } else {
                visible += "," + RewardUserVisibilityType.PRIMARY_TEACHER.intType();
            }
        }
        if (RewardUserVisibilityType.isPrimaryStudentFlag(visibleInt)) {
            if (StringUtils.isBlank(visible)) {
                visible = RewardUserVisibilityType.PRIMARY_STUDENT.intType() + "";
            } else {
                visible += "," + RewardUserVisibilityType.PRIMARY_STUDENT.intType();
            }
        }
        if (RewardUserVisibilityType.isJuniorTeacherFlag(visibleInt)) {
            if (StringUtils.isBlank(visible)) {
                visible = RewardUserVisibilityType.JUNIOR_TEACHER.intType() + "";
            } else {
                visible += "," + RewardUserVisibilityType.JUNIOR_TEACHER.intType();
            }
        }
        if (RewardUserVisibilityType.isJuniorStudentFlag(visibleInt)) {
            if (StringUtils.isBlank(visible)) {
                visible = RewardUserVisibilityType.JUNIOR_STUDENT.intType() + "";
            } else {
                visible += "," + RewardUserVisibilityType.JUNIOR_STUDENT.intType();
            }
        }
        return visible;
    }

    /**
     *  字符串（1,2,4,8）转为或运算获取结果
     * @param visibleStr
     * @return
     */
    protected Integer getVisibleInt(String visibleStr) {
        if (Objects.isNull(visibleStr)) {
            return 0;
        }
        Integer visible = 0;
        String[] visibleArr = visibleStr.split(",");
        for (int i=0; i<visibleArr.length; i++) {
            visible = visible | SafeConverter.toInt(visibleArr[i]);
        }
        return visible;
    }

}
