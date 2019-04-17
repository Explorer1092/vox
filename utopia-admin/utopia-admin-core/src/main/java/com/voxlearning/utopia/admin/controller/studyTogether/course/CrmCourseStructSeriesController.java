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
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSeriesLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSeriesService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSeries;
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
 * <p>系列
 * @author xuerui.zhang
 * @since 2018/9/19 下午2:28
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/series/")
public class CrmCourseStructSeriesController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseStructSeriesLoader.class)
    private CrmCourseStructSeriesLoader courseStructSeriesLoader;

    @ImportService(interfaceClass = CrmCourseStructSeriesService.class)
    private CrmCourseStructSeriesService courseStructSeriesService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);

        Long seriesId = getRequestLong("seriesId");
        String name = getRequestString("name");
        String subject = getRequestString("subject");
        int seriesType = getRequestInt("seriesType");
        int level = getRequestInt("level");
        String createUser = getRequestString("createUser");

        List<CourseStructSeries> seriesList = courseStructSeriesLoader.loadAllCourseStructSeries();
        if (CollectionUtils.isNotEmpty(seriesList)) {
            if (0L != seriesId) {
                seriesList = seriesList.stream().filter(e -> e.getId().equals(seriesId)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(name)) {
                seriesList = seriesList.stream().filter(e -> e.getName().contains(name.trim())).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(subject) && !subject.equals("ALL")) {
                seriesList = seriesList.stream().filter(e -> e.getSubject().equals(subject.trim())).collect(Collectors.toList());
            }
            if (seriesType > 0) {
                seriesList = seriesList.stream().filter(e -> e.getSeriesType().equals(seriesType)).collect(Collectors.toList());
            }
            if (level > 0) {
                seriesList = seriesList.stream().filter(e -> e.getLevel().equals(level)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                seriesList = seriesList.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }
        Page<CourseStructSeries> resultList;
        if (CollectionUtils.isEmpty(seriesList)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            seriesList = seriesList.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(seriesList, pageRequest);
        }
        if(0L != seriesId) model.addAttribute("seriesId", seriesId);
        if(seriesType > 0) model.addAttribute("seriesType", seriesType);
        if(level > 0) model.addAttribute("level", level);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("name", name);
        model.addAttribute("subject", subject);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/series/index";
    }

    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        Long seriesId = getRequestLong("seriesId");
        String username = null;
        if (seriesId > 0L)  {
            CourseStructSeries series = courseStructSeriesLoader.loadCourseStructSeriesById(seriesId);
            if (series != null) {
                username = series.getCreateUser();
                model.addAttribute("content", series);
            }
        } else {
            model.addAttribute("content", new CourseStructSeries());
        }
        model.addAttribute("seriesId", seriesId);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);

        return "opmanager/studyTogether/series/details";
    }

    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        Long seriesId = getRequestLong("seriesId");
        String name = getRequestString("name");
        String subject = getRequestString("subject");
        int courseType = getRequestInt("courseType");
        int seriesType = getRequestInt("seriesType");
        int level = getRequestInt("level");
        String symbol = getRequestString("symbol");
        int envLevel = getRequestInt("envLevel");
        String remark = getRequestString("remark");
        String describe = getRequestString("describe");
        String createUser = getRequestString("createUser");
        int tag = getRequestInt("tag");

        try {
            CourseStructSeries bean;
            CourseStructSeries newObj;
            CourseStructSeries oldObj = new CourseStructSeries();
            if (seriesId <= 0L)  {
                bean = new CourseStructSeries();
                try {
                    Long currentId = AtomicLockManager.getInstance().wrapAtomic(courseStructSeriesLoader)
                            .keyPrefix("SERIES_INCR_ID").keys(courseType, seriesType).proxy().loadMaxId();
                    bean.setId(currentId + 1);
                } catch (Exception e) {
                    logger.error("lock error {}", e.getMessage(), e);
                    return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
                }
            } else {
                bean = courseStructSeriesLoader.loadCourseStructSeriesById(seriesId);
                if (bean == null) return MapMessage.errorMessage("数据不存在");
                bean.setId(seriesId);
                BeanUtils.copyProperties(oldObj, bean);
            }

            bean.setName(name.trim());
            bean.setSubject(subject.trim());
            bean.setCourseType(courseType);
            bean.setSeriesType(seriesType);
            bean.setLevel(level);
            bean.setSymbol(symbol.trim());
            bean.setDescribe(describe);
            bean.setTag(tag);
            if (StringUtils.isNotBlank(remark))bean.setRemark(remark.trim());
            bean.setEnvLevel(envLevel);
            String userName = getCurrentAdminUser().getAdminUserName();
            if (seriesId <= 0L) bean.setCreateUser(createUser);

            newObj = courseStructSeriesService.save(bean);
            if (seriesId <= 0L)  {
                studyCourseBlackWidowServiceClient.justAddChangeLog("系列", userName,
                        ChangeLogType.CourseSeries, newObj.getId().toString(), "新增系列信息");
            } else {
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseSeries, seriesId.toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        Long seriesId = getRequestLong("seriesId");
        if (0L == seriesId)  {
            return CourseConstMapper.SERIES_REDIRECT;
        }
        CourseStructSeries courseStructSeries = courseStructSeriesLoader.loadCourseStructSeriesById(seriesId);
        if (courseStructSeries == null) {
            return CourseConstMapper.SERIES_REDIRECT;
        }
        model.addAttribute("spuId", seriesId);
        model.addAttribute("content", courseStructSeries);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        return "opmanager/studyTogether/series/info";
    }

    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        Long seriesId = getRequestLong("seriesId");
        if (0L == seriesId)  {
            return CourseConstMapper.SERIES_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(seriesId.toString(), ChangeLogType.CourseSeries, pageRequest);

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
        model.addAttribute("seriesId", seriesId);
        return "opmanager/studyTogether/series/logs";
    }
}
