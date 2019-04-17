package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinGroup;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinLessonRef;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.CardLessonMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.parent.AbstractMobileParentController;

import javax.inject.Inject;
import java.util.*;

/**
 * @author jiangpeng
 * @since 2018-05-10 上午11:55
 **/
public abstract class AbstractMobileParentStudyTogetherController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    protected StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    protected StudyLesson getStudyLesson(String lessonId){
        return studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(lessonId));
    }

    protected List<Map<String, Object>> generateGroupMemberMapList(ParentJoinGroup parentJoinGroup, Long currentParentId){
        Long ownerId = parentJoinGroup.getOwnerId();
        List<Map<String, Object>> membersInfoList = new ArrayList<>();

        User user = raikouSystem.loadUser(ownerId);
        if (user == null)
            return Collections.emptyList();
        Map<String, Object> ownerMap = new HashMap<>();
        ownerMap.put("parent_id", ownerId);
        ownerMap.put("avatar", getUserAvatarImgUrl(user));
        ownerMap.put("name", fetchParentName(user, currentParentId));
        ownerMap.put("is_owner", true);
        membersInfoList.add(ownerMap);

        List<Long> memberIdList = parentJoinGroup.getMemberIdList() == null ? Collections.emptyList() : parentJoinGroup.getMemberIdList();
        Map<Long, User> userMap = userLoaderClient.loadUsers(memberIdList);
        userMap.forEach((k,v) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("parent_id", k);
            map.put("avatar", getUserAvatarImgUrl(v));
            map.put("name", fetchParentName(v, currentParentId));
            map.put("is_owner", false);
            membersInfoList.add(map);
        });
        return membersInfoList;
    }

    private String fetchParentName(User parent, Long currentParentId){
        if (parent == null)
            return "";
        if (parent.getId().equals(currentParentId))
            return "我";
        String name = parent.fetchRealname();
        if (StringUtils.isNotEmpty(name))
            return name;
        String showName = "";
        List<User> users = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isNotEmpty(users)){
            User user = users.get(0);
            showName = user.fetchRealname() + "家长";
            if (StringUtils.isNotBlank(showName))
                return showName;
        }
        if (StringUtils.isBlank(showName)){
            return sensitiveUserDataServiceClient.loadUserMobileObscured(parent.getId());
        }
        return "";
    }

    protected CardLessonMapper processMapper(StudyLesson lesson, Map<Integer, List<ParentJoinLessonRef>> parentJoinSkuLessons,
                                             Map<Integer, List<StudyGroup>> studentSkuGroups, Map<String, Integer> progressMap,
                                             Map<String, Integer> finishInfoMap, Long joinCount){
        int spuId = lesson.getSpuId().intValue();
        String lessonId = SafeConverter.toString(lesson.getLessonId());
        boolean isJoin = parentJoinSkuLessons.containsKey(spuId) && parentJoinSkuLessons.get(spuId).stream().anyMatch(ref -> ref.getStudyLessonId().equals(lessonId));
        boolean isOpen = studentSkuGroups.containsKey(spuId) && studentSkuGroups.get(spuId).stream().anyMatch(g -> g.getLessonId().equals(lessonId));
        Integer ratio = progressMap.get(lessonId);
        return processMapper(lesson, isJoin, isOpen, ratio, finishInfoMap, joinCount);
    }

    protected CardLessonMapper processMapper(StudyLesson lesson, boolean isJoin, boolean isOpen, Integer ratio,
                                             Map<String, Integer> finishInfoMap, Long joinCount){
        CardLessonMapper mapper = new CardLessonMapper();
        String lessonId = SafeConverter.toString(lesson.getLessonId());
        mapper.setLessonId(lessonId);
        mapper.setCourseType(lesson.getCourseType());
        mapper.setName(lesson.getTitle());
        mapper.setImg(lesson.getIcon());
        mapper.setSuitableGradeText(lesson.getSuitableGradeText());
        mapper.setSubject(lesson.getSubject().getValue());
        mapper.setTimes(lesson.getTimes());
        mapper.setPhase(lesson.getPhase());
        mapper.setJoinCount(null == joinCount ? 0L : joinCount);
        mapper.setStartDate(DateUtils.dateToString(lesson.getOpenDate(), "M月dd日"));
        mapper.setIsJoin(isJoin);
        mapper.setIsOpen(isOpen);
        mapper.setSkuType(lesson.getSkuType());
        mapper.setSignUpEndDate(lesson.getSighUpEndDate());
        mapper.setCloseDate(lesson.getCloseDate());
        String ratioText = "";
        if (lesson.ratioExpire()) {
            ratio = -2;
            ratioText = "查看学习成果";
        } else if (ratio == null || ratio == -1 ) {
            ratioText = "完成课程可获得丰富奖励哦";
        }
        mapper.setRatio(ratio);
        mapper.setRatioText(ratioText);
        mapper.setLessonStatus(getStatus(lesson.getOpenDate(), lesson.getCloseDate()));
        mapper.setLessonStartCountdown(DateUtils.dayDiff(lesson.getOpenDate(), new Date()) + 1);
        if (MapUtils.isEmpty(finishInfoMap) || !finishInfoMap.containsKey("star")) {
            mapper.setTodayTaskScore(-1);
        } else {
            mapper.setTodayTaskScore(SafeConverter.toInt(finishInfoMap.get("star")));
        }

        if (MapUtils.isEmpty(finishInfoMap) || !finishInfoMap.containsKey("finishCount")) {
            mapper.setTodayTaskFinishCount(0);
        } else {
            mapper.setTodayTaskFinishCount(SafeConverter.toInt(finishInfoMap.get("finishCount")));
        }
        return mapper;

    }

    protected String getStatus(Date start, Date end) {
        if (start == null || end == null) {
            return "over";
        }
        Date date = new Date();
        if (date.before(start)) {
            return "before";
        }
        if (date.before(end)) {
            return "open";
        }
        return "over";
    }
}
