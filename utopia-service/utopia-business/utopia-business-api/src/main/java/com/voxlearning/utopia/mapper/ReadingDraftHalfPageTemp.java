package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by tanguohong on 14-7-7.
 */
@Data
public class ReadingDraftHalfPageTemp implements Serializable {

    private static final long serialVersionUID = 8643260447375238112L;
    private Integer pageNum = 1;
    private String pageLayout = "ptpt";
    private ReadingDraftHalfPageDetailTemp firstHalfPage = new ReadingDraftHalfPageDetailTemp();
    private ReadingDraftHalfPageDetailTemp afterHalfPage = new ReadingDraftHalfPageDetailTemp();
}
