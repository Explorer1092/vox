package com.voxlearning.utopia.agent.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * AgentTeacherSupport
 *
 * @author deliang.che
 * @since  2019/3/21
 */
@Named
public class AgentTeacherSupport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    /**
     * 分批获取老师信息
     *
     * @param teacherIds
     * @return
     */
    private Map<Long, CrmTeacherSummary> batchLoadTeacherSummary(Collection<Long> teacherIds) {
        Map<Long, CrmTeacherSummary> crmTeacherSummaryMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(teacherIds)) {
            AgentResourceService.batchIds(teacherIds, 1000).forEach((k, v) -> {
                crmTeacherSummaryMap.putAll(crmSummaryLoaderClient.loadTeacherSummary(v));
            });
        }
        return crmTeacherSummaryMap;
    }

    private Map<Long, Teacher> batchLoadTeacher(Collection<Long> teacherIds){
        Map<Long, Teacher> teacherMapTemp = new HashMap<>();
        if (CollectionUtils.isNotEmpty(teacherIds)){
            AgentResourceService.batchIds(teacherIds,1000).forEach((k,v) -> {
                if (CollectionUtils.isNotEmpty(v)){
                    Map<Long, Teacher> teacherMap = teacherLoaderClient
                            .loadTeachers(v);
                    teacherMapTemp.putAll(teacherMap);
                }
            });
        }
        return teacherMapTemp;
    }

    /**
     * 分批查询，优先从CrmTeacherSummary中查询，如果CrmTeacherSummary查不到，则从Teacher查询，但结果都封装成CrmTeacherSummary对象
     * 需要注意的是，只填充了部分字段
     * @param teacherIds
     * @return
     */
    public Map<Long, CrmTeacherSummary> batchLoadCrmTeacherSummaryAndTeacher(Collection<Long> teacherIds) {
        if (CollectionUtils.isNotEmpty(teacherIds)) {
            List<Long> copyTeacherIds = new ArrayList<>(teacherIds);
            Map<Long, CrmTeacherSummary> crmTeacherSummaryMap = batchLoadTeacherSummary(copyTeacherIds);
            //移除summary数据存在的
            copyTeacherIds.removeAll(crmTeacherSummaryMap.keySet());
            Map<Long, Teacher> teacherMap = batchLoadTeacher(copyTeacherIds);
            if (MapUtils.isNotEmpty(teacherMap)){
                teacherMap.forEach((k,v) -> crmTeacherSummaryMap.put(k, toCrmTeacherSummary(v)));
            }
            return crmTeacherSummaryMap;
        }
        return new HashMap<>();
    }

    /**
     * 只封装了部分字段
     * @param teacher
     * @return
     */
    private CrmTeacherSummary toCrmTeacherSummary(Teacher teacher){
        if (teacher != null){
            CrmTeacherSummary crmTeacherSummary = new CrmTeacherSummary();
            crmTeacherSummary.setTeacherId(teacher.getId());
            crmTeacherSummary.setRealName(teacher.fetchRealname());
            return crmTeacherSummary;
        }
        return null;
    }


}
