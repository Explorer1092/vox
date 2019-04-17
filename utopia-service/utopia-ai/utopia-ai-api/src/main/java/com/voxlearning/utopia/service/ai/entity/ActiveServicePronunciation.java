package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author guangqing
 * @since 2019/1/4
 * 主动服务发音点评
 */
@Getter
@Setter
public class ActiveServicePronunciation implements Serializable{

    private static final long serialVersionUID = 4033318826547412139L;
    //点评关键字
    private String keyword;
    //发音点评
    private String comment;
    //发音点评音频
    private String audio;

    @Override
    public String toString() {
        return "ActiveServicePronunciation{" +
                "keyword='" + keyword + '\'' +
                ", comment='" + comment + '\'' +
                ", audio='" + audio + '\'' +
                '}';
    }
}
