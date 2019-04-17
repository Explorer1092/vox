package com.voxlearning.utopia.service.afenti.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.afenti.api.AfentiElfService;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionSource;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionStateType;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import org.slf4j.Logger;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.*;
import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

/**
 * @author Ruib
 * @since 2016/6/28
 */
public class AfentiElfServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(AfentiElfServiceClient.class);

    @ImportService(interfaceClass = AfentiElfService.class)
    private AfentiElfService afentiElfService;

    @Deprecated
    public MapMessage fetchElf(Long studentId, Subject subject) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (null == subject || !AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return afentiElfService.fetchElf(studentId, subject);
        } catch (Exception ex) {
            logger.error("Failed fetching elf for user {}, subject {}", studentId, subject, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchElf(Long studentId, Subject subject, AfentiWrongQuestionStateType stateType) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (null == subject || !AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return afentiElfService.fetchElf(studentId, subject, stateType);
        } catch (Exception ex) {
            logger.error("Failed fetching elf for user {}, subject {}", studentId, subject, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchElfPage(Long studentId,
                                   Subject subject,
                                   AfentiWrongQuestionStateType stateType,
                                   AfentiWrongQuestionSource source,
                                   int page,
                                   int pageSize) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (null == subject || !AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        try {
            return afentiElfService.fetchPageElf(studentId, subject, stateType, source, page, pageSize);
        } catch (Exception ex) {
            logger.error("Failed fetching elf for user {}, subject {}", studentId, subject, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    @Deprecated
    public MapMessage fetchIndexElf(Long studentId, Subject subject) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (null == subject || !AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return afentiElfService.fetchElfIndex(studentId, subject);
        } catch (Exception ex) {
            logger.error("Failed fetching elf Index for user {}, subject {}", studentId, subject, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage fetchIndexElfV2(Long studentId, Subject subject) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (null == subject || !AVAILABLE_SUBJECT.contains(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return afentiElfService.fetchElfIndexV2(studentId, subject);
        } catch (Exception ex) {
            logger.error("Failed fetching elf Index for user {}, subject {}", studentId, subject, ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }

    public MapMessage processElfResult(ElfResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("SaveAfentiElfResult")
                    .keys(ctx.getStudent().getId())
                    .callback(() -> afentiElfService.processElfResult(ctx))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(DUPLICATED_OPERATION.getInfo()).setErrorCode(DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed save afenti elf result for context {}", JsonStringSerializer.getInstance().serialize(ctx), ex);
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
        }
    }
}
