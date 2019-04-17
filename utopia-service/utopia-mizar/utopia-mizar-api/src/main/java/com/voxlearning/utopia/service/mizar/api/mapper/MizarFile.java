package com.voxlearning.utopia.service.mizar.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 用于存储附件信息
 * Created by Yuechen.Wang on 2016/12/05.
 */
@Getter
@Setter
public class MizarFile implements Serializable{
    private String name; // 显示名称
    private String url;  // 文件地址
}
