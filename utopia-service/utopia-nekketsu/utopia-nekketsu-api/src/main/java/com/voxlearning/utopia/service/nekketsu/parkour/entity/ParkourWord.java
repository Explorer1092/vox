package com.voxlearning.utopia.service.nekketsu.parkour.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Sadi.Wan on 2014/8/18.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkourWord implements Serializable {
    private static final long serialVersionUID = -2810113591714179108L;

    private int stageId;

    /**
     * wordStock中词id
     */
    private String wordId;

    /**
     * 获得概率
     */
    private int achieveRate;

    /**
     * 集齐单词的4个碎片所奖励的学豆数量
     */
    private int collectIntegeral;
}
