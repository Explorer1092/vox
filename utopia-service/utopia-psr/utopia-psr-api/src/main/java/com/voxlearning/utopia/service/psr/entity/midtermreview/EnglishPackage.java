package com.voxlearning.utopia.service.psr.entity.midtermreview;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/10/9.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class EnglishPackage implements Serializable {
    private static final long serialVersionUID = -4556295386384308330L;

    private String pakName;
    private String bookId;
    private String catalogId;
    private List<String> knowledgePoints;
    private List<EnglishQuestion> questions;
    private String pakId;
    private Integer pakType; // 0 表示主知识点错题， 1 表示次知识点&话题知识点错题；2 表示主知识点类题，3 表示次知识点&话题知识点类题
}
