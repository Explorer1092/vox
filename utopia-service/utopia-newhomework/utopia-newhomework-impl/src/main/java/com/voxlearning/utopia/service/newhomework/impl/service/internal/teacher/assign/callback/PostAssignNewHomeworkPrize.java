/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.mongodb.DuplicateKeyException;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralPool;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.api.support.IntegralCalculator;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkPrize;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;
import lombok.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.教师设置作业奖励_产品平台;

/**
 * @author Administrator
 * @since 2016/6/24
 */
@Named
public class PostAssignNewHomeworkPrize extends NewHomeworkSpringBean implements PostAssignHomework {

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;

    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        LinkedHashMap<Long, NewHomework> assignedHomeworks = context.getAssignedGroupHomework();
        if (Objects.isNull(teacher) || Objects.isNull(context) || Objects.isNull(context.getSource()) || Objects.isNull(context.getSource().get("prize")) || MapUtils.isEmpty(assignedHomeworks))
            return;
        int prize = SafeConverter.toInt(context.getSource().get("prize"));
        //判断条件
        if (prize <= 0)
            return;
        // 教师当前可用园丁豆数量和需要扣除的园丁豆数量
        UserIntegral integral = IntegralCalculator.calculateUserIntegral(com.voxlearning.utopia.api.constant.Currency.GOLD_COIN,
                teacherLoaderClient.loadMainSubTeacherIntegral(teacher.getId()));
        if (Objects.isNull(integral)) return;
        //因为是统一的Newhomework所以统一往上，不放入下面的循环中，提交效率
        Subject subject = teacher.getSubject();

        Map<Long, HomeworkData> gid_hd_map = new HashMap<>();
        for (Long groupId : assignedHomeworks.keySet()) {//布置班级的相关作业
            NewHomework h = assignedHomeworks.get(groupId);
            if (Objects.isNull(h.getId()) || Objects.isNull(h.getCreateAt())) {
                logger.error(Objects.isNull(h.getId()) ? "homework id is null " : "homework create time is null");
                return;
            }
            gid_hd_map.put(groupId, new HomeworkData(h.getId(), h.getCreateAt()));
        }
        // 获取每个组的学豆池，正常情况下这里应该都初始化过了，如果对不上，以获取到学豆池班级为准
        Map<Long, SmartClazzIntegralPool> gid_pool_map = clazzIntegralServiceClient.getClazzIntegralService()
                .loadClazzIntegralPools(gid_hd_map.keySet())
                .getUninterruptibly();//相关的学豆池获取，然后操作
        if (MapUtils.isEmpty(gid_pool_map))
            return;
        List<NewHomeworkPrize> prizes = new ArrayList<>();
        Map<Long, Integer> gid_update_param_map = new HashMap<>();
        int deduction = 0;
        Set<Long> groupIds_hp = new HashSet<>(); // 这些组的作业有奖励
        for (Long groupId : gid_pool_map.keySet()) {
            SmartClazzIntegralPool pool = gid_pool_map.get(groupId);
            if (Objects.isNull(pool))
                continue;
            if (pool.fetchTotalIntegral() < prize) { // 需要充值
                int delta = prize - pool.fetchTotalIntegral();
                // 将delta按1：5换算成园丁豆，这是教师需要扣除的
                int gold = new BigDecimal(delta).divide(new BigDecimal(5), 0, BigDecimal.ROUND_UP).intValue();
                deduction += gold;
                gid_update_param_map.put(groupId, gold);
            }
            HomeworkData hd = gid_hd_map.get(groupId);
            String day = DayRange.newInstance(hd.getCreateDatetime().getTime()).toString();
            NewHomeworkPrize hp = new NewHomeworkPrize();
            NewHomeworkPrize.ID id = new NewHomeworkPrize.ID(day, subject, hd.getHomeworkId());
            hp.setId(id.toString());
            hp.setCreateAt(new Date());
            hp.setUpdateAt(new Date());
            hp.setTeacherId(teacher.getId());
            hp.setQuantity(prize);
            hp.setDetails(new LinkedHashMap<>());
            prizes.add(hp);
            groupIds_hp.add(groupId);
        }
        if (integral.getUsable() < deduction) return;
        if (deduction > 0) {//deduction大于零的时候需要充值相应的处理
            IntegralHistory history = new IntegralHistory(teacher.getId(), 教师设置作业奖励_产品平台, -deduction * 10);
            history.setComment("教师设置作业奖励扣除园丁豆");
            if (userIntegralService.changeIntegral(teacher, history).isSuccess()) {
                for (Long gid : gid_update_param_map.keySet()) {
                    Integer param = gid_update_param_map.get(gid);
                    ClazzIntegralHistory clazzIntegralHistory = new ClazzIntegralHistory();
                    clazzIntegralHistory.setGroupId(gid);
                    clazzIntegralHistory.setClazzIntegralType(ClazzIntegralType.老师兑换班级学豆.getType());
                    clazzIntegralHistory.setIntegral(param * 5);
                    clazzIntegralHistory.setComment(ClazzIntegralType.老师兑换班级学豆.getDescription());
                    clazzIntegralHistory.setAddIntegralUserId(teacher.getId());
                    MapMessage message = clazzIntegralServiceClient.getClazzIntegralService()
                            .changeClazzIntegral(clazzIntegralHistory)
                            .getUninterruptibly();
                    if (!message.isSuccess()) {
                        logger.warn("homework prize teacher exchange clazz integral fail, teacher {}, group {}", gid, teacher.getId());
                    }
                }
            }
        }
        try {
            newHomeworkPrizeDao.inserts(prizes);
        } catch (Exception ex) {
            if (ex instanceof DuplicateKeyException) {
                logger.warn("Failed to inserts newHomeworkPrize duplicated userId{} prizes{}", context.getTeacher().getId(), JsonUtils.toJson(prizes));
            } else {
                logger.warn("Failed to inserts newHomeworkPrize userId{} prizes{}", context.getTeacher().getId(), JsonUtils.toJson(prizes));
            }
        }

        context.getGroupIdsWithHomeworkPrize().addAll(groupIds_hp);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class HomeworkData {
        @NonNull
        private String homeworkId;
        @NonNull
        private Date createDatetime;
    }

}
