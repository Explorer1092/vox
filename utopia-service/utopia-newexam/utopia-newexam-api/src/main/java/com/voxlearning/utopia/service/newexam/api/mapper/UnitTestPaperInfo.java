package com.voxlearning.utopia.service.newexam.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description: 单元测试卷信息
 * @author: Mr_VanGogh
 * @date: 2019/3/19 下午3:34
 */
@Getter
@Setter
public class UnitTestPaperInfo implements Serializable {
    private static final long serialVersionUID = -2073045067974090273L;

    private Integer paperTypeId;
    private String paperType;
    private List<PaperInfo> papers = new LinkedList<>();

    @Getter
    @Setter
    public static class PaperInfo implements Serializable {
        private static final long serialVersionUID = 1679807548129393992L;
        private String paperId;
        private String paperName;
        private int questionNum;
        private Long paperTime;         //试卷的作答时长
        private Long minutes;           //考试时长
        private String description;
        private String author;                                // 出题人
        private Integer difficulty;                            // 难度
        private String previewUrl;      //预览地址
    }
}
