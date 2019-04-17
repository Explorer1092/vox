package com.voxlearning.utopia.schedule.schedule.daite;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.EventUserType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.AccountType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.VendorAppsServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;
import jdk.nashorn.internal.runtime.logging.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 临时的一次性作业
 * <p>
 * Created by zhouwei on 2018/8/6
 **/
@Named
@ScheduledJobDefinition(
        jobName = "初始化戴特合作校事件",
        jobDescription = "初始化戴特合作校事件",
        disabled = {Mode.UNIT_TEST, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "",//todo 需要更改作业的执行时间
        ENABLED = false
)
@Logger
@ProgressTotalWork(100)
public class InitEventJob extends ScheduledJobWithJournalSupport implements InitializingBean {

    @Inject
    private VendorServiceClient vendorServiceClient;

    @Inject
    private GroupLoaderClient groupLoaderClient;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private VendorAppsServiceClient vendorAppsServiceClient;

    @Inject private RaikouSDK raikouSDK;

    private static final String appKey = "Daite";

    private String secretKey = "";

    public void afterPropertiesSet() throws Exception {
        List<VendorApps> apps = vendorAppsServiceClient.getVendorAppsService().loadAllVendorAppsFromDB().get();
        for (VendorApps app : apps) {
            if (Objects.equals(app.getAppKey(), appKey)) {
                this.secretKey = app.getSecretKey();
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        if (null == parameters.get("school_ids")) {
            logger.error("please set school ids");
            return;
        }
        List<Long> schoolIds = new ArrayList<>();
        ((List) parameters.get("school_ids")).forEach(e -> {
            long l = SafeConverter.toLong(e);
            if (l > 0) {
                schoolIds.add(l);
            }
        });
        logger.info("合作校ID：{}", schoolIds);
        Map<Long, Set<Clazz.Location>> clazzInfos = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .queryClazzLocations(schoolIds);
        logger.info("学校的班级信息:{}", clazzInfos);
        progressMonitor.worked(1);

        String url = "http://apitest.iclass30.com/api/sync/zydata";
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            url = "http://api.iclass30.com/api/sync/zydata";
        } else if (RuntimeMode.isTest()) {
            url = "http://apitest.iclass30.com/api/sync/zydata";
        }
        ISimpleProgressMonitor pm = progressMonitor.subTask(99, clazzInfos.size());
        try {
            for (Map.Entry<Long, Set<Clazz.Location>> entry : clazzInfos.entrySet()) {
                try {
                    List<Long> clazzIdsAll = entry.getValue().stream().map(Clazz.Location::getId).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(clazzIdsAll)) {//如果学校下面没有班级，则过滤掉
                        continue;
                    }

                    //过滤掉毕业班
                    Map<Long, Clazz> clazzMaps = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazzs(clazzIdsAll)
                            .stream()
                            .collect(Collectors.toMap(Clazz::getId, Function.identity()));
                    List<Long> clazzIds = clazzMaps.values().stream().filter(c -> !c.isTerminalClazz()).map(Clazz::getId).collect(Collectors.toList());

                    Map<Long, List<Group>> groups = groupLoaderClient.getGroupLoader().loadGroupsByClazzIds(clazzIds).get();//获取所有的组ID
                    logger.info("班级的组信息:{}", groups);
                    List<Long> groupIds = groups.values().stream().collect(ArrayList::new, ((t, u) -> {
                        for (Group g : u) {
                            t.add(g.getId());
                        }
                    }), ((t, u) -> {
                        t.addAll(u);
                    }));
                    Map<Long, List<User>> students = studentLoaderClient.loadGroupStudents(groupIds);//获取所有学生ID
                    logger.info("组关联的学生信息:{}", students);
                    Map<Long, List<Teacher>> teachers = teacherLoaderClient.loadGroupTeacher(groupIds);//获取所有老师ID
                    logger.info("组关联的老师信息:{}", teachers);

                    /**
                     * 开始插入事件信息
                     */
                    this.addEvent(SafeConverter.toLong(entry.getKey()), "school_updated", url);//学校线下同步
                    for (Long classId : clazzIds) {
                        try {
                            this.addEvent(classId, EventUserType.CLAZZ_UPDATED.getEventType(), url);//班级事件
                        } catch (Exception e) {
                            logger.error("add clazz event failed.", e);
                        }
                    }
                    for (List<User> studentInfos : students.values()) {
                        for (User user : studentInfos) {
                            try {
                                if (user.getUserType() == UserType.STUDENT.getType()) {//如果是体验账号，不发送消息
                                    StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(user.getId());
                                    if (studentExtAttribute != null && studentExtAttribute.getAccountType() == AccountType.VIRTUAL) {
                                        continue;
                                    }
                                }
                                this.addEvent(user.getId(), EventUserType.USER_BASIC_INFO_UPDATED.getEventType(), url);//学生基本信息事件
                                this.addEvent(user.getId(), EventUserType.STUDENT_CLAZZ_UPDATED.getEventType(), url);//学生班组关系事件
                                this.addEvent(user.getId(), EventUserType.CHILDREN_UPDATED.getEventType(), url);//学生与家长关系事件
                            } catch (Exception e) {
                                logger.error("add student event failed.", e);
                            }
                        }
                    }
                    for (List<Teacher> teachersInfos : teachers.values()) {
                        for (Teacher teacher : teachersInfos) {
                            try {
                                this.addEvent(teacher.getId(), EventUserType.USER_BASIC_INFO_UPDATED.getEventType(), url);//老师基本信息事件
                                this.addEvent(teacher.getId(), EventUserType.TEACHER_CLAZZ_UPDATED.getEventType(), url);//老师班组关系事件
                            } catch (Exception e) {
                                logger.error("add teacher event failed.", e);
                            }
                        }
                    }
                } finally {
                    pm.worked(1);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        progressMonitor.done();
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
        //logger.info("add event, {}, {}, {}", eventId, eventType, url);
        Map<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);
        params.put("event_type", eventType);
        params.put("timestamp", new Date().getTime());
        params.put("app_key", appKey);
        params.put("sig", this.generateRequestSig(params));
        //logger.info("插入事件：{}", params);
        vendorServiceClient.sendHttpNotify(appKey, url, params);
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
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

}
