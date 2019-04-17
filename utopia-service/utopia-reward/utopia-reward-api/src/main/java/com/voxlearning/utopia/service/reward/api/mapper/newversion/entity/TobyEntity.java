package com.voxlearning.utopia.service.reward.api.mapper.newversion.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class TobyEntity implements Serializable {
    private Image image;
    private Countenance countenance;
    private Props props;
    private Accessory accessory;
    @Getter
    @Setter
    @ToString
    public class Image implements Serializable {
        private Long id;//:"形象id",
        private String url;//"":"形象图片url",
    }
    @Getter
    @Setter
    @ToString
    public class Countenance implements Serializable {
        private Long id;//:"形象id",
        private String url;//"":"形象图片url",
    }
    @Getter
    @Setter
    @ToString
    public class Props implements Serializable {
        private Long id;//:"形象id",
        private String url;//"":"形象图片url",
    }
    @Getter
    @Setter
    @ToString
    public class Accessory implements Serializable {
        private Long id;//:"形象id",
        private String url;//"":"形象图片url",
    }
}
