package com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecitewithscore;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ReadReciteWithScoreClazzData implements Serializable {
    private static final long serialVersionUID = -873512933956618786L;
    private List<ReadReciteWithScoreClazzBasicData> readData = new LinkedList<>();// 朗读部分

    private List<ReadReciteWithScoreClazzBasicData> reciteData = new LinkedList<>();// 背诵部分
}
