package com.voxlearning.utopia.service.newhomework.api.entity.poetry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 古诗
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-poetry")
@DocumentCollection(collection = "poetry_mission")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20190220")
public class AncientPoetryMission implements Serializable {
    private static final long serialVersionUID = -372727785736244260L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                          // 关卡ID(古诗ID)
    private String title;                       // 标题
    private String author;                      // 作者
    private String goalDetail;                  // 目标描述
    private String audioUrl;                    // 上传音频
    private Integer audioSeconds;               // 音频时长
    private String comment;                     // 内容说明
    private String description;                 // 描述
    private List<String> contentList;           // 文本内容
    private String createUserId;
    @DocumentCreateTimestamp
    private Date createDate;                    //创建时间
    @DocumentUpdateTimestamp
    private Date updateDate;                    //修改时间

    private LinkedHashMap<ModelType, Model> models;    // 模块列表<model_type, Model>

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AncientPoetryMission.class, id);
    }

    public static String generateId() {
        return StringUtils.join("AP_", RandomUtils.nextObjectId());
    }

    @JsonIgnore
    public static String getHelpMissionId(){
        return "-1";
    }

    @Getter
    @Setter
    public static class Model implements Serializable {
        private static final long serialVersionUID = -7634291242522899737L;

        private String modelTitle;             //模块标题
        private ModelType modelType;           //模块类型

        private ExpoundContent expoundContent;
        private AppreciateContent appreciateContent;
        private ReciteContent reciteContent;
        private FunContent funContent;
    }

    @Getter
    @Setter
    public static class ExpoundContent implements Serializable {
        private static final long serialVersionUID = -7297792789603284621L;

        private String subTitle;            //子标题
        private String backgroundImgUrl;    //上传背景图
        private String audioUrl;            //上传音频
        private Integer audioSeconds;        //音频时长
    }

    @Getter
    @Setter
    public static class AppreciateContent implements Serializable {
        private static final long serialVersionUID = -2270214862096328767L;

        private String subTitle;            //子标题
        private String backgroundImgUrl;    //上传背景图
        private String audioUrl;            //上传音频
        private Integer audioSeconds;        //音频时长
    }

    @Getter
    @Setter
    public static class ReciteContent implements Serializable {
        private static final long serialVersionUID = 1071914809125149857L;
        private List<Sentence> sentenceList;
    }

    @Getter
    @Setter
    public static class FunContent implements Serializable {
        private static final long serialVersionUID = 6355231167804332079L;
        private List<String> questionIds; //题目ID列表
    }

    @Getter
    @Setter
    public static class Sentence implements Serializable {
        private static final long serialVersionUID = -2625462401468543973L;
        private String sentence;//句子内容
        private String audioUrl;//音频地址
        private Integer audioSeconds;//音频时长
        private Integer rank;//展示顺序
    }
}
