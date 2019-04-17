package com.voxlearning.utopia.agent.bean.school;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.agent.constants.ResearchersJobType;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResource;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResourceExtend;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description: 学校关键人物列表
 * @author: kaibo.he
 * @create: 2019-01-11 18:28
 **/
@Data
@Accessors(chain = true)
public class SchoolKeymanListResult {
    private List<KeyManInfo> schoolMasterList;
    private List<KeyManInfo> directorList;
    private List<KeyManInfo> groupLeaderList;
    private List<KeyManInfo> unregisteredTeacherList;
    private List<KeyManInfo> otherList;

    @Data
    @Accessors(chain = true)
    public static class KeyManInfo {
         private Long id;                               //人物id
         private String name;                           //人物名称
         private String subject;                        //学科
         private Boolean registered;                    //是否已注册
         private Long visitTime;                        //访问时间
         private String departmentName;                 //部门
         private String positionName;                   //职位名称
         private String phoneNum;                       //电话号码
    }

    public static class Builder{
        public static SchoolKeymanListResult build(Map<Long, AgentOuterResource> agentOuterResourceMap, List<AgentOuterResourceExtend> agentOuterResourceExtends) {
            SchoolKeymanListResult result = new SchoolKeymanListResult();
            if (CollectionUtils.isEmpty(agentOuterResourceExtends) || MapUtils.isEmpty(agentOuterResourceMap)) {
                return result;
            }
            List<KeyManInfo> schoolMasterList = new ArrayList<>();
            List<KeyManInfo> directorList = new ArrayList<>();
            List<KeyManInfo> groupLeaderList = new ArrayList<>();
            List<KeyManInfo> unregisteredTeacherList = new ArrayList<>();
            List<KeyManInfo> otherList = new ArrayList<>();
            result.setDirectorList(directorList)
                    .setGroupLeaderList(groupLeaderList)
                    .setSchoolMasterList(schoolMasterList)
                    .setUnregisteredTeacherList(unregisteredTeacherList)
            .setOtherList(otherList);

            agentOuterResourceExtends.stream()
                    .filter(extend -> Objects.nonNull(extend.getResourceId()))
                    .filter(extend -> agentOuterResourceMap.containsKey(extend.getResourceId()))
                    .forEach(extend -> {
                        AgentOuterResource resource = agentOuterResourceMap.get(extend.getResourceId());
                        ResearchersJobType resourceJobType = ResearchersJobType.typeOf(extend.getJob());
                        if (Objects.nonNull(resourceJobType)) {
                            KeyManInfo keyManInfo = new KeyManInfo();
                            keyManInfo
                                    .setId(resource.getId())
                                    .setName(resource.getName())
                                    .setDepartmentName(extend.getDepartment())
                                    .setPhoneNum(resource.getPhone())
                                    .setPositionName(resourceJobType.getJobName())
                                    .setRegistered(true)
                                    .setSubject(Objects.isNull(extend.getSubject()) ? "":extend.getSubject().getValue())
                                    .setVisitTime(Objects.isNull(extend.getVisitTime()) ? 0:extend.getVisitTime().getTime());
                            switch (resourceJobType) {
                                //校长、副校长岗位
                                case HEADMASTER:
                                case DEPUTY_HEADMASTER:
                                    schoolMasterList.add(keyManInfo);
                                    break;
                                //主任、副主任岗位
                                case ACADEMIC_DIRECTOR:
                                case TEACHING_DIRECTOR:
                                    directorList.add(keyManInfo);
                                    break;
                                //各种组长岗位
                                case SUBJECT_LEADER:
                                case TEAM_LEADER:
                                case GRADE_LEADER:
                                case LESSON_PREPARATION_LEADER:
                                    groupLeaderList.add(keyManInfo);
                                    break;
                                //未注册老师
                                case UNREGISTERED_TEACHER:
                                    keyManInfo.setRegistered(extend.getRegistrationStatus());
                                    unregisteredTeacherList.add(keyManInfo);
                                    break;
                                //默认其他
                                default:
                                    otherList.add(keyManInfo);
                                    break;
                            }
                        }
                    });
            return result;
        }
    }
}
