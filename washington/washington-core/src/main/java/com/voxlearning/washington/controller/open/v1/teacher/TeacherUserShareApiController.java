package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
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
import java.util.List;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 老师端分享点赞
 * Created by Shuai Huan on 2016/4/25.
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/teacher/usershare/")
public class TeacherUserShareApiController extends AbstractTeacherApiController {
    @Inject
    private JxtServiceClient jxtServiceClient;

    /**
     * 只有老师端家校通第一个版本在用这个接口。版本升级过后可以删掉这个接口
     */
    @RequestMapping(value = "/vote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage voteUserShare() {
        //点赞类型的Id。兼容第一版1.5.0。这个参数命名就是REQ_USER_SHARE_ID了。不改了。
        String typeId = getRequestString(REQ_USER_SHARE_ID);
        try {
            validateRequired(REQ_USER_SHARE_ID, "作业分享ID");
            validateRequest(REQ_USER_SHARE_ID);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User teacher = getCurrentTeacher();
        JxtVoteType voteType = JxtVoteType.HOMEWORK_SHARE;
        JxtUserShare userShare = jxtLoaderClient.getUserShareById(typeId);
        if (userShare == null) {
            return failMessage(RES_RESULT_USER_SHARE_NOT_EXIST);
        }
        Long ownerId = userShare.getUserId();
        //不能给自己点赞
        if (ownerId.equals(teacher.getId())) {
            return failMessage(RES_RESULT_USER_SHARE_ERROR_SELF_VOTE);
        }
        String typeAndId = JxtUserVoteRecord.generateTypeAndId(voteType, typeId);
        List<JxtUserVoteRecord> voteRecords = jxtLoaderClient.getVoteRecordByUserId(teacher.getId());
        if (CollectionUtils.isNotEmpty(voteRecords) && voteRecords.stream().anyMatch(p -> typeAndId.equals(p.getTypeAndId()))) {
            //已经点过赞了
            return failMessage(RES_RESULT_USER_SHARE_ERROR_HAD_VOTE);
        }

        JxtUserVoteRecord voteRecord = new JxtUserVoteRecord();
        voteRecord.setTypeId(typeId);
        voteRecord.setVoteType(voteType);
        voteRecord.setUserId(teacher.getId());
        voteRecord.setUserName(teacher.fetchRealname() + "老师");
        MapMessage mapMessage = jxtServiceClient.saveUserVoteRecord(voteRecord,ownerId);
        if (mapMessage.isSuccess()) {
            jxtServiceClient.updateCacheWithSaveVoteRecord(voteRecord,ownerId);
            return successMessage().add(RES_RESULT_USER_SHARE_VOTE_USER_NAME, userShare.getUserName());
        } else {
            return failMessage(mapMessage.getInfo());
        }
    }
}
