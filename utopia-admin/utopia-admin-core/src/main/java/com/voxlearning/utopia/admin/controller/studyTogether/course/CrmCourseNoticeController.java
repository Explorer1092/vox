package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseNoticeLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseNoticeService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseNotice;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <P>通知
 * @author xuerui.zhang
 * @since 2018/9/12 下午8:00
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/notice/")
public class CrmCourseNoticeController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseNoticeService.class)
    private CrmCourseNoticeService courseNoticeService;

    @ImportService(interfaceClass = CrmCourseNoticeLoader.class)
    private CrmCourseNoticeLoader courseNoticeLoader;

    private static final String FORMAT_SQL_DATETIME = "yyyy/MM/dd HH:mm:ss";

    /**
     * 课程-通知列表
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String noticeId = getRequestString("noticeId");
        Long skuId = getRequestLong("skuId");
        Integer type = getRequestInt("type");
        String createUser = getRequestString("createUser");

        List<CourseNotice> noticeListList = courseNoticeLoader.loadAllCourseNotice();
        if (CollectionUtils.isNotEmpty(noticeListList)) {
            if (StringUtils.isNotBlank(noticeId)) {
                noticeListList = noticeListList.stream()
                        .filter(e -> e.getId().equals(noticeId.trim())).collect(Collectors.toList());
            }
            if (0L != skuId) {
                noticeListList = noticeListList.stream()
                        .filter(e -> e.getSkuId().equals(skuId)).collect(Collectors.toList());
            }
            if (0 != type && -1 != type) {
                noticeListList = noticeListList.stream()
                        .filter(e -> e.getType().equals(type)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                noticeListList = noticeListList.stream()
                        .filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }

        Page<CourseNotice> resultList;
        if (CollectionUtils.isEmpty(noticeListList)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            noticeListList = noticeListList.stream()
                    .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                    .collect(Collectors.toList());
            resultList = PageableUtils.listToPage(noticeListList, pageRequest);
        }

        if(0L != skuId) model.addAttribute("skuId", skuId);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("noticeId", noticeId);
        model.addAttribute("type", type);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/notice/index";
    }

    /**
     * 通知修改/添加页面
     */
    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        String noticeId = getRequestString("noticeId");
        String username = null;
        if (StringUtils.isNotBlank(noticeId)) {
            CourseNotice courseNotice = courseNoticeLoader.loadCourseNoticeById(noticeId);
            if (courseNotice != null) {
                username = courseNotice.getCreateUser();
                model.addAttribute("content", courseNotice);
            }
        } else {
            model.addAttribute("content", new CourseNotice());
        }
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        model.addAttribute("noticeId", noticeId);
        model.addAttribute("types", CourseConstMapper.NOTICE_TYPE);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        return "opmanager/studyTogether/notice/details";
    }

    /**
     * 添加或修改通知
     */
    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        String noticeId = getRequestString("noticeId");
        Long skuId = getRequestLong("skuId");
        int type = getRequestInt("type");
        int intervalDay = getRequestInt("intervalDay", 0);
        String picUrl = getRequestString("picUrl");
        String content = getRequestString("content");
        String jumpUrl = getRequestString("jumpUrl");
        String startDate = getRequestString("startDate");
        String endDate = getRequestString("endDate");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");

        Date start;
        Date end;
        if (startDate.contains("/")) {
            start = DateUtils.stringToDate(startDate, FORMAT_SQL_DATETIME);
        } else {
            start = DateUtils.stringToDate(startDate);
        }
        if (endDate.contains("/")) {
            end = DateUtils.stringToDate(endDate, FORMAT_SQL_DATETIME);
        } else {
            end = DateUtils.stringToDate(endDate);
        }
        try {
            CourseNotice bean;
            CourseNotice newObj;
            CourseNotice oldObj = new CourseNotice();
            if (StringUtils.isBlank(noticeId)) {
                bean = new CourseNotice();
            } else {
                bean = courseNoticeLoader.loadCourseNoticeById(noticeId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                bean.setId(noticeId);
                BeanUtils.copyProperties(oldObj, bean);
            }
            bean.setSkuId(skuId);
            bean.setType(type);
            bean.setIntervalDay(intervalDay);
            bean.setPicUrl(picUrl);
            bean.setContent(content.trim());
            bean.setJumpUrl(jumpUrl.trim());
            bean.setStartDate(start);
            bean.setEndDate(end);
            bean.setRemark(remark);
            if (StringUtils.isBlank(noticeId)) bean.setCreateUser(createUser);
            String userName = getCurrentAdminUser().getAdminUserName();

            newObj = courseNoticeService.save(bean);
            if (StringUtils.isBlank(noticeId)) {
                studyCourseBlackWidowServiceClient.justAddChangeLog("课程-通知", userName,
                        ChangeLogType.CourseNotice, newObj.getId(), "新增通知信息");
            } else {
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseNotice, newObj.getId());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    /**
     * 通知详情
     */
    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        String noticeId = getRequestString("noticeId");
        if (StringUtils.isBlank(noticeId)) {
            return CourseConstMapper.NOTICE_REDIRECT;
        }
        CourseNotice courseNotice = courseNoticeLoader.loadCourseNoticeById(noticeId);
        if (courseNotice == null) {
            return CourseConstMapper.NOTICE_REDIRECT;
        }
        model.addAttribute("content", courseNotice);
        model.addAttribute("noticeId", noticeId);
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        return "opmanager/studyTogether/notice/info";
    }

    /**
     * 通知-日志信息
     * @since 日志模板类型：CourseNotice
     */
    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        String noticeId = getRequestString("noticeId");
        if (StringUtils.isBlank(noticeId))  {
            return CourseConstMapper.NOTICE_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(noticeId, ChangeLogType.CourseNotice, pageRequest);

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
        model.addAttribute("noticeId", noticeId);
        return "opmanager/studyTogether/notice/logs";
    }

}
