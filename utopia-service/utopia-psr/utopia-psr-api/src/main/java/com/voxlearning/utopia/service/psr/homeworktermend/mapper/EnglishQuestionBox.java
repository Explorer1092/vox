package com.voxlearning.utopia.service.psr.homeworktermend.mapper;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 英语推送题包
 *
 * @author xuesong.zhang
 * @since 2016-05-12
 */
@Setter
@Getter
public class EnglishQuestionBox implements Serializable {

    private static final long serialVersionUID = -5576909790159471202L;

    private Long bookId;
    private String bookName;
    private Long unitId;
    private String unitName;
    private String boxName;
    private String boxId;
    private List<String> questionIds;
}
