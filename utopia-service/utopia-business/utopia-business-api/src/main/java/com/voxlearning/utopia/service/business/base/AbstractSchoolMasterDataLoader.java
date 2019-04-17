package com.voxlearning.utopia.service.business.base;

import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.business.api.SchoolMasterDataLoader;
import org.slf4j.Logger;

/**
 *
 * @author fugui.chang
 * @since 2016-9-27
 */
abstract public class AbstractSchoolMasterDataLoader implements SchoolMasterDataLoader {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
}
