package com.voxlearning.utopia.service.crm.impl.dao.crm;

/**
 * @author fugui.chang
 * @since 2016/11/22
 */

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.crm.api.entities.crm.WechatWfMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WechatWfMessage.class)
public class TestWechatWfMessagePersistence {

    @Inject private WechatWfMessagePersistence wechatWfMessagePersistence;

    @Test
    public void test() throws Exception {
        for (int i = 0; i < 5; ++i) {
            WechatWfMessage mock = new WechatWfMessage();
            mock.setRecordId((long) i);
            mock.setWechatType("");
            mock.setFirstInfo("");
            mock.setKeyword1("");
            mock.setKeyword2("");
            mock.setRemark(i + "");
            mock.setDisabled(false);
            wechatWfMessagePersistence.insert(mock);
        }

        assertEquals("2", wechatWfMessagePersistence.loadByRecordId(2L).getRemark());
    }
}
