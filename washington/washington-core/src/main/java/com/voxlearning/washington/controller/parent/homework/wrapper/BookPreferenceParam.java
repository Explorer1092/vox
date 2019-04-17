package com.voxlearning.washington.controller.parent.homework.wrapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class BookPreferenceParam implements Serializable {
    private String subject;
    private List<String> levels;
    private String bookId;
}
