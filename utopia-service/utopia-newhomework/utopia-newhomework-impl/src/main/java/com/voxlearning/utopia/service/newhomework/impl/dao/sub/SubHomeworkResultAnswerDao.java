package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkShardMongoHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/1/12
 */
@Named
public class SubHomeworkResultAnswerDao {
    @Inject private SubHomeworkResultAnswerAsyncDao subHomeworkResultAnswerAsyncDao;
    @Inject private SubHomeworkResultAnswerShardDao subHomeworkResultAnswerShardDao;
    // @Inject private CommonConfigServiceClient commonConfigServiceClient;

    private boolean isOpenShardMongo() {
        // return SafeConverter.toBoolean(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_SHARD_MONGO"), false);
        // 关闭双写后，这里就不能通过配置来设为false了，不然新数据去旧表会查不到
        return true;
    }

    public SubHomeworkResultAnswer load(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        if (NewHomeworkShardMongoHelper.isShardResultAnswerId(s, isOpenShardMongo())) {
            return subHomeworkResultAnswerShardDao.load(s);
        }
        return subHomeworkResultAnswerAsyncDao.load(s);
    }

    public Map<String, SubHomeworkResultAnswer> loads(Collection<String> strings) {
        if (CollectionUtils.isEmpty(strings)) {
            return Collections.emptyMap();
        }
        Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerMap = new HashMap<>();
        Set<String> shardIds = new HashSet<>();
        Set<String> asyncIds = new HashSet<>();
        boolean isOpen = isOpenShardMongo();
        for (String id : strings) {
            if (NewHomeworkShardMongoHelper.isShardResultAnswerId(id, isOpen)) {
                shardIds.add(id);
            } else {
                asyncIds.add(id);
            }
        }
        if (CollectionUtils.isNotEmpty(shardIds)) {
            Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerShardMap = subHomeworkResultAnswerShardDao.loads(shardIds);
            subHomeworkResultAnswerShardMap.forEach(subHomeworkResultAnswerMap::put);
        }
        if (CollectionUtils.isNotEmpty(asyncIds)) {
            Map<String, SubHomeworkResultAnswer> subHomeworkResultAnswerAsyncMap = subHomeworkResultAnswerAsyncDao.loads(asyncIds);
            subHomeworkResultAnswerAsyncMap.forEach(subHomeworkResultAnswerMap::put);
        }
        return MapUtils.resort(subHomeworkResultAnswerMap, strings);
    }

    public void insert(SubHomeworkResultAnswer subHomeworkResultAnswer) {
        boolean isOpen = isOpenShardMongo();
        if (NewHomeworkShardMongoHelper.isShardResultAnswerId(subHomeworkResultAnswer.getId(), isOpen)) {
            subHomeworkResultAnswerShardDao.insert(subHomeworkResultAnswer);
            // 双写旧表
            // subHomeworkResultAnswerAsyncDao.$insert(subHomeworkResultAnswer).awaitUninterruptibly();
        } else {
            subHomeworkResultAnswerAsyncDao.insert(subHomeworkResultAnswer);
        }
    }

    public void inserts(final Collection<SubHomeworkResultAnswer> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        List<SubHomeworkResultAnswer> shardAnswerList = new ArrayList<>();
        List<SubHomeworkResultAnswer> asyncAnswerList = new ArrayList<>();
        boolean isOpen = isOpenShardMongo();
        for (SubHomeworkResultAnswer document : documents) {
            if (NewHomeworkShardMongoHelper.isShardResultAnswerId(document.getId(), isOpen)) {
                shardAnswerList.add(document);
            } else {
                asyncAnswerList.add(document);
            }
        }
        if (CollectionUtils.isNotEmpty(shardAnswerList)) {
//            subHomeworkResultAnswerShardDao.inserts(shardAnswerList);
            List<SubHomeworkResultAnswer> documentList = CollectionUtils.toLinkedList(shardAnswerList);
            if (documentList.isEmpty()) {
                return;
            }
            subHomeworkResultAnswerShardDao.$inserts(documentList).awaitUninterruptibly();
            Set<String> cacheKeySet = documentList.stream()
                    .map(d -> CacheKeyGenerator.generateCacheKey(SubHomeworkResultAnswer.class, d.getId()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            subHomeworkResultAnswerShardDao.getCache().deletes(cacheKeySet);
            // 双写旧表
            // subHomeworkResultAnswerAsyncDao.$inserts(shardAnswerList).awaitUninterruptibly();
        }
        if (CollectionUtils.isNotEmpty(asyncAnswerList)) {
            subHomeworkResultAnswerAsyncDao.inserts(asyncAnswerList);
        }
    }

    public SubHomeworkResultAnswer upsert(final SubHomeworkResultAnswer subHomeworkResultAnswer) {
        boolean isOpen = isOpenShardMongo();
        if (NewHomeworkShardMongoHelper.isShardResultAnswerId(subHomeworkResultAnswer.getId(), isOpen)) {
            return subHomeworkResultAnswerShardDao.upsert(subHomeworkResultAnswer);
            // 双写旧表
            // subHomeworkResultAnswerAsyncDao.$upsert(subHomeworkResultAnswer).awaitUninterruptibly();
        }
        return subHomeworkResultAnswerAsyncDao.upsert(subHomeworkResultAnswer);
    }
}
