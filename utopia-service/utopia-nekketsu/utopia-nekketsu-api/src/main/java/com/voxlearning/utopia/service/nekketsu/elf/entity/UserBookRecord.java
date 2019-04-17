package com.voxlearning.utopia.service.nekketsu.elf.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sadi.Wan on 2015/2/10.
 */
@Getter
@Setter
public class UserBookRecord implements Serializable {
    private static final long serialVersionUID = 8453900034222913382L;

    private String bookId;

    /**
     * true：已获得此关植物
     */
    private Boolean sunGained;

    /**
     * 获取植物时间
     */
    private Date sgTime;

    public UserBookRecord(){

    }

    public static UserBookRecord getDefault(String bookId){
        UserBookRecord userBookRecord = new UserBookRecord();
        userBookRecord.setBookId(bookId);
        userBookRecord.setSunGained(false);
        return userBookRecord;
    }

    public boolean isSunGained() {
        return sunGained != null && sunGained;
    }
}
