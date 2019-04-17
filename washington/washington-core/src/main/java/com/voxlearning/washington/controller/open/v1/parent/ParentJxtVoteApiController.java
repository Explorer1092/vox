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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.vendor.api.constant.JxtVoteType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtUserShare;
import com.voxlearning.utopia.service.vendor.api.entity.JxtUserVoteRecord;
import com.voxlearning.utopia.service.vendor.consumer.JxtServiceClient;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwe.liao
 * @since 2016-5-31
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/parent/jxt")
public class ParentJxtVoteApiController extends AbstractParentApiController {

    @Inject
    private JxtServiceClient jxtServiceClient;

    /**
     * 1.5.1之后的启用原生通用点赞接口
     */
    @RequestMapping(value = "/vote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage voteUserShare() {
        String typeId = getRequestString(REQ_VOTE_TYPE_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String voteTypeName = getRequestString(REQ_VOTE_TYPE_NAME);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_VOTE_TYPE_ID, "点赞类型ID");
            validateRequired(REQ_EASE_MOB_ID, "聊天组ID");
            //这里兼容1.5.1以上版本对1.5.0的作业动态点赞 不做这个类型的强制校验了。
//            validateRequired(REQ_VOTE_TYPE_NAME, "点赞类型");
            validateRequest(REQ_VOTE_TYPE_ID, REQ_STUDENT_ID, REQ_VOTE_TYPE_NAME, REQ_EASE_MOB_ID);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }

        Long ownerId = 0L;
        String votedUserName = "";
        JxtVoteType voteType = JxtVoteType.parse(voteTypeName);
        if (JxtVoteType.UNKNOWN == voteType) {
            //这里要兼容一下1.5.0版本发布的作业分享在1.5.1以上的版本查看的时候。新版客户端是获取不到voteType的。
            //所以这里先按照作业分享查一遍。查不到再返回点赞类型错误
            JxtUserShare userShare = jxtLoaderClient.getUserShareById(typeId);
            if (userShare == null) {
                return failMessage(RES_RESULT_ERROR_VOTE_TYPE_ERROR);
            }
            ownerId = userShare.getUserId();
            votedUserName = userShare.getUserName();
        }
        if (JxtVoteType.HOMEWORK_SHARE == voteType) {
            JxtUserShare userShare = jxtLoaderClient.getUserShareById(typeId);
            if (userShare == null) {
                return failMessage(RES_RESULT_USER_SHARE_NOT_EXIST);
            }
            ownerId = userShare.getUserId();
            votedUserName = userShare.getUserName();
        } else if (JxtVoteType.VOICE_RECOMMEND == voteType) {
            List<VoiceRecommend> recommendList = voiceRecommendLoaderClient.loadExcludeNoRecommend(Collections.singleton(typeId));
            if (CollectionUtils.isEmpty(recommendList) || recommendList.get(0) == null) {
                return failMessage(RES_RESULT_VOICE_RECOMMEND_NOT_EXIST);
            }
            ownerId = recommendList.get(0).getTeacherId();
            Teacher teacher = teacherLoaderClient.loadTeacher(ownerId);
            String subjectName = teacher != null && teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
            votedUserName = teacher == null ? "" : subjectName + teacher.fetchRealname() + "老师";
        }
        //不能给自己点赞
        if (ownerId.equals(parent.getId())) {
            return failMessage(RES_RESULT_USER_SHARE_ERROR_SELF_VOTE);
        }
        //这里兼容1.5.1以上版本对1.5.0的作业动态点赞
        voteType = JxtVoteType.UNKNOWN == voteType ? JxtVoteType.HOMEWORK_SHARE : voteType;

        String typeAndId = JxtUserVoteRecord.generateTypeAndId(voteType, typeId);
        List<JxtUserVoteRecord> voteRecords = jxtLoaderClient.getVoteRecordByUserId(parent.getId());
        if (CollectionUtils.isNotEmpty(voteRecords) && voteRecords.stream().anyMatch(p -> typeAndId.equals(p.getTypeAndId()))) {
            //已经点过赞了
            return failMessage(RES_RESULT_USER_SHARE_ERROR_HAD_VOTE);
        }
        StudentParentRef studentParentRef = studentLoaderClient.loadStudentParentRefs(studentId).stream().filter(p -> p.getParentId().equals(parent.getId())).findFirst().orElse(null);
        Student student = studentLoaderClient.loadStudent(studentId);
        String userName = student.fetchRealname() + (CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName());

        JxtUserVoteRecord voteRecord = new JxtUserVoteRecord();
        voteRecord.setTypeId(typeId);
        voteRecord.setVoteType(voteType);
        voteRecord.setUserId(parent.getId());
        voteRecord.setUserName(userName);
        voteRecord.setStudentId(studentId);
        MapMessage mapMessage = jxtServiceClient.saveUserVoteRecord(voteRecord,ownerId);
        if (mapMessage.isSuccess()) {
            jxtServiceClient.updateCacheWithSaveVoteRecord(voteRecord,ownerId);
            String typeName = "";
            if (JxtVoteType.HOMEWORK_SHARE == voteType) {
                typeName = "作业分享";
            } else if (JxtVoteType.VOICE_RECOMMEND == voteType) {
                typeName = "语音推荐";
            }
            String content = userName + "给" + votedUserName + "的" + typeName + "点赞";
            return successMessage().add(RES_RESULT_VOTE_RESULT_CONTENT, content);
        } else {
            return failMessage(mapMessage.getInfo());
        }
    }
}
