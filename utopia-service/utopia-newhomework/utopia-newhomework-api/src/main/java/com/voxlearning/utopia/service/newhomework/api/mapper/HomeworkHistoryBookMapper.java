package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

/**
 * 学生历史列表页-作业教材信息
 *
 * @author xuesong.zhang
 * @since 2016-03-23
 */
@Getter
@Setter
public class HomeworkHistoryBookMapper implements Serializable {

    private static final long serialVersionUID = 8004697420965076825L;

    private String bookId;
    private String bookName;
    private String unitId;
    private String unitName;
    private Set<String> sectionIds;
    private Set<String> sectionNames;

    private Integer unitRank;
}
