package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.flower.api.entities.FlowerRankMember;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-07-26 下午5:05
 **/

@Controller
@RequestMapping(value = "/usermobile/flower")
@Slf4j
public class MobileUserFlowerController extends AbstractMobileController {


    @Inject
    private FlowerServiceClient flowerServiceClient;

    @Inject
    private ClazzIntegralServiceClient clazzIntegralServiceClient;

    /**
     * 班级鲜花排行榜
     * 家长短，老师端用
     */
    @RequestMapping(value = "/term_rank.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage integralHistory() {
        User user = currentUser();
        if (user == null)
            return noLoginResult;

        if (!user.isTeacher() && !user.isParent())
            return noLoginResult;
        Long groupId = getRequestLong("group_id");
        if (groupId == 0 )
            return MapMessage.errorMessage("班组id错误");
        GroupMapper groupMapper = deprecatedGroupLoaderClient.loadGroup(groupId, true);
        if (groupMapper == null)
            return MapMessage.errorMessage("班组id错误");
        Long teacherId;
        if (user.isTeacher()){
            Long relTeacherIdBySubject = teacherLoaderClient.loadRelTeacherIdBySubject(user.getId(), groupMapper.getSubject());
            if (relTeacherIdBySubject == null)
                return MapMessage.errorMessage("您无此权限");
            teacherId = user.getId();
        }else if (user.isParent()){
            long studentId = getRequestLong("sid");
            if (studentId == 0)
                return MapMessage.errorMessage("没有学生 id");
            List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
            if (groupMappers.stream().filter( t -> t.getId().equals(groupId)).count() == 0 )
                return MapMessage.errorMessage("您无此权限");
            Teacher teacher = teacherLoaderClient.loadGroupSingleTeacher(Collections.singleton(groupId)).get(groupId);
            if (teacher == null)
                return MapMessage.errorMessage("班级没有老师");
            teacherId = teacher.getId();
        }else
            return noLoginResult;
        AlpsFuture<SmartClazzIntegralPool> smartClazzIntegralPoolAlpsFuture = clazzIntegralServiceClient.getClazzIntegralService().loadClazzIntegralPool(groupId);
        AlpsFuture<List<FlowerRankMember>> listAlpsFuture = flowerServiceClient.getFlowerConditionService().loadFlowerTermRank(SchoolYear.newInstance().currentTermDateRange(), groupId, teacherId);
        Map<Long, GroupMapper.GroupUser> groupUserMap = groupMapper.getStudents().stream().collect(Collectors.toMap(GroupMapper.GroupUser::getId, Function.identity()));
        List<FlowerRankMember> flowerRankMembers = listAlpsFuture.getUninterruptibly();
        List<FlowerRankMember> flowerRankList = new ArrayList<>();
        Set<Long> inRankSidSet = new HashSet<>();
        flowerRankMembers.forEach(t -> {
            inRankSidSet.add(t.getStudentId());
            GroupMapper.GroupUser groupUser = groupUserMap.get(t.getStudentId());
            if (groupUser == null)
                return;
            t.setStudentName(groupUser.getName() + "的家长");
            flowerRankList.add(t);
        });
        groupUserMap.keySet().stream().filter( t -> !inRankSidSet.contains(t)).sorted().forEach(sid -> {
            GroupMapper.GroupUser groupUser = groupUserMap.get(sid);
            if (groupUser == null)
                return;
            FlowerRankMember flowerRankMember = new FlowerRankMember();
            flowerRankMember.setStudentId(sid);
            flowerRankMember.setStudentName(groupUser.getName() + "的家长");
            flowerRankMember.setFlowerCount(0L);
            flowerRankList.add(flowerRankMember);
        });
        Long clazzId = groupMapper.getClazzId();
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadClazz(clazzId);
        if (clazz == null)
            return MapMessage.errorMessage();

        return MapMessage.successMessage().add("clazz_integral_count", smartClazzIntegralPoolAlpsFuture.getUninterruptibly().fetchTotalIntegral())
                .add("flower_rank_list", flowerRankList).add("clazz_name", clazz.formalizeClazzName());
    }
}
