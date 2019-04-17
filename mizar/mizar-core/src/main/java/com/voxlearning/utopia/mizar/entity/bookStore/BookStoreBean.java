package com.voxlearning.utopia.mizar.entity.bookStore;


import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookStoreBean implements Serializable {


    private static final long serialVersionUID = 5448433768944775224L;
    private Long id;
    private String mizarUserId;
    private String bookStoreName;
    private String contactName;
    private String storeAddress;
    private Map<String,Object> storeAddressMap;
    private Integer storeSizeType;
    private String surroundingSchool;
    private String storeQrCode;
    private String createMizarUserId;
    private String updateMizarUserId;
    private Date createDateTime;
    private Date updateDateTime;
    private String mobile;
    private String createMobile;
    private String identityCardNumber;
    private String bankCardNumber;
    private String depositBank;
    private String identityPic;
    private String aliStoreQrCode;
    private Integer orderNum;
    private String createUserName;
    private String sourceMizarUserId;
    private String sourceMizarUserName;
    private String recentDaysOrderNum;
}
