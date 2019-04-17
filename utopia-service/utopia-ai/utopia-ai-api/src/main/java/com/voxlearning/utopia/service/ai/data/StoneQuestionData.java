package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class StoneQuestionData implements Serializable {
    private static final long serialVersionUID = -1L;

    private String id;
    private ChipsQuestionType schemaName;
    private Map<String, Object> jsonData;

    public static StoneQuestionData newInstance(StoneData data) {
        StoneQuestionData result = new StoneQuestionData();
        result.setId(data.getId());
        result.setSchemaName(ChipsQuestionType.of(data.getSchemaName()));
        result.setJsonData(JsonStringDeserializer.getInstance().deserialize(data.getJsonData() != null ? data.getJsonData().replace("http://cdn.17zuoye.com", "https://cdn.17zuoye.com") : ""));
        return result;
    }
}
