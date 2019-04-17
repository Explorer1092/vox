/**
 * Author:   xianlong.zhang
 * Date:     2018/9/21 21:35
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.bean.xtest;

import com.voxlearning.utopia.agent.constants.AgentConstants;

import java.util.Map;

public abstract class BaseXTestData {
    protected abstract Map<Integer, MapXTestData> getXTestMap();

    public MapXTestData fetchXTestData(){
        MapXTestData mapXTestData = getXTestMap().get(AgentConstants.TEST_TYPE_X_TEST);
        return mapXTestData == null ? new MapXTestData() : mapXTestData;
    }

    public MapXTestData fetchExameData(){
        MapXTestData mapXTestData =  getXTestMap().get(AgentConstants.TEST_TYPE_EXAME);
        return mapXTestData == null ? new MapXTestData() : mapXTestData;
    }

    public MapXTestData fetchActivityData(){
        MapXTestData mapXTestData =  getXTestMap().get(AgentConstants.TEST_TYPE_ACTIVITY);
        return mapXTestData == null ? new MapXTestData() : mapXTestData;
    }

}
