/**
 * Author:   xianlong.zhang
 * Date:     2018/9/26 18:24
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.bean.xtest;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MapXTestData {
    private Map<String,XTestData> xTestDataMap  = new HashMap<>();
}
