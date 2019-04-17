package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.agent.dao.mongo.parent.AgentNewRegisterParentDao;
import com.voxlearning.utopia.agent.persist.entity.parent.AgentSchoolNewRegisterParent;
import com.voxlearning.utopia.api.constant.EventUserType;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.AccountType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学校新注册家长
 * @author deliang.che
 * @since 2019/4/1
 */
@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.user.student.parent.ref.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.user.student.parent.ref.topic")
        }
)
public class AgentSchoolNewRegisterParentListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private RaikouSDK raikouSDK;
    @Inject
    private GroupLoaderClient groupLoaderClient;
    @Inject
    private AgentNewRegisterParentDao agentNewRegisterParentDao;

    @Override
    public void onMessage(Message message) {
        String body = new String(message.getBody());
        if (StringUtils.isEmpty(body)) {
            return;
        }
        Map<String, Object> result = JsonUtils.fromJson(body);
        Long eventId = SafeConverter.toLong(result.get("event_id"));
        String eventType = SafeConverter.toString(result.get("event_type"));
        if (Objects.equals(EventUserType.CHILDREN_UPDATED.getEventType(), eventType)
                || Objects.equals(EventUserType.PARENT_UPDATED.getEventType(), eventType)) {

            User user = userLoaderClient.loadUser(eventId);
            //如果是当天注册的家长
            if (user != null && user.isParent() && DateUtils.isSameDay(user.getCreateTime(),new Date())) {
                addNewRegisterParent(user.getId(),user.getCreateTime());
            }
            if (user != null && user.isStudent()){
                List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(user.getId());
                Set<Long> parentIds = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet());
                Map<Long, User> userMap = userLoaderClient.loadUsers(parentIds);
                parentIds.forEach(parentId -> {
                    User parent = userMap.get(parentId);
                    if (parent != null){
                        addNewRegisterParent(parent.getId(),parent.getCreateTime());
                    }
                });
            }
        }
    }

    public void addNewRegisterParent(Long parentId,Date createTime){
        Set<Long> schoolIds = getParentSchoolIds(parentId);
        if (CollectionUtils.isNotEmpty(schoolIds)){
            List<AgentSchoolNewRegisterParent> newRegisterParentList = new ArrayList<>();
            Map<Long, List<AgentSchoolNewRegisterParent>> schoolParentMap = agentNewRegisterParentDao.loadBySchoolIds(schoolIds);
            schoolIds.forEach(schoolId -> {
                AgentSchoolNewRegisterParent newRegisterParent = new AgentSchoolNewRegisterParent();
                newRegisterParent.setSchoolId(schoolId);
                newRegisterParent.setParentId(parentId);
                newRegisterParent.setRegisterTime(createTime);
                List<AgentSchoolNewRegisterParent> parentList = schoolParentMap.get(schoolId);
                if (CollectionUtils.isEmpty(parentList)){
                    newRegisterParentList.add(newRegisterParent);
                }else {
                    parentList = parentList.stream().filter(p -> Objects.equals(p.getParentId(), parentId)).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(parentList)){
                        newRegisterParentList.add(newRegisterParent);
                    }
                }
            });
            if (CollectionUtils.isNotEmpty(newRegisterParentList)){
                agentNewRegisterParentDao.inserts(newRegisterParentList);
            }
        }
    }

    /**
     * 获取家长对应的学校
     * @param userId
     * @return
     */
    public Set<Long> getParentSchoolIds(Long userId){
        Set<Long> schoolIds = new HashSet<>();

        List<StudentParentRef> spRefs = parentLoaderClient.loadParentStudentRefs(userId);
        if (CollectionUtils.isEmpty(spRefs)){
            return schoolIds;
        }
        for (StudentParentRef sp : spRefs) {
            Long sid = sp.getStudentId();

            StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(sid);
            if (studentExtAttribute != null && studentExtAttribute.getAccountType() == AccountType.VIRTUAL) {//如果是体验账号，不发送消息
                continue;
            }

            // 通过班组查询学生的学校信息
            List<GroupStudentTuple> tuples = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .findByStudentId(sid);
            if (CollectionUtils.isEmpty(tuples)) {
                continue;
            }

            for (GroupStudentTuple tuple : tuples) {
                Long gid = tuple.getGroupId();
                Group group = groupLoaderClient.getGroupLoader().loadGroup(gid).getUninterruptibly();
                if (group == null || group.isDisabledTrue()) {
                    continue;
                }

                Long cid = group.getClazzId();
                Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(cid);
                if (clazz == null || clazz.isDisabledTrue()) {
                    continue;
                }
                Long schoolId = clazz.getSchoolId();
                if (schoolId != null){
                    schoolIds.add(schoolId);
                }
            }
        }
        return schoolIds;
    }
}
