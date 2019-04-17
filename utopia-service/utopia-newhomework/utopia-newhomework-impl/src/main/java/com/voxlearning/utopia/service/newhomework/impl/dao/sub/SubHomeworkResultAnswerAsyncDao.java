package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheEncoder;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncDynamicMongoPersistence;
import com.voxlearning.alps.spi.core.Encoder;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;

import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

@Named
@CacheBean(type = SubHomeworkResultAnswer.class, useValueWrapper = true, cacheName = "utopia-homework-cache", useEagerInsert = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SubHomeworkResultAnswerAsyncDao extends AsyncDynamicMongoPersistence<SubHomeworkResultAnswer, String> {
    @Override
    protected String calculateDatabase(String template, SubHomeworkResultAnswer document) {
        SubHomeworkResultAnswer.ID id = document.parseID();
        String month = StringUtils.substring(id.getDay(), 0, 6);
        return StringUtils.formatMessage(template, month);
    }

    @Override
    protected String calculateCollection(String template, SubHomeworkResultAnswer document) {
        SubHomeworkResultAnswer.ID id = document.parseID();
        return StringUtils.formatMessage(template, id.getDay());
    }

    @Override
    protected void calculateCacheDimensions(SubHomeworkResultAnswer document, Collection<String> dimensions) {
        dimensions.add(SubHomeworkResultAnswer.ck_id(document.getId()));
    }

    @Override
    @CacheMethod(encoder = @CacheEncoder(encoder = Encoder.fst, enable = true))
    public Map<String, SubHomeworkResultAnswer> loads(@CacheParameter(multiple = true) Collection<String> strings) {
        return $loads(strings).getUninterruptibly();
    }

    @Override
    @CacheMethod(encoder = @CacheEncoder(encoder = Encoder.fst, enable = true))
    public SubHomeworkResultAnswer load(@CacheParameter String s) {
        return super.$load(s).getUninterruptibly();
    }
}
