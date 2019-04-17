package com.voxlearning.washington.mapper.picturebook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhiqian.ren
 */
@Data
public class PictureBookClazzLevelMapper implements Serializable {
    private static final long serialVersionUID = 8010430604429688610L;

    @JsonProperty("clazz_level")
    private String clazzLevel;
    @JsonProperty("clazz_content")
    private String clazzContent;
}
