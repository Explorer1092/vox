package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignlivecast;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
@Named
public class AHL_CheckClazzGroup extends SpringContainerSupport implements AssignLiveCastHomeworkTask {

    @Override
    public void execute(AssignHomeworkContext context) {
        Long teacherId = context.getTeacher().getId();

        String idListText = SafeConverter.toString(context.getSource().get("clazzIds"));
        if (StringUtils.isBlank(idListText)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST,
                    "op", "assign homework"
            ));
            context.errorResponse("clazzIds is blank");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_NOT_EXIST);
            context.setTerminateTask(true);
            return;
        }

        String[] idTextArr = StringUtils.split(idListText, ",");
        Set<Long> groupIds = new HashSet<>();
        for (String idText : idTextArr) {
            String[] text = StringUtils.split(idText, "_");
            if (text.length == 2) {
                Long groupId = ConversionUtils.toLong(text[1]);
                if (groupId != 0) {
                    groupIds.add(groupId);
                }
            }
        }

        // 验证每个班组是否属于老师
//        List<ThirdPartyGroupMapper> groupMapperList = thirdPartyGroupLoaderClient.loadTeacherGroups(teacherId, ThirdPartyGroupType.USTALK_GROUP);
//        Set<Long> teacherHasGroupIds = groupMapperList.stream().map(ThirdPartyGroupMapper::getId).collect(Collectors.toSet());
//        if (!CollectionUtils.containsAll(groupIds, teacherHasGroupIds)) {
//            LogCollector.info("backend-general", MiscUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", context.getTeacher().getId(),
//                    "mod2", ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_PERMISSION,
//                    "op", "assign homework"
//            ));
//            context.errorResponse("Teacher {} has no groups {}", teacherId, groupIds);
//            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_PERMISSION);
//            context.setTerminateTask(true);
//            return;
//        }

        // 验证每个分组是否有学生
        context.setGroupIds(groupIds);
    }
}
