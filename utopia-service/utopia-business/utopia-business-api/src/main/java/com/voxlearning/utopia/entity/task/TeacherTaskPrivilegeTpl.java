package com.voxlearning.utopia.entity.task;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 老师特权字典表
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-teacher-app")
@DocumentCollection(collection = "vox_teacher_task_privilege_tpl")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("201809011")
public class TeacherTaskPrivilegeTpl implements CacheDimensionDocument {

    private static final long serialVersionUID = 6538593379165360366L;

    //专家题库地区配置，后面等各个特权丰富后，抽取成配置表
    public static final List<String> localExpertTopicConfig = Arrays.asList("370000","320000","120000","310000","210000","360000","420000","130000","440000");

    @DocumentId private Long id;
    private String name;                            //特权名称
    private String subName;                         //副标题
    private Integer sort;                           //排序
    private Boolean isShow;                         //是否暂时特权，比如某个特权下线，临时不在前端显示了
    private String instruction;                     //特权说明
    private String type;                            //特权类型
    private Map<String,Object> skip;                //跳转的信息，给前端提供的
    private String putOnExpr;                       //领取特权表达式
    private Boolean loop;                           //是否为循环特权
    private String cycleUnit;                       //循环单位:W:按周循环 M：按月循环，O:其他
    private Boolean timesLimit;                     //是否有使用次数限制
    private String timesExpr;                       //计算次数的表达式
    private String quantifier;                      //次数的单位
    private Boolean isCoupon;                       //是否有券
    private String couponId;                        //券的编号
    private Map<String, Object> attribute;          //各个特权一些各自特殊的属性

    @Getter
    public enum Privilege {
        BIRTHDAY_INTEGRAL(1L, "BIRTHDAY_INTEGRAL"),                     //生日园丁豆
        FREE_LOTTRY_PRIMARY(2L, "FREE_LOTTRY"),                         //免费抽奖
        FREE_LOTTRY_INTERMEDIATE(3L, "FREE_LOTTRY"),                    //免费抽奖
        FREE_LOTTRY_SENIOR(4L, "FREE_LOTTRY"),                          //免费抽奖
        FREE_LOTTRY_SUPER(5L, "FREE_LOTTRY"),                           //免费抽奖
        COURSEWARE_DOWNLOAD_INTERMEDIATE(6L, "COURSEWARE_DOWNLOAD"),    //课件下载
        COURSEWARE_DOWNLOAD_SENIOR(7L, "COURSEWARE_DOWNLOAD"),          //课件下载
        COURSEWARE_DOWNLOAD_SUPER(8L, "COURSEWARE_DOWNLOAD"),           //课件下载
        AWARD_CHANGE_90(9L, "AWARD_CHANGE"),                            //奖品兑换90折
        AWARD_CHANGE_85(10L, "AWARD_CHANGE"),                           //奖品兑换85折
        AWARD_CHANGE_80(11L, "AWARD_CHANGE"),                           //奖品兑换80折
        CLASS_17_INTERMEDIATE(12L, "CLASS_17"),                         //一起课堂
        CLASS_17_SENIOR(13L, "CLASS_17"),                               //一起课堂
        CLASS_17_SUPER(14L, "CLASS_17"),                                //一起课堂
        ARTICLE_FIRST(15L, "ARTICLE_FIRST"),                            //论文期刊优先发表
        CASH_COUPON_30(16L, "CASH_COUPON"),                             //代金券30
        CASH_COUPON_80(17L, "CASH_COUPON"),                             //代金券80
        CUSTOMER_SERVICE_FIRST(18L, "CUSTOMER_SERVICE_FIRST"),          //客服优先
        NEW_PRODUCT_FIRST(19L, "NEW_PRODUCT_FIRST"),                    //新品优先体验
        DISCUSS_FIRST(20L, "DISCUSS_FIRST"),                            //学术会议优先
        COURSEWARE_DOWNLOAD_NORMAL(21L, "COURSEWARE_DOWNLOAD"),         //课件下载
        COURSEWARE_DOWNLOAD_PRIMARY(22L, "COURSEWARE_DOWNLOAD"),        //课件下载
        CLASS_17_NORMAL(23L, "CLASS_17"),                               //一起课堂
        CLASS_17_PRIMARY(24L, "CLASS_17"),                              //一起课堂
        TEACHING_METHOD(25L, "TEACHING_METHOD"),                        //教学锦囊
        LOCAL_EXPERT_TOPIC(26L, "LOCAL_EXPERT_TOPIC"),                  //本地专家题库
        KOUSUAN_PAIZHAO(27L, "KOUSUAN_PAIZHAO"),                        //口算拍照
        JIANG_LIAN_CE(28L, "JIANG_LIAN_CE"),                            //讲练测
        ;
        private Long id;
        private String type;
        Privilege(Long id, String type) {
            this.id = id;
            this.type = type;
        }
    }

    public enum CycleUnit {
        W,
        M
        ;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
            newCacheKey("ALL")
        };
    }

}