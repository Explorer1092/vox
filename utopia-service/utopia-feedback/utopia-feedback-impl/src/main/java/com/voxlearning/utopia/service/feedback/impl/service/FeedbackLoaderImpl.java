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

package com.voxlearning.utopia.service.feedback.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.feedback.api.FeedbackLoader;
import com.voxlearning.utopia.service.feedback.api.entities.RegisterFeedback;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedbackTag;
import com.voxlearning.utopia.service.feedback.impl.dao.RegisterFeedbackPersistence;
import com.voxlearning.utopia.service.feedback.impl.dao.UserFeedbackTagPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default {@link FeedbackLoader} implementation.
 *
 * @author Xiaohai Zhang
 * @since Jan 16, 2015
 */
@Named("com.voxlearning.utopia.service.feedback.impl.service.FeedbackLoaderImpl")
@ExposeService(interfaceClass = FeedbackLoader.class)
public class FeedbackLoaderImpl extends SpringContainerSupport implements FeedbackLoader {

    @Inject private RegisterFeedbackPersistence registerFeedbackPersistence;
    @Inject private UserFeedbackTagPersistence userFeedbackTagPersistence;

    @Override
    public List<RegisterFeedback> loadRegisterFeedbacks(int state, Date start, Date end) {
        return registerFeedbackPersistence.find(state, start, end);
    }

    @Override
    public List<UserFeedbackTag> findByWatcherName(String watcherName) {
        return userFeedbackTagPersistence.findAllTags()
                .stream()
                .filter(e -> StringUtils.equals(watcherName, e.getWatcherName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> findWatchersWithTag() {
        return userFeedbackTagPersistence.findAllTags()
                .stream()
                .filter(e -> e.getWatcherName() != null)
                .map(UserFeedbackTag::getWatcherName)
                .distinct()
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("WATCHER_NAME", e);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserFeedbackTag loadUserFeedbackTag(Long id) {
        return userFeedbackTagPersistence.load(id);
    }

    @Override
    public UserFeedbackTag findByTagName(String tagName) {
        return userFeedbackTagPersistence.findAllTags()
                .stream()
                .filter(e -> StringUtils.equals(tagName, e.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<UserFeedbackTag> findAllTags() {
        return userFeedbackTagPersistence.findAllTags();
    }
}
