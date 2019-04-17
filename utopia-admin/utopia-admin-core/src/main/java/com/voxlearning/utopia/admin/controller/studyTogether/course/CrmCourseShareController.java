package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseShareLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseShareService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseShare;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.galaxy.service.studycourse.constant.CourseShareType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <P>分享
 * @author xuerui.zhang
 * @since 2018/9/12 下午8:00
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/share/")
public class CrmCourseShareController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseShareService.class)
    private CrmCourseShareService courseShareService;

    @ImportService(interfaceClass = CrmCourseShareLoader.class)
    private CrmCourseShareLoader courseShareLoader;

    /**
     * 课程-分享列表
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String shareId = getRequestString("shareId");
        Long skuId = getRequestLong("skuId");
        String type = getRequestString("type");
        String createUser = getRequestString("createUser");

        List<CourseShare> shareList = courseShareLoader.loadAllCourseShare();
        if (CollectionUtils.isNotEmpty(shareList)) {
            if (StringUtils.isNotBlank(shareId)) {
                shareList = shareList.stream().filter(e -> e.getId().equals(shareId.trim())).collect(Collectors.toList());
            }
            if (0L != skuId) {
                shareList = shareList.stream().filter(e -> e.getSkuId().equals(skuId)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(type) && !type.equals("all")) {
                shareList = shareList.stream().filter(e -> e.getType().equals(type.trim())).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                shareList = shareList.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }
        Page<CourseShare> resultList;
        if (CollectionUtils.isEmpty(shareList)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            shareList = shareList.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(shareList, pageRequest);
        }

        if(0L != skuId) model.addAttribute("skuId", skuId);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("shareId", shareId);
        model.addAttribute("type", type);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/share/index";
    }

    /**
     * 分享修改/添加页面
     */
    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        String shareId = getRequestString("shareId");
        String username = null;
        if (StringUtils.isNotBlank(shareId)) {
            CourseShare courseShare = courseShareLoader.loadCourseShareById(shareId);
            if (courseShare != null) {
                username = courseShare.getCreateUser();
                model.addAttribute("content", courseShare);
            }
        } else {
            model.addAttribute("content", new CourseShare());
        }
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        model.addAttribute("shareId", shareId);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        return "opmanager/studyTogether/share/details";
    }

    /**
     * 添加或修改分享
     */
    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        String shareId = getRequestString("shareId");
        Long skuId = getRequestLong("skuId");
        String type = getRequestString("type");
        String title = getRequestString("title");
        String iconUrl = getRequestString("iconUrl");
        String content = getRequestString("content");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");
        String ebookPicUrl = getRequestString("ebookPicUrl");

        try {
            CourseShare bean;
            CourseShare newObj;
            CourseShare oldObj = new CourseShare();
            if (StringUtils.isBlank(shareId)) {
                bean = new CourseShare();
            } else {
                bean = courseShareLoader.loadCourseShareById(shareId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                BeanUtils.copyProperties(oldObj, bean);
            }
            bean.setSkuId(skuId);
            bean.setType(type);
            bean.setTitle(title.trim());
            bean.setIconUrl(iconUrl.trim());
            bean.setContent(content.trim());
            bean.setRemark(remark);
            if (type.equals("default")) {
                bean.setEbookPicUrl(ebookPicUrl);
            }
            String userName = getCurrentAdminUser().getAdminUserName();

            if (StringUtils.isBlank(shareId)) {
                bean.setCreateUser(createUser);
                for (CourseShareType shareType : CourseShareType.values()) {
                    if (type.equals(shareType.getValue())) {
                        bean.setId(CourseShare.generateId(skuId, shareType));
                    }
                }
                CourseShare save = courseShareService.save(bean);
                studyCourseBlackWidowServiceClient.justAddChangeLog("课程-分享", userName,
                        ChangeLogType.CourseShare, save.getId(), "新增分享信息");
            } else {
                bean.setId(shareId);
                newObj = courseShareService.save(bean);
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseShare, newObj.getId());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    /**
     * 分享详情
     */
    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        String shareId = getRequestString("shareId");
        if (StringUtils.isBlank(shareId)) {
            return CourseConstMapper.SHARE_REDIRECT;
        }
        CourseShare courseShare = courseShareLoader.loadCourseShareById(shareId);
        if (courseShare == null) {
            return CourseConstMapper.SHARE_REDIRECT;
        }
        model.addAttribute("content", courseShare);
        model.addAttribute("shareId", shareId);
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        return "opmanager/studyTogether/share/info";
    }

     /**
     * 分享-日志信息
     * @since 日志模板类型：CourseShare
     */
    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        String shareId = getRequestString("shareId");
        if (StringUtils.isBlank(shareId))  {
            return CourseConstMapper.SHARE_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(shareId, ChangeLogType.CourseShare, pageRequest);

        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("logList", resultList.getContent());
        model.addAttribute("shareId", shareId);
        return "opmanager/studyTogether/share/logs";
    }

    @ResponseBody
    @RequestMapping(value = "check_id.vpage", method = RequestMethod.GET)
    public MapMessage checkId() {
        Long skuId = getRequestLong("skuId");
        String type = getRequestString("type");
        if (0L == skuId && StringUtils.isBlank(type)) {
            return MapMessage.errorMessage();
        }
        String id = "";
        for (CourseShareType shareType : CourseShareType.values()) {
            if (type.equals(shareType.getValue())) {
                id = CourseShare.generateId(skuId, shareType);
            }
        }
        CourseShare courseShare = courseShareLoader.loadCourseShareById(id);
        return courseShare == null ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

}
