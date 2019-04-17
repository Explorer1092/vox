package com.voxlearning.utopia.service.nekketsu.adventure.constant;

import lombok.Getter;

/**
 * 小游戏类型
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/8 15:48
 */
public enum SystemAppCategory {

    RECOGNITION("单词辨识"),//单词辨识
    LISTENING("听音选词"),  //听音选词
    FIGURE("看图识词"),     //看图识词
    SPELLING("单词拼写"),   //单词拼写
    REPEAT("单词跟读");  //单词跟读

    @Getter
    private String value;

    private SystemAppCategory(String value) {
        this.value = value;
    }

}
