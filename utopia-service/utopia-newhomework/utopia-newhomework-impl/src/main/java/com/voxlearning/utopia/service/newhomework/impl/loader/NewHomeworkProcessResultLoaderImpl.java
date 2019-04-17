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

package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkProcessResultLoader;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkProcessResultHBase;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.hbase.HomeworkProcessResultHBasePersistence;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.correction.CorrectHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkHBaseHelper;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkTransform;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
@Service(interfaceClass = NewHomeworkProcessResultLoader.class)
@ExposeService(interfaceClass = NewHomeworkProcessResultLoader.class)
public class NewHomeworkProcessResultLoaderImpl implements NewHomeworkProcessResultLoader {

    @Inject private CorrectHomeworkProcessor correctHomeworkProcessor;
    @Inject private SubHomeworkProcessResultDao subHomeworkProcessResultDao;
    @Inject private HomeworkProcessResultHBasePersistence homeworkProcessResultHBasePersistence;

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    private static final int PAGE_SIZE = 1000;

    @Override
    public NewHomeworkProcessResult load(String homeworkId, String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        if (NewHomeworkUtils.isSubHomework(homeworkId) || NewHomeworkUtils.isShardHomework(homeworkId)) {
            // 默认关闭
            boolean isOpen = SafeConverter.toBoolean(
                    commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_OPEN_HBASE")
                    , false
            );
            if (HomeworkHBaseHelper.isHBaseHomeworkId(homeworkId, isOpen)) {
                HomeworkProcessResultHBase hBase = homeworkProcessResultHBasePersistence.load(id);
                if (hBase != null) {
                    return NewHomeworkProcessResult.of(hBase);
                }
            } else {
                SubHomeworkProcessResult sub = subHomeworkProcessResultDao.load(id);
                if (sub != null) {
                    return NewHomeworkProcessResult.of(sub);
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, NewHomeworkProcessResult> loads(String homeworkId, Collection<String> ids) {
        Map<String, NewHomeworkProcessResult> resultMap = new LinkedHashMap<>();
        int idsSize = 0;
        if (CollectionUtils.isNotEmpty(ids)) {
            idsSize = ids.size();
        }

        if (NewHomeworkUtils.isSubHomework(homeworkId) || NewHomeworkUtils.isShardHomework(homeworkId)) {
            boolean isOpen = SafeConverter.toBoolean(
                    commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_OPEN_HBASE")
                    , false
            );
            if (HomeworkHBaseHelper.isHBaseHomeworkId(homeworkId, isOpen)) {
                Map<String, HomeworkProcessResultHBase> hBaseMap = homeworkProcessResultHBasePersistence.loads(ids);
                for (Map.Entry<String, HomeworkProcessResultHBase> entry : hBaseMap.entrySet()) {
                    resultMap.put(entry.getKey(), NewHomeworkProcessResult.of(entry.getValue()));
                }
            } else {
                FlightRecorder.dot("NewHomeworkProcessResultLoader_loads_1, processId size:" + idsSize);
                Map<String, SubHomeworkProcessResult> subMap = loadsByIds(ids);
                FlightRecorder.dot("NewHomeworkProcessResultLoader_loads_2, processId size:" + idsSize);
                for (Map.Entry<String, SubHomeworkProcessResult> entry : subMap.entrySet()) {
                    resultMap.put(entry.getKey(), NewHomeworkProcessResult.of(entry.getValue()));
                }
                FlightRecorder.dot("NewHomeworkProcessResultLoader_loads_3, processId size:" + idsSize);
            }
        }
        return resultMap;
    }

    @Override
    public Boolean updateCorrection(CorrectHomeworkContext correctHomeworkContext) {
        CorrectHomeworkContext context = correctHomeworkProcessor.process(correctHomeworkContext);
        return context.isSuccessful();
    }

    public SubHomeworkProcessResult loadSubHomeworkProcessResult(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        boolean isOpen = SafeConverter.toBoolean(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_OPEN_HBASE")
                , false
        );
        if (HomeworkHBaseHelper.isHBaseProcessId(id, isOpen)) {
            HomeworkProcessResultHBase hBase = homeworkProcessResultHBasePersistence.load(id);
            return HomeworkTransform.HomeworkProcessResultHBaseToSub(hBase);
        } else {
            return subHomeworkProcessResultDao.load(id);
        }
    }

    public Map<String, SubHomeworkProcessResult> loadSubHomeworkProcessResults(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Map<String, SubHomeworkProcessResult> processResultMap = new HashMap<>();
        Set<String> subIds = new HashSet<>();
        Set<String> hBaseIds = new HashSet<>();
        boolean isOpen = SafeConverter.toBoolean(
                commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_OPEN_HBASE")
                , false
        );
        for (String id : ids) {
            if (HomeworkHBaseHelper.isHBaseProcessId(id, isOpen)) {
                hBaseIds.add(id);
            } else {
                subIds.add(id);
            }
        }
        if (CollectionUtils.isNotEmpty(subIds)) {
            Map<String, SubHomeworkProcessResult> subHomeworkProcessResultMap = subHomeworkProcessResultDao.loads(subIds);
            processResultMap.putAll(subHomeworkProcessResultMap);
        }
        if (CollectionUtils.isNotEmpty(hBaseIds)) {
            Map<String, HomeworkProcessResultHBase> homeworkProcessResultHBaseMap = homeworkProcessResultHBasePersistence.loads(hBaseIds);
            homeworkProcessResultHBaseMap.forEach((k, v) -> processResultMap.put(k, HomeworkTransform.HomeworkProcessResultHBaseToSub(v)));
        }
        return processResultMap;
    }

    private Map<String, SubHomeworkProcessResult> loadsByIds(Collection<String> ids) {
        Set<String> idSet = CollectionUtils.toLinkedHashSet(ids);
        if (idSet.isEmpty()) {
            return Collections.emptyMap();
        }
        List<List<String>> idsList = NewHomeworkUtils.splitList(new ArrayList<>(idSet), PAGE_SIZE);
        Map<String, SubHomeworkProcessResult> subHomeworkProcessResultMap = new LinkedHashMap<>();
        for (List<String> idList : idsList) {
            Map<String, SubHomeworkProcessResult> result = subHomeworkProcessResultDao.loads(idList);
            if (MapUtils.isNotEmpty(result)) {
                subHomeworkProcessResultMap.putAll(result);
            }
        }
        return subHomeworkProcessResultMap;
    }
}
