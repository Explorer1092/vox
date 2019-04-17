package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.entity.task.TeacherMonthTask;
import com.voxlearning.utopia.service.business.api.TeacherMonthTaskService;
import com.voxlearning.utopia.service.business.impl.dao.TeacherMonthTaskDao;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Slf4j
@ExposeService(interfaceClass = TeacherMonthTaskService.class)
public class TeacherMonthTaskServiceImpl implements TeacherMonthTaskService {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @Inject
    private GroupLoaderClient groupLoaderClient;
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    private TeacherMonthTaskDao teacherMonthTaskDao;
    @Inject
    private TeacherRookieTaskServiceImpl teacherRookieTaskService;

    @Inject private RaikouSDK raikouSDK;

    @Override
    public Boolean allowMonthTask(Long teacherId) {
        return false;
        /*TeacherDetail td = teacherLoaderClient.loadTeacherDetail(teacherId);

        boolean authSuccess = Objects.equals(td.getAuthenticationState(), AuthenticationState.SUCCESS.getState());
        Boolean rookieFinished = teacherRookieTaskService.rookieFinished(teacherId);
        boolean monthStart = new Date().getTime() >= MONTH_START_TIME;

        return (rookieFinished || authSuccess) && monthStart;*/
    }

    @Override
    public MapMessage receiveMonthTask(Long teacherId) {
        try {
            AtomicCallback<MapMessage> callback = () -> {
                TeacherMonthTask task = loadMonthTask(teacherId);
                if (task == null) {
                    Boolean allow = allowMonthTask(teacherId);
                    if (!allow) {
                        MapMessage.errorMessage("不符合领取任务的条件");
                    }

                    TeacherMonthTask newTask = startTask(teacherId);

                    // 初始化一下班级, 不然没班时展示页不太好看
                    Set<Clazz> teacherClazz = getTeacherClazz(teacherId);
                    for (Clazz clazz : teacherClazz) {
                        TeacherMonthTask.GroupDetail groupDetail = new TeacherMonthTask.GroupDetail();
                        groupDetail.setClazzId(clazz.getId());
                        groupDetail.setClazzName(clazz.formalizeClazzName());
                        groupDetail.setClazzLevel(clazz.getClazzLevel().getLevel());
                        newTask.getGroups().add(groupDetail);
                    }

                    teacherMonthTaskDao.upsert(newTask);
                    return MapMessage.successMessage().add("task", newTask);
                } else {
                    return MapMessage.errorMessage("不可重复领取");
                }
            };

            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("business:receiveRookieTask")
                    .keys(teacherId)
                    .callback(callback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public TeacherMonthTask loadMonthTask(Long teacherId) {
        TeacherMonthTask task = teacherMonthTaskDao.load(teacherId);
        if (task != null) {
            Date now = new Date();
            if (now.getTime() > task.getExpireDate().getTime()) {
                task.restartTask();
            }
            task.getGroups().sort(new Comparator<TeacherMonthTask.GroupDetail>() {
                @Override
                public int compare(TeacherMonthTask.GroupDetail o1, TeacherMonthTask.GroupDetail o2) {
                    return o1.getClazzLevel().compareTo(o2.getClazzLevel());
                }
            }.thenComparing(new Comparator<TeacherMonthTask.GroupDetail>() {
                @Override
                public int compare(TeacherMonthTask.GroupDetail o1, TeacherMonthTask.GroupDetail o2) {
                    return o1.getClazzName().compareTo(o2.getClazzName());
                }
            }));
        }
        return task;
    }

    @Override
    public MapMessage loadMonthTaskMsg(Long teacherId) {
        TeacherMonthTask task = loadMonthTask(teacherId);
        if (task == null) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().add("task", task);
    }

    @Override
    public MapMessage updateProgress(Long teacherId, Long groupId, TeacherMonthTask.Homework homework) {
        MapMessage paramsErrorMsg = MapMessage.errorMessage("参数错误");

        if (teacherId == null || groupId == null || homework == null) {
            return paramsErrorMsg;
        }
        Integer studentCount = homework.getStudentCount();

        TeacherMonthTask monthTask = loadMonthTask(teacherId);
        if (monthTask == null) {
            return MapMessage.errorMessage("没有该任务");
        }

        Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId).getUninterruptibly();
        if (group == null) {
            return paramsErrorMsg;
        }
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadClazz(group.getClazzId());
        if (clazz == null) {
            return paramsErrorMsg;
        }

        // 如果是一个全新的组
        boolean exists = monthTask.getGroups().stream().anyMatch(i -> Objects.equals(i.getClazzId(), clazz.getId()));
        if (!exists) {
            TeacherMonthTask.GroupDetail newGroup = new TeacherMonthTask.GroupDetail();
            newGroup.setClazzId(clazz.getId());
            newGroup.setClazzLevel(clazz.getClazzLevel().getLevel());
            newGroup.setClazzName(clazz.formalizeClazzName());
            monthTask.getGroups().add(newGroup);
        }

        for (TeacherMonthTask.GroupDetail groupDetail : monthTask.getGroups()) {
            if (!Objects.equals(groupDetail.getClazzId(), clazz.getId())) continue;

            if (groupDetail.getFirst() && groupDetail.getSecond() && groupDetail.getThird()) continue;

            groupDetail.getHomework().add(homework);

            int integralNum = RuntimeMode.isProduction() ? getRewardIntegral(studentCount, groupDetail) : getTestRewardIntegral(studentCount, groupDetail);

            if (integralNum > 0) {
                sendReward(teacherId, integralNum);
                teacherMonthTaskDao.upsert(monthTask);
            }
        }
        return MapMessage.successMessage();
    }

    private TeacherMonthTask startTask(Long teacherId) {
        return TeacherMonthTask.newInstance(teacherId);
    }

    private void sendReward(Long teacherId, Integer sendIntegral) {
        if (Objects.equals(sendIntegral, 0)) return;

        TeacherDetail td = teacherLoaderClient.loadTeacherDetail(teacherId);
        // 小学学豆乘以10
        if (td.isPrimarySchool()) {
            sendIntegral = sendIntegral * 10;
        }
        IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.TEACHER_GROWTH_REWARD, sendIntegral);
        integralHistory.setComment("月活跃任务奖励!");
        MapMessage chgIntegralResult = userIntegralService.changeIntegral(integralHistory);
        if (!chgIntegralResult.isSuccess()) {
            log.error("月活跃任务奖励发放失败 tid:+" + teacherId + " msg:" + chgIntegralResult.getInfo());
        }
    }

    private int getRewardIntegral(Integer studentCount, TeacherMonthTask.GroupDetail groupDetail) {
        int integralNum = 0;                // 发放学豆数
        if (!groupDetail.getFirst()) {
            if (studentCount >= 50) {
                integralNum = 30;
            } else if (studentCount >= 30) {
                integralNum = 20;
            } else if (studentCount >= 10) {
                integralNum = 10;
            }
            if (integralNum > 0) groupDetail.setFirst(true);
        } else if (!groupDetail.getSecond()) {
            if (studentCount >= 50) {
                integralNum = 80;
            } else if (studentCount >= 30) {
                integralNum = 70;
            } else if (studentCount >= 10) {
                integralNum = 60;
            }
            if (integralNum > 0) groupDetail.setSecond(true);
        } else if (!groupDetail.getThird()) {
            if (studentCount >= 50) {
                integralNum = 100;
            } else if (studentCount >= 30) {
                integralNum = 90;
            } else if (studentCount >= 10) {
                integralNum = 80;
            }
            if (integralNum > 0) groupDetail.setThird(true);
        }
        return integralNum;
    }

    private int getTestRewardIntegral(Integer studentCount, TeacherMonthTask.GroupDetail groupDetail) {
        int integralNum = 0;                // 发放学豆数
        if (!groupDetail.getFirst()) {
            if (studentCount >= 4) {
                integralNum = 30;
            } else if (studentCount >= 3) {
                integralNum = 20;
            } else if (studentCount >= 2) {
                integralNum = 10;
            }
            if (integralNum > 0) groupDetail.setFirst(true);
        } else if (!groupDetail.getSecond()) {
            if (studentCount >= 4) {
                integralNum = 80;
            } else if (studentCount >= 3) {
                integralNum = 70;
            } else if (studentCount >= 2) {
                integralNum = 60;
            }
            if (integralNum > 0) groupDetail.setSecond(true);
        } else if (!groupDetail.getThird()) {
            if (studentCount >= 4) {
                integralNum = 100;
            } else if (studentCount >= 3) {
                integralNum = 90;
            } else if (studentCount >= 2) {
                integralNum = 80;
            }
            if (integralNum > 0) groupDetail.setThird(true);
        }
        return integralNum;
    }

    private Set<Clazz> getTeacherClazz(Long mainTeacherId) {
        List<Long> teacherIds = teacherLoaderClient.loadSubTeacherIds(mainTeacherId);
        HashSet<Long> teacherIdSet = new HashSet<>(teacherIds);
        teacherIdSet.add(mainTeacherId);

        return teacherLoaderClient.loadTeachersClazzIds(teacherIdSet)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(clazzId -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId))
                .filter(Objects::nonNull)
                .filter(clazz -> !clazz.isTerminalClazz())
                .collect(Collectors.toSet());
    }

}
