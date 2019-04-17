package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.EventUserType;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.AccountType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.impl.service.VendorAppsServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.service.VendorServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 该监听器负责所有daite的消息通知事件
 * <p>
 * Created by zhouwei on 2018/8/7
 **/
@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.school.update.queue"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.user.basic.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.user.student.parent.ref.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.user.parent.student.ref.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.group.teacher.ref.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.group.student.ref.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.clazz.topic"),
        },
        maxPermits = 1
)
public class DaiteQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private VendorAppsServiceImpl vendorAppsServiceImpl;
    @Inject private VendorServiceImpl vendorServiceImpl;

    private static final String appKey = "Daite";

    private String secretKey = "";

    private Map<String, String> eventTypes = new HashMap<>();

    public void afterPropertiesSet() throws Exception {
        List<VendorApps> apps = vendorAppsServiceImpl.loadAllVendorAppsFromDB().get();
        for (VendorApps app : apps) {
            if (Objects.equals(app.getAppKey(), appKey)) {
                this.secretKey = app.getSecretKey();
                break;
            }
        }
        /**
         * 只设置Daite关心的事件类型
         */
        eventTypes.put(EventUserType.SCHOOL_UPDATED.getEventType(), "school_updated");
        eventTypes.put(EventUserType.CLAZZ_UPDATED.getEventType(), "clazz_updated");
        eventTypes.put(EventUserType.USER_BASIC_INFO_UPDATED.getEventType(), "user_basic_info_updated");
        eventTypes.put(EventUserType.TEACHER_CLAZZ_UPDATED.getEventType(), "teacher_clazz_updated");
        eventTypes.put(EventUserType.STUDENT_CLAZZ_UPDATED.getEventType(), "student_clazz_updated");
        eventTypes.put(EventUserType.CHILDREN_UPDATED.getEventType(), "children_updated");
        eventTypes.put(EventUserType.PARENT_UPDATED.getEventType(), "parent_updated");
        eventTypes.put(EventUserType.TEACHER_CLAZZ_INSERT.getEventType(), "teacher_clazz_updated");
    }

    @Override
    public void onMessage(Message message) {
        String url = "http://apitest.iclass30.com/api/sync/zydata";
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            url = "http://api.iclass30.com/api/sync/zydata";
        } else if (RuntimeMode.isTest()) {
            url = "http://apitest.iclass30.com/api/sync/zydata";
        }
        String body = new String(message.getBody());
        if (StringUtils.isEmpty(body)) {
            return;
        }
        Map<String, Object> result = JsonUtils.fromJson(body);
        Long eventId = SafeConverter.toLong(result.get("event_id"));
        String eventType = SafeConverter.toString(result.get("event_type"));

        if (!eventTypes.keySet().contains(eventType)) {//过滤不关心的事件
            return;
        }

        boolean isDaite = false;

        if (Objects.equals(EventUserType.SCHOOL_UPDATED.getEventType(), eventType)) { //判断学校是否与戴特有关
            isDaite = schoolExtServiceClient.isDaiteSchool(eventId);
        } else if (Objects.equals(EventUserType.CLAZZ_UPDATED.getEventType(), eventType)) { //判断班级是否与戴特有关
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(eventId);
            if (null != clazz) {
                isDaite = schoolExtServiceClient.isDaiteSchool(clazz.getSchoolId());
            }
        } else if (Objects.equals(EventUserType.USER_BASIC_INFO_UPDATED.getEventType(), eventType)
                || Objects.equals(EventUserType.TEACHER_CLAZZ_UPDATED.getEventType(), eventType)
                || Objects.equals(EventUserType.TEACHER_CLAZZ_INSERT.getEventType(), eventType)
                || Objects.equals(EventUserType.STUDENT_CLAZZ_UPDATED.getEventType(), eventType)
                || Objects.equals(EventUserType.CHILDREN_UPDATED.getEventType(), eventType)
                || Objects.equals(EventUserType.PARENT_UPDATED.getEventType(), eventType)) { //判断用户相关ID是否与戴特有关

            Set<Long> evictUserIds = new HashSet<>();

            User user = raikouSystem.loadUser(eventId);
            isDaite = this.isDaiteUser(user, evictUserIds);

            if (!evictUserIds.isEmpty()) {
                evictUserIds.forEach(e -> {
                    raikouSystem.getCacheService().evictUserCache(e);
                    UserCache.getUserCache().delete(StudentExtAttribute.ck_id(e));
                });


            }
        }

        if (isDaite) {//戴特的合作校
            this.addEvent(eventId, eventTypes.get(eventType), url);
        }
    }

    /**
     * 添加一条事件
     *
     * @param eventId
     * @param eventType
     * @param url
     * @author zhouwei
     */
    public void addEvent(Long eventId, String eventType, String url) {
        Map<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);
        params.put("event_type", eventType);
        params.put("timestamp", new Date().getTime());
        params.put("app_key", appKey);
        params.put("sig", this.generateRequestSig(params));
        vendorServiceImpl.sendHttpNotify(appKey, url, params);
    }

    /**
     * 计算对方所需要的sig信息
     *
     * @param paramsMap
     * @return
     * @author zhouwei
     */
    public String generateRequestSig(Map<String, Object> paramsMap) {
        Map<String, String> sigParams = paramsMap.entrySet().stream().collect(Collectors.toMap((p -> p.getKey()), (p -> SafeConverter.toString(p.getValue()))));
        return DigestSignUtils.signMd5(sigParams, this.secretKey);
    }


    /**
     * 判断用户是否是合作校
     *
     * @param user
     * @return
     * @author zhouwei
     */
    private boolean isDaiteUser(User user, Set<Long> evictUserIds) {
        if (null == user) {
            return false;
        }

        if (user.isStudent()) {// 如果是学生
            evictUserIds.add(user.getId());
            StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(user.getId());
            if (studentExtAttribute != null && studentExtAttribute.getAccountType() == AccountType.VIRTUAL) {//如果是体验账号，不发送消息
                return false;
            }

            // 通过班组查询学生的学校信息
            evictUserIds.add(user.getId());
            List<GroupStudentTuple> tuples = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .findByStudentId(user.getId());
            if (CollectionUtils.isEmpty(tuples)) {
                return false;
            }

            for (GroupStudentTuple tuple : tuples) {
                Long gid = tuple.getGroupId();
                Group group = raikouSystem.loadGroup(gid);
                if (group == null) {
                    continue;
                }

                Long cid = group.getClazzId();
                Clazz clazz = raikouSystem.loadClazz(cid);
                if (clazz == null) {
                    continue;
                }

                if (schoolExtServiceClient.isDaiteSchool(clazz.getSchoolId())) {
                    return true;
                }
            }

        } else if (user.isTeacher()) {//如果是老师
            evictUserIds.add(user.getId());
            List<UserSchoolRef> userSchoolRefs = schoolLoaderClient.getSchoolLoader().findUserSchoolRefsByUserId(user.getId()).getUninterruptibly();
            if (CollectionUtils.isEmpty(userSchoolRefs)) {
                return false;
            }
            Long schoolId = userSchoolRefs.get(0).getSchoolId();
            return schoolExtServiceClient.isDaiteSchool(schoolId);
        } else if (user.isParent()) {//如果是家长
            evictUserIds.add(user.getId());
            List<StudentParentRef> spRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
            if (CollectionUtils.isEmpty(spRefs)) {
                return false;
            }

            for (StudentParentRef sp : spRefs) {
                Long sid = sp.getStudentId();

                StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(sid);
                if (studentExtAttribute != null && studentExtAttribute.getAccountType() == AccountType.VIRTUAL) {//如果是体验账号，不发送消息
                    continue;
                }

                // 通过班组查询学生的学校信息
                evictUserIds.add(sid);
                List<GroupStudentTuple> tuples = raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .findByStudentId(sid);
                if (CollectionUtils.isEmpty(tuples)) {
                    continue;
                }

                for (GroupStudentTuple tuple : tuples) {
                    Long gid = tuple.getGroupId();
                    Group group = raikouSystem.loadGroup(gid);
                    if (group == null) {
                        continue;
                    }

                    Long cid = group.getClazzId();
                    Clazz clazz = raikouSystem.loadClazz(cid);
                    if (clazz == null) {
                        continue;
                    }

                    if (schoolExtServiceClient.isDaiteSchool(clazz.getSchoolId())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
