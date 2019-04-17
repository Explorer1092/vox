package com.voxlearning.utopia.mizar.entity.bookStore;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class XueMizarWhiteListBean  {

    private Long totalElements;
    private List<XueMizarWhiteListExtendBean> whiteListExtendBeans;

}