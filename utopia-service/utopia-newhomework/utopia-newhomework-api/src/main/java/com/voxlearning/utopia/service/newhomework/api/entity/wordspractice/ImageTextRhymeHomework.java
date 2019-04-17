package com.voxlearning.utopia.service.newhomework.api.entity.wordspractice;

import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 图文入韵
 * @author: Mr_VanGogh
 * @date: 2018/11/27 下午2:45
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageTextRhymeHomework implements Serializable {
    private static final long serialVersionUID = -3980951414651424350L;

    private String chapterId;
    private String title;
    private String imageUrl;
    private List<NewHomeworkQuestion> chapterQuestions;
}
