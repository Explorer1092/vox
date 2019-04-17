package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.api.StudyTogetherJoinGroupService;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinGroup;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinLessonRef;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组团报名
 * http://wiki.17zuoye.net/pages/viewpage.action?pageId=38182140
 * @author jiangpeng
 * @since 2018-05-07 下午6:26
 **/
@Deprecated
@Controller
@RequestMapping(value = "/parentMobile/study_together/join_group")
public class MobileParentStudyTogetherJoinGroupController extends AbstractMobileParentStudyTogetherController {


    @ImportService(interfaceClass = StudyTogetherJoinGroupService.class)
    private StudyTogetherJoinGroupService studyTogetherJoinGroupService;

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;


    @RequestMapping(value = "lesson_info.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage lessonInfo(){
        User parent = currentParent();
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程ID错误");
        Long studentId = getRequestLong("sid");
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null )
            return MapMessage.errorMessage(" 课程不存在");
        if (!studyLesson.joinNeedGroup())
            return MapMessage.errorMessage("此课程不支持拼团报名！");
        AlpsFuture<Long> joinCountFuture = studyTogetherServiceClient.loadLessonJoinCount(lessonId);
        ParentJoinLessonRef parentJoinLessonRef = parent != null ? studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, parent.getId()) : null;
        StudyGroup studyGroup = loadParentStudentActiveGroup(parent, lessonId);
        ParentJoinGroup parentJoinGroup = parent != null ? studyTogetherJoinGroupService.loadParentJoinGroup(lessonId, parent.getId()) : null;
        Map<String, Object> lessonInfoMap = lessonInfoMap(studyLesson, joinCountFuture, parentJoinLessonRef, parentJoinGroup != null);
        lessonInfoMap.put("is_active", studyGroup != null);
        return MapMessage.successMessage().add("lesson_info", lessonInfoMap);
    }

    private StudyGroup loadParentStudentActiveGroup(User parent, String lessonId) {
        if (parent == null)
            return null;
        List<User> users = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isEmpty(users))
            return null;
        for (User user : users) {
            StudyGroup studyGroup = studyTogetherServiceClient.loadStudentGroupByLessonId(user.getId(), lessonId);
            if (studyGroup != null)
                return studyGroup;
        }
        return null;
    }

    private Map<String, Object> lessonInfoMap(StudyLesson studyLesson, AlpsFuture<Long> joinCountFuture, ParentJoinLessonRef parentJoinLessonRef, boolean isJoinGroup){
        Map<String, Object> lessonInfoMap = new HashMap<>();
        lessonInfoMap.put("name", studyLesson.getTitle());
        lessonInfoMap.put("phase", studyLesson.getPhase());
        lessonInfoMap.put("start_date", DateUtils.dateToString(studyLesson.getOpenDate(), "MM.dd"));
        lessonInfoMap.put("times", studyLesson.getTimes());
        lessonInfoMap.put("clazz_level_text", studyLesson.getSuitableGradeText());
        lessonInfoMap.put("price", 199); //fixme 取产品价格？
        lessonInfoMap.put("group_count", studyLesson.joinGroupLimit());
        lessonInfoMap.put("join_count", joinCountFuture.getUninterruptibly());
        lessonInfoMap.put("is_join", parentJoinLessonRef != null);
        lessonInfoMap.put("product_id", studyLesson.getProductId());
        lessonInfoMap.put("is_group", isJoinGroup);
        lessonInfoMap.put("is_join_end", studyLesson.getSighUpEndDate().before(new Date()));
        lessonInfoMap.put("is_closed", studyLesson.isClosed());
        lessonInfoMap.put("course_type", studyLesson.getCourseType());
        return lessonInfoMap;
    }

    @RequestMapping(value = "group_info.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage groupInfo(){
        User parent = currentParent();
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程ID错误");
        long ownerId = getRequestLong("owner_id");
        if (ownerId == 0 && parent == null){
            return MapMessage.errorMessage("亲，拼主id去哪了？");
        }

        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null )
            return MapMessage.errorMessage("课程不存在");
        if (!studyLesson.joinNeedGroup())
            return MapMessage.errorMessage("此课程不支持拼团报名！");
        AlpsFuture<Long> joinCountFuture = studyTogetherServiceClient.loadLessonJoinCount(lessonId);
        ParentJoinLessonRef parentJoinLessonRef = parent != null ? studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, parent.getId()) : null;

        Long queryGroupPid;
        if (ownerId == 0L)
            queryGroupPid = parent.getId();
        else
            queryGroupPid = ownerId;
        ParentJoinGroup requestQueryGroup = studyTogetherJoinGroupService.loadParentJoinGroup(lessonId, queryGroupPid);
        if (requestQueryGroup == null || ( ownerId != 0L && !Long.valueOf(ownerId).equals(requestQueryGroup.getOwnerId())))
            return MapMessage.errorMessage("对不起，此拼团已过期或不存在！");
        ParentJoinGroup parentCurrentInGroup = parent == null ? null : studyTogetherJoinGroupService.loadParentJoinGroup(lessonId, parent.getId());

        ParentJoinGroup showGroup;
        if (parentCurrentInGroup != null)
            showGroup = parentCurrentInGroup;
        else
            showGroup = requestQueryGroup;

        Map<String, Object> lessonInfoMap = lessonInfoMap(studyLesson, joinCountFuture, parentJoinLessonRef, parentCurrentInGroup != null );
        List<Map<String, Object>> membersInfoList = generateGroupMemberMapList(showGroup, parent == null ? -1: parent.getId());

        Map<String, Object> groupInfoMap = new HashMap<>();
        groupInfoMap.put("is_full", showGroup.groupSuccess(studyLesson.joinGroupLimit()));
        groupInfoMap.put("expiry_countdown", (int)((DateUtils.calculateDateDay(showGroup.getCreateDate(), 1).getTime() - System.currentTimeMillis())/1000));
        groupInfoMap.put("is_expire", true);
        groupInfoMap.put("remainder", studyLesson.joinGroupLimit() - showGroup.memberCount() - 1);
        groupInfoMap.put("members", membersInfoList);
        groupInfoMap.put("owner_id", showGroup.getOwnerId());


        return MapMessage.successMessage().add("lesson_info", lessonInfoMap).add("group_info", groupInfoMap);

    }


    @RequestMapping(value = "join.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage parentJoinGroup(){
        User parent = currentParent();
        if (parent == null)
            return MapMessage.errorMessage().setErrorCode("666");
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程id缺失");
        Long ownerId = getRequestLong("owner_id");
        if (ownerId == 0)
            return MapMessage.errorMessage("团拼主id缺失");
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null)
            return MapMessage.errorMessage("课程不存在呢");
        if (!studyLesson.joinNeedGroup())
            return MapMessage.errorMessage("此课程不支持拼团报名哦~");
        ParentJoinGroup parentJoinGroup = studyTogetherJoinGroupService.loadParentJoinGroup(lessonId, ownerId);
        if (parentJoinGroup == null){
            return MapMessage.errorMessage("该拼团不存在呢~");
        }
        if (parentJoinGroup.groupSuccess(studyLesson.joinGroupLimit())){
            return MapMessage.errorMessage("对不起，该团已满，请返回~");
        }
        if (true)
            return MapMessage.errorMessage("对不起，该团已过期！");
        return AtomicLockManager.getInstance().wrapAtomic(studyTogetherJoinGroupService)
                .keyPrefix("studyTogetherParentJoinGroup")
                .keys(lessonId, ownerId, parent)
                .proxy()
                .parentJoinGroup(lessonId, ownerId,  parent.getId());
    }


    @RequestMapping(value = "create.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage parentCreateGroup(){
        User parent = currentParent();
        if (parent == null)
            return MapMessage.errorMessage().setErrorCode("666");;
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程id缺失");
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null)
            return MapMessage.errorMessage("课程不存在呢");
        if (!studyLesson.joinNeedGroup())
            return MapMessage.errorMessage("此课程不支持拼团报名哦~");
        return AtomicLockManager.getInstance().wrapAtomic(studyTogetherJoinGroupService)
                .keyPrefix("studyTogetherParentCreateGroup")
                .keys(lessonId, parent.getId())
                .proxy()
                .parentCreateGroup(parent.getId(), lessonId);
    }

}
