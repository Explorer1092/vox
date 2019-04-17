package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
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
import java.util.List;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwe.liao
 * @since 2016/4/18
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/parent/usershare")
public class ParentUserShareApiController extends AbstractParentApiController {

    @Inject
    private JxtServiceClient jxtServiceClient;

    /**
     * 只有1.5.0版本在用这个接口。版本升级过后可以删掉这个接口
     */
    @RequestMapping(value = "vote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage voteUserShare() {
        String typeId = getRequestString(REQ_USER_SHARE_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_USER_SHARE_ID, "作业分享ID");
                validateRequest(REQ_USER_SHARE_ID, REQ_STUDENT_ID);
        } catch (IllegalVendorUserException e) {
            return failMessage(e);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User parent = getCurrentParent();
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }

        JxtVoteType voteType = JxtVoteType.HOMEWORK_SHARE;
        JxtUserShare userShare = jxtLoaderClient.getUserShareById(typeId);
        if (userShare == null) {
            return failMessage(RES_RESULT_USER_SHARE_NOT_EXIST);
        }
        Long ownerId = userShare.getUserId();
        //不能给自己点赞
        if (ownerId.equals(parent.getId())) {
            return failMessage(RES_RESULT_USER_SHARE_ERROR_SELF_VOTE);
        }
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
            return successMessage().add(RES_RESULT_USER_SHARE_VOTE_USER_NAME, userShare.getUserName());
        } else {
            return failMessage(mapMessage.getInfo());
        }
    }
}
