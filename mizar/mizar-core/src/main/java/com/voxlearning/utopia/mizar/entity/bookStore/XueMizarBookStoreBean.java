package com.voxlearning.utopia.mizar.entity.bookStore;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class XueMizarBookStoreBean implements Serializable {

    private static final long serialVersionUID = 6843444880606685658L;

    private Long totalElements;
    private List<BookStoreBean> bookStoreList;

}