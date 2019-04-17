package com.voxlearning.utopia.service.vendor.impl.support;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author xinxin
 * @since 28/7/2016
 */
@RunWith(AlpsTestRunner.class)
public class TestJpushTimingMessageSendTimeCalculator {

    @Test
    public void testSendTimeCeil(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt = LocalDateTime.from(formatter.parse("2016-07-28 10:10:00"));
        Instant origin = dt.toInstant(ZoneOffset.ofHours(8));
        long result = JpushTimingMessageSendTimeCalculator.sendTimeCeil(origin.toEpochMilli());

        Assert.assertEquals(origin.getEpochSecond(),result);

        dt = LocalDateTime.from(formatter.parse("2016-07-28 10:08:00"));
        Instant origin1 = dt.toInstant(ZoneOffset.ofHours(8));
        long result1 = JpushTimingMessageSendTimeCalculator.sendTimeCeil(origin1.toEpochMilli());

        Assert.assertEquals(origin.getEpochSecond(),result1);
    }

    @Test
    public void testSendTimeFloor(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt = LocalDateTime.from(formatter.parse("2016-07-28 10:10:00"));
        Instant origin = dt.toInstant(ZoneOffset.ofHours(8));
        long result = JpushTimingMessageSendTimeCalculator.sendTimeFloor(origin.toEpochMilli());

        Assert.assertEquals(origin.getEpochSecond(),result);

        dt = LocalDateTime.from(formatter.parse("2016-07-28 10:12:00"));
        Instant origin1 = dt.toInstant(ZoneOffset.ofHours(8));
        long result1 = JpushTimingMessageSendTimeCalculator.sendTimeFloor(origin1.toEpochMilli());

        Assert.assertEquals(origin.getEpochSecond(),result1);
    }

}
