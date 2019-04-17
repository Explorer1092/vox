package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherSubTaskEntry;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.apache.commons.jexl2.JexlContext;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 任务维度的流水线上各个阶段的扩展方法，每个具体的任务，都会有一个实现类
 * 核心流水线有3条：1.更新任务的进度，2.初始化任务信息，3.加载任务信息用于显示
 *
 * 设计理念：
 * 1.每个任务都有各自特殊的逻辑以及属性需要扩展，无法完全在流水线上完全实现，否则会导致逻辑混乱难以理解
 * 2.个性化的逻辑以及扩展，全部基于这个接口去扩展与实现，并将定义好的方法嵌入至流水线中的某一个环节发挥作用
 *
 * Created by zhouwei on 2018/8/30
 */
public interface TeacherTaskInit {

    /**
     * 修改任务中的一些信息，比如target、status等，归属：更新任务的进度
     *
     * @author zhouwei
     */
    void updateTaskInfo(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, Map<String,Object> varMap);

    /**
     * 再一次判断是否可以领取这个任务，主要针对一些特殊逻辑或拆分tpl等情况，导致无法完全兼容TPL了，归属：初始化任务信息
     * @param teacherDetail
     * @param calContext
     * @return
     * @author zhouwei
     */
    boolean isPutOn(TeacherDetail teacherDetail, JexlContext calContext);

    /**
     * 任务是否需要重新开始，归属：初始化任务信息
     *
     * 比如任务是周期性任务，但是已经过期，需要初始化
     * 比如任务是周期性任务，但是已经不满足继续执行条件，需要是初始化
     *
     * @return
     * @author zhouwei
     */
    boolean isNeedRestart(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress);

    /**
     * 任务初始化的时候，添加一些变量进入Jexl_Context，用来计算，归属：初始化任务信息
     *
     * @param jexlContext
     * @author zhouwei
     */
    void initAddJexlContext(TeacherDetail teacherDetail, JexlContext jexlContext);

    /**
     * 任务初始化的后，最后在可以在这里设置teacher以及progress的一些信息，归属：初始化任务信息
     *
     * @param teacherDetail
     * @param teacherTask
     * @param teacherTaskProgress
     * @param oldTeacherTask                //只有周期性任务才有这个参数，主要为了延续周期性任务的一些属性
     * @param oldTeacherTaskProgress        //只有周期性任务才有这个参数，主要为了延续周期性任务的一些属性
     * @author zhouwei
     */
    void initTaskAndProgress(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, TeacherTask oldTeacherTask, TeacherTaskProgress oldTeacherTaskProgress);

    /**
     * 正在进行中的任务，因为一些特殊的条件，需要对数据做一些处理，归属：初始化任务信息
     * @param teacherDetail
     * @param teacherTask
     * @param teacherTaskProgress
     * @return 是否有修改
     * @author zhouwei
     */
    boolean onGoingTaskInit(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress);

    /**
     * 更新任务进度的时候，需要初始化的一些变量进入Jexl_Context，用来计算，归属：更新任务的进度
     *
     * @author zhouwei
     */
    void progressAddVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress);

    /**
     * 开始之前，处理NewVars，归属：更新任务的进度
     *
     * 主要因为有一些个性化的处理
     *
     * @return true表示当前可以继续执行，否则不能执行
     * @author zhouwei
     */
    boolean processNewVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, Map<String, Object> newVars);

    /**
     * 总任务完成后，处理一些信息，归属：更新任务的进度
     * @param teacherDetail
     * @param teacherTaskProgress
     * @param subTaskProgress
     */
    void taskComplete(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress);

    /**
     * 一个子任务完成后，处理一些信息，归属：更新任务的进度
     * @param teacherDetail
     * @param teacherTaskProgress
     * @param subTaskProgress
     */
    void subTaskComplete(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress);

    /**
     * 一个进度完成后，处理一些信息，归属：更新任务的进度
     */
    void oneProcessComplete(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress);

    /**
     * 总任务完成后的描述信息，归属：更新任务的进度
     *
     * @author zhouwei
     */
    String rewardTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.Reward reward);

    /**
     * 子任务任务完成后的描述信息，归属：更新任务的进度
     *
     * @author zhouwei
     */
    String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward);

    /**
     * 任务进度完成后的描述信息，归属：更新任务的进度
     *
     * @author zhouwei
     */
    String rewardProgressComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.ProgressReward reward);

    /**
     * 返回当前任务初始化的适用类
     * @return
     * @author zhouwei
     */
    TeacherTaskTpl.Tpl getTeacherTaskTpl();

    /**
     * 获取加载任务列表的实体，归属：加载任务信息用于显示
     * @param teacherDetail
     * @param teacherTaskProgress
     * @return
     * @author zhouwei
     */
    TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress);

    /**
     * 获取加载子任务列表的实体，归属：加载任务信息用于显示
     * @param teacherDetail
     * @param teacherTask
     * @param teacherTaskProgress
     * @return
     * @author zhouwei
     */
    List<TeacherSubTaskEntry> getTeacherSubTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress);

    /**
     * 是否显示任务，归属：加载任务信息用于显示
     * @param teacherDetail
     * @param teacherTaskList
     * @param teacherTaskProgressList
     * @return
     * @author zhouwei
     */
    boolean isDisplay(TeacherDetail teacherDetail, List<TeacherTask> teacherTaskList, List<TeacherTaskProgress> teacherTaskProgressList, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress);

    /**
     * 判断任务信息是否有修改，是否要归档，可以减少一些数据量
     * @param teacherDetail
     * @param teacherTask
     * @param teacherTaskProgress
     * @return
     */
    boolean isStoreLog(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress);

    /**
     * 动态计算任务的过期时间, 比 activeTime 优先级要高
     */
    Date calcExpireDate(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress);
}