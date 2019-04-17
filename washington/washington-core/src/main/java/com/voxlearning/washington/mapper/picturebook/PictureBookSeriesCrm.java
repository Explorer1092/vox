package com.voxlearning.washington.mapper.picturebook;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhiqian.ren
 */
@Data
public class PictureBookSeriesCrm implements Serializable {
    private static final long serialVersionUID = 6299922020042078600L;

    private String seriesId;
    private String bannerImg;
    private String seriesName;
    private String seriesContent;
    private List<String> pictureBookIds;
}
