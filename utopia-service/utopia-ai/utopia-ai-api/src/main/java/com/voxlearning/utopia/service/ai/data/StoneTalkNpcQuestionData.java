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
public class StoneTalkNpcQuestionData implements Serializable {
    private static final long serialVersionUID = -1L;

    private String id;
    private ChipsQuestionType schemaName;
    private Npc jsonData;

    public static StoneTalkNpcQuestionData newInstance(StoneData data) {
        StoneTalkNpcQuestionData result = new StoneTalkNpcQuestionData();
        result.setId(data.getId());
        result.setSchemaName(ChipsQuestionType.of(data.getSchemaName()));
        result.setJsonData(JsonStringDeserializer.getInstance().deserialize(data.getJsonData() != null ? data.getJsonData().replace("http://cdn.17zuoye.com", "https://cdn.17zuoye.com") : "", Npc.class));
        return result;
    }

    @Getter
    @Setter
    public static class Npc implements Serializable{
        private static final long serialVersionUID = -1L;
        private String npc_name;
        private Boolean status;
        private String right_tip;
        private String background_image;
        private String role_image;
        private List<String> content_ids;

    }
}
