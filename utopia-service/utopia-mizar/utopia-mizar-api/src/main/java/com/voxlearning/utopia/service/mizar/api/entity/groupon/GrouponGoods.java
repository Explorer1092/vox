package com.voxlearning.utopia.service.mizar.api.entity.groupon;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品详情
 *
 * @author xiang.lv
 * @date 2016/9/21   10:32
 */
@Setter
@Getter
@DocumentIndexes(value = {
        @DocumentIndex(def = "{'category_code':1}", background = true),
        @DocumentIndex(def = "{'data_source':1,'outer_goods_id':1}", background = true)
})
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_groupon_goods")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160921")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@ToString
public class GrouponGoods implements Serializable {

    private static final long serialVersionUID = -4876146569414606025L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;

    private String tmallId;                        //天猫的id,不需要存库

    @DocumentField("outer_goods_id")
    private String outerGoodsId;

    @DocumentField("category_code")
    private String categoryCode;

    @DocumentField("short_title")
    private String shortTitle;                       //短标题

    @DocumentField
    private String title;                           //标题

    @DocumentField
    private String description;                     //描述

    @DocumentField
    private Double price;                           //当前价格

    @DocumentField("original_price")
    private Double originalPrice;                   //原价

    @DocumentField
    private String image;                           //当前需要显示的图片,从imagesList选取一个

    @DocumentField("sale_count")
    private Integer saleCount;                      //销量

    @DocumentField("post_free")
    private Boolean postFree;                       //是否包邮,true-是,false-否

    @DocumentField("goods_source")
    private String goodsSource;                     //商品来源,天猫、淘宝、京东等

    @DocumentCreateTimestamp
    private Date createAt;                          //商品创建时间

    @DocumentUpdateTimestamp
    private Date updateAt;                           //商品更新时间

    @DocumentField("deploy_time")
    private Date deployTime;                          //发布时间,原来的开始时间和结束时间不要了

    @Deprecated
    @DocumentField("begin_time")
    private Date beginTime;                          //开始时间

    @Deprecated
    @DocumentField("end_time")
    private Date endTime;                           //结束时间

    @DocumentField("out_of_sales")
    private Boolean oos;                            //是否卖光 true-是,false-否

    @DocumentField("data_source")
    private String dataSource;                      //数据来源,折800采集、什么值得买采集、人工编辑

    @DocumentField("status")
    private String status;                          //商品状态 下线-OFFLINE,上线-ONLINE,审核中-PENDING

    @DocumentField("special_tag")
    private String specialTag ;                      //特色标签,多个以逗号分隔

    @DocumentField("recommend")
    private String recommend ;                       //推荐文本

    @DocumentField("goods_tag")
    private String goodsTag ;                      //商品标签,多个以逗号分隔

    @DocumentField("tag")
    private String tag;                            //商品标签,多个以逗号分隔

    @DocumentField("order_index")
    private Integer orderIndex;                     //推荐排序值

    @DocumentField
    private String url;                             //跳转url

   @DocumentField("origin_url")
    private String originUrl;                      //拉取过来的原始链接,后台页面不显示

    public static String ck_category(String categoryCode) {
        return CacheKeyGenerator.generateCacheKey(GrouponGoods.class, "CC", categoryCode);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(GrouponGoods.class, id);
    }
}
