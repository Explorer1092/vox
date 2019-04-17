package com.voxlearning.utopia.service.newhomework.api.mapper.response.outside;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.AllArgsConstructor;
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
public class TeacherReportBookDetailResp extends BaseResp {

    private static final long serialVersionUID = -1259539403950751263L;
    private String readingId;
    private String bookId;
    private String bookName;
    private String coverPic;                        // 封面
    private String author;                          // 作者
    private Double totalWords;                      // 总字数
    private Date endTime;                           // 截止时间
    private Long remainMs;                          //距截止时间毫秒数
    private Integer leadinAudioCount;               // 导读数量
    private Integer missionCount;                   // 练习套数

    private Double groupAvgFinishRate;              //班级平均阅读进度
    private List<StudentDetail> studentDetails = new LinkedList<>();     //学生阅读进度详情

    @Getter
    @Setter
    @AllArgsConstructor
    public static class StudentDetail implements Serializable {
        private static final long serialVersionUID = -7527873143316243673L;

        private Long studentId;
        private String studentName;
        private Integer finishCount;
        private Date lastFinishAt;
    }
}
