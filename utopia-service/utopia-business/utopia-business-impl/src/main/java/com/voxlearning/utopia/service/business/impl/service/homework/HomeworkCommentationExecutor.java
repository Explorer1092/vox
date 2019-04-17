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

package com.voxlearning.utopia.service.business.impl.service.homework;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Named
@Scope("prototype")
public class HomeworkCommentationExecutor extends BusinessServiceSpringBean {

    public Long teacherId;
    public List<Long> studentIds;
    public String comment;
    public Serializable homeworkId;
    public HomeworkType homeworkType;
    public Integer integral;
    //班级中剩余学豆
    public Integer poolIntegral;
    public Long groupId;

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;

    public void execute() {
        if (teacherId == null) {
            logger.warn("No teacher id specified");
            return;
        }
        if (CollectionUtils.isEmpty(studentIds)) {
            logger.warn("No student selected for homework commentation");
            return;
        }
        if (integral < 0) {
            logger.warn("Teacher homework history reward student integral number wrong");
            return;
        }
        int studentIntegral = integral / studentIds.size();
        if (integral > 0) {
            try {
                MapMessage message;
                if (poolIntegral < integral) {
                    // 去兑换 看看差几个
                    int diff = integral - poolIntegral;
                    int deductGold = diff / 5 + (diff % 5 > 0 ? 1 : 0);
                    // 扣减老师金币
                    IntegralHistory integralHistory = new IntegralHistory();
                    integralHistory.setIntegral(deductGold * -10);
                    integralHistory.setComment(IntegralType.智慧教室老师兑换学豆.getDescription());
                    integralHistory.setIntegralType(IntegralType.智慧教室老师兑换学豆.getType());
                    integralHistory.setUserId(teacherId);
                    message = userIntegralService.changeIntegral(userLoaderClient.loadUser(teacherId), integralHistory);
                    if (!message.isSuccess()) {
                        logger.warn("teacher comment decr integral error, teacher {}", teacherId);
                    }
                    // 先充值
                    ClazzIntegralHistory history = new ClazzIntegralHistory();
                    history.setGroupId(groupId);
                    history.setClazzIntegralType(ClazzIntegralType.老师兑换班级学豆.getType());
                    history.setIntegral(deductGold * 5);
                    history.setComment(ClazzIntegralType.老师兑换班级学豆.getDescription());
                    history.setAddIntegralUserId(teacherId);
                    message = clazzIntegralServiceClient.getClazzIntegralService()
                            .changeClazzIntegral(history)
                            .getUninterruptibly();
                    if (!message.isSuccess()) {
                        logger.warn("teacher write comment change clazz pool fail, groupId {}", groupId);
                    }

                    // 执行发放
                    ClazzIntegralHistory decrHistory = new ClazzIntegralHistory();
                    decrHistory.setGroupId(groupId);
                    decrHistory.setClazzIntegralType(ClazzIntegralType.老师写评语奖励学生.getType());
                    decrHistory.setIntegral(-integral);
                    decrHistory.setComment(ClazzIntegralType.老师写评语奖励学生.getDescription());
                    message = clazzIntegralServiceClient.getClazzIntegralService()
                            .changeClazzIntegral(decrHistory)
                            .getUninterruptibly();
                    if (!message.isSuccess()) {
                        logger.warn("teacher write comment change clazz pool fail, groupId {}", groupId);
                    }
                } else {
                    ClazzIntegralHistory decrHistory = new ClazzIntegralHistory();
                    decrHistory.setGroupId(groupId);
                    decrHistory.setClazzIntegralType(ClazzIntegralType.老师写评语奖励学生.getType());
                    decrHistory.setIntegral(-integral);
                    decrHistory.setComment(ClazzIntegralType.老师写评语奖励学生.getDescription());
                    message = clazzIntegralServiceClient.getClazzIntegralService()
                            .changeClazzIntegral(decrHistory)
                            .getUninterruptibly();
                    if (!message.isSuccess()) {
                        logger.warn("teacher write comment change clazz pool fail, groupId {}", groupId);
                    }
                }
                if (message.isSuccess()) {
                    List<HomeworkComment> homeworkComments = new LinkedList<>();
                    //给学生奖励学豆
                    for (Long studentId : studentIds) {
                        HomeworkComment homeworkComment = new HomeworkComment();
                        homeworkComment.setStudentId(studentId);
                        homeworkComment.setTeacherId(teacherId);
                        homeworkComment.setComment(comment);
                        homeworkComment.setRewardIntegral(studentIntegral);
                        homeworkComment.setHomeworkId(ConversionUtils.toString(homeworkId, null));
                        homeworkComment.setHomeworkType(homeworkType != null ? homeworkType.name() : null);
                        homeworkComments.add(homeworkComment);

                        //给学生奖励学豆
                        IntegralHistory studentHistory = new IntegralHistory(studentId, IntegralType.学生通过评语收到老师奖励学豆, studentIntegral);
                        studentHistory.setComment("学生通过评语收到老师奖励学豆");
                        message = userIntegralService.changeIntegral(userLoaderClient.loadUser(studentId), studentHistory);
                        if (!message.isSuccess()) {
                            logger.warn("teacher comment reward student integral fail, student is {}", studentId);
                        }
                    }
                    homeworkCommentServiceClient.createHomeworkComments(homeworkComments);
                    // need review:奖励学豆后给学生家长推送一条模版消息
                    Map<String, Object> extensionInfo = new HashMap<>();
                    extensionInfo.put("integral", studentIntegral);
                    wechatServiceClient.processWithStudents(WechatNoticeProcessorType.XxtTeacherSendIntegralNoticeAfterCheckHomework, studentIds, extensionInfo, WechatType.PARENT);
                }
            } catch (Exception ex) {
                logger.error("teacher homework history reward student integral error, {}", ex.getMessage());
            }
        } else {
            List<HomeworkComment> homeworkComments = new LinkedList<>();
            for (Long studentId : studentIds) {
                HomeworkComment homeworkComment = new HomeworkComment();
                homeworkComment.setStudentId(studentId);
                homeworkComment.setTeacherId(teacherId);
                homeworkComment.setComment(comment);
                homeworkComment.setRewardIntegral(studentIntegral);
                homeworkComment.setHomeworkId(ConversionUtils.toString(homeworkId, null));
                homeworkComment.setHomeworkType(homeworkType != null ? homeworkType.name() : null);
                homeworkComments.add(homeworkComment);
            }
            homeworkCommentServiceClient.createHomeworkComments(homeworkComments);
        }

    }
}
