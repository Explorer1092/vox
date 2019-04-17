package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamReportLoaderClient;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.student.StudentNewExamController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/5/9
 */
@Controller
@RequestMapping(value = "/parentMobile/jzt/newexam")
public class MobileJztNewExamController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private NewExamReportLoaderClient newExamReportLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    @RequestMapping(value = "independent/report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getStudentIndependentReport() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录账号");
        }
        String newExamId = getRequestString("newExamId");
        Long studentId = getRequestLong("sid");
        if (StringUtils.isBlank(newExamId) || studentId == 0) {
            return MapMessage.errorMessage("参数错误 newExamId {} sid {}", newExamId, studentId);
        }
        MapMessage mapMessage = newExamReportLoaderClient.independentExamDetailForParent(newExamId, studentId);
        if (mapMessage.isSuccess()) {
            String imgUrl = SafeConverter.toString(mapMessage.get("imgUrl"));
            mapMessage.put("imgUrl", getUserAvatarImgUrl(imgUrl));
        }
        return mapMessage;
    }


    /**
     * 考试列表 @copy from {@link StudentNewExamController#list()}
     * @return
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage list() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录账号");
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(getRequestLong("studentId"));
        String from = getRequestString("from");
        boolean fromPc = "pc".equalsIgnoreCase(from);
        boolean filterOld = getRequestBool("filterOld");
        boolean independent = getRequestBool("independent");
        if (studentDetail != null && studentDetail.getClazz() != null) {
            Clazz clazz = studentDetail.getClazz();
            School school = schoolLoaderClient.getSchoolLoader()
                    .loadSchool(clazz.getSchoolId())
                    .getUninterruptibly();
            if (school != null && school.getRegionCode() != null) {
                ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                MapMessage mapMessage = newExamServiceClient.loadAllExams(studentDetail, school, exRegion);
                if (!mapMessage.isSuccess()) {
                    return mapMessage;
                }
                //來自PC
                if (fromPc) {
                    return mapMessage;
                } else {
                    //是否过滤老模块数据
                    if (mapMessage.containsKey("newExamList")) {
                        List<Map<String, Object>> exams = (List<Map<String, Object>>) mapMessage.get("newExamList");
                        if (filterOld) {
                            exams = exams.stream()
                                    .filter(o -> !SafeConverter.toBoolean(o.get("oldNewExam")))
                                    .filter(o -> {
                                        //是不是自主考试
                                        boolean d = Objects.equals(SafeConverter.toString(o.get("examType")), NewExamType.independent.name());
                                        if (independent) {
                                            return d;
                                        } else {
                                            return !d;
                                        }
                                    })
                                    .collect(Collectors.toList());
                            mapMessage.put("newExamList", exams);
                            return mapMessage;
                        } else {
                            exams = exams.stream()
                                    .filter(o -> SafeConverter.toBoolean(o.get("oldNewExam")))
                                    .collect(Collectors.toList());
                            mapMessage.put("newExamList", exams);
                            return mapMessage;
                        }
                    } else {
                        return MapMessage.errorMessage("系统异常，请稍候重试");
                    }
                }
            }
        } else {
            return MapMessage.successMessage().add("newExamList", Collections.emptyList());
        }
        return MapMessage.errorMessage("系统异常，请稍候重试");
    }

    /**
     * 作业记录
     * 单元检测列表
     */
    @RequestMapping(value = "unit/test/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage unitTestList() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录账号");
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(getRequestLong("studentId"));
        return newExamServiceClient.loadStudentUnitTestHistoryList(studentDetail);
    }

}
