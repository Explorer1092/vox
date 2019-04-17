package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jiangpeng
 * @since 2017-03-29 下午2:40
 **/
@Setter
@Getter
public class FollowReadRangeWrapper extends FollowReadLikeCountScoreMapper {
    private static final long serialVersionUID = -5867611838653374843L;

    @JsonProperty("student_id")
    private Long studentId;

    @JsonProperty("student_name")
    private String studentName;

    private String avatar;

    @JsonProperty("school_name")
    private String schoolName;

    @JsonProperty("clazz_name")
    private String clazzName;

    @JsonProperty("unit_name")
    private String unitName;

    @JsonProperty("unit_id")
    private String unitId;

    @JsonProperty("book_id")
    private String bookId;

}
