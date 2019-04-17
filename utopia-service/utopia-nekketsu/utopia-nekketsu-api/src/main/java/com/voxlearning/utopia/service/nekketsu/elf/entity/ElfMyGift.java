package com.voxlearning.utopia.service.nekketsu.elf.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sadi.Wan on 2015/2/26.
 */
@Getter
@Setter
@AllArgsConstructor
public class ElfMyGift implements Serializable {
    private static final long serialVersionUID = 5832900939697167087L;
    private Long userId;

    /**
     * objectId
     */
    private String giftId;
    private ElfGiftType giftType;
    private Integer count;
    private Date getTime;
    private String desc;

    public ElfMyGift(){
    };
}
