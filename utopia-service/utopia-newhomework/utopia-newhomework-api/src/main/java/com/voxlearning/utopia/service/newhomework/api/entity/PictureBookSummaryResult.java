package com.voxlearning.utopia.service.newhomework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 绘本do接口返回的绘本摘要数据
 *
 * @author xuesong.zhang
 * @since 2016-07-15
 */
@Getter
@Setter
public class PictureBookSummaryResult implements Serializable {

    private static final long serialVersionUID = -1147578823557965640L;

    private String practiceId;          // 练习id，阅读绘本写死是67
    private String name;                // 应该是绘本名字
    private String author;              // 作者
    private String frontCoverPic;       // 封面图片
    private String frontCoverPicThumb;  // 封面缩略图

    private String appUrl;              // 每个绘本的请求地址
    private String appMobileUrl;        // 移动端专用
    private Boolean finished;           // 绘本是否已做完
    private String questionUrl;
    private String completedUrl;

}
