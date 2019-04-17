package com.voxlearning.utopia.service.wechat.api.data;

/**
 * @author guangqing
 * @since 2019/1/13
 */
public enum CreateQrcodeType {
    LIMIT_INT_QR(1, "QR_LIMIT_SCENE"),
    LIMIT_STR_QR(2, "QR_LIMIT_STR_SCENE"),
    TEMPORARY_INT_QR(3, "QR_SCENE"),
    TEMPORARY_STR_QR(4, "QR_STR_SCENE"),;

    private int type;
    private String qRscene;

    CreateQrcodeType(int type, String qRscene) {
        this.type = type;
        this.qRscene = qRscene;
    }

    public Integer getType() {
        return type;
    }

    public String getQRscene() {
        return qRscene;
    }

    public static CreateQrcodeType of(int type) {
        for (CreateQrcodeType qrcodeType : CreateQrcodeType.values())
            if (qrcodeType.getType() == type) {
                return qrcodeType;
            }
        return null;
    }
}
