package com.voxlearning.utopia.service.newhomework.api.mapper.response.outside;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/11/16
 */
@Getter
@Setter
public class StudentBookshelfResp extends BaseResp {
    private static final long serialVersionUID = -458055609358015665L;

    /** 学生基本信息*/
    private Long studentId;
    private String studentName;
    private String studentImage;

    /** 学生成就*/
    private double totalReadingCount;     // 阅读成就字数
    private int goldenWordsCount;      // 收藏好词好句数量
    private List<ShelfBookInfo> shelfBookInfos = new LinkedList<>();

    @Getter
    @Setter
    public static class ShelfBookInfo implements Serializable {
        private static final long serialVersionUID = 4003136683612152522L;

        private String outsideReadingId;
        private String bookId;
        private String bookName;
        private String coverPic;
        private String dynamic;
        private Date endTime;
        private int totalMission;
        private int finishMission;
    }
}
