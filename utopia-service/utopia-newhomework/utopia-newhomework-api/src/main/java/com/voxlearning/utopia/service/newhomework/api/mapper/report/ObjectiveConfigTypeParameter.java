package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ObjectiveConfigTypeParameter implements Serializable {
    private static final long serialVersionUID = -250080679025971337L;

    //BasicApp 参数
    private Integer categoryId;                         // 练习类型id（VOX_PRACTICE_TYPE中的）
    //BasicApp 参数
    private String lessonId;                            // 我是一个很奇怪的属性。

    //Reading 参数
    private String pictureBookId;                       // 绘本id（阅读绘本）

    //KeyPoint 参数
    private String videoId;                             // 视频id（难重点专项）

    //NewReadRecite 参数
    private String questionBoxId;                       // 题包id（语文读背）

    //Dubbing 参数
    private String dubbingId;                           // 配音id（趣味配音）

    //主题数据参数
    private String stoneId;                             //题包id (口语交际)
}
