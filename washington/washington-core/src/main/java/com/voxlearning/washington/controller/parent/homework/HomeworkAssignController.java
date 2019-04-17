package com.voxlearning.washington.controller.parent.homework;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignService;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesService;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.api.mapper.UserPreference;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.parent.homework.util.HomeworkUtil;
import com.voxlearning.washington.controller.parent.homework.wrapper.BookPreferenceParam;
import com.voxlearning.washington.controller.parent.homework.wrapper.QuestionBoxParam;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 布置作业
 *
 * @author chongfeng.qi
 * @version 20181111
 */
@Controller
@RequestMapping(value = "/parent/homework/assign")
@Slf4j
public class HomeworkAssignController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = HomeworkAssignLoader.class)
    private HomeworkAssignLoader homeworkAssignLoader;
    @ImportService(interfaceClass = HomeworkUserPreferencesService.class)
    private HomeworkUserPreferencesService homeworkUserPreferencesService;

    @ImportService(interfaceClass = HomeworkUserPreferencesLoader.class)
    private HomeworkUserPreferencesLoader homeworkUserPreferencesLoader;

    @ImportService(interfaceClass = HomeworkAssignService.class)
    private HomeworkAssignService homeworkService;

    /**
     * 学生的基础信息
     *
     * @return
     */
    @RequestMapping(value = "/baseinfo.vpage")
    @ResponseBody
    public MapMessage baseInfo() {
        long studentId = getRequestLong("studentId");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        StudentInfo studentInfo = homeworkAssignLoader.loadStudentInfo(studentId);
        if (studentInfo == null) {
            return MapMessage.errorMessage("学生信息有误");
        }
        Map<String, Object> data = JsonUtils.safeConvertObjectToMap(studentInfo);
        if (data == null) {
            return MapMessage.errorMessage("学生信息错误");
        }
        ExRegion exRegion = raikouSystem.loadRegion(studentInfo.getRegionCode());
        if (exRegion != null) {
            data.put("regionName", exRegion.getName());
            data.put("cityCode", exRegion.getCityCode());
            data.put("cityName", exRegion.getCityName());
            data.put("provinceCode", exRegion.getProvinceCode());
            data.put("provinceName", exRegion.getProvinceName());
        }
        if (studentInfo.getClazzLevel() != null) {
            data.put("clazzLevelName", ClazzLevel.getDescription(studentInfo.getClazzLevel()));
        }
        return MapMessage.successMessage().add("data", data);
    }

    /**
     * 教材列表
     *
     * @return
     */
    @RequestMapping(value = "/books.vpage")
    @ResponseBody
    public MapMessage books() {
        long studentId = getRequestLong("studentId");
        int clazzLevel = getRequestInt("clazzLevel");
        int regionCode = getRequestInt("regionCode");
        String subject = getRequestString("subject");
        String bizType = getRequestString("bizType");
        if (studentId == 0L) {
            return MapMessage.errorMessage("学生信息有误");
        }
        return homeworkAssignLoader.loadBooks(subject, studentId, clazzLevel, regionCode, bizType);
    }

    /**
     * 获取题包
     *
     * @return
     */
    @RequestMapping(value = "/questionboxes.vpage")
    @ResponseBody
    public MapMessage questionBoxes() {
        long studentId = getRequestLong("studentId");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        HomeworkParam param = new HomeworkParam();
        param.setStudentId(studentId);
        String unitIds = getRequestString("unitId");
        List<String> unitIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(unitIds)) {
            unitIdList = Arrays.asList(unitIds.split(","));
        }
        param.setUnitIds(unitIdList);
        param.setBookId(getRequestString("bookId"));
        param.setBizType(getRequestParameter("bizType", "EXAM"));
        // 默认去科目列表的第一个返回
        param.setSubject(getRequestString("subject"));
        param.setData(MapUtils.map("count", getRequestInt("count")));
        MapMessage result = homeworkAssignLoader.loadQuestionBoxes(param);
        return result;
    }

    /**
     * 设置偏好
     *
     * @return
     */
    @RequestMapping(value = "/setpreference.vpage")
    @ResponseBody
    public MapMessage setPreference() {
        long studentId = getRequestLong("studentId");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        // 如果学生没有班组关系 regionCode必须
        int regionCode = getRequestInt("regionCode");
        int clazzLevel = getRequestInt("clazzLevel");
        // 没有班组的regionCode 和年级存到 vox_channelc_user_attribute
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage();
        }
        if (studentDetail.getClazz() == null) {
            if (regionCode == 0 || clazzLevel == 0) {
                return MapMessage.errorMessage("请完善孩子的基本信息");
            }
        }
        String bizType = getRequestParameter("bizType", "EXAM");
        List<BookPreferenceParam> books = JsonUtils.fromJsonToList(getRequestString("books"), BookPreferenceParam.class);
        MapMessage mapMessage = MapMessage.successMessage();
        if (CollectionUtils.isNotEmpty(books)) {
            List<UserPreference> userPreferences = new ArrayList<>();
            for (BookPreferenceParam param : books) {
                UserPreference userPreference = new UserPreference();
                userPreference.setLevels(param.getLevels());
                userPreference.setBookId(param.getBookId());
                userPreference.setSubject(param.getSubject());
                userPreference.setUserId(studentId);
                userPreference.setBizType(bizType);
                userPreferences.add(userPreference);
            }
            mapMessage = homeworkUserPreferencesService.upsertHomeworkUserPreferences(userPreferences);
        }
        // 更新孩子的C端信息
        if (mapMessage.isSuccess() && studentDetail.getClazz() == null) {
            MapMessage message = studentServiceClient.updateChannelCStudentClazzLevelOrRegionCode(
                    studentId,
                    clazzLevel,
                    regionCode);
            if (!message.isSuccess()) {
                return message;
            }
        }
        return mapMessage;
    }

    /**
     * 布置作业
     *
     * @return
     */
    @RequestMapping(value = "/homework.vpage")
    @ResponseBody
    public MapMessage homework() {
        long studentId = getRequestLong("studentId");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录");
        }
        // 获得选取的题包
        String questionBoxes = getRequestString("questionBoxes");
        if (StringUtils.isBlank(questionBoxes)) {
            return MapMessage.errorMessage("请选择题包");
        }
        List<QuestionBoxParam> params = JsonUtils.fromJsonToList(questionBoxes, QuestionBoxParam.class);
        if (CollectionUtils.isEmpty(params)) {
            return MapMessage.errorMessage("题包选择错误");
        }
        // 批次
        String actionId = HomeworkUtil.generatorDayID();
        List<String> homeworkIds = Lists.newArrayList();
        for (QuestionBoxParam param : params) {
            HomeworkParam homeworkParam = new HomeworkParam();
            homeworkParam.setCurrentUserId(user.getId());
            homeworkParam.setStudentId(studentId);
            homeworkParam.setSubject(param.getSubject());
            homeworkParam.setSource("parent");
            homeworkParam.setBookId(param.getBookId());
            Map<String, Object> data = MapUtils.map("boxIds", Arrays.asList(param.getBoxId().split(",")));
            data.put("actionId", actionId);
            homeworkParam.setData(data);
            homeworkParam.setBizType(param.getBizType());
            MapMessage mapMessage = homeworkService.assignHomework(homeworkParam);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }
            homeworkIds.add((String) mapMessage.get("homeworkId"));
        }
        return MapMessage.successMessage().add("homeworkIds", homeworkIds);
    }

    /**
     * 同步作业布置入口
     *
     * @return
     */
    @RequestMapping(value = "/assignhome.vpage")
    @ResponseBody
    public MapMessage assignHome() {
        long studentId = getRequestLong("studentId");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录");
        }
        // 是否B端用户
        boolean isBStudent = studentLoaderClient.loadStudentDetail(studentId).getClazz() != null;
        // 是否初始化
        boolean isInit = homeworkUserPreferencesLoader.loadHomeworkUserPreference(studentId, Subject.CHINESE.name()) != null;
        String requireUrl;
        if (isInit) {
            requireUrl = "/view/mobile/parent/homework_parent/assign_homework.vpage";
        } else {
            if (isBStudent) {
                requireUrl = "/view/mobile/parent/homework_parent/select_book.vpage";
            } else {
                requireUrl = "/view/mobile/parent/homework_parent/select_area.vpage";
            }
        }
        return MapMessage.successMessage().add("requireUrl", ProductConfig.getMainSiteBaseUrl() + requireUrl);
    }

    /**
     * 口算作业布置入口
     *
     * @return
     */
    @RequestMapping(value = "/mental/assignhome.vpage")
    @ResponseBody
    public MapMessage mentalAssignHome() {
        long studentId = getRequestLong("studentId");
        if (studentId == 0L) {
            return MapMessage.errorMessage();
        }
        User user = currentParent();
        if (user == null) {
            return MapMessage.errorMessage("用户未登录");
        }
        // 是否B端用户
        StudentInfo studentInfo = homeworkAssignLoader.loadStudentInfo(studentId);
        // 是否初始化
        boolean isInit = studentInfo.getClazzLevel() != null && studentInfo.getRegionCode() != null;
        String requireUrl;
        if (isInit) {
            requireUrl = "/view/mobile/parent/online_oral/index.vpage";
        } else {
            requireUrl = "/view/mobile/parent/online_oral/select_area.vpage";
        }
        return MapMessage.successMessage().add("requireUrl", ProductConfig.getMainSiteBaseUrl() + requireUrl);
    }


}
