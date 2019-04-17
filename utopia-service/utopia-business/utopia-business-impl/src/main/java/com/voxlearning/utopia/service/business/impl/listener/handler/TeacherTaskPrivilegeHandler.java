package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilege;
import com.voxlearning.utopia.service.business.impl.service.TeacherTaskPrivilegeServiceImpl;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
 *
 * 老师等级发生变化后的事件处理
 *
 * @author zhouwei
 */
@Named
@Slf4j
public class TeacherTaskPrivilegeHandler {

    @Inject
    private TeacherTaskPrivilegeServiceImpl teacherTaskPrivilegeService;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    private static final String TEACHER_NEW_CHANGE_LEVEL_PREFIX_APP = "teacher_new_change_level_app_";

    private static final String TEACHER_NEW_CHANGE_LEVEL_PREFIX_PC = "teacher_new_change_level_pc_";

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    public void handler(Message message) {
        Map<String, Object> msgMap;
        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            log.warn("TeacherTaskPrivilegeHandler message decode failed!", JsonUtils.toJson(message.decodeBody()));
            return;
        }
        this.handlerMessage(msgMap);
    }

    /**
     * 处理与老师任务成长体系相关的事件
     * @param msg
     */
    private void handlerMessage(Map<String, Object> msg) {
        Long teacherId = MapUtils.getLong(msg, "teacherId");
        // 1 升级 -1 降级 0 保级
        Integer change = MapUtils.getInteger(msg, "change");
        Integer oldLevel = MapUtils.getInteger(msg, "oldLevel");
        if (teacherId == null || change == null) {
            printLog(msg, teacherId, change);
            return;
        }
        if (oldLevel == null || 0 == oldLevel) {//初始化的事件就不在发消息了
            printLog(msg, teacherId, change);
        }

        if (0 == change || -1 == change || 1 == change) {
            MapMessage initMessage = teacherTaskPrivilegeService.initPrivilegeAndGet(teacherId);
            if (!initMessage.isSuccess()) {
                return;
            }

            TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege)initMessage.get("teacherTaskPrivilege");
            if (null == teacherTaskPrivilege) {
                return;
            }

            Integer level = teacherTaskPrivilege.getLevel();
            TeacherExtAttribute.NewLevel newLevelByLevel = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level);
            if (null == newLevelByLevel) {
                return;
            }

            if (oldLevel == null || 0 == oldLevel) {//初始化的事件就不在发消息了
                return;
            }

            CacheSystem.CBS.getCache("persistence").set(TEACHER_NEW_CHANGE_LEVEL_PREFIX_APP + teacherId, 30 * 24 * 60 * 60,  change);
            if (0 == change) {
                pushAndMessge(teacherId, "恭喜您！" + newLevelByLevel.getValue() + "保级成功，再接再厉哦~");
            } else if (-1 == change) {
                pushAndMessge(teacherId, "很遗憾！您的等级变更为" + newLevelByLevel.getValue() + "，快快做任务赚积分升等级吧！");
            } else if (1 == change) {
                CacheSystem.CBS.getCache("persistence").set("teacher_app_personal_uplevel_" + teacherId, 30 * 24 * 60 * 60,  true);//老师个人中心闪光动画
                pushAndMessge(teacherId, "恭喜您！成功升级为" + newLevelByLevel.getValue() + "，升等级赢特权哦~");
            }

        }
    }

    /**
     * PUSH与消息
     * @param teacherId
     */
    public void pushAndMessge(Long teacherId, String msgContent) {
        String msgTitle = "等级变更通知";

        //发送App系统消息
        AppMessage msg = new AppMessage();
        msg.setUserId(teacherId);
        msg.setMessageType(TeacherMessageType.ACTIVIY.getType());
        msg.setContent(msgContent);
        msg.setTitle(msgTitle);
        msg.setCreateTime(new Date().getTime());
        msg.setLinkType(1);
        msg.setLinkUrl("/view/mobile/teacher/activity2018/primary/task_system/index");
        msg.setIsTop(false);
        messageCommandServiceClient.getMessageCommandService().createAppMessage(msg);

        //发送PC系统消息
        teacherLoaderClient.sendTeacherMessage(teacherId, msgContent);

    }

    private void printLog(Map<String, Object> msg, Long teacherId, Integer change) {
        try {
            //打印一下日志
            Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "teacherId", teacherId, "op", "teacher_task_privilege", "messageType", change);
            logMap.put("messageInfo", JsonUtils.toJson(msg));
            LogCollector.info("backend-general", logMap);
        } catch (Throwable t){
        }
    }
}