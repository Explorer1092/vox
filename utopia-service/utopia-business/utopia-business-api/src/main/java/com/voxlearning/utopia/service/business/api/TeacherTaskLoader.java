package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherSubTaskEntry;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ServiceVersion(version = "20190311")
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TeacherTaskLoader {

    List<TeacherTask> internalLoadTaskList(Long teacherId);

    TeacherTask internalLoadTaskList(Long teacherId,Long tplId);

    /**
     * 如果需要获取数据的任务数据以及任务的明细数据，【注意】请使用此方法，否则直接从数据获取可能拿到过期的任务信息
     *
     * 1.该方法会初始化任务的信息，比如将过期的任务设置为过期，重新初始化周期性任务等
     *
     * @param teacherId
     * @return
     * @author zhouwei
     */
    List<TeacherTask> loadAndInitTaskList(Long teacherId);

    List<TeacherTaskTpl> loadAllTaskTpl();

    /**
     * 获取模板信息
     * @param tplId
     * @return
     */
    TeacherTaskTpl loadTaskTpl(Long tplId);

    default Map<Long,TeacherTaskTpl> loadTaskTplMap(){
        return loadAllTaskTpl().stream().collect(Collectors.toMap(k -> k.getId(),v -> v));
    }

    List<TeacherTaskProgress> loadTaskProgress(Long teacherId);

    default Map<Long,TeacherTaskProgress> loadTaskProgressMap(Long teacherId){
        return loadTaskProgress(teacherId).stream().collect(Collectors.toMap( k -> k.getTaskId(), v -> v));
    }

    default  TeacherTaskProgress loadTaskProgressById(Long teacherId,Long taskId){
        return loadTaskProgressMap(teacherId).get(taskId);
    }

    /**
     * 获取给前端暂时的任务信息列表
     * @param teacherId
     * @return
     * @author zhouwei
     */
    List<TeacherTaskEntry> getTeacherTaskEntry(Long teacherId);

    /**
     * 获取子任务的信息列表
     * @param teacherId
     * @param taskId
     * @return
     * @author zhouwei
     */
    List<TeacherSubTaskEntry> getTeacherSubTaskEntry(Long teacherId, Long taskId);


    /**
     * 获取给CRM展示的任务信息列表
     * @param teacherId
     * @return
     * @author zhouwei
     */
    List<TeacherTaskEntry> getCrmTeacherTaskEntry(Long teacherId);

    /**
     * 判断是否完成了新手任务
     *
     * 注意：如果该用户没有领取新手任务的资格，即没有领取新手任务（比如，老用户已经认证，就不用完成），也会返回true，具体使用的时候，请注意匹配自己的逻辑
     *
     * @param teacherId
     * @return
     * @author zhouwei
     */
    @Deprecated
    boolean hadFinishedRookieTask(Long teacherId);

    /**
     * 用户一定是领取了新手任务，并且完成了，则返回ture，没有领取过或者没有完成，则返回false
     * @param teacherId
     * @return
     * @author zhouwei
     */
    boolean receiveAndFinishedRookieTask(Long teacherId);

    /**
     * 查询用户下面的任务是否达到了目标
     * @param teacherId
     * @param tplId
     * @return
     */
    boolean hadReachTarget(Long teacherId, Long tplId);

    /**
     * @param tplIds
     * @param status
     * @return
     * @author zhouwei
     */
    List<Long> getTeacherIdByInfos(List<Long> tplIds, String status);

    /**
     * 删除任务的历史数据，由任务负责调用
     * @param receiveTime
     * @author zhouwei
     */
    void removeTeacherTaskLog(Long receiveTime);

    /**
     * 获取任务中的老师ID，每次获取1000个老师ID，方便获取所有老师ID，具体可以查看实现
     *
     * 主要为任务调用，请不要随意调用
     *
     * @param teacherId
     * @return
     * @author zhouwei
     */
    List<Long> getTaskTeacherId(Long teacherId);

    /**
     * 替换任务模板 注意：请勿随意调用接口，谢谢
     * @param teacherTaskTpl
     * @return
     * @author zhouwei
     */
    @Deprecated
    TeacherTaskTpl replace(TeacherTaskTpl teacherTaskTpl);

    /**
     * 替换某条任务进度 注意：请勿随意调用接口，谢谢
     * @param teacherTaskProgress
     * @return
     * @author zhouwei
     */
    @Deprecated
    TeacherTaskProgress replace(TeacherTaskProgress teacherTaskProgress);

    /**
     * 处理线上处理，将补发老师奖励的任务状态设置为已经完成
     * @return
     */
    @Deprecated
    MapMessage resolveTeacherTaskRookie(Long taskId, Boolean confirm);

    /**
     * 删除一个老师的所有任务，用来处理异常数据，请勿调用
     * @param teacherId
     * @return
     */
    @Deprecated
    MapMessage deleteTeacherTask(Long teacherId);

    /**
     * 查询每周3次作业任务
     */
    MapMessage loadTeacherWeekTask(Long teacherId);

    Long getInviteTeacherCount();

    Long incrInviteTeacherCount(Long incr);

    Long setInviteTeacherCount(Long incr);

}
