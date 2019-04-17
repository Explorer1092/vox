package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
@Getter
@Setter
public class StoneBookData implements Serializable {
    private static final long serialVersionUID = -1L;
    private String id;
    private String name;
    private Book jsonData;
    private String customName;

    public static StoneBookData newInstance(StoneData data) {
        StoneBookData bookData = new StoneBookData();
        bookData.setId(data.getId());
        bookData.setCustomName(data.getCustomName());
        bookData.setJsonData(JsonStringDeserializer.getInstance().deserialize(data.getJsonData() != null ? data.getJsonData().replace("http://cdn.17zuoye.com", "https://cdn.17zuoye.com") : "", Book.class));
        return bookData;
    }

    @Getter
    @Setter
    public static class Book implements Serializable {
        private static final long serialVersionUID = -1L;
        private String name;
        private String cover_image;
        private List<Node> children;
    }

    @Getter
    @Setter
    public static class Node implements Serializable {
        private static final long serialVersionUID = -1L;
        private String stone_data_id;
        private List<Node> children;
    }
}
