package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 大冒险简化教材信息
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/1 4:16
 */
@Data
public class AdventureBook implements Serializable {
    private static final long serialVersionUID = -1636931749880428187L;

    private Long id;
    private String name;   //中文名称
    private Integer termType;//学期，上学期1、下学期2

    public static AdventureBook newInstence(Long id, String name, Integer termType) {
        AdventureBook book = new AdventureBook();
        book.setId(id);
        book.setName(name);
        book.setTermType(termType);
        return book;
    }

}
