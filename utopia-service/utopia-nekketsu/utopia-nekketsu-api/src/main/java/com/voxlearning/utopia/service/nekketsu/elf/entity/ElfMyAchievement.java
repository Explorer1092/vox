package com.voxlearning.utopia.service.nekketsu.elf.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Sadi.Wan on 2015/2/26.
 */
@Getter
@Setter
public class ElfMyAchievement implements Serializable{

    private static final long serialVersionUID = 4523383458303043884L;
    private ElfAchievementType achievementType;
    private int stage;

    /**
     * 当前阶段的成就是否可领取
     */
    private boolean exchangable;
}
