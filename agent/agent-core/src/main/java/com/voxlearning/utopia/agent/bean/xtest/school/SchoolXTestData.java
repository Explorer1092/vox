/**
 * Author:   xianlong.zhang
 * Date:     2018/9/21 21:41
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.bean.xtest.school;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.utopia.agent.bean.xtest.BaseXTestData;
import com.voxlearning.utopia.agent.bean.xtest.MapXTestData;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@UtopiaCacheRevision("20180929")
public class SchoolXTestData extends BaseXTestData implements Serializable {
    private Integer day;                                                           // 业绩日期
    private Long id;
    private String name;                                                           // 名称
    private Map<Integer, MapXTestData> xTestMap = new HashMap<>();
}
