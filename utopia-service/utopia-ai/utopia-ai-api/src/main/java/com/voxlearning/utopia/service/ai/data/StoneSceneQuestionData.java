package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class StoneSceneQuestionData implements Serializable {
    private static final long serialVersionUID = -1L;

    private String id;
    private ChipsQuestionType schemaName;
    private Topic jsonData;

    public static StoneSceneQuestionData newInstance(StoneData data) {
        StoneSceneQuestionData result = new StoneSceneQuestionData();
        result.setId(data.getId());
        result.setSchemaName(ChipsQuestionType.of(data.getSchemaName()));
        result.setJsonData(JsonStringDeserializer.getInstance().deserialize(data.getJsonData() != null ? data.getJsonData().replace("http://cdn.17zuoye.com", "https://cdn.17zuoye.com") : "", Topic.class));
        return result;
    }

    @Getter
    @Setter
    public static class Topic implements Serializable{
        private static final long serialVersionUID = -1L;
        private String cn_translation;
        private String translation;
        private String role_image;
        private String video;
        private String audio;
        private List<Feedback> jsgf_content;
        private List<TalkResultInfoKnowledgeSentence> sentences;

    }

    @Getter
    @Setter
    public static class Feedback implements Serializable {
        private static final long serialVersionUID = -1L;
        private String level;
        private String cn_translation;
        private String translation;
        private String tip;
        private String feedback;
        private String jsgf;

        //视频对话
        private String video;
        private String role_image;
        private String feedback_cover_pic; //首帧图

        //任务对话
        private String audio;
    }
}
