package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304;

import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.IProcessor;

import javax.inject.Named;

import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.CID;

/**
 * 二维码识别
 *
 * @author Wenlong Meng
 * @since  Feb 25,2019
 */
@Named("activity.ocrMAv20190304.QRCodeProcessor")
public class QRCodeProcessor  implements IProcessor<ActivityContext> {

    /**
     * 执行
     *
     * @param c context see {@link ActivityContext}
     */
    @Override
    public void process(ActivityContext c) {
        c.set("cid", CID);
    }
}
