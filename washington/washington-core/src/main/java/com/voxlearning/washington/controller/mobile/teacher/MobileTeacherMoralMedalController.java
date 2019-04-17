package com.voxlearning.washington.controller.mobile.teacher;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.MoralMedalService;
import com.voxlearning.utopia.service.campaign.api.constant.MoralMedalConstant;
import com.voxlearning.utopia.service.campaign.api.enums.MoralMedalEnum;
import com.voxlearning.utopia.service.campaign.api.mapper.SendMedalMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Named;
import java.util.Date;
import java.util.Objects;

@Named
@RequestMapping("/teacherMobile/moral_medal/")
public class MobileTeacherMoralMedalController extends AbstractMobileTeacherController {

    @ImportService(interfaceClass = MoralMedalService.class)
    private MoralMedalService moralMedalService;

    @ResponseBody
    @RequestMapping("teacher_group.vpage")
    public MapMessage teacherClazz() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }

        return moralMedalService.loadTeacherGroup(user.getId());
    }

    @ResponseBody
    @RequestMapping("teacher_group_detail.vpage")
    public MapMessage teacherClazzDetail() {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }
        long groupId = getRequestLong("group_id");
        if (groupId == 0) {
            return MapMessage.errorMessage("group_id 不可为空");
        }

        return moralMedalService.loadTeacherGroupDetail(user.getId(), groupId);
    }

    @ResponseBody
    @RequestMapping(value = "send_medal.vpage", method = RequestMethod.POST)
    public MapMessage sendMedal(@RequestBody SendMedalMapper input) {
        User user = currentUser();
        if (user == null || (!user.isTeacher())) {
            return noLoginResult;
        }
        if (input == null || input.getGroupId() == null
                || CollectionUtils.isEmpty(input.getStudentIds())
                || CollectionUtils.isEmpty(input.getMedalIds())) {
            return MapMessage.errorMessage("参数异常");
        }

        return moralMedalService.sendMedal(user.getId(), input.getGroupId(),
                input.getStudentIds(), input.getMedalIds());
    }

    /**
     * 班级德育表现
     */
    @RequestMapping(value = "/clazz_moral.vpage")
    @ResponseBody
    public MapMessage clazzMoral() {

        long tid = getRequestLong("tid");
        if (Objects.equals(tid, 0L)) {
            return MapMessage.errorMessage("教师id不能为空");
        }

        Long groupId = getRequestLong("groupId");
        if (Objects.equals(groupId, 0L)) {
            return MapMessage.errorMessage("班级id不能为空");
        }

        String datestr = getRequestString("date");
        if (StringUtils.isEmpty(datestr)) {
            return MapMessage.errorMessage("时间不能为空");
        }
        Date date = DateUtils.stringToDate(datestr, DateUtils.FORMAT_SQL_DATE);
        if (date == null || MoralMedalConstant.ON_LINE_DATE.getTime() > date.getTime()) {
            return MapMessage.errorMessage("请检查日期");
        }

        return moralMedalService.loadClazzMoral(tid, groupId, datestr);
    }

    /**
     * 勋章列表
     */
    @RequestMapping(value = "medal_list.vpage")
    @ResponseBody
    public MapMessage medalList() {

        MoralMedalEnum[] values = MoralMedalEnum.values();
        JSONArray array = new JSONArray();
        for (MoralMedalEnum value : values) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", value.getId());
            jsonObject.put("name", value.name());
            jsonObject.put("icon", value.getIcon());
            array.add(jsonObject);
        }

        return MapMessage.successMessage().add("data", array);
    }

}
