package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseTargetType;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCourse;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCourseTarget;
import com.voxlearning.utopia.service.mizar.consumer.loader.MicroCourseLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/9/21.
 */
@Controller
@RequestMapping("/mizar/course")
public class CrmMizarCourseController extends CrmMizarAbstractController {

    @Inject private MizarLoaderClient mizarLoaderClient;
    @Inject private MicroCourseLoaderClient microCourseLoaderClient;

    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    // 列表页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        // 获取全部课程
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 10);

        String title = getRequestString("title");
        String category = getRequestString("category");
        String status = getRequestString("status");
        String escapeStr = "";
        if (StringUtils.isNotBlank(title)) {
            escapeStr = StringRegexUtils.escapeExprSpecialWord(title);
        }
        Page<MizarCourse> coursePage = mizarLoaderClient.loadPageCourseByParams(pageable, escapeStr, status, category);
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("currentPage", coursePage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", coursePage.getTotalPages());
        model.addAttribute("hasPrev", coursePage.hasPrevious());
        model.addAttribute("hasNext", coursePage.hasNext());
        model.addAttribute("title", title);
        model.addAttribute("category", category);
        model.addAttribute("categorys", MizarCourseCategory.values());
        model.addAttribute("mode", getRequestString("mode"));
        model.addAttribute("status", status);
        return "mizar/course/index";
    }

    // 添加编辑跳转
    @RequestMapping(value = "coursedetail.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        String courseId = getRequestString("courseId");
        model.addAttribute("courseId", courseId);
        model.addAttribute("status", MizarCourse.Status.values());
        model.addAttribute("courseCategory", MizarCourseCategory.values());
        if (StringUtils.isNotBlank(courseId)) {
            MizarCourse course = mizarLoaderClient.loadMizarCourseById(courseId);
            if (course != null) {
                model.addAttribute("course", course);
                // 微课堂特殊处理
                if (MizarCourseCategory.MICRO_COURSE_OPENING.name().equals(course.getCategory())
                        || MizarCourseCategory.MICRO_COURSE_NORMAL.name().equals(course.getCategory())) {
                    return "mizar/course/microcourse";
                }
            }
        }
        // 微课堂特殊处理
        if ("mc".equals(getRequestString("mode"))) {
            return "mizar/course/microcourse";
        }
        return "mizar/course/coursedetail";
    }

    // 添加编辑 post
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveCourse() {
        // 获取参数
        String courseId = getRequestString("courseId");
        String title = getRequestString("title");
        String subTitle = getRequestString("subTitle");
        String description = getRequestString("description");
        String redirectUrl = getRequestString("redirectUrl");
        String keynoteSpeaker = getRequestString("keynoteSpeaker");
        String tags = getRequestString("tags");
        String clazzLevels = getRequestString("clazzLevels");
        String status = getRequestString("status");
        String category = getRequestString("category");
        Boolean indexShow = getRequestBool("indexShow");
        Boolean top = getRequestBool("top");
        Long activityId = getRequestLong("activityId");
        String classTime = getRequestString("classTime");
        String price = getRequestString("price");
        Integer priority = getRequestInt("priority");
        if (StringUtils.isBlank(redirectUrl)) {
            return MapMessage.errorMessage("课程跳转URL不能为空");
        }
        try {
            MizarCourse course = null;
            if (StringUtils.isNotBlank(courseId)) {
                course = mizarLoaderClient.loadMizarCourseById(courseId);
            }
            if (course == null) {
                course = new MizarCourse();
            }
            // 微课堂特殊处理
            if (MizarCourseCategory.MICRO_COURSE_OPENING.name().equals(course.getCategory())
                    || MizarCourseCategory.MICRO_COURSE_NORMAL.name().equals(course.getCategory())) {
                if (microCourseLoaderClient.getCourseLoader().loadCoursePeriod(title) == null) {
                    return MapMessage.errorMessage("无效的课时ID：" + title);
                }
                course.setRedirectUrl(redirectUrl + title);
                course.setBackground(getRequestString("background"));

                // 检验是否有相同的课时
                List<MizarCourse> content = mizarLoaderClient.loadPageCourseByParams(new PageRequest(1, 1), course.getTitle(), null, category)
                        .getContent();
                if (CollectionUtils.isNotEmpty(content)) {
                    return MapMessage.errorMessage("重复的课时ID");
                }
//                // 上线之前校验是否有图片
//                if (MizarCourse.Status.ONLINE.name().equals(status) && StringUtils.isBlank(course.getSpeakerAvatar())) {
//                    return MapMessage.errorMessage("未上传图片不允许置上线状态");
//                }
            }
            course.setTitle(title);
            course.setSubTitle(subTitle);
            course.setDescription(description);
            course.setRedirectUrl(redirectUrl);
            course.setKeynoteSpeaker(keynoteSpeaker);
            course.setTags(splitString(tags));
            course.setClazzLevels(splitString(clazzLevels));
            course.setStatus(MizarCourse.Status.valueOf(status));
            course.setCategory(category);
            course.setIndexShow(indexShow);
            course.setTop(top);
            course.setActivityId(activityId);
            course.setClassTime(classTime);
            course.setPrice(price);
            course.setPriority(priority);
            return mizarServiceClient.saveMizarCourse(course);
        } catch (Exception ex) {
            logger.error("Save Mizar course failed.", ex);
            return MapMessage.errorMessage("保存课程失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadPhoto() {
        String courseId = getRequestString("courseId");
        String field = getRequestString("field");
        try {
            MizarCourse course = mizarLoaderClient.loadMizarCourseById(courseId);
            if (course == null) {
                return MapMessage.errorMessage("无效的信息");
            }
            // 上传文件
            String fileName = uploadPhoto("file");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("文件上传失败");
            }
            if (StringUtils.isNotBlank(field) && field.equals("avatar")) {
                course.setSpeakerAvatar(fileName);
            } else {
                course.setBackground(fileName);
            }
            return mizarServiceClient.saveMizarCourse(course);
        } catch (Exception ex) {
            logger.error("Upload Mizar course photo failed, courseId={}", courseId, ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    @RequestMapping(value = "deletephoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deletePhoto() {
        String courseId = getRequestString("courseId");
        String fileName = getRequestString("file");
        String field = getRequestString("field");
        if (StringUtils.isBlank(fileName)) {
            return MapMessage.errorMessage("无效的图片信息");
        }
        try {
            MizarCourse course = mizarLoaderClient.loadMizarCourseById(courseId);
            if (course == null) {
                return MapMessage.errorMessage("无效的信息");
            }
            if (StringUtils.isNotBlank(field) && field.equals("avatar")) {
                course.setSpeakerAvatar("");
            } else {
                course.setBackground("");
            }
            MapMessage msg = mizarServiceClient.saveMizarCourse(course);
            if (msg.isSuccess()) {
                deletePhoto(fileName);
            }
            return msg;
        } catch (Exception ex) {
            logger.error("Delete Mizar course background failed, courseId={}, file={}", courseId, fileName, ex);
            return MapMessage.errorMessage("图片删除失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "deletecourse.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteCourse() {
        String courseId = getRequestString("courseId");
        try {
            MizarCourse course = mizarLoaderClient.loadMizarCourseById(courseId);
            if (course == null) {
                return MapMessage.errorMessage("无效的信息");
            }
            return mizarServiceClient.removeMizarCourse(courseId);
        } catch (Exception ex) {
            logger.error("Upload Mizar course photo failed, courseId={}", courseId, ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    // 配置投放策略
    @RequestMapping(value = "courseconfig.vpage", method = RequestMethod.GET)
    public String adTarget(Model model) {
        String courseId = getRequestString("courseId");
        MizarCourse course = mizarLoaderClient.loadMizarCourseById(courseId);
        if (course == null) {
            model.addAttribute("error", "无效的ID信息");
            return "mizar/course/courseconfig";
        }
        model.addAttribute("course", course);
        generateDetailTargets(courseId, model);
        return "mizar/course/courseconfig";
    }

    @RequestMapping(value = "saveregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetRegion() {
        String courseId = getRequestString("courseId");
        Integer type = getRequestInt("type");
        String regions = getRequestString("regionList");
        if (MizarCourseTargetType.of(type) != MizarCourseTargetType.TARGET_TYPE_REGION) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(regions)) {
            return MapMessage.errorMessage("选择地区不能为空！");
        }
        try {

            List<String> regionList = Arrays.asList(regions.split(","));
            return mizarServiceClient.saveCourseTargets(courseId, type, regionList, false);
        } catch (Exception ex) {
            logger.error("保存投放地区失败! id={},type={}, ex={}", courseId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放地区失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "saveids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTargetIds() {
        String courseId = getRequestString("courseId");
        Integer type = getRequestInt("type");
        String targetIds = getRequestString("targetIds");
        Boolean append = getRequestBool("append");
        MizarCourseTargetType targetType = MizarCourseTargetType.of(type);
        if (targetType != MizarCourseTargetType.TARGET_TYPE_SCHOOL && targetType != MizarCourseTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        if (StringUtils.isBlank(targetIds)) {
            return MapMessage.errorMessage("请输入有效的内容！");
        }
        try {
            // 没有校验用户输入是否符合规范
            List<String> targetList = Arrays.stream(targetIds.split(DEFAULT_LINE_SEPARATOR))
                    .map(t -> t.replaceAll("\\s", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            return mizarServiceClient.saveCourseTargets(courseId, type, targetList, append);
        } catch (Exception ex) {
            logger.error("保存投放用户失败:id={},type={},ex={}", courseId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("保存投放用户失败:" + ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "cleartargets.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearTargets() {
        String courseId = getRequestString("courseId");
        Integer type = getRequestInt("type");
        MizarCourseTargetType targetType = MizarCourseTargetType.of(type);
        if (targetType != MizarCourseTargetType.TARGET_TYPE_SCHOOL
                && targetType != MizarCourseTargetType.TARGET_TYPE_REGION
                && targetType != MizarCourseTargetType.TARGET_TYPE_ALL) {
            return MapMessage.errorMessage("无效的参数！");
        }
        try {
            return mizarServiceClient.clearCourseTargets(courseId, type);
        } catch (Exception ex) {
            logger.error("清空投放对象失败:id={},type={},ex={}", courseId, type, ex.getMessage(), ex);
            return MapMessage.errorMessage("清空投放对象失败:" + ex.getMessage(), ex);
        }
    }

    private void generateDetailTargets(String courseId, Model model) {
        Map<Integer, List<MizarCourseTarget>> targetMap = mizarLoaderClient.loadCourseTargetsGroupByType(courseId);
        int type = 3;
        List<Integer> regions = new ArrayList<>();
        String targetSchool = null;
        if (targetMap.get(MizarCourseTargetType.TARGET_TYPE_REGION.getType()) != null) {
            type = MizarCourseTargetType.TARGET_TYPE_REGION.getType();
            regions = targetMap.get(type).stream().map(ad -> SafeConverter.toInt(ad.getTargetStr())).collect(Collectors.toList());
        }
        if (targetMap.get(MizarCourseTargetType.TARGET_TYPE_SCHOOL.getType()) != null) {
            type = MizarCourseTargetType.TARGET_TYPE_SCHOOL.getType();
            List<String> schools = targetMap.get(type).stream().map(MizarCourseTarget::getTargetStr).collect(Collectors.toList());
            int size = schools.size() > 2000 ? 2000 : schools.size();
            targetSchool = StringUtils.join(schools.subList(0, size), DEFAULT_LINE_SEPARATOR);
            model.addAttribute("schoolSize", schools.size());
        }
        List<KeyValuePair<Integer, String>> targetTypes = MizarCourseTargetType.toKeyValuePairs();
        for (KeyValuePair<Integer, String> target : targetTypes) {
            model.addAttribute("has_" + target.getKey(), targetMap.containsKey(target.getKey()));
        }
        model.addAttribute("targetType", type);
        model.addAttribute("targetRegion", JsonUtils.toJson(crmRegionService.buildRegionTree(regions)));
        model.addAttribute("targetSchool", targetSchool);
    }

}
