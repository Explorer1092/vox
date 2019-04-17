/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkPrize;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkPrizeDao;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author tanguohong
 * @since 2016/6/24
 */
@Named
public class PostFinishNewHomeworkPrize extends SpringContainerSupport implements PostFinishHomework {

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;

    @Inject private NewHomeworkPrizeDao newHomeworkPrizeDao;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        if (context.getHomework().isHomeworkTerminated()) return;

        String homeworkId = context.getHomeworkId();
        Subject subject = context.getHomework().getSubject();
        // 判断此次完成的作业是否有红包，如果没有，滚粗
        String day = DayRange.newInstance(context.getHomework().getCreateAt().getTime()).toString();
        NewHomeworkPrize.ID id = new NewHomeworkPrize.ID(day, subject, homeworkId);
        NewHomeworkPrize prize = newHomeworkPrizeDao.load(id.toString());
        if (prize == null) return;
        // 该作业有奖励
        String key = CacheKeyGenerator.generateCacheKey("NEW_HOMEWORK_PRIZE",
                new String[]{"HID", "SUBJECT"},
                new Object[]{homeworkId, subject.name()});
        try {
            AtomicLockManager.instance().acquireLock(key);
        } catch (CannotAcquireLockException ignored) { // 没有获得锁，悲催啊，滚粗
            return;
        }

        try {
            // 获得锁，有机会获得红包了~~ 但是得看看红包里面还有没有学豆
            int left = prize.left();
            if (left <= 0) {
                send(context.getUserId(), 0, context.getTeacher());
                return;
            }

            // 还有剩余
            int max = new BigDecimal(left).divide(new BigDecimal(4), 0, BigDecimal.ROUND_UP).intValue();
            int random = RandomUtils.nextInt(0, max); // 获奖的学豆数量
            if (random <= 0) {
                send(context.getUserId(), 0, context.getTeacher());
                return;
            }

            // 中奖了，但是没准班级的余额不够扣~~ 所以需要重新算一下
            Long groupId = context.getHomework().getClazzGroupId();
            Map<Long, SmartClazzIntegralPool> poolMap = clazzIntegralServiceClient.getClazzIntegralService()
                    .loadClazzIntegralPools(Collections.singleton(groupId))
                    .getUninterruptibly();
            SmartClazzIntegralPool pool = poolMap.get(groupId);
            if (pool == null) {
                send(context.getUserId(), 0, context.getTeacher());
                return;
            }

            int real = Math.min(pool.fetchTotalIntegral(), random); // 实际获奖的学豆数量
            if (real <= 0) {
                send(context.getUserId(), 0, context.getTeacher());
                return;
            }

            IntegralHistory history = new IntegralHistory(context.getUserId(), IntegralType.学生获得作业奖励_产品平台, real);
            history.setComment("学生获得作业奖励");
            if (userIntegralService.changeIntegral(context.getUser(), history).isSuccess()) {
                ClazzIntegralHistory clazzIntegralHistory = new ClazzIntegralHistory();
                clazzIntegralHistory.setGroupId(groupId);
                clazzIntegralHistory.setClazzIntegralType(ClazzIntegralType.学生完成作业抽奖.getType());
                clazzIntegralHistory.setIntegral(-real);
                clazzIntegralHistory.setComment(ClazzIntegralType.学生完成作业抽奖.getDescription());
                history.setAddIntegralUserId(context.getUserId());
                MapMessage msg = clazzIntegralServiceClient.getClazzIntegralService()
                        .changeClazzIntegral(clazzIntegralHistory)
                        .getUninterruptibly();
                if (!msg.isSuccess()) {
                    logger.warn("student homework prize decr clazz integral fail, student {}, group {}", context.getUserId(), groupId);
                }
                String source = StringUtils.isBlank(context.getClientType()) ? "pc" : context.getClientType();
                newHomeworkPrizeDao.bingo(id, context.getUserId(), source, real, new Date());
                send(context.getUserId(), real, context.getTeacher());
            } else {
                send(context.getUserId(), 0, context.getTeacher());
            }
        } catch (Exception ex) {
            logger.error("STUDENT {} GET HOMEWORK {} PRIZE FAILED", context.getUser(), context.getHomework(), ex);
        } finally {
            AtomicLockManager.instance().releaseLock(key);
        }

    }

    // 发送app通知和右下角弹窗
    private void send(Long studentId, int prize, Teacher teacher) {
        // FIXME 学生PC的提醒不要了...
//        if (teacher == null || studentId == null) return;
//        String firstName = StringUtils.substring(teacher.fetchRealname(), 0, 1);
//        String content = "恭喜！你获得了" + firstName + "老师设置的" + prize + "学豆奖励";
//        if (prize <= 0) {
//            content = "很遗憾，没有抽到随机奖励，提醒老师推荐更多的练习吧";
//        }
//
//        // 发送右下角弹窗
//        userPopupServiceClient.createPopup(studentId)
//                .content(content)
//                .type(PopupType.GET_HOMEWORK_PRIZE)
//                .category(PopupCategory.LOWER_RIGHT)
//                .create();

    }
}
