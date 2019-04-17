package com.voxlearning.utopia.service.newhomework.api.mapper.response.outside;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/11/16
 */
@Getter
@Setter
public class TeacherReportBookListResp extends BaseResp {
    private static final long serialVersionUID = -2163890140574313081L;

    private String readingId;
    private String bookId;
    private String bookName;
    private String coverPic;         //封面
    private Date endTime;            //截止时间
    private Long remainMs;           //距截止时间毫秒数
    private Double finishRate;      //阅读进度
}
