package com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Setter
@Getter
public class LiveCastPictureBookInfo implements Serializable {
    private static final long serialVersionUID = 8157940560432663705L;
    private int score;
    private int duration;
    private String pictureBookId;
    private String time;
    private Map<String, Object> pictureBookInfo;
    private boolean flag;
}
