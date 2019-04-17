package com.voxlearning.washington.hotfix;

import com.cedarsoftware.util.ExceptionUtilities;
import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.api.bootstrap.PostBootstrapModule;

@Install
final public class TriggerFreemarkerLoggerInitialization implements PostBootstrapModule {

    @Override
    public void postBootstrapModule() {
        // FIXME: init freemarker's logger library (default for log4j will cause warning and may not output log)
        // FIXME: but this doesn't work, slf4j still finds log4j in this project ...
        try {
            freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_SLF4J);
        } catch (ClassNotFoundException ex) {
            // well, we can not find any logger library ?
            ExceptionUtilities.safelyIgnoreException(ex);
        }
    }
}
