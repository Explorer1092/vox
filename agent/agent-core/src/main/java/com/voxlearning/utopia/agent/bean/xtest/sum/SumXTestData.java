/**
 * Author:   xianlong.zhang
 * Date:     2018/9/21 21:41
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.bean.xtest.sum;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.utopia.agent.bean.xtest.BaseXTestData;
import com.voxlearning.utopia.agent.bean.xtest.MapXTestData;
import com.voxlearning.utopia.agent.bean.xtest.XTestData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@UtopiaCacheRevision("20180928")
public class SumXTestData extends BaseXTestData implements Serializable {
    private Integer day;                                                           // 业绩日期
    private Long id;
    private Integer dataType;                         // 1:部门   2：user    3: 部门未分配
//    private Integer schoolLevel;                    // 1: 小学，   2： 初中，  4：高中，   5：学前 ，  24：初高中
    private String name;                                                           // 名称
    private Map<Integer, MapXTestData> xTestMap = new HashMap<>();
}
