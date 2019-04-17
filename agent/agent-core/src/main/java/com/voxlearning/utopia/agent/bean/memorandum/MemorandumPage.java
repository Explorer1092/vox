package com.voxlearning.utopia.agent.bean.memorandum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by yaguang.wang
 * on 2017/5/11.
 */
@Getter
@Setter
@NoArgsConstructor
public class MemorandumPage {
    private String orderFiled;
    private List<Map<String, String>> info;
}
