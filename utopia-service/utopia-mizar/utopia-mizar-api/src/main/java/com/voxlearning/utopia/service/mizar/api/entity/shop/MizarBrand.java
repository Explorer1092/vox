package com.voxlearning.utopia.service.mizar.api.entity.shop;

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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/8/15.
 * 机构导流——品牌
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_brand")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161020")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentIndexes(value = {
        @DocumentIndex(def = "{'show_list':-1}", background = true)
})
public class MizarBrand implements Serializable {

    private static final long serialVersionUID = 1473161902269693702L;
    public static final String FACULTY_NAME = "name";  // 名字
    public static final String FACULTY_PHOTO = "photo"; // 头像
    public static final String FACULTY_EXPERIENCE = "experience"; // 教龄
    public static final String FACULTY_COURSE = "course";   // 科目
    public static final String FACULTY_DESCRIPTION = "description"; // 描述

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    @DocumentField("brand_name") private String brandName;                            // 品牌名称
    @DocumentField("brand_logo") private String brandLogo;                            // 品牌LOGO
    @DocumentField("introduction") private String introduction;                       // 品牌介绍
    @DocumentField("brand_photo") private List<String> brandPhoto;                    // 中心图片
    @DocumentField("establishment") private String establishment;                     // 创立时间
    @DocumentField("shop_scale") private String shopScale;                            // 品牌规模
    @DocumentField("faculty") private List<Map<String, Object>> faculty;              // 师资力量 name,photo
    @DocumentField("certification_photos") private List<String> certificationPhotos;  // 获奖证书 photos
    @DocumentField("certificationName") private String certificationName;             // 获奖证书 描述
    @DocumentField("points") private List<String> points;                             // 品牌特点
    @DocumentField("show_list") private Boolean showList;                             //是否显示在品牌管列表
    @DocumentField("order_index") private Integer orderIndex;                         //品牌管列表中的排序值,大值在前
    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MizarBrand.class, id);
    }

}

