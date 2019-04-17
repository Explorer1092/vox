package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

/**
 * app内容管理的服务层测试
 * Created by yaguang.wang on 2016/8/3.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAgentAppContentPacketService {
}
