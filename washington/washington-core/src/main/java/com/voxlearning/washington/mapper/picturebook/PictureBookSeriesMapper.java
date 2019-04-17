package com.voxlearning.washington.mapper.picturebook;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhiqian.ren
 */
@Data
public class PictureBookSeriesMapper implements Serializable {
    private static final long serialVersionUID = 6299922020042078600L;

    private String seriesId;
    private List<String> seriesIcon;
    private String seriesName;
    private String seriesContent;
}
