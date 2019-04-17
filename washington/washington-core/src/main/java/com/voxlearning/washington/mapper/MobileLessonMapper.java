package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.mapper.MobilePracticeMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by libin on 14-3-26.
 */
public class MobileLessonMapper implements Serializable {
    private static final long serialVersionUID = -53881069607988904L;

    @Getter @Setter private Long id;
    @Getter @Setter private String cname;
    @Getter @Setter private String ename;
    @Getter @Setter private List<MobilePracticeMapper> taskList;

    @Getter @Setter private List<MobileLessonMapper> lessonSubList;

    public MobileLessonMapper(Long id, String cname, String ename) {
        this.id = id;
        this.cname = cname;
        this.ename = ename;
    }

    public MobileLessonMapper() {
    }
}
