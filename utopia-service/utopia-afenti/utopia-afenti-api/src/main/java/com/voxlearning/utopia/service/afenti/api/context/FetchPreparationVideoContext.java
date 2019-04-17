package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.data.BookUnit;
import com.voxlearning.utopia.service.afenti.api.data.BookVideo;
import com.voxlearning.utopia.service.question.api.entity.Video;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.*;

/**
 * @author songtao
 * @since 17/7/20
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchPreparationVideoContext extends AbstractAfentiContext<FetchPreparationVideoContext> {
    private static final long serialVersionUID = 1L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;

    // middle
    private AfentiBook book; // 当前正在使用的教材
    private List<Video> videoList = new LinkedList<>(); // 每个单元获得星星的数量


    // out
    private List<BookVideo> videos = new LinkedList<>();
    private String bookName = "";
}