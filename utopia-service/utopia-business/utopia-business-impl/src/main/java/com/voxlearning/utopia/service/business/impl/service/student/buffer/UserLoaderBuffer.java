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

package com.voxlearning.utopia.service.business.impl.service.student.buffer;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 先临时做这个来优化。
 * 在加载学生首页的时候，可能会发生多次加载User的行为。
 * 这是一个本地buffer的实现，如果有已经加载的User，就放入这里。
 * 可以减少访问缓存服务器的次数。
 * 这个buffer的生命周期是LoadStudentIndex方法，同context。
 * 这是有状态的类，不能作为bean的成员变量，可以放入context中。
 * 非线程安全。
 *
 * @author Xiaohai Zhang
 * @since Oct 12, 2015
 */
public class UserLoaderBuffer {

    private final Map<Long, User> buffer = new HashMap<>();
    private final UserLoaderClient userLoaderClient;

    public UserLoaderBuffer(StudentDetail student, UserLoaderClient userLoaderClient) {
        this.buffer.put(student.getId(), student);
        this.userLoaderClient = userLoaderClient;
    }

    public User loadUser(Long userId) {
        return loadUsers(Collections.singleton(userId)).get(userId);
    }

    public Map<Long, User> loadUsers(Collection<Long> userIds) {
        userIds = CollectionUtils.toLinkedHashSet(userIds);
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, User> result = userIds.stream()
                .map(buffer::get)
                .filter(t -> t != null)
                .collect(Collectors.toMap(User::getId, t -> t));

        Set<Long> missedUserIds = userIds.stream()
                .filter(t -> !result.containsKey(t))
                .collect(Collectors.toSet());

        if (!missedUserIds.isEmpty()) {
            Map<Long, User> missed = userLoaderClient.loadUsers(missedUserIds);
            buffer.putAll(missed);
            result.putAll(missed);
        }

        return result;
    }
}
