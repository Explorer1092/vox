package com.voxlearning.utopia.core.helper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/3/11
 * \* Time: 6:06 PM
 * \* Description: 戴特语文绘本配置
 * \
 */
@Getter
@Setter
public class DaiTeLevelReadingsConfig implements Serializable{
    private static final long serialVersionUID = 4318423509017692101L;
    String clazzLevelName;
    String bookId;
    String unitName;
    String unitId;
    String sectionName;
    String sectionId;
    List<LevelReadingBook> levelReadingBooks;

    @Setter
    @Getter
    public static class LevelReadingBook implements Serializable{
        private static final long serialVersionUID = -4990364434350031242L;
        String pictureBookName;
        String pictureBookId;
    }
}
