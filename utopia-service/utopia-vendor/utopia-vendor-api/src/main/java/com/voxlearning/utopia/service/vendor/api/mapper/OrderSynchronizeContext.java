package com.voxlearning.utopia.service.vendor.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xinxin
 * @since 8/8/17.
 */
@Getter
@Setter
public class OrderSynchronizeContext implements Serializable {
    private static final long serialVersionUID = -5282540417303826729L;

    private String packageId;
    private List<OrderSynchronizeBookInfo> books;

    public void addBook(String source, String bookId, Integer period, Integer price) {
        if (null == books) {
            books = new ArrayList<>();
        }

        OrderSynchronizeBookInfo info = new OrderSynchronizeBookInfo();
        info.setSource(source);
        info.setBookId(bookId);
        info.setPeriod(period);
        info.setPrice(price);
        books.add(info);
    }
}
