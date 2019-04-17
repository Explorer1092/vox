package com.voxlearning.utopia.service.reward.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.reward.constant.PublicGoodModel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 奖品中心 - 活动
 * Created by haitian.gan on 2017/2/4.
 */
@DocumentTable(table = "VOX_REWARD_ACTIVITY")
@DocumentConnection(configName = "hs_reward")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180629")
public class RewardActivity extends AbstractDatabaseEntity implements CacheDimensionDocument,Cloneable {

    private static final long serialVersionUID = 1665918230972337349L;
    private static final String MODEL_NONE = PublicGoodModel.NONE.name();

    @UtopiaSqlColumn @Getter @Setter private String name;
    @UtopiaSqlColumn @Getter @Setter private Double progress;
    @UtopiaSqlColumn @Getter @Setter private Integer partakeNums;
    @UtopiaSqlColumn @Getter @Setter private String imgUrl;
    @UtopiaSqlColumn @Getter @Setter private String imgUrlSquare;
    @UtopiaSqlColumn @Getter @Setter private Boolean online;
    @UtopiaSqlColumn @Getter @Setter private Long raisedMoney;
    @UtopiaSqlColumn @Getter @Setter private Long targetMoney;
    @UtopiaSqlColumn @Getter @Setter private String description;
    @UtopiaSqlColumn @Getter @Setter private String progressDetail;
    @UtopiaSqlColumn @Getter @Setter private String status;
    @UtopiaSqlColumn @Getter @Setter private Date detailUpdattime;
    @UtopiaSqlColumn @Getter @Setter private Integer orderWeights;  // 排序权重
    @UtopiaSqlColumn @Getter @Setter private Date finishTime;       // 完成时间
    @UtopiaSqlColumn @Getter @Setter private String summary;        // 简介(概要)
    @UtopiaSqlColumn @Getter @Setter private String model;          // 模式

    @JsonIgnore
    public boolean isOldModel() {
        return !isNewModel();
    }

    @JsonIgnore
    public boolean isNewModel() {
        return (model != null) && (!Objects.equals(model, MODEL_NONE));
    }

    @DocumentFieldIgnore
    @Getter
    @Setter
    private List<String> images;

    @DocumentFieldIgnore
    @Getter
    @Setter
    private boolean participatedIn;// 传参用的，标注用户今天是否参加过活动

    @DocumentFieldIgnore
    @Getter
    @Setter
    private String finishTimeStr;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    public enum Status {
        ONGOING,// 进行中
        FINISHED// 已完成
    }

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(RewardActivity.class, id);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(RewardActivity.class, "ALL");
    }

    public void calculateProgress() {
        if (targetMoney != 0) {
            // 进度为百分比，最少显示0.01%，最多不能超过100
            /*this.progress = Math.max((double)raisedMoney / targetMoney * 100d,0.01d) ;
            this.progress = Math.min(this.progress,100d);
            // 截取小数点后两位
            BigDecimal bdProgress = new BigDecimal(this.progress);
            bdProgress = bdProgress.setScale(2,BigDecimal.ROUND_HALF_UP);*/

            this.progress = BigDecimal.valueOf(raisedMoney)
                    .multiply(BigDecimal.valueOf(100d))
                    .divide(BigDecimal.valueOf(targetMoney), 2, BigDecimal.ROUND_FLOOR)
                    .max(BigDecimal.valueOf(0.01d))
                    .min(BigDecimal.valueOf(100d))
                    .doubleValue();
        } else
            this.progress = 100d;
    }

    public boolean isOnGoing(){
        return Objects.equals(status,Status.ONGOING.name());
    }

    public boolean isFinished(){
        return Objects.equals(status,Status.FINISHED.name());
    }

    @Override
    public RewardActivity clone() {
        try {
            return (RewardActivity) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }
}