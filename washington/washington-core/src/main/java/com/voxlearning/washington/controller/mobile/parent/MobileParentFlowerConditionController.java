package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.FlowerConditionType;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.flower.api.constant.TeachersDay17Activity;
import com.voxlearning.utopia.service.flower.api.entities.FlowerCondition;
import com.voxlearning.utopia.service.flower.api.entities.FlowerRankMember;
import com.voxlearning.utopia.service.flower.api.entities.FlowerTodayRecordMember;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-07-25 下午3:53
 **/
@Controller
@RequestMapping(value = "/parentMobile/flower_condition")
@Slf4j
public class MobileParentFlowerConditionController extends AbstractMobileParentController{

    @Inject
    private FlowerServiceClient flowerServiceClient;

    /**
     * 送鲜花页面
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0L)
            return MapMessage.errorMessage("没有学生id");
        Long groupId = getRequestLong("group_id");
        if (groupId == 0L) {
            return MapMessage.errorMessage("班级错误");
        }
        MapMessage resultMap = MapMessage.successMessage();

        Set<Long> sharedGroupIds = deprecatedGroupLoaderClient.loadSharedGroupIds(groupId);
        Boolean multipleSubject = CollectionUtils.isNotEmpty(sharedGroupIds);
        List<Long> allGroupId = new ArrayList<>(sharedGroupIds);
        allGroupId.add(groupId);
        resultMap.add("multiple_subject", multipleSubject);
        if (multipleSubject) {
            Map<Long, GroupMapper> groupMapperMap = deprecatedGroupLoaderClient.loadGroups(allGroupId, false);
            List<Map<String, Object>> subjectGroupMapList = groupMapperMap.values().stream()
                    .sorted(Comparator.comparingInt(o -> o.getSubject().getKey()))
                    .map(t -> {
                        Map<String, Object> map = new HashMap();
                        map.put("display", t.getSubject().getValue());
                        map.put("id", t.getId());
                        return map;
                    }).collect(Collectors.toList());
            resultMap.add("subjects", subjectGroupMapList);
            //取排序后的第一个 groupId
            if (getRequestLong("group_id") == 0)
                groupId = SafeConverter.toLong(subjectGroupMapList.get(0).get("id"));
        }
        resultMap.put("current_group_id", groupId);
        Teacher teacher = teacherLoaderClient.loadGroupSingleTeacher(Collections.singleton(groupId)).get(groupId);
        if (teacher == null)
            return MapMessage.errorMessage("班级异常,没有老师?");
        AlpsFuture<FlowerRankMember> flowerRankMemberAlpsFuture = flowerServiceClient.getFlowerConditionService().loadStudentTermSendFlower(SchoolYear.newInstance().currentTermDateRange(), groupId, teacher.getId(), studentId);
        resultMap.add("send_count", flowerRankMemberAlpsFuture.getUninterruptibly().getFlowerCount());
        List<FlowerConditionType> flowerConditionTypes = Arrays.asList(FlowerConditionType.HOMEWORK, FlowerConditionType.REWARD_CHILD, FlowerConditionType.TEACHER_DAY_17);
        List<FlowerConditionType> typeList = new ArrayList<>(flowerConditionTypes);
        List<FlowerCondition> flowerConditions = flowerServiceClient.getFlowerConditionService().loadFlowerConditions(DayRange.current(), groupId, teacher.getId(), studentId);
        boolean teachersDayInPeriod = TeachersDay17Activity.isInPeriod();
        FlowerCondition.ConditionStatus homeworkStatus = FlowerCondition.ConditionStatus.NOT_YET;
        List<FlowerCondition> resultList = new ArrayList<>();
        for (FlowerCondition t : flowerConditions) {
            FlowerConditionType conditionType = t.getFlowerConditionType();
            boolean isTeachersDayType = conditionType == FlowerConditionType.TEACHER_DAY_17;
            if (!teachersDayInPeriod && isTeachersDayType)
                continue;
            if (conditionType == FlowerConditionType.HOMEWORK)
                homeworkStatus = t.getConditionStatus();
            if (isTeachersDayType){
                if (homeworkStatus == FlowerCondition.ConditionStatus.NOT_YET)
                    continue;
            }
            t.setConditionText(conditionType.getDesc());
            t.setId(conditionType + "|" + t.getId());
            resultList.add(t);
            typeList.remove(conditionType);
        }

        if (CollectionUtils.isNotEmpty(typeList)){
            typeList.forEach(t -> {
                if (t == FlowerConditionType.TEACHER_DAY_17)
                    return;
                FlowerCondition flowerCondition = new FlowerCondition();
                flowerCondition.setConditionText(t.getDesc());
                flowerCondition.setFlowerConditionType(t);
                flowerCondition.setId("");
                flowerCondition.setConditionStatus(FlowerCondition.ConditionStatus.NOT_YET);
                resultList.add(flowerCondition);
            });
        }
        resultMap.add("conditions", resultList);

        //先带第一页的 record
        Pageable page = new PageRequest(0, 20);
        List<Map<String, Object>> todayFlowerRecord = getTodayFlowerRecord(page, groupId, teacher.getId());
        resultMap.add("records", todayFlowerRecord);
        return resultMap;
    }

    /**
     * 送花记录
     */
    @RequestMapping(value = "record.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage record() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        long groupId = getRequestLong("group_id");
        if (groupId == 0)
            return MapMessage.errorMessage("error groupId");
        Teacher teacher = teacherLoaderClient.loadGroupSingleTeacher(Collections.singleton(groupId)).get(groupId);
        if (teacher == null)
            return MapMessage.errorMessage("班级没有老师?");
        Integer currentPage = getRequestInt("currentPage", 1);
        Pageable page = new PageRequest(currentPage - 1, 20);

        return MapMessage.successMessage().add("records", getTodayFlowerRecord(page, groupId, teacher.getId()));
    }

    private List<Map<String, Object>> getTodayFlowerRecord(Pageable pageable, Long groupId, Long teacherId){
        List<FlowerTodayRecordMember> flowerTodayRecordMembers = flowerServiceClient.getFlowerConditionService().loadGroupFlowerRecord(DayRange.current(), groupId, teacherId, pageable);
        List<Long> studentIds = flowerTodayRecordMembers.stream().map(FlowerTodayRecordMember::getStudentId).collect(Collectors.toList());
        Map<Long, User> studentMaps = userLoaderClient.loadUsers(studentIds);
        List<Map<String, Object>> recordMapList = new ArrayList<>();
        flowerTodayRecordMembers.forEach(t -> {
            User user = studentMaps.get(t.getStudentId());
            if (user == null || !user.isStudent())
                return;
            Map<String, Object> map = new HashMap<>();
            map.put("avatar", getUserAvatarImgUrl(user));
            map.put("name", user.fetchRealname());
            map.put("time", t.getMinuteSecond());
            recordMapList.add(map);
        });
        return recordMapList;
    }


    @RequestMapping(value = "send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendFlower() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        long studentId = getRequestLong("sid");
        if (studentId == 0)
            return MapMessage.errorMessage("没有学生id");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail.getClazzId() == null)
            return MapMessage.errorMessage("学生没有班级");
        String combineConditionId = getRequestString("condition_id");
        if (StringUtils.isBlank(combineConditionId))
            return MapMessage.errorMessage("参数错误");
        String[] split = combineConditionId.split("\\|");
        FlowerConditionType conditionType = FlowerConditionType.of(split[0]);
        if (conditionType == null)
            return MapMessage.errorMessage("错误的id");
        String conditionId  = split[1];
        FlowerCondition flowerCondition = FlowerCondition.parseFromId(conditionId);
        if (flowerCondition == null)
            return MapMessage.successMessage("鲜花条件id错误");
        flowerCondition.setStudentId(studentId);
        flowerCondition.setFlowerConditionType(conditionType);
        if (!DayRange.current().contains(flowerCondition.getCreateDate()))
            return MapMessage.errorMessage("老师今天没有哦，请刷新页面");
        try {
            return AtomicLockManager.getInstance().wrapAtomic(flowerServiceClient).keyPrefix("sendFlowerCondition")
                    .keys(conditionId).proxy().getFlowerConditionService().sendFlowerCondition(SchoolYear.newInstance().currentTermDateRange(), flowerCondition, studentDetail.getClazzId(), parent.getId());

        }catch (DuplicatedOperationException ex) {
            return MapMessage.successMessage("鲜花正在发放中哦~");
        }
    }
}
