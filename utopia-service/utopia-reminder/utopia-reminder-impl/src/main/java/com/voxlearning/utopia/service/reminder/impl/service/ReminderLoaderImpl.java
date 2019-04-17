package com.voxlearning.utopia.service.reminder.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.reminder.api.ReminderLoader;
import com.voxlearning.utopia.service.reminder.api.mapper.ReminderContext;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.reminder.constant.ReminderTarget;
import com.voxlearning.utopia.service.reminder.impl.support.ReminderCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2017-5-10
 */
@Named
@ExposeService(interfaceClass = ReminderLoader.class)
public class ReminderLoaderImpl implements ReminderLoader {
    @Inject
    private ReminderCacheSystem reminderCacheSystem;

    @Override
    public ReminderContext loadUserReminder(Long userId, ReminderPosition position) {
        if (userId == null || position == null) {
            return null;
        }
        return reminderCacheSystem.getReminderCache().load(ReminderTarget.USER, position, userId.toString());
    }

    @Override
    public Map<Long, ReminderContext> loadUsersReminder(Collection<Long> userIds, ReminderPosition position) {
        if (CollectionUtils.isEmpty(userIds) || position == null) {
            return Collections.emptyMap();
        }
        Set<String> targetIds = userIds.stream().map(SafeConverter::toString).collect(Collectors.toSet());
        Map<String, ReminderContext> contextMap = reminderCacheSystem.getReminderCache().loads(ReminderTarget.USER, position, targetIds);
        return contextMap.values().stream()
                .filter(p -> p != null)
                .filter(p -> StringUtils.isNotBlank(p.getTargetId()))
                .collect(Collectors.toMap(e -> SafeConverter.toLong(e.getTargetId()), Function.identity()));
    }

    @Override
    public ReminderContext loadClazzGroupReminder(Long groupId, ReminderPosition position) {
        if (groupId == null || position == null) {
            return null;
        }
        return reminderCacheSystem.getReminderCache().load(ReminderTarget.CLAZZ_GROUP, position, groupId.toString());
    }

    @Override
    public Map<Long, ReminderContext> loadClazzGroupsReminder(Collection<Long> groupIds, ReminderPosition position) {
        if (CollectionUtils.isEmpty(groupIds) || position == null) {
            return Collections.emptyMap();
        }
        Set<String> targetIds = groupIds.stream().map(SafeConverter::toString).collect(Collectors.toSet());
        Map<String, ReminderContext> contextMap = reminderCacheSystem.getReminderCache().loads(ReminderTarget.CLAZZ_GROUP, position, targetIds);
        return contextMap.values().stream()
                .filter(p -> p != null)
                .filter(p -> StringUtils.isNotBlank(p.getTargetId()))
                .collect(Collectors.toMap(e -> SafeConverter.toLong(e.getTargetId()), Function.identity()));
    }

    @Override
    public Map<Long, ReminderContext> loadUserGroupReminder(Long userId, Collection<Long> groupIds, ReminderPosition position) {
        if (userId == null || CollectionUtils.isEmpty(groupIds) || position == null) {
            return Collections.emptyMap();
        }
        Map<Long, String> groupIdTargetIdMap = new HashMap<>();
        groupIds.forEach(p -> groupIdTargetIdMap.put(p, "UID=" + userId + ",GID=" + p));
        Map<String, ReminderContext> contextMap = reminderCacheSystem.getReminderCache().loads(ReminderTarget.USER_IN_GROUP, position, groupIdTargetIdMap.values());
        Map<Long, ReminderContext> result = new HashMap<>();
        groupIds.forEach(p -> {
            String targetId = groupIdTargetIdMap.get(p);
            if (contextMap.containsKey(targetId)) {
                result.put(p, contextMap.get(targetId));
            }
        });
        return result;
    }

    @Override
    public Map<ReminderPosition, ReminderContext> loadUserReminder(Long userId, Collection<ReminderPosition> positions) {
        if (userId == null || CollectionUtils.isEmpty(positions)) {
            return Collections.emptyMap();
        }
        return reminderCacheSystem.getReminderCache().loads(ReminderTarget.USER, SafeConverter.toString(userId), positions);
    }
}
