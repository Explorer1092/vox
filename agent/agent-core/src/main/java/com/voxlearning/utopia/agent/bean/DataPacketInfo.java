package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * App 数据包的页面类型
 * Created by yaguang.wang on 2016/8/5.
 */
@Getter
@Setter
@NoArgsConstructor
public class DataPacketInfo implements Serializable {

    private static final long serialVersionUID = 1597488979167383940L;

    private String id;                              // 数据包的记录ID
    private String fileName;                        // 文件名
    private String fileUrl;                         // 文件路径
}
