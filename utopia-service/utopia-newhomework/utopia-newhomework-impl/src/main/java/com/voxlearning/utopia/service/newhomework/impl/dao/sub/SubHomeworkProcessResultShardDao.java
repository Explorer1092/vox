package com.voxlearning.utopia.service.newhomework.impl.dao.sub;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheEncoder;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.DynamicMongoShardPersistence;
import com.voxlearning.alps.lang.support.RangeableId;
import com.voxlearning.alps.lang.support.RangeableIdVersion;
import com.voxlearning.alps.spi.core.Encoder;
import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Named
@CacheBean(type = SubHomeworkProcessResult.class, cacheName = "utopia-homework-cache", useEagerInsert = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class SubHomeworkProcessResultShardDao extends DynamicMongoShardPersistence<SubHomeworkProcessResult, String> {
    @Override
    protected String calculateDatabase(String template, SubHomeworkProcessResult document) {
        RangeableId rangeableId = RangeableIdVersion.V1.parse(document.getId());
        Objects.requireNonNull(rangeableId);
        return StringUtils.formatMessage(template, rangeableId.range(DateRangeType.M).toString());
    }

    @Override
    protected String calculateCollection(String template, SubHomeworkProcessResult document) {
        RangeableId rangeableId = RangeableIdVersion.V1.parse(document.getId());
        Objects.requireNonNull(rangeableId);
        return StringUtils.formatMessage(template, rangeableId.range(DateRangeType.D).toString());
    }

    @Override
    protected void calculateCacheDimensions(SubHomeworkProcessResult document, Collection<String> dimensions) {
        dimensions.add(SubHomeworkProcessResult.ck_id(document.getId()));
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

        SubHomeworkProcessResult subHomeworkProcessResult = load(id);
        if (subHomeworkProcessResult == null) {
            return null;
        }
        // 是否已阅
        if (Objects.equals(Boolean.TRUE, review)) {
            subHomeworkProcessResult.setReview(review);
        }
        // 批改类型
        if (correctType != null) {
            subHomeworkProcessResult.setCorrectType(correctType);
        } else {
            // 默认属性
            subHomeworkProcessResult.setCorrectType(CorrectType.CORRECT);
        }
        // 批改信息，不为空且为非批量/一键操作的时候
        if (Objects.equals(Boolean.FALSE, isBatch)) {
            subHomeworkProcessResult.setCorrection(correction);
        }
        // 评语
        if (StringUtils.isNotBlank(teacherMark)) {
            subHomeworkProcessResult.setTeacherMark(teacherMark);
        }
        subHomeworkProcessResult.setUpdateAt(new Date());

        return replace(subHomeworkProcessResult);
    }

    @Override
    @CacheMethod(encoder = @CacheEncoder(encoder = Encoder.fst, enable = true))
    public Map<String, SubHomeworkProcessResult> loads(@CacheParameter(multiple = true) Collection<String> strings) {
        return $loads(strings).getUninterruptibly();
    }

    @Override
    @CacheMethod(encoder = @CacheEncoder(encoder = Encoder.fst, enable = true))
    public SubHomeworkProcessResult load(@CacheParameter String s) {
        return super.$load(s).getUninterruptibly();
    }
}
