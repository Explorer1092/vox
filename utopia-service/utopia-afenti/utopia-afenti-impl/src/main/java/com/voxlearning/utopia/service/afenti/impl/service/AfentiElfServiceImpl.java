/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.impl.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.afenti.api.AfentiElfService;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionSource;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionStateType;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfContext;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfIndexContext;
import com.voxlearning.utopia.service.afenti.api.context.FetchElfPageContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.elf.FetchElfIndexProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.elf.FetchElfPageProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.elf.FetchElfProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.result.IncorrectElfResultProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.result.SimilarElfResultProcessor;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2CORRECT;
import static com.voxlearning.utopia.api.constant.AfentiState.INCORRECT2MASTER;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.NEED_LOGIN;

/**
 * @author Ruib
 * @since 2016/6/28
 */
@Named
@ExposeService(interfaceClass = AfentiElfService.class)
public class AfentiElfServiceImpl extends UtopiaAfentiSpringBean implements AfentiElfService {

    @Inject private FetchElfIndexProcessor fetchElfIndexProcessor;
    @Inject private FetchElfProcessor fetchElfProcessor;
    @Inject private FetchElfPageProcessor fetchElfPageProcessor;
    @Inject private IncorrectElfResultProcessor incorrectElfResultProcessor;
    @Inject private SimilarElfResultProcessor similarElfResultProcessor;

    @Override
    public MapMessage fetchElf(Long studentId, Subject subject) {
        return fetchElf(studentId, subject, AfentiWrongQuestionStateType.incorrect);
    }

    @Override
    public MapMessage fetchElf(Long studentId, Subject subject, AfentiWrongQuestionStateType stateType) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AfentiUtils.isSubjectAvailable(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            FetchElfContext context = fetchElfProcessor.process(new FetchElfContext(studentId, subject, stateType));
            if (context.isSuccessful()) {
                return MapMessage.successMessage().add("incorrect", context.getIncorrect())
                        .add("similar", context.getSimilar())
                        .add("rescued", context.getRescued());
            }
        } catch (Exception ex) {
            logger.error("Failed fetch elf for user {}", studentId, ex);
        }
        return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
    }

    @Override
    public MapMessage fetchPageElf(Long studentId,
                                   Subject subject,
                                   AfentiWrongQuestionStateType stateType,
                                   AfentiWrongQuestionSource source,
                                   Integer page,
                                   Integer pageSize) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AfentiUtils.isSubjectAvailable(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {

            FetchElfPageContext context = fetchElfPageProcessor.process(new FetchElfPageContext(studentId, subject, stateType,source, page, pageSize));
            if (context.isSuccessful()) {
                return MapMessage.successMessage()
                                 .add("result", context.getResult())
                                 .add("pageNum", context.getPageNum())
                                 .add("totalNum", context.getTotalNum())
                                 .add("afentiNum", context.getAfentiNum())
                                 .add("homeworkNum", context.getHomeworkNum());

            }
        } catch (Exception ex) {
            logger.error("Failed fetch elf page for user:{}, subject:{}, stateType:{}, source:{}, page:{}, pageSize:{}",
                    studentId, subject, stateType, source, page, pageSize, ex);
        }
        return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
    }

    @Override
    public MapMessage fetchElfIndex(Long studentId, Subject subject) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AfentiUtils.isSubjectAvailable(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            FetchElfIndexContext context = fetchElfIndexProcessor.process(new FetchElfIndexContext(studentId, subject, true));
            if (context.isSuccessful()) {
                return MapMessage.successMessage()
                        .add("incorrect", context.getIncorrect())
                        .add("similar", context.getSimilar())
                        .add("rescued", context.getRescued());
            }
        } catch (Exception ex) {
            logger.error("Failed fetch  elf index for user {}", studentId, ex);
        }
        return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
    }

    @Override
    public MapMessage fetchElfIndexV2(Long studentId, Subject subject) {
        if (null == studentId)
            return MapMessage.errorMessage(NEED_LOGIN.getInfo()).setErrorCode(NEED_LOGIN.getCode());
        if (!AfentiUtils.isSubjectAvailable(subject))
            return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());

        try {
            FetchElfIndexContext context = fetchElfIndexProcessor.process(new FetchElfIndexContext(studentId, subject, false));
            if (context.isSuccessful()) {
                return MapMessage.successMessage()
                        .add("incorrect", context.getIncorrect())
                        .add("similar", context.getSimilar())
                        .add("rescued", context.getRescued());
            }
        } catch (Exception ex) {
            logger.error("Failed fetch  elf index for user {}", studentId, ex);
        }
        return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
    }

    @Override
    public MapMessage processElfResult(ElfResultContext ctx) {
        if (null == ctx) return MapMessage.errorMessage();

        try {
            ElfResultContext context;
            if (ctx.getAfentiState() == INCORRECT2CORRECT) {
                context = incorrectElfResultProcessor.process(ctx);
            } else if (ctx.getAfentiState() == INCORRECT2MASTER) {
                context = similarElfResultProcessor.process(ctx);
            } else {
                MapMessage mesg = MapMessage.successMessage();
                mesg.putAll(ctx.getResult());
                return mesg;
            }
            if (context.isSuccessful()) {
                MapMessage mesg = MapMessage.successMessage();
                mesg.putAll(context.getResult());
                return mesg;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return MapMessage.errorMessage(DEFAULT.getInfo()).setErrorCode(DEFAULT.getCode());
    }
}
