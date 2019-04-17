package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSpuLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSpuService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSpu;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>SPU
 * @author xuerui.zhang
 * @since 2018/9/19 下午2:28
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/spu/")
public class CrmCourseStructSpuController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseStructSpuLoader.class)
    private CrmCourseStructSpuLoader courseStructSpuLoader;

    @ImportService(interfaceClass = CrmCourseStructSpuService.class)
    private CrmCourseStructSpuService courseStructSpuService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);

        Long spuId = getRequestLong("spuId");
        Long seriesId = getRequestLong("seriesId");
        int type = getRequestInt("type");
        int hasReview = getRequestInt("hasReview");//1:true 2:false
        int envLevel = getRequestInt("envLevel");
        String name = getRequestString("name");
        String createUser = getRequestString("createUser");

        List<CourseStructSpu> spuList = courseStructSpuLoader.loadAllCourseStructSpu();
        if (CollectionUtils.isNotEmpty(spuList)) {
            if (0L != spuId) {
                spuList = spuList.stream().filter(e -> e.getId().equals(spuId)).collect(Collectors.toList());
            }
            if (0L != seriesId) {
                spuList = spuList.stream().filter(e -> e.getSeriesId().equals(seriesId)).collect(Collectors.toList());
            }
            if (type > 0) {
                spuList = spuList.stream().filter(e -> e.getType().equals(type)).collect(Collectors.toList());
            }
            if (envLevel > 0) {
                spuList = spuList.stream().filter(e -> e.getEnvLevel().equals(envLevel)).collect(Collectors.toList());
            }
            if (hasReview > 0) {
                spuList = spuList.stream().filter(e -> e.getHasReview().equals(1 == hasReview)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(name)) {
                spuList = spuList.stream().filter(e -> e.getName().contains(name.trim())).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                spuList = spuList.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }

        Page<CourseStructSpu> resultList;
        if (CollectionUtils.isEmpty(spuList)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            spuList = spuList.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(spuList, pageRequest);
        }

        if(0L != spuId) model.addAttribute("skuId", spuId);
        if(0L != seriesId) model.addAttribute("seriesId", seriesId);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("type", type);
        model.addAttribute("name", name);
        model.addAttribute("hasReview", hasReview);
        model.addAttribute("envLevel", envLevel);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/spu/index";
    }

    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        Long spuId = getRequestLong("spuId");
        String username = null;
        if (spuId > 0L)  {
            CourseStructSpu courseStructSpu = courseStructSpuLoader.loadCourseStructSpuById(spuId);
            if (courseStructSpu != null) {
                int min,max;
                List<Integer> grades = courseStructSpu.getGrades();
                if (grades == null){
                    min = max = 0;
                }else {
                    min = grades.get(0);
                    max = grades.get(grades.size() - 1);
                }
                model.addAttribute("content", courseStructSpu);
                model.addAttribute("min", min);
                model.addAttribute("max", max);
                username = courseStructSpu.getCreateUser();
            }
        } else {
            model.addAttribute("content", new CourseStructSpu());
        }
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        model.addAttribute("spuId", spuId);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        return "opmanager/studyTogether/spu/details";
    }

    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        Long spuId = getRequestLong("spuId");
        Long seriesId = getRequestLong("seriesId");
        String name = getRequestString("name");
        String subTitle = getRequestString("subTitle");
        int type = getRequestInt("type");
        int min = getRequestInt("min");
        int max = getRequestInt("max");
        int days = getRequestInt("days");
        String icon = getRequestString("icon");
        String courseBigPic = getRequestString("courseBigPic");
        String courseLittlePic = getRequestString("courseLittlePic");
        String desc = getRequestString("desc");
        int envLevel = getRequestInt("envLevel");
        String tags = getRequestString("tags");
        boolean hasReview = getRequestBool("hasReview");
        int knowledgeCount = getRequestInt("knowledgeCount");
        Long experienceLessonId = getRequestLong("experienceLessonId");
        boolean dynamicAdapt = getRequestBool("dynamicAdapt");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");

        /* 故宫项目新增字段*/
        String headIcon = getRequestString("headIcon");
        int seq = getRequestInt("seq");

        boolean isLessonSet = getRequestBool("isLessonSet");

        try {
            CourseStructSpu bean;
            CourseStructSpu newObj;
            CourseStructSpu oldObj = new CourseStructSpu();
            if (spuId <= 0L)  {
                bean = new CourseStructSpu();
                try {
                    Long currentId = AtomicLockManager.getInstance().wrapAtomic(courseStructSpuLoader)
                            .keyPrefix("SPU_INCR_ID").keys(seriesId).proxy().loadMaxId();
                    bean.setId(currentId + 1);
                } catch (Exception e) {
                    logger.error("lock error {}", e.getMessage(), e);
                    return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
                }
            } else {
                bean = courseStructSpuLoader.loadCourseStructSpuById(spuId);
                if (bean == null) return MapMessage.errorMessage("数据不存在");
                BeanUtils.copyProperties(oldObj, bean);
                bean.setId(spuId);
            }
            bean.setSeriesId(seriesId);
            bean.setName(name.trim());
            bean.setSubtitle(subTitle.trim());
            bean.setType(type);
            bean.setDays(days);
            bean.setIcon(icon.trim());
            bean.setDesc(desc.trim());
            bean.setEnvLevel(envLevel);
            bean.setHasReview(hasReview);
            bean.setKnowledgeCount(knowledgeCount);
            bean.setDynamicAdapt(dynamicAdapt);
            bean.setRemark(remark);
            bean.setCourseBigPic(courseBigPic);
            bean.setCourseLittlePic(courseLittlePic);
            bean.setIsLessonSet(isLessonSet);
            bean.setExperienceLessonId(experienceLessonId);

            String userName = getCurrentAdminUser().getAdminUserName();
            if (min == 0){
                min = 1;
            }
            if (max == 0){
                max = 6;
            }
            if (min > max){
                return MapMessage.errorMessage("年级配置错误！");
            }
            List<Integer> grades = new ArrayList<>();
            for (int i = min ; i<=max ;i++){
                grades.add(i);
            }
            bean.setGrades(grades);
            if (StringUtils.isNotBlank(tags)) {
                String[] split = tags.trim().split(",");
                bean.setTags(Arrays.asList(split));
            } else {
                return MapMessage.errorMessage("能力细分描述为空");
            }
            if (spuId <= 0L) bean.setCreateUser(createUser);

            if (seq >= 0) {
                bean.setSeq(seq);
            }
            bean.setHeadIcon(headIcon.trim());

            newObj = courseStructSpuService.save(bean);
            if (spuId <= 0L)  {
                studyCourseBlackWidowServiceClient.justAddChangeLog("SPU", userName,
                        ChangeLogType.CourseSpu, newObj.getId().toString(), "新增SPU信息");
            } else {
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseSpu, newObj.getId().toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        Long spuId = getRequestLong("spuId");
        if (0L == spuId)  {
            return CourseConstMapper.SPU_REDIRECT;
        }
        CourseStructSpu courseStructSpu = courseStructSpuLoader.loadCourseStructSpuById(spuId);
        if (courseStructSpu == null) {
            return CourseConstMapper.SPU_REDIRECT;
        }
        int min,max;
        List<Integer> grades = courseStructSpu.getGrades();
        if (grades == null){
            min = max = 0;
        }else {
            min = grades.get(0);
            max = grades.get(grades.size() - 1);
        }
        model.addAttribute("min", min);
        model.addAttribute("max", max);
        model.addAttribute("spuId", spuId);
        model.addAttribute("content", courseStructSpu);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        model.addAttribute("types", CourseConstMapper.SPU_TYPE);
        model.addAttribute("reviews", CourseConstMapper.REVIEW_TYPE);
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        return "opmanager/studyTogether/spu/info";
    }

    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        Long spuId = getRequestLong("spuId");
        if (0L == spuId)  {
            return CourseConstMapper.SPU_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(spuId.toString(), ChangeLogType.CourseSpu, pageRequest);

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
        model.addAttribute("spuId", spuId);
        return "opmanager/studyTogether/spu/logs";
    }

}
