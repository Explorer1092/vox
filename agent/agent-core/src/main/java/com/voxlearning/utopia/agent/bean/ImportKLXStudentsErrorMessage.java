package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tao.zang on 2017/4/11.
 * 快乐学学生上传 数据校验失败信息
 * 标记 失败信息所在行
 */
@Getter
@Setter
public class ImportKLXStudentsErrorMessage implements Serializable {
    private ImportKLXStudentsErrorType errorType;
    private List<Integer> rows;
    private boolean exit;
    public ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType type){
        errorType = type;
        rows = new ArrayList<>();
        exit = false;
    }
}
