package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/7/25.
 */
@Controller
@RequestMapping(value = "/parentMobile/summerActivity")
@Slf4j
public class MobileSummerPreparationController extends AbstractMobileParentController {

    @RequestMapping(value = "/book_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage bookList() {
        long sid = getRequestLong("sid");
        User parent = currentParent();
        MapMessage mapMessage = checkUser(parent, sid);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        String sys = getRequestString("sys");
        if (StringUtils.isBlank(sys)) {
            return MapMessage.errorMessage("系统参数错误");
        }
        StudentDetail studentDetail = (StudentDetail) mapMessage.get("studentDetail");
        List<Map<String, Object>> maps = parentSelfStudyPublicHelper.recommendPicListenBook(studentDetail, parent, sys);
        Map<Subject, List<Map<String, Object>>> subjectListMap = maps.stream().
                map(t -> {
                    t.put("img", getCdnBaseUrlStaticSharedWithSep() + t.get("img"));
                    return t;
                })
                .collect(Collectors.groupingBy(t -> Subject.valueOf(SafeConverter.toString(t.get("subject")))));
//        MapMessage mapMessage = MapMessage.successMessage().add("book_list", subjectListMap);
//        if (studentDetail.getClazz() != null
//                && !studentDetail.getClazz().isTerminalClazz()
//                && studentDetail.isPrimaryStudent()
//                ) {
//            mapMessage.add("clazz", studentDetail.getClazzLevelAsInteger());
//        }
        return MapMessage.successMessage().add("book_list", subjectListMap).add("clazz",SafeConverter.toInt(mapMessage.get("clazz")));


    }


    @RequestMapping(value = "/is_correct_user.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isCorrectUser() {
        User parent = currentParent();
        long studentId = getRequestLong("sid");
        MapMessage mapMessage = checkUser(parent, studentId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        StudentDetail studentDetail = (StudentDetail) mapMessage.get("studentDetail");
        Boolean isBlackListParent = userBlacklistServiceClient.isInUserBlackList(parent);
        Boolean isBlackListStudent = userBlacklistServiceClient.isInUserBlackList(studentDetail);
        if (isBlackListParent || isBlackListStudent) {
            return MapMessage.errorMessage().add("is_correct_user", Boolean.FALSE);
        }
        return MapMessage.successMessage().add("is_correct_user", Boolean.TRUE);
    }


    private MapMessage checkUser(User parent, long studentId) {
        if (parent == null)
            return noLoginResult.add("is_correct_user", Boolean.FALSE);
        if (studentId == 0L)
            return MapMessage.errorMessage("您没有孩子哦!").add("is_correct_user", Boolean.FALSE);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("学生数据错误").add("is_correct_user", Boolean.FALSE);
        if (studentDetail.getClazz() != null
                && !studentDetail.getClazz().isTerminalClazz()
                && studentDetail.isPrimaryStudent()
                ) {
            return MapMessage.successMessage().add("studentDetail", studentDetail).add("clazz", studentDetail.getClazzLevelAsInteger());
        } else {
            return MapMessage.errorMessage().add("is_correct_user", Boolean.FALSE);
        }
    }
}
