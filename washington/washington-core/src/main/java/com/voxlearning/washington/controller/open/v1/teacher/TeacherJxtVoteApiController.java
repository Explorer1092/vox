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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.vendor.api.constant.JxtVoteType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtUserShare;
import com.voxlearning.utopia.service.vendor.api.entity.JxtUserVoteRecord;
import com.voxlearning.utopia.service.vendor.consumer.JxtServiceClient;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
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
@RequestMapping(value = "/v1/teacher/jxt/")
public class TeacherJxtVoteApiController extends AbstractTeacherApiController {
    @Inject
    private JxtServiceClient jxtServiceClient;

    /**
     * 1.5.1开始启用的通用点赞接口
     */
    @RequestMapping(value = "/vote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage voteUserShare() {
        String typeId = getRequestString(REQ_VOTE_TYPE_ID);
        String voteTypeName = getRequestString(REQ_VOTE_TYPE_NAME);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_VOTE_TYPE_ID, "点赞类型ID");
            //这里兼容1.5.1以上版本对1.5.0的作业动态点赞 不做这个类型的强制校验了。
            //validateRequired(REQ_VOTE_TYPE_NAME, "点赞类型");
            //1.2.0.21 ios 有bug. 没有传这个参数。先兼容一下
            //同步的安卓版本是1.2.0.1008。所以取了1.2.0.500这个版本来过滤
            if(VersionUtil.compareVersion(ver,"1.2.0.500")>0){
                validateRequired(REQ_EASE_MOB_ID, "聊天组ID");
                validateRequest(REQ_VOTE_TYPE_ID, REQ_VOTE_TYPE_NAME, REQ_EASE_MOB_ID);
            }else{
                validateRequest(REQ_VOTE_TYPE_ID, REQ_VOTE_TYPE_NAME);
            }

        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE);
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
            Teacher ownerTeacher = teacherLoaderClient.loadTeacher(ownerId);
            String subjectName = ownerTeacher != null && ownerTeacher.getSubject() != null ? ownerTeacher.getSubject().getValue() : "";
            votedUserName = ownerTeacher == null ? "" : subjectName + ownerTeacher.fetchRealname() + "老师";
        }
        //不能给自己点赞
        if (ownerId.equals(teacher.getId())) {
            return failMessage(RES_RESULT_USER_SHARE_ERROR_SELF_VOTE);
        }
        //这里兼容1.5.1以上版本对1.5.0的作业动态点赞
        voteType = JxtVoteType.UNKNOWN == voteType ? JxtVoteType.HOMEWORK_SHARE : voteType;

        String typeAndId = JxtUserVoteRecord.generateTypeAndId(voteType, typeId);
        List<JxtUserVoteRecord> voteRecords = jxtLoaderClient.getVoteRecordByUserId(teacher.getId());
        if (CollectionUtils.isNotEmpty(voteRecords) && voteRecords.stream().anyMatch(p -> typeAndId.equals(p.getTypeAndId()))) {
            //已经点过赞了
            return failMessage(RES_RESULT_USER_SHARE_ERROR_HAD_VOTE);
        }
        String currentTeacherSubjectName = teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
        String currentTeacherName = currentTeacherSubjectName + teacher.fetchRealname() + "老师";

        JxtUserVoteRecord voteRecord = new JxtUserVoteRecord();
        voteRecord.setTypeId(typeId);
        voteRecord.setVoteType(voteType);
        voteRecord.setUserId(teacher.getId());
        voteRecord.setUserName(currentTeacherName);
        MapMessage mapMessage = jxtServiceClient.saveUserVoteRecord(voteRecord,ownerId);
        if (mapMessage.isSuccess()) {
            jxtServiceClient.updateCacheWithSaveVoteRecord(voteRecord,ownerId);
            String typeName = "";
            if (JxtVoteType.HOMEWORK_SHARE == voteType) {
                typeName = "作业分享";
            } else if (JxtVoteType.VOICE_RECOMMEND == voteType) {
                typeName = "语音推荐";
            }
            String content = currentTeacherName + "给" + votedUserName + "的" + typeName + "点赞";
            return successMessage().add(RES_RESULT_VOTE_RESULT_CONTENT, content);
        } else {
            return failMessage(mapMessage.getInfo());
        }
    }
}
