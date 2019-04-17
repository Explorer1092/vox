package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherFakeService;
import com.voxlearning.utopia.api.constant.ReviewStatus;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * @author Jia HuanYin
 * @since 2015/12/3
 */
@Controller
@RequestMapping(value = "/crm/teacher_fake")
public class CrmTeacherFakeController extends CrmAbstractController {

    @Inject
    CrmTeacherFakeService crmTeacherFakeService;

    @RequestMapping(value = "teacher_fakes.vpage")
    public String teacherFakes(Model model) {
        ReviewStatus reviewStatus = ReviewStatus.nameOf(requestString("reviewStatus", ReviewStatus.WAIT.name()));
        Long teacherId = requestLong("teacherId");
        Pageable pageable = buildSortPageRequest(25, "createTime");
        Page<CrmTeacherFake> teacherFakes = crmTeacherFakeService.loadTeacherFakes(reviewStatus, teacherId, pageable);
        model.addAttribute("teacherFakes", teacherFakes);
        model.addAttribute("reviewStatuses", ReviewStatus.values());
        model.addAttribute("reviewStatus", reviewStatus);
        model.addAttribute("teacherId", teacherId);
        return "crm/teacher_fake/teacher_fakes";
    }

    @RequestMapping(value = "review_fake.vpage")
    @ResponseBody
    public CrmTeacherFake reviewFake() {
        String id = requestString("id");
        ReviewStatus reviewStatus = ReviewStatus.nameOf(requestString("reviewStatus"));
        String reviewNote = requestString("reviewNote");
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        return crmTeacherFakeService.reviewTeacherFake(id, reviewStatus, reviewNote, adminUser);
    }
}
