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

package com.voxlearning.washington.support;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.JournalDuplicationPolicy;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import com.voxlearning.utopia.temp.ForbidModifyNameAndPortrait;
import com.voxlearning.washington.support.upload.*;

import javax.inject.Inject;
import java.util.List;

import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.CLAZZ_LATEST_BEST_PREFIX;

abstract public class AbstractUploadController extends AbstractClientController {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Inject protected ClazzJournalPhotoUploader clazzJournalPhotoUploader;
    @Inject protected MothersDayCardPhotoUploader mothersDayCardPhotoUploader;
    @Inject protected TeacherResourceUploader teacherResourceUploader;
    @Inject protected UserImageUploader userImageUploader;
    @Inject protected VoiceUploader voiceUploader;
    @Inject protected TeacherResourceDownloader teacherResourceDownloader;
    @Inject protected MissionPictureUploader missionPictureUploader;
    @Inject protected DubbingUploader dubbingUploader;

    @StorageClientLocation(storage = "user")
    StorageClient storageClient;

    protected MapMessage updateUserAvatar(User user, String avatarData) {
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
                if (!clazz.isSystemClazz()) {// 非系统自建班级
                    zoneQueueServiceClient.createClazzJournal(clazz.getId())
                            .withUser(user.getId())
                            .withUser(user.fetchUserType())
                            .withClazzJournalType(ClazzJournalType.CHANGE_IMG)
                            .withClazzJournalCategory(ClazzJournalCategory.MISC)
                            .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                            .withPolicy(JournalDuplicationPolicy.DAILY)
                            .commit();
                } else {// 系统自建班级
                    List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(user.getId(), false);
                    //TODO currently send to one group for the student
                    //TODO or one message will be saw multi-times for other students
                    if (groups.size() > 0) {
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
                }
                // 清除缓存
                String key = CacheKeyGenerator.generateCacheKey(CLAZZ_LATEST_BEST_PREFIX, null, new Object[]{clazz.getId()});
                washingtonCacheSystem.CBS.flushable.delete(key);
            }
        }

        return MapMessage.successMessage().add("row", filename);
    }

    //这个接口只负责更新头像的相关操作。所以必须要先有头像url和gfsId才能调用
    protected MapMessage onlyUpdateUserAvatar(User user, String filename, String gfsId) {
        if (user == null) {
            return MapMessage.errorMessage();
        }
        MapMessage message;
        try {
            message = userServiceClient.userImageUploaded(user.getId(), filename, gfsId);
        } catch (Exception ex) {
            message = MapMessage.errorMessage();
        }
        if (!message.isSuccess()) {
            logger.warn("Failed to update user image [userId:{},imageUrl:{},imageGfsId:{}]", user.getId(), filename, gfsId);
            return MapMessage.errorMessage();
        }
        if (user.isStudent()) {
            Clazz clazz;
            if (user instanceof StudentDetail) {
                clazz = ((StudentDetail) user).getClazz();
            } else {
                clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
            }
            if (clazz != null) {
                String content = user.fetchRealname() + "更换了头像";
                if (!clazz.isSystemClazz()) {// 非系统自建班级
                    zoneQueueServiceClient.createClazzJournal(clazz.getId())
                            .withUser(user.getId())
                            .withUser(user.fetchUserType())
                            .withClazzJournalType(ClazzJournalType.CHANGE_IMG)
                            .withClazzJournalCategory(ClazzJournalCategory.MISC)
                            .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content)))
                            .withPolicy(JournalDuplicationPolicy.DAILY)
                            .commit();
                } else {// 系统自建班级
                    List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(user.getId(), false);
                    //TODO currently send to one group for the student
                    //TODO or one message will be saw multi-times for other students
                    if (groups.size() > 0) {
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
                }
                // 清除缓存
                String key = CacheKeyGenerator.generateCacheKey(CLAZZ_LATEST_BEST_PREFIX, null, new Object[]{clazz.getId()});
                washingtonCacheSystem.CBS.flushable.delete(key);
            }
        }
        return MapMessage.successMessage();
    }
}
