package com.voxlearning.utopia.service.nekketsu.elf.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sadi.Wan on 2015/2/25.
 */
@Getter
@Setter
public class ReadingTimer implements Serializable{
    private static final long serialVersionUID = -8808390129082521048L;

    /**
     * 正在阅读书籍
     */
    private String rdingBid;

    /**
     * 预计完成时间
     */
    private Date fnTime;

    public ReadingTimer(){
        this.rdingBid = "";
        this.fnTime = new Date();
    }

}
