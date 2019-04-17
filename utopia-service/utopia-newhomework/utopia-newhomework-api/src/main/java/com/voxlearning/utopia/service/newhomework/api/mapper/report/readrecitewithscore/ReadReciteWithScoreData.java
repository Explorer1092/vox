package com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ReadReciteWithScoreData implements Serializable {
    private static final long serialVersionUID = -7544707546148207684L;
    private List<ReadReciteWithScoreBasicData> readData = new LinkedList<>();// 朗读部分

    private List<ReadReciteWithScoreBasicData> reciteData = new LinkedList<>();// 背诵部分
}
