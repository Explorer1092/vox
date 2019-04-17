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

package com.voxlearning.utopia.service.piclisten.buffer.internal;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedTextBookManagementList;
import com.voxlearning.utopia.service.piclisten.buffer.TextBookManagementBuffer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/4/5.
 */
@ThreadSafe
public class JVMTextBookManagementBuffer implements TextBookManagementBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    private long version;
    private List<TextBookManagement> textBookManagementList;
    private Map<String, TextBookManagement> idMap;
    private List<TextBookMapper> publisherList;
    private Map<Integer, List<TextBookManagement>> clazzLevelMap;

    @Override
    public void attach(VersionedTextBookManagementList data) {
        Objects.requireNonNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            version = data.getVersion();
            textBookManagementList = data.getTextBookManagementList().stream()
                    .filter(t -> t != null && t.getClazzLevel() != null).collect(Collectors.toList());
            idMap = textBookManagementList.stream()
                    .collect(Collectors.toMap(TextBookManagement::getBookId, Function.identity()));
            clazzLevelMap = textBookManagementList.stream().collect(Collectors.groupingBy(TextBookManagement::getClazzLevel));
            publisherList = new ArrayList<>(data.getTextBookMapperList());
        });
    }

    @Override
    public VersionedTextBookManagementList dump() {
        return locker.withinReadLock(() -> {
            VersionedTextBookManagementList data = new VersionedTextBookManagementList();
            data.setVersion(version);
            data.setTextBookManagementList(textBookManagementList);
            data.setTextBookMapperList(publisherList);
            return data;
        });
    }

    @Override
    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    @Override
    public List<TextBookManagement> getTextBookManagementList() {
        return locker.withinReadLock(() -> textBookManagementList);
    }

    @Override
    public Map<Integer, List<TextBookManagement>> getClazzLevelMap() {
        return locker.withinReadLock(() -> clazzLevelMap);
    }

    @Override
    public List<TextBookMapper> getTextBookMapperList() {
        return locker.withinReadLock(() -> publisherList);
    }

    @Override
    public Map<String, TextBookManagement> loadByIds(Collection<String> ids) {
        Set<String> set = CollectionUtils.toLinkedHashSet(ids);
        if (set.isEmpty()) {
            return Collections.emptyMap();
        }
        return locker.withinReadLock(() -> {
            Map<String, TextBookManagement> result = new LinkedHashMap<>();
            for (String id : set) {
                TextBookManagement element = idMap.get(id);
                if (element == null) {
                    continue;
                }
                result.put(id, element);
            }
            return result;
        });
    }
}
