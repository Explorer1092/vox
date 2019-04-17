package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkShardMongoHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/13
 */
@Named
public class SubHomeworkProcessResultDao {
    @Inject private SubHomeworkProcessResultAsyncDao subHomeworkProcessResultAsyncDao;
    @Inject private SubHomeworkProcessResultShardDao subHomeworkProcessResultShardDao;
    // @Inject private CommonConfigServiceClient commonConfigServiceClient;

    private boolean isOpenShardMongo() {
        // return SafeConverter.toBoolean(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "HOMEWORK_SHARD_MONGO"), false);
        // 关闭双写后，这里就不能通过配置来设为false了，不然新数据去旧表会查不到
        return true;
    }

    /**
     * 更新老师对题的批改状态
     * 参数有点多，原因是为了避免许多校验带来的性能损耗，直接在update的时候都做了。
     *
     * @param id          id
     * @param review      是否已阅
     * @param correctType 批改类型
     * @param correction  批改信息
     * @param teacherMark 老师评语
     * @return boolean
     */
    public SubHomeworkProcessResult updateCorrection(String id,
                                                     Boolean review,
                                                     CorrectType correctType,
                                                     Correction correction,
                                                     String teacherMark,
                                                     Boolean isBatch) {
        boolean isOpen = isOpenShardMongo();
        if (NewHomeworkShardMongoHelper.isShardProcessId(id, isOpen)) {
            return subHomeworkProcessResultShardDao.updateCorrection(id, review, correctType, correction, teacherMark, isBatch);
            // 双写旧表
            // subHomeworkProcessResultAsyncDao.updateCorrection(id, review, correctType, correction, teacherMark, isBatch, false);
        }
        return subHomeworkProcessResultAsyncDao.updateCorrection(id, review, correctType, correction, teacherMark, isBatch, true);
    }

    public SubHomeworkProcessResult load(String s) {
        if (NewHomeworkShardMongoHelper.isShardProcessId(s, isOpenShardMongo())) {
            return subHomeworkProcessResultShardDao.load(s);
        }
        return subHomeworkProcessResultAsyncDao.load(s);
    }

    public Map<String, SubHomeworkProcessResult> loads(Collection<String> strings) {
        if (CollectionUtils.isEmpty(strings)) {
            return Collections.emptyMap();
        }
        Map<String, SubHomeworkProcessResult> subHomeworkProcessResultMap = new HashMap<>();
        Set<String> shardIds = new HashSet<>();
        Set<String> asyncIds = new HashSet<>();
        boolean isOpen = isOpenShardMongo();
        for (String id : strings) {
            if (NewHomeworkShardMongoHelper.isShardProcessId(id, isOpen)) {
                shardIds.add(id);
            } else {
                asyncIds.add(id);
            }
        }
        if (CollectionUtils.isNotEmpty(shardIds)) {
            Map<String, SubHomeworkProcessResult> subHomeworkProcessResultShardMap = subHomeworkProcessResultShardDao.loads(shardIds);
            subHomeworkProcessResultShardMap.forEach(subHomeworkProcessResultMap::put);
        }
        if (CollectionUtils.isNotEmpty(asyncIds)) {
            Map<String, SubHomeworkProcessResult> subHomeworkProcessResultAsyncMap = subHomeworkProcessResultAsyncDao.loads(asyncIds);
            subHomeworkProcessResultAsyncMap.forEach(subHomeworkProcessResultMap::put);
        }
        return MapUtils.resort(subHomeworkProcessResultMap, strings);
    }

    public void insert(SubHomeworkProcessResult document) {
        if (NewHomeworkShardMongoHelper.isShardProcessId(document.getId(), isOpenShardMongo())) {
            subHomeworkProcessResultShardDao.insert(document);
            // 双写旧表
            // subHomeworkProcessResultAsyncDao.$insert(document).awaitUninterruptibly();
        } else {
            subHomeworkProcessResultAsyncDao.insert(document);
        }
    }

    public void inserts(Collection<SubHomeworkProcessResult> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        List<SubHomeworkProcessResult> shardResultList = new ArrayList<>();
        List<SubHomeworkProcessResult> asyncResultList = new ArrayList<>();
        boolean isOpen = isOpenShardMongo();
        for (SubHomeworkProcessResult document : documents) {
            if (NewHomeworkShardMongoHelper.isShardProcessId(document.getId(), isOpen)) {
                shardResultList.add(document);
            } else {
                asyncResultList.add(document);
            }
        }
        if (CollectionUtils.isNotEmpty(shardResultList)) {
            subHomeworkProcessResultShardDao.inserts(shardResultList);
            // 双写旧表
            // subHomeworkProcessResultAsyncDao.$inserts(shardResultList).awaitUninterruptibly();
        }
        if (CollectionUtils.isNotEmpty(asyncResultList)) {
            subHomeworkProcessResultAsyncDao.inserts(asyncResultList);
        }
    }

    public void upsert(SubHomeworkProcessResult document) {
        if (NewHomeworkShardMongoHelper.isShardProcessId(document.getId(), isOpenShardMongo())) {
            subHomeworkProcessResultShardDao.upsert(document);
        } else {
            subHomeworkProcessResultAsyncDao.upsert(document);
        }
    }

}
