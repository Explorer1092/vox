package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 小游戏结果Sentence
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/11 10:35
 */
@Data
public class GameSentence implements Serializable {
    private static final long serialVersionUID = -4331514071944378833L;

    private Long duration;//每题完成时间
    private Integer score;
    private Boolean correct;
    private Boolean isCorrect;
    private Boolean right;
    private Boolean isRight;
    private Boolean isDo;
    private String content;
    private Long sentenceId;


}
