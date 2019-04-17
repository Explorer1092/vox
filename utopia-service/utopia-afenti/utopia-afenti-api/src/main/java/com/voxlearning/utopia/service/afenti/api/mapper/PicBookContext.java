package com.voxlearning.utopia.service.afenti.api.mapper;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@UtopiaCacheRevision("20181106")
public class PicBookContext {

    private Long userId;
    private String type;
    private List<PictureBookPlus> books;
    private List<PictureBookNewClazzLevel> clazzLevels;
    private String orderBy;
    private Integer subjectId;
    private String bookType;

}
