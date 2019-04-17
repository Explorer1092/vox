package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseSubjectLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseSubjectService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseSubject;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>主题
 * @author xuerui.zhang
 * @since 2018/9/19 下午2:29
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/subject/")
public class CrmCourseSubjectController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseSubjectLoader.class)
    private CrmCourseSubjectLoader courseSubjectLoader;

    @ImportService(interfaceClass = CrmCourseSubjectService.class)
    private CrmCourseSubjectService courseSubjectService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Long subjectId = getRequestLong("subjectId");
        Long seriesId = getRequestLong("seriesId");
        Integer seq = getRequestInt("seq");
        String name = getRequestString("name");
        Integer envLevel = getRequestInt("envLevel");
        String createUser = getRequestString("createUser");

        List<CourseSubject> courseSubjects = courseSubjectLoader.loadAllCourseSubject();
        if (CollectionUtils.isNotEmpty(courseSubjects)) {
            if (0L != subjectId) {
                courseSubjects = courseSubjects.stream().filter(e -> e.getId().equals(subjectId)).collect(Collectors.toList());
            }
            if (0L != seriesId) {
                courseSubjects = courseSubjects.stream().filter(e -> e.getSeriesId().equals(seriesId)).collect(Collectors.toList());
            }
            if (seq > 0) {
                courseSubjects = courseSubjects.stream().filter(e -> e.getSeq().equals(seq)).collect(Collectors.toList());
            }
            if (envLevel > 0) {
                courseSubjects = courseSubjects.stream().filter(e -> e.getEnvLevel().equals(envLevel)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(name)) {
                courseSubjects = courseSubjects.stream().filter(e -> e.getName().contains(name.trim())).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                courseSubjects = courseSubjects.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }

        Page<CourseSubject> resultList;
        if (CollectionUtils.isEmpty(courseSubjects)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            courseSubjects = courseSubjects.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(courseSubjects, pageRequest);
        }

        if(0L != subjectId) model.addAttribute("subjectId", subjectId);
        if(0L != seriesId) model.addAttribute("seriesId", seriesId);
        if(seq > 0) model.addAttribute("seq", seq);

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("name", name);
        model.addAttribute("envLevel", envLevel);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/subject/index";
    }

    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        Long subjectId = getRequestLong("subjectId");
        if (0L != subjectId) {
            CourseSubject courseSubject = courseSubjectLoader.loadCourseSubjectById(subjectId);
            if (courseSubject != null) {
                model.addAttribute("content", courseSubject);
            }
        } else {
            model.addAttribute("content", new CourseSubject());
        }
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        model.addAttribute("createUser", getCurrentAdminUser().getAdminUserName());
        return "opmanager/studyTogether/subject/details";
    }

    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        Long subjectId = getRequestLong("subjectId");
        Long seriesId = getRequestLong("seriesId");
        String name = getRequestString("name");
        Integer seq = getRequestInt("seq");
        Integer envLevel = getRequestInt("envLevel");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");

        try {
            CourseSubject bean;
            CourseSubject newObj;
            CourseSubject oldObj = new CourseSubject();

            if (subjectId <= 0L) {
                bean = new CourseSubject();
                try {
                    Long currentId = AtomicLockManager.getInstance().wrapAtomic(courseSubjectLoader)
                            .keyPrefix("SUBJECT_INCR_ID").keys(seriesId, seq).proxy().loadMaxId();
                    bean.setId(currentId + 1);
                } catch (Exception e) {
                    logger.error("lock error {}", e.getMessage(), e);
                    return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
                }
            } else {
                bean = courseSubjectLoader.loadCourseSubjectById(subjectId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                bean.setId(subjectId);
                BeanUtils.copyProperties(oldObj, bean);
            }

            bean.setName(name.trim());
            bean.setSeq(seq);
            bean.setSeriesId(seriesId);
            bean.setEnvLevel(envLevel);
            bean.setRemark(remark);
            String userName = getCurrentAdminUser().getAdminUserName();

            if (subjectId <= 0) {
                bean.setCreateUser(createUser);
                courseSubjectService.save(bean);
                studyCourseBlackWidowServiceClient.justAddChangeLog("课程-主题", userName,
                        ChangeLogType.CourseSubject, bean.getId().toString(), "新增主题信息");
            } else {
                newObj = courseSubjectService.save(bean);
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseSubject, newObj.getId().toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        Long subjectId = getRequestLong("subjectId");
        if (0L == subjectId)  {
            return CourseConstMapper.SUBJECT_REDIRECT;
        }
        CourseSubject courseSubject = courseSubjectLoader.loadCourseSubjectById(subjectId);
        if (courseSubject == null) {
            return CourseConstMapper.SUBJECT_REDIRECT;
        }
        model.addAttribute("content", courseSubject);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        return "opmanager/studyTogether/subject/info";
    }

    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        Long subjectId = getRequestLong("subjectId");
        if (0L == subjectId)  {
            return CourseConstMapper.SUBJECT_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(subjectId.toString(), ChangeLogType.CourseSubject, pageRequest);

//        Page<ContentChangeLog> resultList;
//        if (CollectionUtils.isEmpty(changeLogList)) {
//            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
//        } else {
//            resultList = PageableUtils.listToPage(changeLogList, pageRequest);
//        }
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("logList", resultList.getContent());
        model.addAttribute("subjectId", subjectId);
        return "opmanager/studyTogether/subject/logs";
    }

    @ResponseBody
    @RequestMapping(value = "checkId.vpage", method = RequestMethod.POST)
    public MapMessage checkSubjectId() {
        Long subjectId = getRequestLong("subjectId");
        CourseSubject courseSubject = courseSubjectLoader.loadCourseSubjectById(subjectId);
        if (null == courseSubject) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("当前ID已经存在");
        }
    }

    @ResponseBody
    @RequestMapping(value = "check_seq.vpage", method = RequestMethod.POST)
    public MapMessage checkSeq() {
        Long seriesId = getRequestLong("seriesId");
        Integer seq = getRequestInt("seq");
        List<CourseSubject> courseSubjects = courseSubjectLoader.loadAllCourseSubject();
        List<Integer> seqs = courseSubjects
                .stream().filter(e -> e.getSeriesId().equals(seriesId)).map(CourseSubject::getSeq).collect(Collectors.toList());
        if (!seqs.contains(seq)) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }

}
