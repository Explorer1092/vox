/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.parkour.entity;

import lombok.*;

import java.io.Serializable;

/**
 * Created by Sadi.Wan on 2014/8/18.
 */
@Setter
@Getter
@EqualsAndHashCode(of = {"wordId", "puzzlePlace"})
@NoArgsConstructor
@AllArgsConstructor
public class WordPuzzle implements Serializable {
    private static final long serialVersionUID = -2032874872456730109L;

    /**
     * 对应Wordstock中词的id
     */
    private String wordId;

    /**
     * 拼图在单词中的位置。每个单词由4块拼图构成，puzzlePlace值为1——4，分别对应位置：左上、右上、左下、右下
     */
    private int puzzlePlace;
}
