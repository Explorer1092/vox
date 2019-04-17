package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.api.StudyTogetherSummerPackageService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherSummerInvitationRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2018-06-26 上午11:54
 **/
@Controller
@RequestMapping(value = "/parentMobile/study_together/summer_package")
public class MobileParentStudyTogetherSummerPakcageController extends AbstractMobileParentStudyTogetherController {

    @ImportService(interfaceClass = StudyTogetherSummerPackageService.class)
    private StudyTogetherSummerPackageService summerPackageService;

    @ResponseBody
    @RequestMapping(value = "/invite_record.vpage", method = RequestMethod.GET)
    public MapMessage inviteRecord() {
        User parent = currentParent();
        if (parent == null)
            return go2LoginPageResult;
        List<StudyTogetherSummerInvitationRef> studyTogetherSummerInvitationRefs = summerPackageService.loadParentInviteList(parent.getId());
        if (CollectionUtils.isEmpty(studyTogetherSummerInvitationRefs)){
            return MapMessage.successMessage().add("record_list", Collections.emptyList()).add("invite_count", 0);
        }
        Set<Long> pidList = studyTogetherSummerInvitationRefs.stream().map(StudyTogetherSummerInvitationRef::getInviteeId).collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(pidList);
        List<Map<String, Object>> recordList = new ArrayList<>();
        studyTogetherSummerInvitationRefs.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime())).forEach(t -> {
            User user = userMap.get(t.getInviteeId());
            if (user == null)
                return;
            Map<String, Object> map = new HashMap<>();
            map.put("avatar", getUserAvatarImgUrl(user));
            map.put("name", fetchParentName(user));
            map.put("date", DateUtils.dateToString(t.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
            recordList.add(map);
        });
        return MapMessage.successMessage().add("record_list", recordList).add("invite_count", recordList.size());
    }

    private String fetchParentName(User parent){
        if (parent == null)
            return "";
        String name = parent.fetchRealname();
        if (StringUtils.isNotEmpty(name))
            return name;
        String showName = "";
        List<User> users = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isNotEmpty(users)){
            User user = users.get(0);
            return user.fetchRealname() + "家长";
        }
        if (StringUtils.isBlank(showName)){
            return sensitiveUserDataServiceClient.loadUserMobileObscured(parent.getId());
        }
        return "";
    }

    /**
     * APP端获取暑期课程列表
     */
    @ResponseBody
    @RequestMapping(value = "/app_lesson_tabs.vpage", method = RequestMethod.GET)
    public MapMessage getSummerLessonTabsInApp() {
        User parent = currentParent();
        Long studentId = getRequestLong("sid",0L);
        int order = getRequestInt("order", 0);
        if (null == parent) {
            return summerPackageService.getDefaultSummerLessonTabs(order).add("could_join_lesson", true);
        }
        String fetchParentName = fetchParentName(parent);
        return summerPackageService.getSummerLessonTabsInApp(parent.getId(), studentId, order).add("fetch_parent_name", fetchParentName);
    }

    /**
     * 微信端获取暑期课程列表
     */
    @ResponseBody
    @RequestMapping(value = "/wechat_lesson_tabs.vpage", method = RequestMethod.GET)
    public MapMessage getSummerLessonTabsInWechat() {
        User parent = currentParent();
        int order = getRequestInt("order", 0);
        if (null == parent) {
            return summerPackageService.getDefaultSummerLessonTabs(order).add("could_join_lesson", true);
        }
        String fetchParentName = fetchParentName(parent);
        return summerPackageService.getSummerLessonTabsInWechat(parent.getId(), order).add("fetch_parent_name", fetchParentName);
    }

    /**
     * 获取当前用户孩子信息
     */
    @ResponseBody
    @RequestMapping(value = "/kid_info.vpage", method = RequestMethod.GET)
    public MapMessage getStudentInfo() {
        User parent = currentParent();
        if (null == parent) {
            return go2LoginPageResult;
        }
        Long studentId = getRequestLong("student_id",0L);
        return summerPackageService.getStudentInfo(parent.getId(), studentId);
    }

}
