package com.voxlearning.utopia.service.mizar.api.entity.groupon;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Created by 17ZY-HPYKFD2 on 2016/9/21.
 * 商品分类实体
 *
 * @author 17ZY-HPYKFD2
 * @date 2016/9/21   10:24
 */
@Setter
@Getter
@DocumentIndexes(value = {
        @DocumentIndex(def = "{'category_code':1}", background = true)
})
@ToString
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "mizar_goods_category")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160921")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class GoodsCategory implements Serializable {

    private static final long serialVersionUID = -221449237802785341L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;

    @DocumentField("category_name")
    private String categoryName;                    //分类名称

    @DocumentField("category_code")
    private String categoryCode;                    //分类名称

    @JsonIgnore
    @DocumentField("category_path")
    private String categoryPath;                  //分类路径,兼容多级分类情况

    @DocumentField
    private Boolean disabled;                       //false表示启用， true表示关闭

    @DocumentField("order_index")
    private Integer orderIndex;                   //分类排序值

    @DocumentCreateTimestamp
    private Date createAt;

    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_categoryCode(String categoryCode) {
        return CacheKeyGenerator.generateCacheKey(GoodsCategory.class, categoryCode);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(GoodsCategory.class, "ALL");
    }

}
