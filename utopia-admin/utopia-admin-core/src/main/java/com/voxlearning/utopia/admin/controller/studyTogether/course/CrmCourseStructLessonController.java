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
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructLessonLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructLessonService;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSkuLoader;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructLesson;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSku;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>课节
 * @author xuerui.zhang
 * @since 2018/9/6 下午6:09
 */
@Slf4j
@Controller
@RequestMapping(value = "opmanager/studytogether/clazzfestival/")
public class CrmCourseStructLessonController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseStructLessonService.class)
    private CrmCourseStructLessonService courseStructLessonService;

    @ImportService(interfaceClass = CrmCourseStructLessonLoader.class)
    private CrmCourseStructLessonLoader courseStructLessonLoader;

    @ImportService(interfaceClass = CrmCourseStructSkuLoader.class)
    private CrmCourseStructSkuLoader crmCourseStructSkuLoader;


    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);

        Long clazzFestivalId = getRequestLong("clazzFestivalId");
        String clazzFestivalName = getRequestString("clazzFestivalName");
        Long chapterId = getRequestLong("chapterId");
        Long skuId = getRequestLong("skuId");
        Integer type = getRequestInt("type");
        Integer envLevel = getRequestInt("envLevel");
        String createUser = getRequestString("createUser");

        List<CourseStructLesson> ccfList = courseStructLessonLoader.loadAllCourseStructLesson();
        if (CollectionUtils.isNotEmpty(ccfList)) {
            if (0L != clazzFestivalId) {
                ccfList = ccfList.stream().filter(e -> e.getId().equals(clazzFestivalId)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(clazzFestivalName)) {
                ccfList = ccfList.stream()
                        .filter(e -> (StringUtils.isNotBlank(e.getName()) && e.getName().contains(clazzFestivalName.trim())))
                        .collect(Collectors.toList());
            }
            if (0L != chapterId) {
                ccfList = ccfList.stream().filter(e -> Objects.equals(e.getChapterId(), chapterId)).collect(Collectors.toList());
            }
            if (0L != skuId) {
                ccfList = ccfList.stream().map(this::setSkuId).filter(e -> Objects.equals(e.getSkuId(), skuId)).collect(Collectors.toList());
            }
            if (0 != type && -1 != type) {
                ccfList = ccfList.stream().filter(e -> Objects.equals(e.getType(), type)).collect(Collectors.toList());
            }
            if (0 != envLevel && -1 != envLevel) {
                ccfList = ccfList.stream().filter(e -> Objects.equals(e.getEnvLevel(), envLevel)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                ccfList = ccfList.stream()
                        .filter(e -> StringUtils.isNotBlank(e.getCreateUser()) && e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }
        Page<CourseStructLesson> resultList;
        if (CollectionUtils.isEmpty(ccfList)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            ccfList = ccfList.stream().map(this::setSkuId).sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(ccfList, pageRequest);
        }
        if(0L != clazzFestivalId) model.addAttribute("clazzFestivalId", clazzFestivalId);
        if(0L != chapterId)  model.addAttribute("chapterId", chapterId);
        if(0L != skuId)  model.addAttribute("skuId", skuId);

        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("clazzFestivalName", clazzFestivalName);
        model.addAttribute("type", type);
        model.addAttribute("envLevel", envLevel);
        model.addAttribute("createUser", createUser);

        return "/opmanager/studyTogether/clazzfestival/index";
    }

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;
    private CourseStructLesson setSkuId(CourseStructLesson lesson){
        CourseStructLesson lessonFromBuffer = studyCourseStructLoaderClient.loadCourseStructLessonById(lesson.getId());
        if (lessonFromBuffer != null){
            lesson.setSkuId(lessonFromBuffer.getSkuId());
        }
        return lesson;
    }

    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        Long clazzFestivalId = getRequestLong("clazzFestivalId", 0L);
        String username = null;
        if (0L != clazzFestivalId) {
            CourseStructLesson courseClazzFestival = courseStructLessonLoader.loadCourseStructLesson(clazzFestivalId);
            if (courseClazzFestival != null) {
                courseClazzFestival = setSkuId(courseClazzFestival);
                username = courseClazzFestival.getCreateUser();
                model.addAttribute("content", courseClazzFestival);
            }
        } else {
            model.addAttribute("content", new CourseStructLesson());
        }

        List<CourseStructSku> skuList =  crmCourseStructSkuLoader.loadAllCourseStructSku();
        List<Long> skuIds = skuList.stream().map(CourseStructSku::getId).collect(Collectors.toList());

        model.addAttribute("clazzFestivalId", clazzFestivalId);
        model.addAttribute("types", CourseConstMapper.CLAZZ_TYPE);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        model.addAttribute("skuIds", skuIds);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        return "opmanager/studyTogether/clazzfestival/details";
    }

    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        Long clazzFestivalId = getRequestLong("clazzFestivalId");
        Long chapterId = getRequestLong("chapterId");
        Long skuId = getRequestLong("skuId");
        Integer type = getRequestInt("type");
        String name = getRequestString("name");
        Integer seq = getRequestInt("seq");
        String openDate = getRequestString("openDate");
        Integer signScore = getRequestInt("signScore");
        Integer signExtraScore = getRequestInt("signExtraScore");
        Long templateId = getRequestLong("templateId");
        Integer envLevel = getRequestInt("envLevel");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");

        try {
            CourseStructLesson bean;
            CourseStructLesson newObj;
            CourseStructLesson oldObj = new CourseStructLesson();

            if (0L == clazzFestivalId) {
                bean = new CourseStructLesson();
                try {
                    Long currentId = AtomicLockManager.getInstance().wrapAtomic(courseStructLessonLoader)
                            .keyPrefix("StructLesson_INCR_ID").keys(chapterId, type).proxy().loadMaxId();
                    bean.setId(currentId + 1);
                } catch (Exception e) {
                    logger.error("lock error {}", e.getMessage(), e);
                    return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
                }
            } else {
                bean = courseStructLessonLoader.loadCourseStructLesson(clazzFestivalId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                bean.setId(clazzFestivalId);
                BeanUtils.copyProperties(oldObj, bean);
            }
            bean.setSkuId(skuId);
            bean.setChapterId(chapterId);
            bean.setType(type);
            bean.setName(name);
            bean.setSeq(seq);
            if (StringUtils.isNotBlank(openDate)) {
                bean.setOpenDate(CrmCourseCommonController.safeConvertDate(openDate));
            } else {
                bean.setOpenDate(null);
            }
            bean.setSignScore(signScore);
            bean.setSignExtraScore(signExtraScore);
            bean.setTemplateId(templateId);
            bean.setEnvLevel(envLevel);
            bean.setRemark(remark);
            String userName = getCurrentAdminUser().getAdminUserName();
            if (0L == clazzFestivalId) {
                bean.setCreateUser(createUser);
                courseStructLessonService.save(bean);
                studyCourseBlackWidowServiceClient.justAddChangeLog("课节", userName,
                        ChangeLogType.CourseClazzFestive, bean.getId().toString(), "新增课节信息");
            } else {
                newObj = courseStructLessonService.save(bean);
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseClazzFestive, bean.getId().toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        Long clazzFestivalId = getRequestLong("clazzFestivalId", 0L);
        if (0L == clazzFestivalId) {
            return CourseConstMapper.CLAZZ_REDIRECT;
        }
        CourseStructLesson courseStructLesson = courseStructLessonLoader.loadCourseStructLesson(clazzFestivalId);
        if (courseStructLesson == null) {
            return CourseConstMapper.CLAZZ_REDIRECT;
        }
        courseStructLesson = setSkuId(courseStructLesson);
        model.addAttribute("content", courseStructLesson);
        return "opmanager/studyTogether/clazzfestival/info";
    }

    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        Long clazzFestivalId = getRequestLong("clazzFestivalId", 0L);
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(clazzFestivalId.toString(), ChangeLogType.CourseClazzFestive, pageRequest);

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
        model.addAttribute("clazzFestivalId", clazzFestivalId);
        return "opmanager/studyTogether/clazzfestival/logs";
    }

    @ResponseBody
    @RequestMapping(value = "check_seq.vpage", method = RequestMethod.POST)
    public MapMessage checkSeq() {
        Long chapterId = getRequestLong("chapterId");
        Integer seq = getRequestInt("seq");
        List<CourseStructLesson> courseStructLessons = courseStructLessonLoader.loadAllCourseStructLesson();
        List<Integer> seqs = courseStructLessons
                .stream().filter(e -> e.getChapterId().equals(chapterId)).map(CourseStructLesson::getSeq).collect(Collectors.toList());
        if (!seqs.contains(seq)) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage();
        }
    }

}
