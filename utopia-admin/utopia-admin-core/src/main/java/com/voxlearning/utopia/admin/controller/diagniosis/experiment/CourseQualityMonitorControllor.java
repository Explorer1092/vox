package com.voxlearning.utopia.admin.controller.diagniosis.experiment;

import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.entity.*;
import com.voxlearning.utopia.admin.service.experiment.CourseQualityMonitorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2018/8/16
 */
@Controller
@RequestMapping("/crm/course/monitor")
public class CourseQualityMonitorControllor extends AbstractAdminSystemController {

    @Inject
    private CourseQualityMonitorService courseQualityMonitorService;

    @RequestMapping(value = "course/list.vpage", method = RequestMethod.GET)
    public String listBookCourseAnalysis(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        List<BookCourseAnalysisResult> bookCourseAnalysisResultList = courseQualityMonitorService.findAllBookCourseAnalysisResult();
        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 20);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(courseQualityMonitorService.convertBookCourseAnalysisList(bookCourseAnalysisResultList), pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("total", bookCourseAnalysisResultList != null ? bookCourseAnalysisResultList.size() : 0);
        model.addAttribute("pageNumber", pageNumber);
        return "experiment/bookCourseAnalysisList";
    }

    @RequestMapping(value = "book/preview.vpage", method = RequestMethod.GET)
    public String bookPreview(Model model) {
        String seriesId = getRequestString("seriesId");
        String bookId = getRequestString("bookId");
        String bookName = getRequestString("bookName");
        List<BookVariantCourseAnalysisResult> bookVariantCourseAnalysisResultList = courseQualityMonitorService.finaBookVariantCourseAnalysisResultBySeriesIdBookId(seriesId, bookId);
        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 20);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(courseQualityMonitorService.convertBookVariantCourseAnalysisResult(bookVariantCourseAnalysisResultList), pageable);
        model.addAttribute("seriesId", seriesId);
        model.addAttribute("bookId", bookId);
        model.addAttribute("pageData", pageData);
        model.addAttribute("bookName", bookName);
        model.addAttribute("total", bookVariantCourseAnalysisResultList != null ? bookVariantCourseAnalysisResultList.size() : 0);
        model.addAttribute("pageNumber", pageNumber);
        return "experiment/bookVariantCourseAnalysisList";
    }

    @RequestMapping(value = "variant/list.vpage", method = RequestMethod.GET)
    public String variantSummary(Model model) {
        String seriesId = getRequestString("seriesId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String sectionId = getRequestString("sectionId");
        String variantId = getRequestString("variantId");
        String variantName = getRequestString("variantName");
        String sectionName = getRequestString("sectionName");
        List<VariantCourseAnalysisResult> variantCourseAnalysisResultList = courseQualityMonitorService
                .findVariantCourseAnalysisResult(seriesId, bookId, unitId, sectionId, variantId);
        int pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 20);
        Page<Map<String, Object>> pageData = PageableUtils.listToPage(courseQualityMonitorService.convertVariantCourseAnalysisResult(variantCourseAnalysisResultList), pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("seriesId", seriesId);
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("sectionId", sectionId);
        model.addAttribute("variantId", variantId);
        model.addAttribute("variantName", variantName);
        model.addAttribute("sectionName", sectionName);
        model.addAttribute("total", variantCourseAnalysisResultList != null ? variantCourseAnalysisResultList.size() : 0);
        model.addAttribute("pageNumber", pageNumber);
        return "experiment/variantSummaryList";
    }
    @RequestMapping(value = "variant/course/detail.vpage", method = RequestMethod.GET)
    public String variantCourseDetail(Model model) {
        String seriesId = getRequestString("seriesId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String sectionId = getRequestString("sectionId");
        String variantId = getRequestString("variantId");
        String courseId = getRequestString("courseId");
        String preId = getRequestString("preId");
        String postId = getRequestString("postId");
        String courseName = getRequestString("courseName");
        List<CoursePageAnalysisResult> coursePageAnalysisResultList = courseQualityMonitorService.findCoursePageAnalysisResult(seriesId, bookId, unitId, sectionId, variantId, courseId, preId, postId);
        List<Map<String, Object>> pageData = courseQualityMonitorService.convertCoursePageAnalysisResult(coursePageAnalysisResultList);
        model.addAttribute("pageData", pageData);
        model.addAttribute("total", coursePageAnalysisResultList != null ? coursePageAnalysisResultList.size() : 0);
        List<CourseAnswer> courseAnswerList = courseQualityMonitorService.findCourseAnswerAnalysisResult(seriesId, bookId, unitId, sectionId, variantId, courseId, preId, postId);
        model.addAttribute("courseAnswerList",courseAnswerList);

        model.addAttribute("preId", preId);
        model.addAttribute("postId", postId);
        model.addAttribute("courseName", courseName);
        model.addAttribute("courseId", courseId);
        return "experiment/variantCourseDetail";
    }

}
