package com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecite;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


/**
 * 朗读背诵报告数据
 */
@Getter
@Setter
public class ReadReciteData implements Serializable {
    private static final long serialVersionUID = -5992269883907902899L;

    private List<ReadReciteBasicData> readData = new LinkedList<>();// 朗读部分

    private List<ReadReciteBasicData> reciteData = new LinkedList<>();// 背诵部分

}
