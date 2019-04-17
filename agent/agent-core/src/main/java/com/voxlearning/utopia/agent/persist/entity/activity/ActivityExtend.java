package com.voxlearning.utopia.agent.persist.entity.activity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_activity_extend")
public class ActivityExtend implements CacheDimensionDocument {

    @DocumentId
    private String id;
    private String activityId;

    private List<String> iconUrls;             // 活动图标
    private String linkUrl;                    // 点击活动记录跳转到的URL

    private List<String> posterUrls;           // 海报图片URL

    private String introductionUrl;            // 产品介绍及二维码URL

    private String recordUrl;                     // 记录明细URL

    private Integer qrCodeX;                   // 二维码在海报中的位置 x轴
    private Integer qrCodeY;                   // 二维码在海报中的位置 y轴

    private String slogan;                     // 宣传语（推广文案）

    private List<String> materialUrls;         // 推广素材

    private Integer form;                      // 活动形式  1：普通推广    2：链式推广    3: 组团  4： 礼品卡

    private Integer meetConditionDays;            // 需要参加的课程天数

    private Boolean multipleOrderFlag;            // 用户是否可以下多个订单

    private Boolean hasGift;                      // 是否有礼品赠送




    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("aid", this.activityId)
        };
    }
}
