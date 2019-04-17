package com.voxlearning.washington.mapper.picturebook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 描述:
 * 系列中绘本Mapper
 *
 * @author zhiqian.ren.Sirius
 * @create 2018-12-04
 */
@Data
public class PicBookInSeriesMapper implements Serializable {

    private static final long serialVersionUID = 7118634374837651099L;

    @JsonProperty("pb_id")
    private String pictureBookId;
    @JsonProperty("en_name")
    private String enName;
    @JsonProperty("cn_name")
    private String cnName;
    @JsonProperty("topic_text")
    private String topicText;
    @JsonProperty("re_word")
    private String recommendWord;
    @JsonProperty("re_word_second")
    private String recommendWordSecond;
    private String thumbnail;
    @JsonProperty("config_words")
    private String configWords;
}