//package com.voxlearning.utopia.service.campaign.api.document;

///**
// * Lottery Configuration
// * Created by alex on 2018/3/20.
// */
//@Getter
//@Setter
//@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
//@DocumentTable(table = "VOX_LOTTERY_CONFIG")
//@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
//@UtopiaCacheRevision("20180320")
//public class LotteryConfig extends AbstractDatabaseEntity implements CacheDimensionDocument {
//    private static final long serialVersionUID = 1266664143345161315L;
//
//    private String campaignId;           // 活动ID  @LotteryCampaign
//    private Integer awardId;             // 奖品ID
//    private String awardLevelName;       // 奖品级别
//    private String awardName;            // 奖品名称
//    private String awardContent;         // 奖品内容 根据这个字段内去发放奖品
//    private Integer awardRate;           // 中奖率，百万为单位的整数
//    private Boolean bigAward;            // 是否大奖  中大奖的会进入大奖动态
//    private Integer totalAwardNum;       // 总奖品数量      只有大奖才需要，用来控制不超出中奖预算
//    private Integer remainAwardNum;      // 剩余奖品数量    只有大奖才需要，用来控制不超出中奖预算
//    private Date startTime;              // 开始时间        只有大奖才需要，用来控制出大奖的时间分布
//    private Date endTime;                // 结束时间        只有大奖才需要，用来控制出大奖的时间分布
//
//    @Override
//    public String[] generateCacheDimensions() {
//        return new String[]{
//                newCacheKey("CID", campaignId)
//        };
//    }
//}
