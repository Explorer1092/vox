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
 * UTOPIA AFENTI EXCEPTION.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since 2/8/14
 */
public class UtopiaAfentiException extends Exception {

    // 错误码
    //        400: 星星数不够
    private int errorCode;

    private static final long serialVersionUID = -6315062712262312020L;

    public UtopiaAfentiException() {
    }

    public UtopiaAfentiException(String message) {
        super(message);
    }

    public UtopiaAfentiException(Throwable cause) {
        super(cause);
    }

    public UtopiaAfentiException(String message, int code) {
        super(message);
        this.errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
