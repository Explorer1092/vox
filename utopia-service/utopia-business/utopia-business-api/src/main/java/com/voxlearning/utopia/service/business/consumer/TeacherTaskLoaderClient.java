package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.task.TeacherRookieTask;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.api.TeacherRookieTaskService;
import com.voxlearning.utopia.service.business.api.TeacherTaskLoader;

import java.util.*;
import java.util.stream.Collectors;

public class TeacherTaskLoaderClient {

    @ImportService(interfaceClass = TeacherTaskLoader.class)
    private TeacherTaskLoader ttLoader;
    @ImportService(interfaceClass = TeacherRookieTaskService.class)
    private TeacherRookieTaskService teacherRookieTaskService;

    public TeacherTaskLoader getTtLoader() {
        return this.ttLoader;
    }

    public MapMessage loadHistoryTask(Long teacherId){
        Map<Long, TeacherTaskTpl> tplMap = ttLoader.loadTaskTplMap();
        List<Map<String,Object>> data = ttLoader.loadAndInitTaskList(teacherId)
                .stream()
                .filter(t -> {
                    TeacherTaskTpl tpl = tplMap.get(t.getTplId());
                    if (tpl == null) {
                        return true;
                    }
                    if (tpl.getLoop() != null && tpl.getLoop() == true) {//循环任务不进历史任务
                        return false;
                    } else {
                        return true;
                    }
                })
                .filter(TeacherTask::isInHistory)
                .map(t -> MapUtils.m("name",t.getName(),"status",t.getStatus()))
                .collect(Collectors.toList());

        TeacherRookieTask teacherRookieTask = teacherRookieTaskService.loadRookieTask(teacherId);
        if (teacherRookieTask != null && teacherRookieTask.fetchFinished()) {
            Map<String, Object> rookieTask = MapUtils.m("name", "新手任务", "status", TeacherTask.Status.FINISHED.name());
            data.add(0, rookieTask);
        }
        return MapMessage.successMessage().add("data",data);
    }

    public MapMessage loadTaskList(Long teacherId){
        MapMessage resultMsg = MapMessage.successMessage();
        List<TeacherTaskEntry> teacherTaskEntry = ttLoader.getTeacherTaskEntry(teacherId);
        teacherTaskEntry = teacherTaskEntry.stream().filter(i -> !Objects.equals(i.getType(), TeacherTaskTpl.Type.Deprecated.name())).collect(Collectors.toList());
        List<Map<String, Object>> data = getSortMaps(teacherTaskEntry);
        return resultMsg.add("data",data);
    }

    public MapMessage loadCrmTaskList(Long teacherId) {
        MapMessage resultMsg = MapMessage.successMessage();
        List<TeacherTaskEntry> teacherTaskEntry = ttLoader.getCrmTeacherTaskEntry(teacherId);
        List<Map<String, Object>> data = getSortMaps(teacherTaskEntry);
        return resultMsg.add("data",data);
    }

    private List<Map<String, Object>> getSortMaps(List<TeacherTaskEntry> teacherTaskEntry) {
        Map<String, List<TeacherTaskEntry>> entryMap = teacherTaskEntry.stream().collect(Collectors.groupingBy(k -> k.getType()));
        List<Map<String, Object>> data = new ArrayList<>();
        entryMap.forEach((type, entryList) -> {
            TeacherTaskEntry firstEntry = entryList.stream().findFirst().orElse(null);
            if (firstEntry == null){
                return;
            }

            TeacherTaskTpl.Type ttt = TeacherTaskTpl.Type.of(firstEntry.getType());
            Map<String, Object> rootTaskMap = new HashMap<>();
            rootTaskMap.put("name", ttt.getDesc());
            rootTaskMap.put("type", ttt.name());
            rootTaskMap.put("typeInfo", TeacherTaskTpl.Type.valueOf(type));
            rootTaskMap.put("taskList", entryList);
            Collections.sort(entryList, (e1, e2) -> e1.getSort() == e2.getSort() ? 0 : e1.getSort() > e2.getSort() ? 1 : -1);
            data.add(rootTaskMap);
        });
        Collections.sort(data, (m1,m2) -> {
            TeacherTaskTpl.Type m1Type = (TeacherTaskTpl.Type)m1.get("typeInfo");
            TeacherTaskTpl.Type m2Type = (TeacherTaskTpl.Type)m2.get("typeInfo");
            return m1Type.getSort() == m2Type.getSort() ? 0 : m1Type.getSort() > m2Type.getSort() ? 1 : -1;
        });
        return data;
    }


    public MapMessage loadSubTask(Long teacherId, Long taskId) {
        TeacherTaskProgress progress = ttLoader.loadTaskProgress(teacherId)
                .stream()
                .filter(tp -> Objects.equals(tp.getTaskId(), taskId))
                .findFirst()
                .orElse(null);

        if (progress == null) {
            return MapMessage.errorMessage("任务进度数据为空!");
        }
        return MapMessage.successMessage().add("data", ttLoader.getTeacherSubTaskEntry(teacherId, taskId));
    }

    public List<Long> getTeacherIdByInfos(List<Long> tplIds, String status) {
        return ttLoader.getTeacherIdByInfos(tplIds, status);
    }

    public boolean hadFinishedRookieTask(Long teacherId){
        return ttLoader.hadFinishedRookieTask(teacherId);
    }

    public boolean receiveAndFinishedRookieTask(Long teacherId) {
        return ttLoader.receiveAndFinishedRookieTask(teacherId);
    }

    public boolean hadReachTarget(Long teacherId, Long tplId) {
        return ttLoader.hadReachTarget(teacherId, tplId);
    }

    public void removeTeacherTaskLog(Long time) {
        ttLoader.removeTeacherTaskLog(time);
    }

    public List<Long> getTaskTeacherId(Long teacherId) {
        return ttLoader.getTaskTeacherId(teacherId);
    }

    public TeacherTaskTpl replace(TeacherTaskTpl teacherTaskTpl) {
        return ttLoader.replace(teacherTaskTpl);
    }

    public TeacherTaskProgress replace(TeacherTaskProgress teacherTaskProgress) {
        return ttLoader.replace(teacherTaskProgress);
    }

    public MapMessage loadTeacherWeekTask(Long teacherId) {
        return ttLoader.loadTeacherWeekTask(teacherId);
    }

    public Long getInviteTeacherCount() {
        return ttLoader.getInviteTeacherCount();
    }

    public Long incrInviteTeacherCount(Long teacherId) {
        return ttLoader.incrInviteTeacherCount(teacherId);
    }
}
