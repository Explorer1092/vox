package com.voxlearning.utopia.service.newhomework.api.mapper.assign;

import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/7/3 21:59
 */

@Setter
@Getter
public class NaturalSpellingCategoryBO implements Serializable {
    private static final long serialVersionUID = 8007921390477299219L;

    private Integer categoryId;
    private String categoryName;
    private Integer categoryIcon;
    private List<String> categoryIcons;
    private boolean checked;
    private EmbedBook book;
    private List<Map<String, Object>> practices;
    private Integer teacherAssignTimes;
    private List<String> previewImages;
    private String previewVideo;
}
