package com.voxlearning.washington.mapper.picturebook;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zhiqian.ren
 */
@Data
public class PictureBookSeriesDetailMapper implements Serializable {

    private static final long serialVersionUID = 7210448444872013567L;

    private String seriesId;
    private String seriesIcon;
    private String seriesName;
    private String seriesContent;
    private Map<String,Object> picBooks;
}
