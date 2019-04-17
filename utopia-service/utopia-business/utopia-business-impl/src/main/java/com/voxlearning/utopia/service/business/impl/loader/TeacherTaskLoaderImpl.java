package com.voxlearning.utopia.service.business.impl.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.entity.task.*;
import com.voxlearning.utopia.entity.task.week.ClazzDetail;
import com.voxlearning.utopia.entity.task.week.TeacherTaskWeekMapper;
import com.voxlearning.utopia.mapper.TeacherSubTaskEntry;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.api.TeacherTaskLoader;
import com.voxlearning.utopia.service.business.impl.dao.*;
import com.voxlearning.utopia.service.business.impl.service.TeacherMonthTaskServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.TeacherRookieTaskServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.TeacherTaskServiceImpl;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator.TeacherTaskHandlerEvaluator;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.TeacherTaskHandlerInit;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.TeacherTaskInit;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.business.impl.utils.TeacherWeekTaskRewardCalc;
import com.voxlearning.utopia.service.campaign.api.UserActivityService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityRef;
import com.voxlearning.utopia.service.campaign.api.enums.TeacherActivityEnum;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Named
@ExposeService(interfaceClass = TeacherTaskLoader.class)
public class TeacherTaskLoaderImpl implements TeacherTaskLoader {

    /** Logger **/
    private static Logger logger = LoggerFactory.getLogger(TeacherTaskLoaderImpl.class);

    @Inject private TeacherTaskDao taskDao;
    @Inject private TeacherTaskTplDao teacherTaskTplDao;
    @Inject private TeacherTaskProgressDao teacherTaskProgressDao;
    @Inject private TeacherTaskServiceImpl ttService;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherTaskHandlerInit teacherTaskHandlerInit;
    @Inject private TeacherTaskHandlerEvaluator teacherTaskHandlerEvaluator;
    @Inject private TeacherTaskLogDao teacherTaskLogDao;
    @Inject private TeacherTaskProgressLogDao teacherTaskProgressLogDao;
    @Inject private TeacherRookieTaskServiceImpl teacherRookieTaskService;
    @Inject private TeacherMonthTaskServiceImpl teacherMonthTaskService;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private BusinessCacheSystem businessCacheSystem;

    @ImportService(interfaceClass = UserActivityService.class)
    private UserActivityService userActivityService;

    public List<TeacherTask> internalLoadTaskList(Long teacherId){
        return taskDao.loadByTeacherId(teacherId);
    }

    @Override
    public TeacherTask internalLoadTaskList(Long teacherId, Long tplId) {
        return internalLoadTaskList(teacherId).stream().filter(i -> Objects.equals(i.getTplId(), tplId)).findFirst().orElse(null);
    }

    public List<TeacherTask> loadTaskListInStatus(Long teacherId, TeacherTask.Status status){
        return internalLoadTaskList(teacherId)
                .stream()
                .filter(t -> Objects.equals(t.getStatus(), status.name()))
                .collect(Collectors.toList());
    }

    /**
     * 加载老师的当前所有任务，如果有未初始化的任务，顺便初始化
     * @param teacherId
     * @return
     */
    @Override
    public List<TeacherTask> loadAndInitTaskList(Long teacherId) {
        try {
            TeacherDetail td = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (td == null){
                return Collections.emptyList();
            }
            AtomicCallback<List<TeacherTask>> callback = () -> {
                List<TeacherTask> taskList = internalLoadTaskList(teacherId);
                Map<Long, TeacherTask> taskMap = taskList.stream().collect(Collectors.toMap(k -> k.getTplId(), v -> v));
                List<TeacherTask> newTeacherTask = new ArrayList<>();
                Map<Long,TeacherTaskTpl> tplMap = loadTaskTplMap();
                for (TeacherTaskTpl tpl : loadAllTaskTpl()) {
                    TeacherTask tt = taskMap.get(tpl.getId());

                    if (tt != null) {//vox_teacher_task已经存在，就不在初始化这个任务。

                        if (Objects.equals(tt.getStatus(), TeacherTask.Status.INIT.name())) {//如果初始化任务状态的任务，但是已经不具备领取条件，则删除
                            TeacherTaskTpl teacherTaskTpl = tplMap.get(tt.getTplId());
                            boolean putOn = ttService.isPutOn(td, teacherTaskTpl);
                            if (false == putOn) {//如果不具备，则删除任务
                                ttService.deleteTaskAndProgress(tt);
                            } else {
                                newTeacherTask.add(tt);
                            }
                        } else {
                            newTeacherTask.add(tt);
                        }
                        continue;

                    }

                    // 初始化新的任务模板
                    MapMessage initResult = ttService.newTaskAndProgress(td, tpl);
                    if (!initResult.isSuccess()) {
                        logger.error("TT:Init task error!tId:{}", td.getId());
                    } else {
                        Optional.ofNullable(initResult.get("newTask")).map(o -> (TeacherTask) o).ifPresent(newTeacherTask::add);
                    }
                }

                return newTeacherTask;
            };

            List<TeacherTask> teacherTasksNew = AtomicCallbackBuilderFactory.getInstance()
                    .<List<TeacherTask>>newBuilder()
                    .keyPrefix("TT:NewTaskAndProgress")
                    .keys(td.getId())
                    .callback(callback)
                    .build()
                    .execute();

            return teacherTasksNew;
        } catch (Exception e){
            return internalLoadTaskList(teacherId);
        }
    }

    public List<TeacherTaskTpl> loadAllTaskTpl(){
        return teacherTaskTplDao.loadAll();
    }

    @Override
    public TeacherTaskTpl loadTaskTpl(Long tplId) {
        List<TeacherTaskTpl> teacherTaskTpls = teacherTaskTplDao.loadAll();
        for (TeacherTaskTpl tpl : teacherTaskTpls) {
            if (Objects.equals(tpl.getId(),tplId)) {
                return tpl;
            }
        }
        return null;
    }

    /**
     * 所有的任务状态每次也会进来(点击任务列表页、更新任务进度)
     *
     * 1. 在这里可以给周期任务、已经过期的任务，重新设置他们的状态
     * 2. 对于正在进行中的任务，需要做特殊处理的，也可以在这里处理
     *
     * @param teacherId
     * @return
     */
    @Override
    public List<TeacherTaskProgress> loadTaskProgress(Long teacherId) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (null == teacherDetail) {
            return new ArrayList<>();
        }
        List<TeacherTaskProgress> ttpList = teacherTaskProgressDao.loadTeacherProgress(teacherId);
        Map<Long, TeacherTaskTpl> taskTplMap = loadTaskTplMap();
        // 以taskId为key的TeacherTask的Map，更新状态用的
        LazyInitializationSupplier<Map<Long,TeacherTask>> taskMapSupplier = new LazyInitializationSupplier<>(() -> {//获取老师的所有任务
            return internalLoadTaskList(teacherId).stream().collect(Collectors.toMap(t -> t.getId(),v -> v));
        });

        Date now = new Date();
        List<TeacherTaskProgress> newTTpList = new ArrayList<>();
        for (TeacherTaskProgress ttp : ttpList) {
            TeacherTaskTpl tpl = taskTplMap.get(ttp.getTplId());
            TeacherTask teacherTask = taskMapSupplier.initializeIfNecessary().get(ttp.getTaskId());
            if(tpl == null || teacherTask == null) {//如果任务或者模板没有，则不处理
                continue;
            }

            TeacherTaskInit teacherTaskInit = teacherTaskHandlerInit.getHandler(TeacherTaskTpl.Tpl.getTplById(ttp.getTplId()));//获取这个任务的初始化信息
            boolean isChangeTTp = false;//本次是否对ttp有修改
            /**
             * 只处理有过期时间的任务
             */
            if (teacherTask.getExpireDate() != null) {
                boolean expired = false;//是否已经过期
                long expireTime = teacherTask.getExpireDate().getTime();
                String oldStatus = teacherTask.getStatus();
                if (now.getTime() >= expireTime) {//任务已经过期
                    if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.ONGOING.name())) {//如果正在进行中的任务，任务已经过期，设置为过期状态。这里之前有个bug，会把已经完成的任务设置成过期状态。不影响奖励等发放，只是状态不对。
                        teacherTask.setStatus(TeacherTask.Status.EXPIRED.name());
                        ttp.setStatus(TeacherTask.Status.EXPIRED.name());
                        isChangeTTp = true;
                    }
                    expired = true;
                }

                /**
                 * 初始化周期性任务
                 * 1.如果已经过期，并且是循环任务，则初始化任务
                 * 2.任务虽然没有过期，但是任务已经不满足继续进行状态，也需要初始化，签到任务，如果不连续签到，则需要重新签
                 */
                if ((expired || (teacherTaskInit.isNeedRestart(teacherDetail, ttp)))
                        && BooleanUtils.isTrue(tpl.getLoop())) {
                    MapMessage result = ttService.createTaskAndProgress(teacherDetail, tpl, teacherTask, ttp);
                    if (result.isSuccess() && result.get("isNew") != null) {//是否新生成了任务
                        Boolean isNew = (Boolean) result.get("isNew");
                        if (true == isNew) {//如果添加成功
                            TeacherTask oldTeacherTask = teacherTask;
                            TeacherTaskProgress oldTeacherTaskProgress = ttp;

                            teacherTask = (TeacherTask) result.get("newTask");
                            ttp = (TeacherTaskProgress) result.get("progress");
                            isChangeTTp = true;

                            //添加成功，则将老的任务放入归档的表中
                            setTeacherTaskLogInfo(teacherDetail, oldTeacherTask, oldTeacherTaskProgress, oldStatus);
                        }
                    }
                }
            }

            /** 对于一些还在进行中的任务，可能需要重置一些信息 **/
            if (ttp.isOnGoing() && teacherTaskInit.onGoingTaskInit(teacherDetail, teacherTask, ttp)) {
                isChangeTTp = true;
            }

            if (isChangeTTp) {//如果有修改，则更新teachTask与progress
                taskDao.updateTeacherTask(teacherTask);
                teacherTaskProgressDao.upsert(ttp);
            }
            newTTpList.add(ttp);
        }
        return newTTpList;
    }

    /**
     * 将过期需要重新生成的任务做归档到日志表中
     * @param teacherTask
     * @param teacherTaskProgress
     */
    private void setTeacherTaskLogInfo(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, String oldStatus) {
        try {
            TeacherTaskInit teacherTaskInit = teacherTaskHandlerInit.getHandler(TeacherTaskTpl.Tpl.getTplById(teacherTask.getTplId()));//获取这个任务的初始化执行类
            if (!teacherTaskInit.isStoreLog(teacherDetail, teacherTask, teacherTaskProgress)) {
                return;
            }
            TeacherTaskLog teacherTaskLog = new TeacherTaskLog();
            teacherTaskLog.setTeacherId(teacherTask.getTeacherId());
            teacherTaskLog.setType(teacherTask.getType());
            teacherTaskLog.setName(teacherTask.getName());
            teacherTaskLog.setTplId(teacherTask.getTplId());
            teacherTaskLog.setStatus(oldStatus);
            teacherTaskLog.setExpireDate(teacherTask.getExpireDate());
            if (teacherTask.getReceiveDate() == null && teacherTask.getExpireDate() != null) {
                teacherTaskLog.setReceiveDate(new Date(teacherTask.getExpireDate().getTime() - 15 * 24 * 60 * 60 * 1000));
            } else {//没有过期时间的任务，使用创建时间来代替
                teacherTaskLog.setReceiveDate(teacherTask.getCreateDatetime());
            }
            teacherTaskLog.setFinishedDate(teacherTask.getFinishedDate());
            teacherTaskLog.setCancelDate(teacherTask.getCancelDate());
            teacherTaskLogDao.insert(teacherTaskLog);

            TeacherTaskProgressLog teacherTaskProgressLog = JsonUtils.fromJson(JsonUtils.toJson(teacherTaskProgress), TeacherTaskProgressLog.class);
            teacherTaskProgressLog.setTaskId(teacherTaskLog.getId());//绑定归档后的taskId
            teacherTaskProgressLog.setId(null);//备份数据，将ID设置为NULL
            teacherTaskProgressLog.setStatus(oldStatus);
            if (teacherTaskProgressLog.getReceiveTime() == null) {//清理数据需要用receiveTime，老任务之前没有这个字段的，用createTime兼容下
                teacherTaskProgressLog.setReceiveTime(teacherTaskProgressLog.getCreateTime());
            }
            teacherTaskProgressLogDao.insert(teacherTaskProgressLog);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    @Deprecated
    public boolean hadFinishedRookieTask(Long teacherId) {
        return this.receiveAndFinishedRookieTask(teacherId);
    }

    @Override
    public boolean receiveAndFinishedRookieTask(Long teacherId) {
        boolean haveRooke = false;
        boolean finishRookie = false;
        Map<Long,TeacherTaskTpl> tplMap = loadTaskTplMap();
        Map<Long,TeacherTaskTpl> rookieTplMap = tplMap.values().stream()
                .filter(t -> t.getId().equals(TeacherTaskTpl.Tpl.JUNIOR_ROOKIE.getTplId())
                        || t.getId().equals(TeacherTaskTpl.Tpl.PRIMARY_ROOKIE.getTplId())
                        || t.getId().equals(TeacherTaskTpl.Tpl.PRIMARY_ROOKIE_ENGLISH_CHINESE.getTplId())
                ).collect(Collectors.toMap(t -> t.getId(), t -> t));
        List<TeacherTask> teacherTasks = internalLoadTaskList(teacherId);
        for (TeacherTask teacherTask : teacherTasks) {
            TeacherTaskTpl teacherTaskTpl = rookieTplMap.get(teacherTask.getTplId());
            if (null == teacherTaskTpl) {
                continue;
            }
            haveRooke = true;
            if (Objects.equals(teacherTask.getStatus(),TeacherTask.Status.FINISHED.name())) {
                finishRookie = true;
            }
        }
        return haveRooke && finishRookie;
    }

    @Override
    public boolean hadReachTarget(Long teacherId, Long tplId) {
        return loadTaskProgress(teacherId)
                .stream()
                .filter(t -> Objects.equals(t.getTplId(),tplId))
                .map(p -> p.getOngoingSubTask())
                .map(st -> st.getProgress())
                .map(p -> {
                    int curr = p.getCurr();
                    int target = p.getTarget();
                    return curr >= target;
                })
                .findFirst()
                .orElse(false);
    }

    @Override
    public List<TeacherTaskEntry> getCrmTeacherTaskEntry(Long teacherId) {
        loadTaskProgressMap(teacherId);//进来，先初始化现有任务的，将任务设置为过期等信息，否则可能任务的状态会不对
        List<TeacherTask> taskList = loadAndInitTaskList(teacherId);//加载任务，并初始化任务(vox_teacher_task、vox_teacher_task_progress)。如果老师第一次进来，会给他设置符合条件的所有任务
        Map<Long, TeacherTaskProgress> progressMap = loadTaskProgressMap(teacherId);//重新加载任务的进度
        Map<Long, TeacherTaskTpl> tplMap = loadTaskTplMap();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        List<TeacherTaskEntry> entries = new ArrayList<>();
        taskList.forEach(teacherTask -> {
            TeacherTaskTpl teacherTaskTpl = tplMap.get(teacherTask.getTplId());
            TeacherTaskTpl.Tpl tpl = TeacherTaskTpl.Tpl.getTplById(teacherTask.getTplId());
            TeacherTaskProgress progress = progressMap.get(teacherTask.getId());
            if (tpl == null || progress == null || teacherTaskTpl == null) {
                return;
            }
            TeacherTaskInit handler = teacherTaskHandlerInit.getHandler(tpl);
            TeacherTaskEntry teacherTaskEntry = handler.getTeacherTaskEntry(teacherDetail, teacherTask, progress);
            entries.add(teacherTaskEntry);
        });

        // 添加新手任务、月活跃任务
        addRookieMonth(teacherId, entries);

        return entries;
    }

    private void addRookieMonth(Long teacherId, List<TeacherTaskEntry> entries) {
        try {
            // 新手任务
            TeacherRookieTask rookieTask = teacherRookieTaskService.loadRookieTask(teacherId);

            TeacherTaskEntry rookieTaskEntry = new TeacherTaskEntry();
            rookieTaskEntry.setCrmIsDisplay(true);
            rookieTaskEntry.setAutoReceive(false);
            rookieTaskEntry.setCycle(false);
            rookieTaskEntry.setName("新手任务");
            rookieTaskEntry.setSort(-2);
            rookieTaskEntry.setType(TeacherTaskTpl.Type.Special.name());

            if (rookieTask != null) {
                rookieTaskEntry.setStatus(rookieTask.getStatus());
                rookieTaskEntry.setReceiveDate(DateFormatUtils.format(rookieTask.getReceiveDate(), DateUtils.FORMAT_SQL_DATETIME));
                if (rookieTaskEntry.getFinishDate() != null) {
                    rookieTaskEntry.setFinishDate(DateFormatUtils.format(rookieTask.getFinishedDate(), DateUtils.FORMAT_SQL_DATETIME));
                }

                List<TeacherTaskEntry.CrmProgress> list = new ArrayList<>();
                rookieTaskEntry.setCrmProgressList(list);
                TeacherTaskEntry.CrmProgress crmProgress = new TeacherTaskEntry.CrmProgress();
                crmProgress.setTarget(rookieTask.getSubTask().size());
                long count = rookieTask.getSubTask().stream().filter(TeacherRookieTask.SubTask::fetchFinished).count();
                crmProgress.setCurr(SafeConverter.toInt(count));
                list.add(crmProgress);

                entries.add(rookieTaskEntry);
            } else {
                Boolean allow = teacherRookieTaskService.allowRookieTask(teacherId);
                if (allow) {
                    rookieTaskEntry.setStatus(TeacherTask.Status.INIT.name());
                    entries.add(rookieTaskEntry);
                }
            }

            // 月任务
            TeacherMonthTask monthTask = teacherMonthTaskService.loadMonthTask(teacherId);

            TeacherTaskEntry monthTaskEntry = new TeacherTaskEntry();
            monthTaskEntry.setCrmIsDisplay(true);
            monthTaskEntry.setAutoReceive(false);
            monthTaskEntry.setCycle(true);
            monthTaskEntry.setCycleUnit("M");
            monthTaskEntry.setName("月活跃任务");
            monthTaskEntry.setSort(-1);
            monthTaskEntry.setType(TeacherTaskTpl.Type.Special.name());

            if (monthTask != null) {
                monthTaskEntry.setStatus(monthTask.getStatus());
                if (monthTask.getReceiveDate() != null) {
                    monthTaskEntry.setReceiveDate(DateFormatUtils.format(monthTask.getReceiveDate(), DateUtils.FORMAT_SQL_DATETIME));
                }
                monthTaskEntry.setExpireDate(DateFormatUtils.format(monthTask.getExpireDate(), DateUtils.FORMAT_SQL_DATETIME));
                entries.add(monthTaskEntry);
            } else {
                Boolean allow = teacherMonthTaskService.allowMonthTask(teacherId);
                if (allow) {
                    monthTaskEntry.setStatus(TeacherTask.Status.INIT.name());
                    entries.add(monthTaskEntry);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public List<TeacherTaskEntry> getTeacherTaskEntry(Long teacherId) {
        loadTaskProgressMap(teacherId);//进来，先初始化现有任务的，将任务设置为过期等信息，否则可能任务的状态会不对
        List<TeacherTask> taskList = loadAndInitTaskList(teacherId);//加载任务，并初始化任务(vox_teacher_task、vox_teacher_task_progress)。如果老师第一次进来，会给他设置符合条件的所有任务
        Map<Long, TeacherTaskProgress> progressMap = loadTaskProgressMap(teacherId);//重新加载任务的进度
        Map<Long, TeacherTaskTpl> tplMap = loadTaskTplMap();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        List<TeacherTaskEntry> entries = new ArrayList<>();
        taskList.forEach(teacherTask -> {
            TeacherTaskTpl teacherTaskTpl = tplMap.get(teacherTask.getTplId());
            TeacherTaskTpl.Tpl tpl = TeacherTaskTpl.Tpl.getTplById(teacherTask.getTplId());
            TeacherTaskProgress progress = progressMap.get(teacherTask.getId());
            if (tpl == null || progress == null || teacherTaskTpl == null) {
                return;
            }
            if(teacherTask.isInHistory() && teacherTaskTpl.getLoop() != true) {//如果不是循环任务，则过期或者完成的任务，不在列表页展示
                return;
            }
            if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.CANCEL.name())) {//取消状态的任务不显示
                return;
            }
            TeacherTaskInit handler = teacherTaskHandlerInit.getHandler(tpl);
            boolean display = handler.isDisplay(teacherDetail, taskList, progressMap.values().stream().collect(Collectors.toList()), teacherTask, progress);

            /** 是否显示该任务 **/
            if (display) {
                TeacherTaskEntry teacherTaskEntry = handler.getTeacherTaskEntry(teacherDetail, teacherTask, progress);
                entries.add(teacherTaskEntry);
            }
        });
        return entries;
    }

    @Override
    public List<TeacherSubTaskEntry> getTeacherSubTaskEntry(Long teacherId, Long taskId) {
        List<TeacherTask> teacherTasks = this.loadAndInitTaskList(teacherId);
        List<TeacherTaskProgress> teacherTaskProgressList = this.loadTaskProgress(teacherId);
        TeacherTask teacherTask = teacherTasks.stream().filter(t -> t.getId().equals(taskId)).findAny().orElse(null);
        TeacherTaskProgress progress = teacherTaskProgressList.stream().filter(p -> p.getTaskId().equals(taskId)).findAny().orElse(null);
        if (teacherTask == null || progress == null) {
            return new ArrayList<>();
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        TeacherTaskTpl.Tpl tpl = TeacherTaskTpl.Tpl.getTplById(teacherTask.getTplId());
        TeacherTaskInit handler = teacherTaskHandlerInit.getHandler(tpl);
        return handler.getTeacherSubTaskEntry(teacherDetail, teacherTask, progress);
    }

    @Override
    public List<Long> getTeacherIdByInfos(List<Long> tplIds, String status) {
        return taskDao.getTeacherIdByInfo(tplIds, status);
    }

    @Override
    public List<Long> getTaskTeacherId(Long teacherId) {
        return taskDao.getTeacherIdByLimit(teacherId);
    }

    @Override
    public void removeTeacherTaskLog(Long receiveTime) {
        teacherTaskLogDao.removeByReceiveData(new Date(receiveTime));
        teacherTaskProgressLogDao.removeByReceiveTime(receiveTime);
    }

    @Override
    public TeacherTaskTpl replace(TeacherTaskTpl teacherTaskTpl) {
        return this.teacherTaskTplDao.replace(teacherTaskTpl);
    }

    @Override
    public TeacherTaskProgress replace(TeacherTaskProgress teacherTaskProgress) {
        return this.teacherTaskProgressDao.replace(teacherTaskProgress);
    }

    @Override
    public MapMessage resolveTeacherTaskRookie(Long taskId, Boolean confirm){
        MapMessage mapMessage = MapMessage.successMessage();
        TeacherTask teacherTask = taskDao.load(taskId);
        if (teacherTask == null || teacherTask.getTplId() != TeacherTaskTpl.Tpl.JUNIOR_ROOKIE.getTplId()) {
            return mapMessage;
        }
        List<TeacherTaskProgress> teacherTaskProgressList = teacherTaskProgressDao.loadTeacherProgress(teacherTask.getTeacherId());
        TeacherTaskProgress progress = teacherTaskProgressList.stream().filter(t -> t.getTplId() == TeacherTaskTpl.Tpl.JUNIOR_ROOKIE.getTplId()).findAny().orElse(null);
        if (progress == null) {
            return mapMessage;
        }
        if (confirm) {
            teacherTask.setStatus(TeacherTask.Status.FINISHED.name());
            progress.setStatus(TeacherTask.Status.FINISHED.name());
            progress.getRewards().get(0).setReceived(true);
            taskDao.upsert(teacherTask);
            teacherTaskProgressDao.upsert(progress);
        }
        mapMessage.set("teacherTaks", JsonUtils.toJson(teacherTask)).set("progress", JsonUtils.toJson(progress));
        return mapMessage;
    }

    @Override
    public MapMessage deleteTeacherTask(Long teacherId) {
        List<TeacherTask> teacherTasks = taskDao.loadByTeacherId(teacherId);
        List<TeacherTaskProgress> teacherTaskProgressList = teacherTaskProgressDao.loadTeacherProgress(teacherId);
        for (TeacherTask teacherTask : teacherTasks) {
            teacherTask.setDisabled(true);
            taskDao.upsert(teacherTask);
        }
        for (TeacherTaskProgress teacherTaskProgress : teacherTaskProgressList) {
            teacherTaskProgressDao.remove(teacherTaskProgress.getId());
        }
        return MapMessage.successMessage().add("teacherId", teacherId);
    }

    @Override
    public MapMessage loadTeacherWeekTask(Long teacherId) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        List<TeacherTaskEntry> teacherTaskEntry = getTeacherTaskEntry(teacherId);
        TeacherTaskEntry taskEntry = teacherTaskEntry.stream().filter(i -> Objects.equals(i.getTaskTplId(), TeacherTaskTpl.Tpl.WEEK_CHECK_HOMEWORK_2019.getTplId())).findFirst().orElse(null);
        if (taskEntry == null) {
            return MapMessage.errorMessage("尚未领取任务");
        }

        boolean showIntegral = TeacherWeekTaskRewardCalc.showIntegral(teacherDetail.getCityCode());

        Set<Long> allTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Clazz> allClazz = deprecatedClazzLoaderClient.getRemoteReference()
                .loadTeacherClazzs(allTeacherIds).values().stream()
                .flatMap(Collection::stream)
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(Comparator.comparing(Clazz::getClazzLevel).thenComparing(Clazz::formalizeClazzName))
                .distinct()
                .collect(toList());

        TeacherTaskProgress.SubTaskProgress firstSubTask = taskEntry.getSubTaskProgressList().get(0);
        Object clazzList = firstSubTask.getVars().get("clazzList");
        String json = JsonUtils.toJson(clazzList);
        List<ClazzDetail> clazzDetailList = JsonUtils.fromJsonToList(json, ClazzDetail.class);
        Map<Long, ClazzDetail> clazzDetailMap = clazzDetailList.stream().collect(toMap(ClazzDetail::getClazzId, Function.identity()));

        List<TeacherTaskWeekMapper> resultList = new ArrayList<>();

        for (Clazz clazz : allClazz) {
            TeacherTaskWeekMapper mapper = new TeacherTaskWeekMapper();
            mapper.setClazzId(clazz.getId());
            mapper.setClazzName(clazz.formalizeClazzName());

            // 先预填充3次作业情况和活跃值
            List<TeacherTaskWeekMapper.Homework> originHomeworkList = initHomework(teacherDetail.getCityCode());

            ClazzDetail clazzDetail = clazzDetailMap.get(clazz.getId());

            if (clazzDetail != null) {
                // 使用自己的作业情况去覆盖预填充的作业情况
                List<ClazzDetail.HomeworkDetail> meHomeworkList = clazzDetail.getHomeworkList();
                for (int i = 0; i < meHomeworkList.size(); i++) {
                    ClazzDetail.HomeworkDetail meHomework = meHomeworkList.get(i);
                    TeacherTaskWeekMapper.Homework defaultHomework = originHomeworkList.get(i);

                    defaultHomework.setHomeworkId(meHomework.getHomeworkId());
                    defaultHomework.setFinishNum(meHomework.getFinishNum());
                    defaultHomework.setExpNum(meHomework.getExpNum());
                    defaultHomework.setFinish(true);
                    if (showIntegral) {
                        defaultHomework.setIntegralNum(meHomework.getIntegralNum());
                    }
                }
            }
            mapper.setHomeworkList(originHomeworkList);
            resultList.add(mapper);
        }
        return MapMessage.successMessage()
                .add("clazzList", resultList)
                .add("showIntegral", showIntegral)
                .add("subject", Optional.ofNullable(teacherDetail.getSubject()).map(Enum::name).orElse(null))
                .add("cityName", teacherDetail.getCityName());
    }

    private static final String CACHE_KEY = "BE:INVITE_TASK_2019";
    private static final Long CACHE_KEY_INIT = 3382L;

    @Override
    public Long getInviteTeacherCount() {
        CacheObject<Object> object = businessCacheSystem.CBS.storage.get(CACHE_KEY);
        if (object.containsValue()) {
            return Long.parseLong(object.getValue().toString().trim());
        }
        return CACHE_KEY_INIT;
    }

    @Override
    public Long incrInviteTeacherCount(Long teacherId) {
        try {
            AtomicCallback<Long> callback = () -> {
                String name = TeacherActivityEnum.INVI_CLICK_2019.name();
                TeacherActivityRef teacherActivityRef = userActivityService.loadUserActivity(teacherId, name);
                if (teacherActivityRef == null) {
                    userActivityService.saveUserActivity(teacherId, name);
                    return businessCacheSystem.CBS.storage.incr(CACHE_KEY, 1L, CACHE_KEY_INIT, getExpirationInSeconds());
                }
                return getInviteTeacherCount();
            };

            AtomicCallbackBuilderFactory.getInstance()
                    .<Long>newBuilder()
                    .keyPrefix("incrInviteTeacherCount")
                    .keys(teacherId)
                    .callback(callback)
                    .build()
                    .execute();
        } catch (Exception ignord) {
        }
        return 0L;
    }

    @Override
    public Long setInviteTeacherCount(Long incr) {
        businessCacheSystem.CBS.storage.delete(CACHE_KEY);
        return businessCacheSystem.CBS.storage.incr(CACHE_KEY, 1L, incr, getExpirationInSeconds());
    }

    /**
     * 距离 6月1号还有多少秒
     */
    private int getExpirationInSeconds() {
        return SafeConverter.toInt(DateUtils.getCurrentToDateEndSecond(new Date(1559318400000L)));
    }

    private List<TeacherTaskWeekMapper.Homework> initHomework(Integer cityCode) {
        List<TeacherTaskWeekMapper.Homework> result = new ArrayList<>();
        TeacherWeekTaskRewardCalc.CityReward cityReward = TeacherWeekTaskRewardCalc.getCityReward(cityCode);
        for (int i = 0; i < 3; i++) {
            TeacherTaskWeekMapper.Homework item = new TeacherTaskWeekMapper.Homework();
            if (i == 0) {
                item.setExpNum(cityReward.getOneExp());
            } else if (i == 1) {
                item.setExpNum(cityReward.getTwoExp());
            } else {
                item.setExpNum(cityReward.getThreeExp());
            }
            result.add(item);
        }
        return result;
    }
}
