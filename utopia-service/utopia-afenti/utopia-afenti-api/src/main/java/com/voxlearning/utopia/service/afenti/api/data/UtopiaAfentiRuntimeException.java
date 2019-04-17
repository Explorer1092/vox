/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.afenti.api.data;

/**
 * UTOPIA AFENTI RUNTIME EXCEPTION.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since 2/8/14
 */
public class UtopiaAfentiRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -324752723651680097L;

    public UtopiaAfentiRuntimeException() {
    }

    public UtopiaAfentiRuntimeException(Throwable cause) {
        super(cause);
    }
}
