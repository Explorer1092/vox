package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Changyuan on 2015/1/15.
 * 教研员薄弱知识点页面中单行mapper
 */
@Data
public class ResearchStaffWeakPointUnitMapper implements Serializable {

    private static final long serialVersionUID = 4236120817579808127L;

    private String name;

    private String word;
    private String grammar;
    private String topic;
}
