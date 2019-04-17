package com.voxlearning.utopia.mizar.entity.bookStore;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class XueMizarBookStoreOrderBean implements Serializable {


    private Long totalElements;
    private List<XueMizarBookStoreOrderInfoBean> orderInfoList;

}