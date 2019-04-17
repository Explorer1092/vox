package com.voxlearning.utopia.service.mizar.api.mapper;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by Summer Yang on 2016/9/13.
 * 获取列表页参数context
 */
public class LoadMizarShopContext implements Serializable{

    private static final long serialVersionUID = -1836632496744107897L;

    public String firstCategory;
    public String secondCategory;
    public Integer regionCode;
    public String tradeArea;
    public String orderBy;
    public Integer pageSize;
    public Integer pageNum;
    public String longitude;
    public String latitude;
    public String shopName;
    public Long schoolId;
    public Integer schoolRegionCode;
    public Collection<Integer> clazzLevels;
    public Long studentId;
}
