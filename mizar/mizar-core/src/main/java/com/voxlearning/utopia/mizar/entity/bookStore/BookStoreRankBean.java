package com.voxlearning.utopia.mizar.entity.bookStore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookStoreRankBean {
    private String bookStoreName;
    private Long bookStoreId;
    private Integer orderNum;
    private String mizarUserId;
    private Long marketId;
}
