package com.voxlearning.utopia.service.newhomework.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.utopia.service.newhomework.api.constant.OralCommunicationContentType;
import com.voxlearning.utopia.service.newhomework.api.constant.OralStarScoreLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/22
 * \* Time: 3:29 PM
 * \* Description:口语交际
 * \
 */
@Getter
@Setter
public class OralCommunicationSummaryResult implements Serializable {

    private static final long serialVersionUID = -2320622243685512924L;

    @JsonProperty("stone_id")
    private String stoneId;         //内容库stoneId

    @JsonProperty("stone_type_name")
    private String stoneTypeName;   //情景包类型名称（互动对话/互动绘本/互动视频）

    @JsonProperty("stone_type")
    private OralCommunicationContentType stoneType;       //情景包类型 ： INTERACTIVE_CONVERSATION(互动会话）/INTERACTIVE_PICTURE_BOOK(互动绘本)/INTERACTIVE_VIDEO(互动视频)

    @JsonProperty("thumb_url")
    private String thumbUrl;        //情景包缩略图

    @JsonProperty("topic_trans")
    private String topicTrans;      //话题名称翻译

    @JsonProperty("topic_name")
    private String topicName;       //情景包名称

    @JsonProperty("is_finished")
    private Boolean isFinished = false;  //是否完成

    @JsonProperty("level")
    private OralStarScoreLevel level;//级别

    @JsonProperty("star")
    private int star;   //星级


}
