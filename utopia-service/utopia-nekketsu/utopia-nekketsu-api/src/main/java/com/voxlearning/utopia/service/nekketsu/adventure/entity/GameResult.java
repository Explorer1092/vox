package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 小游戏结果
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/11 10:33
 */
@Data
public class GameResult implements Serializable {
    private static final long serialVersionUID = 5640719078080781158L;

    private List<GameSentence> sentence;
    private Long totalTime;

}
