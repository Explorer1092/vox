package com.voxlearning.utopia.service.ai.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author guangqing
 * @since 2019/1/4
 * 主动服务语法点讲解
 */
@Getter
@Setter
public class ActiveServiceGrammar implements Serializable {

    private static final long serialVersionUID = 4642086493202012424L;
    //level
    private String level;
    //语法点讲解
    private String comment;
    //语法点讲解音频
    private String audio;

    @Override
    public String toString() {
        return "ActiveServiceGrammar{" +
                "level=" + level +
                ", comment='" + comment + '\'' +
                ", audio='" + audio + '\'' +
                '}';
    }
}
