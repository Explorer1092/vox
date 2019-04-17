package com.voxlearning.utopia.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeacherTaskVar<T> {

    private String name;
    private T value;

}
