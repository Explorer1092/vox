package com.voxlearning.utopia.enanalyze.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.utopia.enanalyze.assemble.AINLPClient;
import com.voxlearning.utopia.enanalyze.model.ArticleBasicAbility;
import com.voxlearning.utopia.enanalyze.model.ArticleCompositeAbility;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 作文
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "article")
public class ArticleEntity implements Serializable {

    @DocumentId
    private String id;

    /**
     * openid
     */
    private String openId;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * ocr结果，作文文本
     */
    private String text;

    /**
     * npl结果
     */
    private AINLPClient.Result nlpResult;

    /**
     * 基本能力
     */
    private ArticleBasicAbility basicAbility;

    /**
     * 综合能力
     */
    private ArticleCompositeAbility compositeAbility;

    /**
     * 更新日期
     */
    @DocumentCreateTimestamp
    private Date createDate;

    /**
     * 更新日期
     */
    @DocumentCreateTimestamp
    private Date updateDate;

    /**
     * 逻辑删除
     */
    private Boolean disable;


}
