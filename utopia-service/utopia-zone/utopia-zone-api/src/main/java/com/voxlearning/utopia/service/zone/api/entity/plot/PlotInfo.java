package com.voxlearning.utopia.service.zone.api.entity.plot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author : kai.sun
 * @version : 2018-11-09
 * @description : 剧情配置表
 **/
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_clazz_plot_info")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181109")
public class PlotInfo implements Serializable {

    private static final long serialVersionUID = 6906866737256359899L;

    /**剧情id : 例如 activityId_1_1  activityId_1_2*/
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                 //对应剧情

    @DocumentField("activity_id")
    private Integer activityId;         //活动id

    @DocumentField("plot_group")
    private Integer plotGroup;          //剧情组(1-x)

    @DocumentField("plot_num")
    private Integer plotNum;            //剧情 （1 ~ x）

    @DocumentField
    private String title;               //剧情标题

    @DocumentField
    private String text;                //剧情字幕

    @DocumentField("unlock_cover_img")
    private String unlockCoverImg;      //未解锁的背景图

    @DocumentField
    private String bgm;                 //背景音乐

    @DocumentField("audio_url")
    private String audioUrl;            //音频绝对路径

    @DocumentField
    private List<Dialog> dialog;        //NPC话语

    @DocumentField("need_unlock")
    private Boolean needUnlock;         //需要解锁

    @DocumentField("last_plot_id")
    private String lastPlotId;        //上一剧情

    @DocumentField("next_plot_id")
    private String nextPlotId;        //下一剧情

    @JsonIgnore
    @DocumentField("modify_date")
    @DocumentUpdateTimestamp
    private Date modifyDate;

    @JsonIgnore
    @DocumentField("create_date")
    @DocumentCreateTimestamp
    private Date createDate;

    public static String generatorId(Integer activityId,Integer plotGroup,Integer plotNum){
        if(activityId==null||plotGroup==null||plotNum==null) return null;
        return  String.valueOf(activityId)+"_"+String.valueOf(plotGroup)+"_"+String.valueOf(plotNum);
    }

    //缓存key
    public static String ck_regex(String regex) {
        return CacheKeyGenerator.generateCacheKey(PlotInfo.class,new String[]{"regex"},new Object[]{regex});
    }

    public static String db_regex(Integer activityId,Integer plotGroup){
        return  String.valueOf(activityId)+"_"+String.valueOf(plotGroup)+"_";
    }

    public static String ck_plotInfoList() {
        return CacheKeyGenerator.generateCacheKey(PlotInfo.class, "plotInfoList");
    }

}
