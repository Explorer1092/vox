package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 公益 - 奖励字典
 * Created by ganhaitian on 2018/6/11.
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-public-good")
@DocumentCollection(collection = "vox_public_good_reward")
@UtopiaCacheRevision("20180929")
public class PublicGoodReward implements CacheDimensionDocument{

    private static final long serialVersionUID = 9138512554269341704L;

    @DocumentId private Long id;
    private String model;              // 模式
    private String type;               // 类型
    private String name;               // 名称
    private String imgUrl;             // ICON
    private String expression;         // 表达式
    private Boolean canHeap;           // 能否堆叠
    private Map<String,Object> extAttr;// 自定义属性

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(PublicGoodReward.class, "ALL");
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    public Object getExtAttrValue(String key){
        return Optional.ofNullable(extAttr)
                .map(am -> am.get(key))
                .orElse(null);
    }

    public Map<String,Object> setExtAttrValue(String key,Object val){
        if(this.extAttr == null)
            this.extAttr = new HashMap<>();

        this.extAttr.put(key, val);
        return this.extAttr;
    }

    public void putAllExtAttr(Map<String,Object> m){
        if(this.extAttr == null)
            this.extAttr = new HashMap<>();

        if(m != null){
            this.extAttr.putAll(m);
        }
    }

    /**
     * 为了在奖励页面整齐展示，要切得只剩四个字
     * @return
     */
    public String fetchPrettyCutName(){
        if(this.name == null)
            return "";

        return this.name.length() > 4 ? this.name.substring(0,4) : this.name;
    }
}
