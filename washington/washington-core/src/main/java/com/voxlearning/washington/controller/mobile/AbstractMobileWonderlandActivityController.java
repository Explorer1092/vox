package com.voxlearning.washington.controller.mobile;

import com.voxlearning.utopia.service.wonderland.client.BabyEagleChinaCultureLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleChinaCultureServiceClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.BabyEagleServiceClient;

import javax.inject.Inject;

/**
 * @author fugui.chang
 * @since 2017/5/9
 */
public class AbstractMobileWonderlandActivityController extends AbstractMobileController {
    @Inject protected BabyEagleLoaderClient babyEagleLoaderClient;
    @Inject protected BabyEagleServiceClient babyEagleServiceClient;
    @Inject protected BabyEagleChinaCultureLoaderClient babyEagleChinaCultureLoaderClient;
    @Inject protected BabyEagleChinaCultureServiceClient babyEagleChinaCultureServiceClient;
}
