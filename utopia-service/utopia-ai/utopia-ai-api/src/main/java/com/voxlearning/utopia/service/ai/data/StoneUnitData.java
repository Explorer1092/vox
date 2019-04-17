package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class StoneUnitData implements Serializable {
    private static final long serialVersionUID = -1L;
    private String id;
    private Unit jsonData;
    private String customName;

    public static StoneUnitData newInstance(StoneData data) {
        StoneUnitData stoneUnitData = new StoneUnitData();
        stoneUnitData.setId(data.getId());
        stoneUnitData.setCustomName(data.getCustomName());
        stoneUnitData.setJsonData(JsonStringDeserializer.getInstance().deserialize(data.getJsonData() != null ? data.getJsonData().replace("http://cdn.17zuoye.com", "https://cdn.17zuoye.com") : "", Unit.class));
        return stoneUnitData;
    }

    @Getter
    @Setter
    public static class Unit implements Serializable {
        private static final long serialVersionUID = -1L;
        private String name;
        private ChipsUnitType unit_type;
        private String title;
        private String names;
        private String sub_title;
        private String image_title;
        private String image_discription;
        private String cover_image;
        private String key_points;
        private String key_points_audio;
        private String video;//单元讲解视频,一对一视频点评使用

        private String reward_illust_id;//图鉴id
    }
}
