package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskDao;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskProgressDao;
import com.voxlearning.utopia.service.business.impl.loader.TeacherTaskLoaderImpl;
import com.voxlearning.utopia.service.business.impl.service.TeacherTaskServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * 用户的学段发生变化，处理用户的任务信息
 *
 * Created by zhouwei on 2018/9/26
 **/
@Named
@Slf4j
public class TeacherTaskKtwelveMsgHandler {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private TeacherTaskLoaderImpl teacherTaskLoader;

    @Inject
    private TeacherTaskDao teacherTaskDao;

    @Inject
    private TeacherTaskProgressDao teacherTaskProgressDao;

    @Inject
    private TeacherTaskServiceImpl teacherTaskService;

    public void handler(Message message) {
        Map<String, Object> msgMap;
        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            log.warn("TeacherTaskKtwelveMsgHandler message decode message failed!", JsonUtils.toJson(message.decodeBody()));
            return;
        }
        this.handlerMessage(msgMap);
    }


    private void handlerMessage(Map<String, Object> msg) {
        Long teacherId = MapUtils.getLong(msg, "event_id");
        String messageType = MapUtils.getString(msg, "event_type");
        if (!"user_basic_info_updated".equals(messageType) || teacherId == null) {
            return;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (null == teacherDetail) {
            return;
        }
        List<TeacherTask> teacherTaskList = teacherTaskDao.loadByTeacherId(teacherId);
        List<TeacherTaskProgress> teacherTaskProgressList = teacherTaskProgressDao.loadTeacherProgress(teacherId);
        Map<Long, TeacherTask> teacherTaskMap = teacherTaskList.stream().collect(Collectors.toMap(t -> t.getTplId(), t -> t));
        Map<Long, TeacherTaskProgress> progressMap = teacherTaskProgressList.stream().collect(Collectors.toMap(t -> t.getTaskId(), t -> t));
        if (CollectionUtils.isEmpty(teacherTaskList)) {//为空，则不处理
            return;
        }

        if (teacherDetail.isPrimarySchool()) {
            for (TeacherTaskTpl.Tpl tpl : TeacherTaskTpl.getNotPrimaryTpl()){
                deleteJob(teacherTaskMap, progressMap, tpl.getTplId());
            }
        } else {
            for (TeacherTaskTpl.Tpl tpl : TeacherTaskTpl.getPrimaryTpl()){
                deleteJob(teacherTaskMap, progressMap, tpl.getTplId());
            }
        }
    }

    private void deleteJob(Map<Long, TeacherTask> teacherTaskMap, Map<Long, TeacherTaskProgress> progressMap, Long tplId) {
        TeacherTask teacherTask = teacherTaskMap.get(tplId);
        if (null == teacherTask || TeacherTask.Status.FINISHED.equals(teacherTask.getStatus())
                || TeacherTask.Status.EXPIRED.equals(teacherTask.getStatus())) {//如果是完成与过期的的，或者为NULL，则不处理
            return;
        }
        teacherTask.setDisabled(true);
        TeacherTaskProgress progress = progressMap.get(teacherTask.getId());
        if (null != progress) {
            teacherTaskProgressDao.remove(progress.getId());
        }
        teacherTaskDao.replace(teacherTask);
    }
}