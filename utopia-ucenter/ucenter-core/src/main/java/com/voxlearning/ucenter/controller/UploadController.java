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

package com.voxlearning.ucenter.controller;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.ucenter.support.uploader.UserImageUploader;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import com.voxlearning.utopia.temp.ForbidModifyNameAndPortrait;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author changyuan.liu
 * @since 2015.12.22
 */
@Controller
@RequestMapping("/uploadfile")
public class UploadController extends AbstractWebController {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Inject private UserImageUploader userImageUploader;

    @StorageClientLocation(storage = "user") private StorageClient storageClient;

    @RequestMapping(value = "avatar.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage avatar(HttpServletRequest request) {
        return MapMessage.errorMessage("改头像功能暂停使用了哦");
        //return updateUserAvatar(currentUser(), request.getParameter("filedata"));
    }

    private MapMessage updateUserAvatar(User user, String avatarData) {
        // Feature #54929
        if (ForbidModifyNameAndPortrait.check()) {
            return ForbidModifyNameAndPortrait.errorMessage;
        }
        if (user == null || user.getId() == null || StringUtils.isEmpty(avatarData)) {
            return MapMessage.errorMessage();
        }

        Long userId = user.getId();
        String id = RandomUtils.nextObjectId();

        String filename = userImageUploader.uploadImageFromFiledata(userId, id, avatarData);
        if (filename == null) {
            logger.warn("User '{}' failed to upload image", userId);
            return MapMessage.errorMessage();
        }

        MapMessage message;
        try {
            // remove from oss
            // storageClient.deleteByName(user.getProfile().getImgUrl(), "gridfs");

            message = userServiceClient.userImageUploaded(userId, filename, id);
        } catch (Exception ex) {
            message = MapMessage.errorMessage();
        }
        if (!message.isSuccess()) {
            logger.warn("Failed to update user image [userId:{},imageUrl:{},imageGfsId:{}]", userId, filename, id);
            return MapMessage.errorMessage();
        }

        if (user.isStudent()) {
            Clazz clazz;
            if (user instanceof StudentDetail) {
                clazz = ((StudentDetail) user).getClazz();
            } else {
                clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(userId);
            }
            if (clazz != null) {
                String content = user.fetchRealname() + "更换了头像";

                List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(user.getId(), false);
                //TODO currently send to one group for the student
                //TODO or one message will be saw multi-times for other students
                if (CollectionUtils.isNotEmpty(groups)) {
                    zoneQueueServiceClient.createClazzJournal(clazz.getId())
                            .withUser(user.getId())
                            .withUser(user.fetchUserType())
                            .withClazzJournalType(ClazzJournalType.CHANGE_IMG)
                            .withClazzJournalCategory(ClazzJournalCategory.MISC)
                            .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                            .withPolicy(JournalDuplicationPolicy.DAILY)
                            .withGroup(groups.get(0).getId())
                            .commit();
                }
                // 清除缓存
                String key = CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.CLAZZ_LATEST_BEST_PREFIX, null, new Object[]{clazz.getId()});
                ucenterWebCacheSystem.CBS.flushable.delete(key);
            }
        }

        return MapMessage.successMessage().add("row", filename);
    }
}
