package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.utopia.service.business.impl.processor.AbstractProcessor;
import com.voxlearning.utopia.service.business.impl.processor.annotation.ExecuteTaskSupport;

import javax.inject.Named;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Named
@ExecuteTaskSupport({
        FSMA_Validate.class,
        FSMA_PersonalWhite.class,
        FSMA_PersonalBlack.class,
        FSMA_ParentCloseVap.class,
        FSMA_VendorApps.class,
        FSMA_FairylandProduct.class,
        FSMA_CandidateAppKeys.class,
        FSMA_PurchaseStatus.class,

})
public class FetchStudentMobileAppProcessor extends AbstractProcessor<FetchStudentAppContext> {
}
