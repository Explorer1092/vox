package com.voxlearning.utopia.service.nekketsu.parkour.entity;

import lombok.Getter;

/**
 * Created by Sadi.Wan on 2014/8/29.
 */
@Getter
public enum ParkourItem {
    PI_00001("活力加满");
    private ParkourItem(String name){
        this.name = name;
    }
    private String name;
}
