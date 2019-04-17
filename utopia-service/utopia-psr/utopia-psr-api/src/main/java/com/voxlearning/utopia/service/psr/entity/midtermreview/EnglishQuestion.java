package com.voxlearning.utopia.service.psr.entity.midtermreview;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/9.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class EnglishQuestion implements Serializable {
    private static final long serialVersionUID = 6276616057029204980L;

    private String docId;
    private Double accurate;
}
