package com.voxlearning.washington.mapper.picturebook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**首页绘本推荐mapper
 * @author zhiqian.ren
 */
@Data
public class PicBookRecommendMapper implements Serializable {
    private static final long serialVersionUID = -2125676585679463018L;

    private String id;
    @JsonProperty("cover_url")
    private String coverUrl;
    /**封面缩略图*/
    private String thumbnail;
    private String name;
    private String nameZH;
    /**0:绘本系列；1：单个绘本*/
    private int type;
    /**横屏landscape、竖屏vertical*/
    @JsonProperty("screen_mode")
    private String screenMode;
    /**总的词汇量*/
    @JsonProperty("word_length")
    private Integer wordsLength;
    @JsonProperty("topic_name")
    private String topicText;
    private List<String> tags;
    private Integer isTop;
}
