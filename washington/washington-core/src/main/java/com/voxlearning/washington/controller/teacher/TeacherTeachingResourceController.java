package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/teachingresource")
public class TeacherTeachingResourceController extends AbstractTeacherController {
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return "redirect:/teacher/showtip.vpage";
        }
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        model.addAttribute("subject", teacherDetail.getSubject());
        List<Map<String, Object>> subjectMapperList = teacherDetail.getSubjects()
                .stream()
                .map(subject -> MapUtils.m("subject", subject, "subjectName", subject.getValue()))
                .collect(Collectors.toList());
        model.addAttribute("subjects", JsonUtils.toJson(subjectMapperList));
        return "teacherv3/teachingresource/index";
    }

    @RequestMapping(value = "book.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage defaultBook() {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("请登录后再访问");
        }
        return newHomeworkContentServiceClient.loadTeachingResourceDefaultBook(teacherDetail);
    }

    @RequestMapping(value = "changebook.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeBook() {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("请登录后再访问");
        }
        String bookId = getRequestString("bookId");
        if (StringUtils.isEmpty(bookId)) {
            return MapMessage.errorMessage("教材id不能为空");
        }
        return newHomeworkContentServiceClient.changeTeachingResourceDefaultBook(teacherDetail, bookId);
    }

    @RequestMapping(value = "types.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadTypeList() {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("请登录后再访问");
        }
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        return newHomeworkContentServiceClient.loadTeachingResourceTypeList(teacherDetail, bookId, unitId);
    }

    @RequestMapping(value = "content.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadContent() {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return MapMessage.errorMessage("请登录后再访问");
        }
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String type = getRequestString("type");
        String sectionId = getRequestString("sectionId");
        String ziKaWord = getRequestString("ziKaWord");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        int clazzLevel = getRequestInt("clazzLevel", 0);
        int termType = getRequestInt("termType", 0);
        String levelReadingsClazzLevel = getRequestString("levelReadingsClazzLevel");
        String topicIds = getRequestString("topicIds");
        String seriesIds = getRequestString("seriesIds");
        int naturalSpellingLevel = getRequestInt("naturalSpellingLevel");
        Map params = new HashMap();
        Pageable page = new PageRequest((pageNum < 1) ? 0 : pageNum - 1, pageSize);
        params.put("page", page);
        params.put("ziKaWord", ziKaWord);
        params.put("clazzLevel", clazzLevel);
        params.put("termType", termType);
        params.put("levelReadingsClazzLevel", levelReadingsClazzLevel);
        params.put("topicIds", topicIds);
        params.put("seriesIds", seriesIds);
        params.put("naturalSpellingLevel", naturalSpellingLevel);
        return newHomeworkContentServiceClient.loadTeachingResourceContent(teacherDetail, bookId, unitId, sectionId, type, params);
    }

    @RequestMapping(value = "naturalspellinglevels.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadNaturalSpellingLevels() {
        String bookId = getRequestString("bookId");
        if (StringUtils.isEmpty(bookId)) {
            return MapMessage.errorMessage("请求参数不完整");
        }
        return newHomeworkContentServiceClient.loadNaturalSpellingLevelsContent(bookId);
    }

    @RequestMapping(value = "previewbasicapp.vpage", method = RequestMethod.GET)
    public String previewBasicapp(Model model) {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return "redirect:/teacher/showtip.vpage";
        }
        model.addAttribute("subject", teacherDetail.getSubject());
        return "teacherv3/teachingresource/previewbasicapp";
    }

    @RequestMapping(value = "preview.vpage", method = RequestMethod.GET)
    public String preview(Model model) {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return "redirect:/teacher/showtip.vpage";
        }
        model.addAttribute("subject", teacherDetail.getSubject());
        return "teacherv3/teachingresource/preview";
    }

    @RequestMapping(value = "wordrecognitionandreadingdetail.vpage", method = RequestMethod.GET)
    public String wordrecognitionandreadingdetail(Model model) {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return "redirect:/teacher/showtip.vpage";
        }
        model.addAttribute("subject", teacherDetail.getSubject());
        return "teacherv3/teachingresource/wordrecognitionandreadingdetail";
    }

    @RequestMapping(value = "previewvideo.vpage", method = RequestMethod.GET)
    public String previewvideo(Model model) {
        TeacherDetail teacherDetail = getSubjectSpecifiedTeacherDetail();
        if (teacherDetail == null) {
            return "redirect:/teacher/showtip.vpage";
        }
        model.addAttribute("subject", teacherDetail.getSubject());
        return "teacherv3/teachingresource/previewvideo";
    }
}