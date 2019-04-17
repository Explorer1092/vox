package com.voxlearning.washington.mapper.picturebook;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhiqian.ren
 */
@Data
public class PicBookTopicMapper implements Serializable {
    private static final long serialVersionUID = -3899879544922203634L;

    private String topicId;
    private String topicName;
    private List<PicBookRecommendMapper> books;
}
