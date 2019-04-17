package com.voxlearning.wechat.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author guangqing
 * @since 2018/8/10
 */
public class  ChipsAutoReplayKeyWord {

    @Getter
    private KeyWordType type;
    @Getter
    @Setter
    private Integer value;//今日学习内容 和第N天 对应的值


    public enum KeyWordType{
        TODAYSTUDY(),//今日学习内容 和第N天
        GRADINGREPORT(),//定级报告
        ELECTRONICTEXTBOOK(),//电子教材
        TEACHER(),//老师
        GRADUATIONCERTIFICATE(),//毕业证书
        REFUND(),//退费,退款
    }

    public ChipsAutoReplayKeyWord(KeyWordType type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public ChipsAutoReplayKeyWord(KeyWordType type) {
        this.type = type;
    }
}

