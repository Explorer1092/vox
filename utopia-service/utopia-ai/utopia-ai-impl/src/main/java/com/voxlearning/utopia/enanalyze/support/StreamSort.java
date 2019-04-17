package com.voxlearning.utopia.enanalyze.support;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xiaolei.li
 * @version 2018/7/25
 */
public class StreamSort {

    @Data
    @AllArgsConstructor
    static class Item implements Serializable {
        private String a;
        private float b;
    }

    public static void main(String[] args) {
        List<Item> list = Stream.of(
                new Item("d", 1.0f),
                new Item("a", 2.0f),
                new Item("c", 2.0f))
                .sorted(Comparator.comparing(Item::getB).reversed().thenComparing(Item::getA))
                .collect(Collectors.toList());
        System.out.println(list);
    }
}

