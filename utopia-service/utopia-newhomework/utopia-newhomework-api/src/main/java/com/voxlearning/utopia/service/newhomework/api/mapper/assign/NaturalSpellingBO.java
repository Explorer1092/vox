package com.voxlearning.utopia.service.newhomework.api.mapper.assign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/7/3 21:20
 */

@Setter
@Getter
public class NaturalSpellingBO implements Serializable {
    private static final long serialVersionUID = -595494151018082460L;

    private List<String> sentences;
    private List<NaturalSpellingCategoryBO> categories;
    private Boolean newLine;    //句子是否需要换行
}
