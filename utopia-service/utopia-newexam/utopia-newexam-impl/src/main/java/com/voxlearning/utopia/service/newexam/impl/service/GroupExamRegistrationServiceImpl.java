package com.voxlearning.utopia.service.newexam.impl.service;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamPublishMessageType;
import com.voxlearning.utopia.service.newexam.api.entity.GroupExamRegistration;
import com.voxlearning.utopia.service.newexam.api.service.GroupExamRegistrationService;
import com.voxlearning.utopia.service.newexam.impl.pubsub.NewExamPublisher;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/28
 */
@Named
@ExposeService(interfaceClass = GroupExamRegistrationService.class)
public class GroupExamRegistrationServiceImpl extends NewExamSpringBean implements GroupExamRegistrationService {

    @Inject private NewExamPublisher newExamPublisher;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public MapMessage fetchExamRegistrationResult(NewExam newExam, Long teacherId) {
        Date currentDate = new Date();
        Map<String, NewPaper> newPaperMap = paperLoaderClient.loadNewPapersByDocIds0(newExam.obtainPaperIds());
        if (MapUtils.isEmpty(newPaperMap)) {
            logger.error("NewExam paper exception : newExamId {} ,teacherId {}", newExam.getId(), teacherId);
            return MapMessage.errorMessage("试卷信息不存在");
        }
        String questionVolume;
        if (newPaperMap.size() == 1) {
            NewPaper newPaper = newPaperMap.values().stream().findFirst().orElse(null);
            if (newPaper == null) {
                logger.error("NewExam paper exception : newExamId {} ,teacherId {}", newExam.getId(), teacherId);
                return MapMessage.errorMessage("试卷信息不存在");
            }
            questionVolume = StringUtils.join(newPaper.getQuestions().size(), "题");
        } else {
            List<String> paperQuestionVolume = newExam.obtainEmbedPapers().stream()
                    .map(p -> StringUtils.join(p.getPaperName(), newPaperMap.get(p.getPaperId()).getQuestions().size(), "题"))
                    .collect(Collectors.toList());
            questionVolume = StringUtils.join(paperQuestionVolume, "/");
        }

        // 班组报名状态
        List<Map<String, Object>> clazzGroupInfo = new ArrayList<>();
        if (teacherId != null) {
            List<Long> teacherGroupIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByTeacherId(teacherId)
                    .stream()
                    .map(GroupTeacherTuple::getGroupId)
                    .distinct()
                    .collect(Collectors.toList());
            List<String> groupExamRegistrationIds = teacherGroupIds.stream().map(cg -> GroupExamRegistration.generateId(newExam.getCreatedAt(), newExam.getId(), cg)).collect(Collectors.toList());
            Map<String, GroupExamRegistration> examRegistrationMap = groupExamRegistrationDao.loadsUncancelled(groupExamRegistrationIds);
            Set<Long> registerGroupIds = examRegistrationMap.values().stream().map(GroupExamRegistration::getClazzGroupId).collect(Collectors.toSet());

            Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(teacherGroupIds, Boolean.FALSE);
            Map<Long, GroupMapper> clazzGroupMap = groupMapperMap.values().stream().filter(gm -> gm.getSubject().equals(newExam.getSubject())).collect(Collectors.toMap(GroupMapper::getClazzId, Function.identity()));
            Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzs(clazzGroupMap.keySet())
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));

            clazzMap.values().stream().sorted(new Clazz.ClazzLevelAndNameComparator()).forEach(clazz -> {
                GroupMapper groupMapper = clazzGroupMap.get(clazz.getId());
                if (newExam.getClazzLevels().contains(clazz.getClazzLevel().getLevel()) && groupMapper != null) {
                    clazzGroupInfo.add(MapUtils.m("clazzGroupId", groupMapper.getId(), "clazzId", groupMapper.getClazzId(), "clazzName", clazz.formalizeClazzName(), "register", registerGroupIds.contains(groupMapper.getId())));
                }
            });
        }

        List<String> clazzLevelDescription = newExam.getClazzLevels().stream().map(cl -> ClazzLevel.parse(cl).getDescription()).collect(Collectors.toList());
        String pattern = "yyyy.MM.dd HH:mm";
        return MapMessage.successMessage().add("result", MapUtils.m(
                "examName", newExam.getName(),
                "subjectName", newExam.getSubject().getValue(),
                "clazzLevels", StringUtils.join(clazzLevelDescription, "/"),
                "questionVolume", questionVolume,
                "registerTime", StringUtils.join(DateUtils.dateToString(newExam.getApplyStartAt(), pattern), "-", DateUtils.dateToString(newExam.getApplyStopAt(), pattern)),
                "matchRegion", Boolean.TRUE,
                "canRegister", Boolean.TRUE,
                "publishedResults", currentDate.after(newExam.getResultIssueAt()),
                "applyStop", currentDate.after(newExam.getApplyStopAt()),
                "clazzGroupInfo", clazzGroupInfo));
    }

    @Override
    public MapMessage register(TeacherDetail teacherDetail, NewExam newExam, List<Long> groupIds) {
        Date currentDate = new Date();
        for (Long groupId : groupIds) {
            String groupExamRegistrationId = GroupExamRegistration.generateId(newExam.getCreatedAt(), newExam.getId(), groupId);
            GroupExamRegistration registration = new GroupExamRegistration();
            registration.setId(groupExamRegistrationId);
            registration.setBeenCanceled(Boolean.FALSE);
            registration.setClazzGroupId(groupId);
            registration.setNewExamId(newExam.getId());
            registration.setRegisterAt(currentDate);
            registration.setUpdateAt(currentDate);
            // 一个老师班级数量不多, 考录到取消报名重新报名. 循环upsert
            groupExamRegistrationDao.upsert(registration);
            //广播老师参加报名考试消息
            Map<String, Object> map = new HashMap<>();
            map.put("messageType", NewExamPublishMessageType.assignApply);
            map.put("newExamId", newExam.getId());
            map.put("groupId", groupId);
            map.put("teacherId", teacherDetail.getId());
            newExamPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }
        return MapMessage.successMessage("报名成功");
    }

    @Override
    public MapMessage unRegister(String newExamId, Long groupId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("当前考试不存在");
        }
        Date currentDate = new Date();
        String groupExamRegistrationId = GroupExamRegistration.generateId(newExam.getCreatedAt(), newExam.getId(), groupId);
        GroupExamRegistration registration = groupExamRegistrationDao.load(groupExamRegistrationId);
        if (registration == null) {
            return MapMessage.errorMessage("没有对应报名信息");
        }
        registration.setBeenCanceled(Boolean.TRUE);
        registration.setUpdateAt(currentDate);
        groupExamRegistrationDao.upsert(registration);
        return MapMessage.successMessage("操作成功");
    }

    @Override
    public MapMessage shareReport(TeacherDetail teacherDetail, String newExamId, Long groupId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("当前考试不存在");
        }
        //广播老师参加分享报告消息
        Map<String, Object> map = new HashMap<>();
        map.put("messageType", NewExamPublishMessageType.shareReport);
        map.put("newExamId", newExamId);
        map.put("groupId", groupId);
        map.put("teacherId", teacherDetail.getId());
        newExamPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        return MapMessage.successMessage("分享成功");
    }


}
