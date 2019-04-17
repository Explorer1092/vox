package com.voxlearning.washington.mapper.picturebook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhiqian.ren
 */
@Data
public class PictureBookTagMapper implements Serializable {
    private static final long serialVersionUID = 8010430604429688610L;

    @JsonProperty("tag_id")
    private String tagId;
    @JsonProperty("tag_name")
    private String tagName;
    @JsonIgnore
    private Integer isHomeShow;
}
