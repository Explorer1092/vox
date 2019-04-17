package com.voxlearning.utopia.service.afenti.api.data;

import java.io.Serializable;

/**
 * @author 宋涛
 * @since 17-7-20
 */
public class BookVideo implements Serializable {
    private static final long serialVersionUID = 611852045156191490L;

    public String coverUrl;                   //视频封面图片
    public String videoUrl;                   //视频播放地址
    public String videoName;                  //视频名称
}
