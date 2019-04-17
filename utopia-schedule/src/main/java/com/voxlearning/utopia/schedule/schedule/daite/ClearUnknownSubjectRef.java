package com.voxlearning.utopia.schedule.schedule.daite;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 戴特因为学生可能先进系统，所以前期会虚拟一个老师账号来绑定学生关系
 * <p>
 * 在正式老师进来后，需要删除之前这个虚拟老师的班组关系
 * <p>
 * 虚拟老师的学科为:UNKNOWN
 * <p>
 * Created by zhouwei on 2018/8/20
 **/
@Named
@ScheduledJobDefinition(
        jobName = "清理戴特的垃圾班组关系",
        jobDescription = "清理戴特的垃圾班组关系",
        disabled = {Mode.UNIT_TEST, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "10 10 3 * * ?",
        ENABLED = true
)
public class ClearUnknownSubjectRef extends ScheduledJobWithJournalSupport {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<SchoolExtInfo> schoolExtInfos = schoolExtServiceClient.getSchoolExtService().loadAllDaiteSchool().getUninterruptibly();
        for (SchoolExtInfo schoolExtInfo : schoolExtInfos) {
            try {
                Long schoolId = schoolExtInfo.getId();
                List<Clazz> clazzes = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadSchoolClazzs(schoolId)
                        .toList();
                if (CollectionUtils.isEmpty(clazzes)) {
                    continue;
                }
                List<Long> clazzIds = clazzes.stream().map(Clazz::getId).collect(Collectors.toList());
                Map<Long, List<Group>> clazzGroupsMap = raikouSDK.getClazzClient()
                        .getGroupLoaderClient()
                        .groupByClazzIds(clazzIds);
                for (List<Group> groups : clazzGroupsMap.values()) {
                    if (CollectionUtils.isEmpty(groups)) {
                        continue;
                    }
                    List<Group> groupsNotUnknown = groups.stream().filter(group -> !Objects.equals(group.getSubject(), Subject.UNKNOWN)).collect(Collectors.toList());
                    List<Group> groupsUnknown = groups.stream().filter(group -> Objects.equals(group.getSubject(), Subject.UNKNOWN)).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(groupsUnknown) || CollectionUtils.isEmpty(groupsNotUnknown)) {//没有unknown学科或者非unknown学科，则不处理这个班的组
                        continue;
                    }

                    //非Unknown学科的学生与班组关系
                    List<Long> groupsNotUnknownIds = groupsNotUnknown.stream().map(Group::getId).collect(Collectors.toList());
                    List<Long> studentIdsHasSubject = raikouSDK.getClazzClient()
                            .getGroupStudentTupleServiceClient()
                            .getGroupStudentTupleService()
                            .dbFindByGroupIdsIncludeDisabled(groupsNotUnknownIds)
                            .getUninterruptibly()
                            .stream()
                            .filter(e -> !e.isDisabledTrue())
                            .map(GroupStudentTuple::getStudentId)
                            .distinct()
                            .collect(Collectors.toList());

                    if (CollectionUtils.isEmpty(studentIdsHasSubject)) {
                        continue;
                    }

                    //Unknown学科的学生与班组关系
                    List<Long> groupsUnknownIds = groupsUnknown.stream().map(Group::getId).collect(Collectors.toList());
                    Map<Long, List<GroupStudentTuple>> studentRefMapsUnknown = raikouSDK.getClazzClient()
                            .getGroupStudentTupleServiceClient()
                            .getGroupStudentTupleService()
                            .dbFindByGroupIdsIncludeDisabled(groupsUnknownIds)
                            .getUninterruptibly()
                            .stream()
                            .filter(e -> !e.isDisabledTrue())
                            .collect(Collectors.groupingBy(GroupStudentTuple::getGroupId));

                    for (Long groupId : studentRefMapsUnknown.keySet()) {
                        List<GroupStudentTuple> refs = studentRefMapsUnknown.get(groupId);
                        for (GroupStudentTuple ref : refs) {
                            if (studentIdsHasSubject.contains(ref.getStudentId())) {//如果该学生已经有其他学科则删除Unknow的关系组3
                                asyncGroupServiceClient.getAsyncGroupService().disableGroupStudentRef(ref.getStudentId(), ref.getGroupId()).get();
                            } else {
                                continue;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
