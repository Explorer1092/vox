package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.UserAvatar;
import com.voxlearning.utopia.service.reward.consumer.newversion.RewardCenterClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Created by alex on 2018/2/27.
 */
@Controller
@Slf4j
@RequestMapping(value = "/userMobile/avatar")
public class MobileUserAvatarController extends AbstractMobileJxtController {

    @Inject
    private RewardCenterClient rewardCenterClient;

    @RequestMapping(value = "/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateAvatar() {
        User updatedUser = currentUser();
        if (updatedUser == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String avatarUrl = "";
        int key = getRequestInt("avatarCode");
        Integer avatarType = getRequestInt("avatarType");

        // 家长可能会更新孩子
        if (updatedUser.isParent()) {
            Long sid = getRequestLong("sid");
            if (sid > 0L) {
                StudentDetail student = studentLoaderClient.loadStudentDetail(sid);
                if (student == null || !student.isStudent()) {
                    return MapMessage.errorMessage("无效的请求!");
                }

                List<StudentParentRef> refList = parentLoaderClient.loadParentStudentRefs(updatedUser.getId());
                StudentParentRef ref = refList.stream().filter(p -> Objects.equals(p.getStudentId(), sid)).findAny().orElse(null);
                if (ref == null) {
                    return MapMessage.errorMessage("无效的请求!");
                }
                updatedUser = student;

                // 获取头像URL
                UserAvatar studentAvatar = student.isPrimaryStudent() ? UserAvatar.parsePrimaryStudentKey(key) : UserAvatar.parseMiddleStudentKey(key);
                if (studentAvatar == null) {
                    return MapMessage.errorMessage("请选取正确头像");
                }

                avatarUrl = studentAvatar.getUrl();
            } else {
                UserAvatar parentAvatar = UserAvatar.parseParentKey(key);
                if (parentAvatar == null) {
                    return MapMessage.errorMessage("请选取正确头像");
                }

                avatarUrl = parentAvatar.getUrl();
            }
        } else if (updatedUser.isStudent()) {
            StudentDetail student = currentStudentDetail();
            UserAvatar studentAvatar = student.isPrimaryStudent() ? UserAvatar.parsePrimaryStudentKey(key) : UserAvatar.parseMiddleStudentKey(key);
            if (studentAvatar == null) {
                return MapMessage.errorMessage("请选取正确头像");
            }

            avatarUrl = studentAvatar.getUrl();
        } else if (updatedUser.isTeacher()) {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (!teacherDetail.isPrimarySchool()) {//暂时只支持小学
                return MapMessage.errorMessage("暂时不支持换头像");
            }
            avatarType = 0;//老师默认传0
            UserAvatar studentAvatar = UserAvatar.parseTeacherKey(key);
            if (studentAvatar == null) {
                return MapMessage.errorMessage("请选取正确头像");
            }
            avatarUrl = studentAvatar.getUrl();
        } else {
            return MapMessage.errorMessage("暂时不支持换头像");
        }
        //设置头像类型标记（包括toby截图类型、其他头像类型）
        rewardCenterClient.updateAvaterType(updatedUser.getId(), avatarType);

//
//        // 判断用户是否第一次更改头像，点击更改头像后要有个弹窗，再次确认才更改
//        Boolean checkFirstChangeAvatar = getRequestBool("checkFirstChangeAvatar");
//        User curUser = userLoaderClient.loadUser(uid);
//        if (curUser == null) {
//            return MapMessage.errorMessage();
//        }
//        // 查看用户当前头像是不是固定头像
//        if (checkFirstChangeAvatar) {
//            if (!StudentAvatar.imgUrlList().contains(curUser.getProfile().getImgUrl())) {
//                return MapMessage.successMessage(); // 是第一次修改
//            }
//            return MapMessage.errorMessage(); // 不是第一次修改
//        }
//        int key = getRequestInt("avatarCode");
//        StudentAvatar studentAvatar = StudentAvatar.parse(key);
//        if (studentAvatar == null) {
//            return MapMessage.errorMessage("请选取正确头像");
//        }

        String imageGfsId = avatarUrl.replace(".jpg", "").replace(".png", "");
        return userServiceClient.userImageUploaded(updatedUser.getId(), avatarUrl, imageGfsId);
    }

    @RequestMapping(value = "/check_avatar.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage checkAvatar() {
        User curUser = currentUser();
        if (curUser == null) {
            return MapMessage.errorMessage("请重新登录");
        }
        boolean canUpload;
        List<UserAvatar> userAvatarList = null;
        Long studentId = curUser.getId();
        boolean systemImg;
        if (curUser.isParent()) {
            List<StudentParentRef> refList = parentLoaderClient.loadParentStudentRefs(curUser.getId());
            List<Long> childs = refList.stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
            canUpload = userLevelLoader.hasPrivilegeForUploadAvatar(childs);

            Long sid = getRequestLong("sid");
            studentId = sid;
            if (sid > 0L) {
                StudentDetail student = studentLoaderClient.loadStudentDetail(sid);
                if (student == null || !student.isStudent()) {
                    return MapMessage.errorMessage("无效的请求!");
                }

                StudentParentRef ref = refList.stream().filter(p -> Objects.equals(p.getStudentId(), sid)).findAny().orElse(null);
                if (ref == null) {
                    return MapMessage.errorMessage("无效的请求!");
                }

                userAvatarList = student.isPrimaryStudent() ? UserAvatar.getPrimaryStudentAvatars() : UserAvatar.getMiddleStudentAvatars();
                systemImg = StringUtils.isNotEmpty(student.fetchImageUrl()) && CollectionUtils.isNotEmpty(userAvatarList) &&
                        userAvatarList.stream().map(UserAvatar::getUrl).collect(Collectors.toList()).contains(student.fetchImageUrl());
            } else {
                userAvatarList = UserAvatar.getParentAvatars();
                systemImg = StringUtils.isNotEmpty(curUser.fetchImageUrl()) && CollectionUtils.isNotEmpty(userAvatarList) &&
                        userAvatarList.stream().map(UserAvatar::getUrl).collect(Collectors.toList()).contains(curUser.fetchImageUrl());
            }
        } else if (curUser.isStudent()) {
            canUpload = userLevelLoader.hasPrivilegeForUploadAvatar(Collections.singleton(curUser.getId()));

            StudentDetail studentDetail = currentStudentDetail();
            userAvatarList = studentDetail.isPrimaryStudent() ? UserAvatar.getPrimaryStudentAvatars() : UserAvatar.getMiddleStudentAvatars();
            systemImg = StringUtils.isNotEmpty(studentDetail.fetchImageUrl()) && CollectionUtils.isNotEmpty(userAvatarList) &&
                    userAvatarList.stream().map(UserAvatar::getUrl).collect(Collectors.toList()).contains(studentDetail.fetchImageUrl());
        } else if (curUser.isTeacher()) {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (!teacherDetail.isPrimarySchool()) {//暂时只支持小学
                return MapMessage.errorMessage("暂时不支持换头像");
            }
            canUpload = false;
            userAvatarList = UserAvatar.getTeacherAvatars();
            systemImg = StringUtils.isNotEmpty(teacherDetail.fetchImageUrl()) && CollectionUtils.isNotEmpty(userAvatarList) &&
                    userAvatarList.stream().map(UserAvatar::getUrl).collect(Collectors.toList()).contains(teacherDetail.fetchImageUrl());
        } else {
            return MapMessage.errorMessage("暂时不支持换头像");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userAvatarList)) {
            for (UserAvatar avatar : userAvatarList) {
                Map<String, Object> map = new HashMap<>();
                map.put("avatarCode", ConversionUtils.toString(avatar.getKey()));
                map.put("avatarName", avatar.getName());
                map.put("avatarUrl", avatar.getUrl());
                if (StringUtils.isNotBlank(curUser.fetchImageUrl()) && avatar.getUrl().equals(curUser.fetchImageUrl())) {
                    map.put("isUsed", true);
                }else {
                    map.put("isUsed", false);
                }
                result.add(map);
            }
        }
        //获取头像类型是否是toby截图类型
        boolean isTobyAvatar = rewardCenterClient.isTobyAvatarType(studentId);
        return MapMessage.successMessage()
                .add("result", result)
                .add("canUpload", canUpload)
                .add("systemImg", systemImg)
                .add("isTobyAvatar", isTobyAvatar);
    }

}
