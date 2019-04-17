package com.voxlearning.washington.mapper.picturebook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhiqian.ren
 */
@Data
public class PictureBookSeriesOptionMapper implements Serializable {
    private static final long serialVersionUID = 6299922020042078600L;
    @JsonProperty("series_id")
    private String seriesId;
    @JsonProperty("series_name")
    private String seriesName;
}
