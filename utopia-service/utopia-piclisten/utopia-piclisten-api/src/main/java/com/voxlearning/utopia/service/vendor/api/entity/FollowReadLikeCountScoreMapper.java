package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.lang.calendar.DateRange;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 后六位留给 周期的开始时间到作品创建时间的秒数。如果创建时间早于周期开始时间,则全为0;
 *
 */
@Getter
@Setter
public class FollowReadLikeCountScoreMapper implements Serializable {
    private static final long serialVersionUID = -3766768828689314934L;

    @JsonProperty("like_count")
    protected Long likeCount;

    protected DateRange dateRange;

    protected Date createTime;

    @JsonProperty("collection_id")
    protected String collectionId;

    public FollowReadLikeCountScoreMapper(){}

    public FollowReadLikeCountScoreMapper(Long likeCount, DateRange range, Date createTime){
        this.likeCount = likeCount;
        this.dateRange = range;
        this.createTime = createTime;
    }


    /**
     * 再redis里排名使用的分数,
     * 先根据点赞数倒序排名,点赞数相同的,创建时间越晚的排名越靠前
     * @return
     */
    public Double toScore(){
        long time = createTime.getTime() - dateRange.getStartTime();
        if (time < 0)
            time = 0L;
        return (double) (likeCount * 1000000 + time / 1000);
    }

    /**
     * dataRage is missing
     * createTime is missing
     * @param score
     * @return
     */
    public static FollowReadLikeCountScoreMapper instantsFromScore(Double score, String collectionId){
        if (score == null || score == 0)
            return null;
        FollowReadLikeCountScoreMapper mapper = new FollowReadLikeCountScoreMapper();
        long likeCount = getLikeCountFromScore(score);
        if (likeCount == 0) {
            return null;
        }
        mapper.setLikeCount(likeCount);
        mapper.setCollectionId(collectionId);
        return mapper;
    }

    public static Long getLikeCountFromScore(Double score){
        if (score == null || score == 0)
            return 0L;
        return Double.valueOf(score / 1000000).longValue();
    }

    public static Double toScoreWithoutTime(Long likeCount){
        return (double) (likeCount * 1000000);
    }
}
