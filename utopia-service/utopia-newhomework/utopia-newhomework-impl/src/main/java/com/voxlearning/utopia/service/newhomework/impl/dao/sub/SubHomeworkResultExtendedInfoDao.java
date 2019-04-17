package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultExtendedInfo;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Named
@CacheBean(type = SubHomeworkResultExtendedInfo.class, cacheName = "utopia-homework-cache", useEagerInsert = true, useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SubHomeworkResultExtendedInfoDao extends DynamicMongoShardPersistence<SubHomeworkResultExtendedInfo, String> {
    @Override
    protected String calculateDatabase(String template, SubHomeworkResultExtendedInfo document) {
        SubHomeworkResultExtendedInfo.ID id = document.parseID();
        String month = StringUtils.substring(id.getDay(), 0, 6);
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, SubHomeworkResultExtendedInfo document) {
        SubHomeworkResultExtendedInfo.ID id = document.parseID();
        return StringUtils.formatMessage(template, id.getDay());
    }

    @Override
    protected void calculateCacheDimensions(SubHomeworkResultExtendedInfo document, Collection<String> dimensions) {
        dimensions.add(SubHomeworkResultExtendedInfo.ck_id(document.getId()));
    }

    /**
     * 查询扩展属性
     * @param id
     * @param extendedKey
     * @return
     */
    public String loadExtendedInfo(String id, String extendedKey) {
        SubHomeworkResultExtendedInfo extendedInfo = load(id);
        if (extendedInfo == null || MapUtils.isEmpty(extendedInfo.getInfo())) {
            return null;
        }
        return extendedInfo.getInfo().get(extendedKey);
    }

    public Map<String, SubHomeworkResultExtendedInfo> loadExtendedInfos(List<String> ids) {
        Map<String, SubHomeworkResultExtendedInfo> loads = loads(ids);
        if (MapUtils.isEmpty(loads)) {
            return null;
        }
        return loads;
    }

    public SubHomeworkResultExtendedInfo updateSubHomeworkResultExtendedInfo(String id, Map<String, String> extendedInfo) {
        SubHomeworkResultExtendedInfo subHomeworkResultExtendedInfo = load(id);
        if (subHomeworkResultExtendedInfo == null) {
            subHomeworkResultExtendedInfo = new SubHomeworkResultExtendedInfo();
            subHomeworkResultExtendedInfo.setInfo(extendedInfo);
            subHomeworkResultExtendedInfo.setId(id);
        } else {
            subHomeworkResultExtendedInfo.getInfo().putAll(extendedInfo);
        }
        $upsert(subHomeworkResultExtendedInfo).getUninterruptibly();
        changeCache(subHomeworkResultExtendedInfo);
        return subHomeworkResultExtendedInfo;
    }

    private void changeCache(SubHomeworkResultExtendedInfo modified) {
        getCache().createCacheValueModifier()
                .key(SubHomeworkResultExtendedInfo.ck_id(modified.getId()))
                .expiration(getDefaultCacheExpirationInSeconds())
                .modifier(currentValue -> modified)
                .execute();
    }
}
