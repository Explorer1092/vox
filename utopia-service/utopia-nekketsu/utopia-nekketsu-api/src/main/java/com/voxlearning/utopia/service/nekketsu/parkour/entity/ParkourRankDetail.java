package com.voxlearning.utopia.service.nekketsu.parkour.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sadi.Wan on 2014/8/20.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParkourRankDetail implements Serializable,Comparable<ParkourRankDetail>{
    private static final long serialVersionUID = -4538712579822692997L;
    private long roleId;
    private int personalBest;
    private int star;
    private Date accomplishTime;
    @Override
    public boolean equals(Object o ){
        try{
            return roleId == ((ParkourRankDetail)o).roleId;
        }catch(Exception e){
            return false;
        }
    }

    @Override
    public int hashCode(){
        return Long.valueOf(roleId).hashCode();
    }

    @Override
    public int compareTo(ParkourRankDetail o) {
        int timeCompare = Integer.valueOf(personalBest).compareTo(o.personalBest);
        int dateCompare = accomplishTime.compareTo(o.accomplishTime);
        int roleIdCompare = Long.valueOf(roleId).compareTo(o.roleId);
        return timeCompare != 0 ? timeCompare : (dateCompare != 0 ? dateCompare : roleIdCompare);
    }
}
