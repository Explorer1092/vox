package com.voxlearning.washington.controller.newexamv2;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.washington.controller.teacher.AbstractTeacherController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author maofeng.lu
 * @since 2017/10/24.17:58
 */
@Controller
@RequestMapping("/newexamv2")
public class NewExamv2Controller extends AbstractTeacherController {
    /**
     * 统考报告详情
     */
    @RequestMapping(value = "viewstudent.vpage", method = RequestMethod.GET)
    public String historyDetail(Model model) {
        String newExamId = getRequestString("examId");
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return "redirect:/index.vpage";
        }
        Subject subject = newExam.getSubject();
        model.addAttribute("subject", subject);
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        if(newExam.getSchoolLevel() == SchoolLevel.JUNIOR && isNewModelExam(newExam)){
            return "newexamv3/viewstudent";
        }else{
            return "newexamv2/viewstudent";
        }
    }

    /**
     * 统考报告试卷预览
     */
    @RequestMapping(value = "previewpaper.vpage", method = RequestMethod.GET)
    public String previewPaper(Model model) {
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return "newexamv3/previewpaper";
    }
}
