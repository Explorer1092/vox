/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.GameTime;
import com.voxlearning.utopia.service.content.api.entity.Lesson;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * English flash game combo data structure.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since Nov 30, 2014
 */
@Getter
@Setter
public class EnglishFlashGameComboData implements Serializable {
    private static final long serialVersionUID = -1319586649281678678L;

    private Lesson lesson;
    private List<Sentence> sentences;
    private GameTime gameTime;
    private Book book;

    public Integer fetchGameTime() {
        return gameTime == null ? null : gameTime.getGameTime();
    }

    public Integer fetchClazzLevel() {
        return book == null ? null : book.getClassLevel();
    }
}
