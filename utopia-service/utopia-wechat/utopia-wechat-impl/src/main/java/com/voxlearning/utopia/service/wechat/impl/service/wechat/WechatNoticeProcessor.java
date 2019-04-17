package com.voxlearning.utopia.service.wechat.impl.service.wechat;

import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Wechat notice processor interface.
 *
 * @author Xiaohai Zhang
 * @since Jan 19, 2015
 */
public interface WechatNoticeProcessor {
    WechatNoticeProcessorType type();

    void process(Clazz clazz, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType);

    void process(Long clazzId, Long groupId, Teacher teacher, Map<String, Object> extensionInfo, WechatType wechatType);

    void processWithSpecificUsers(List<User> students, Teacher teacher, Long clazzId, Map<String, Object> extensionInfo, WechatType wechatType);

    void processSingleUser(Long userId, Map<String, Object> extensionInfo, WechatType wechatType);

    void processSingleUserOpenId(Long userId, String openId, Map<String, Object> extensionInfo);

    void processMultiUsersMultiOpenIds(Map<Long, List<String>> user_openId_map, Map<String, Object> extensionInfo);

    void processWithStudents(Collection<Long> studentIds, Map<String, Object> extensionInfo, WechatType wechatType);
}
