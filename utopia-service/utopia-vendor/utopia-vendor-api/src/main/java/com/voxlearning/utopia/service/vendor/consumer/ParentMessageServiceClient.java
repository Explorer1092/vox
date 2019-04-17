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

package com.voxlearning.utopia.service.vendor.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.vendor.api.ParentMessageService;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Jia HuanYin
 * @since 2015/9/16
 */
public class ParentMessageServiceClient implements ParentMessageService {


    @ImportService(interfaceClass = ParentMessageService.class)
    private ParentMessageService remoteReference;
    @Inject
    private AppMessageServiceClient appMessageServiceClient;


//    public boolean smartClazzReward(String teacherName, List<User> students,
//                                    ParentLoader parentLoader) {
//        if (CollectionUtils.isEmpty(students)) {
//            return false;
//        }
//        Map<Long, User> iStudents = new HashMap<>();
//        students.forEach(student -> iStudents.put(student.getId(), student));
//        Map<Long, List<StudentParent>> studentParents = parentLoader.loadStudentParents(new HashSet<>(iStudents.keySet()));
//        if (MapUtils.isEmpty(studentParents)) {
//            return false;
//        }
//        String teacher = teacherName == null ? "" : teacherName;
//        String content = "家长您好：\n" + teacher + "老师刚刚在课堂上奖励您的孩子。";
//        String linkUrl = "/parentMobile/homework/loadsmart.vpage?sid=";
//        for (Long studentId : studentParents.keySet()) {
//            List<StudentParent> iParents = studentParents.get(studentId);
//            if (CollectionUtils.isNotEmpty(iParents)) {
//                List<Long> parentIds = iParents.stream().map(StudentParent::getParentUser).map(User::getId).collect(Collectors.toList());
//                String iContent = iStudents.get(studentId).fetchRealname() + content;
//                String iLink = "/parentMobile/homework/loadsmart.vpage?sid=" + SafeConverter.toString(studentId);
//                List<AppUserMessage> messageList = new ArrayList<>();
//                Map<String, Object> extInfo = new HashMap<>();
//                extInfo.put("studentId", studentId);
//                extInfo.put("tag", ParentMessageTag.课堂奖励.name());
//                extInfo.put("type", ParentMessageType.REMINDER.name());
//                extInfo.put("senderName", teacher);
//                for (Long parentId : parentIds) {
//                    //新消息中心
//                    AppUserMessage message = new AppUserMessage();
//                    message.setUserId(parentId);
//                    message.setContent(iContent);
//                    message.setLinkType(1);
//                    message.setLinkUrl(iLink);
//                    message.setImageUrl("");
//                    message.setExtInfo(extInfo);
//                    message.setMessageType(ParentMessageType.REMINDER.getType());
//                    messageList.add(message);
//                }
//                messageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppUserMessage);
//                //发送jpush
//                Map<String, Object> extras = new HashMap<>();
//                extras.put("studentId", studentId);
//                extras.put("url", linkUrl);
//                extras.put("tag", ParentMessageTag.课堂奖励.name());
//                //新的push参数
//                extras.put("s", ParentAppPushType.CLASS_REWARD.name());
//                appMessageServiceClient.sendAppJpushMessageByIds(iContent, AppMessageSource.PARENT, parentIds, extras);
//
//            }
//        }
//        return true;
//    }


    /**
     * @param parentIds  必传
     * @param studentId  可选
     * @param content    必传
     * @param imageUrl   可选
     * @param linkUrl    可选
     * @param senderName 可选
     * @param tag        必传
     * @param type       必传
     */
    @Override
    public boolean postParentMessage(List<Long> parentIds, Long studentId, String content, String imageUrl, String linkUrl, String senderName, ParentMessageTag tag,
                                     ParentMessageType type) {
        return remoteReference.postParentMessage(parentIds, studentId, content, imageUrl, linkUrl, senderName, tag, type);
    }

    @Override
    public boolean postParentMessage(List<Long> parentIds, Long studentId, String content, String imageUrl, Integer linkType, String linkUrl, String senderName, ParentMessageTag tag, ParentMessageType type) {
        return remoteReference.postParentMessage(parentIds, studentId, content, imageUrl, linkType, linkUrl, senderName, tag, type);
    }

}
