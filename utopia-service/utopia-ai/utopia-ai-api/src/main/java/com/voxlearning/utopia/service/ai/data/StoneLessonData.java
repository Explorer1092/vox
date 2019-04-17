package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class StoneLessonData implements Serializable {

    private static final long serialVersionUID = -1L;
    private String id;
    private Lesson jsonData;

    public static StoneLessonData newInstance(StoneData data) {
        StoneLessonData result = new StoneLessonData();
        result.setId(data.getId());
        result.setJsonData(JsonStringDeserializer.getInstance().deserialize(data.getJsonData() != null ? data.getJsonData().replace("http://cdn.17zuoye.com", "https://cdn.17zuoye.com") : "", Lesson.class));
        return result;
    }

    @Getter
    @Setter
    public static class Lesson implements Serializable {
        private static final long serialVersionUID = -1L;
        private String name;
        private LessonType lesson_type;
        private String background_intro;
        private String background_image;
        private String intro_audio;
        private String target;
        private String target_audio;
        private String video;//课程分享题视频,一对一视频点评使用
        private List<String> content_ids;
    }
}
