package com.voxlearning.utopia.service.ai.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ChipsEnglishTeacher {
    Hailey("frenchfriesteacher01", "http://cdn.17zuoye.com/fs-resource/5b7cca9b498ca4a109edbd47.jpeg", ""),
    David("frenchfriesteacher01", "http://cdn.17zuoye.com/fs-resource/5b7cca9b498ca4a109edbd47.jpeg", "http://cdn.17zuoye.com/fs-resource/5c9b3a65b43327cd2d0cf7e1.jpeg"),
    Winston("shutiao004", "http://cdn.17zuoye.com/fs-resource/5b7ccc0be8ddca79084ce4f8.jpg", ""),
    Wilson("shutiao010", "http://cdn.17zuoye.com/fs-resource/5b7fa72d498ca458add52134.jpeg", "http://cdn.17zuoye.com/fs-resource/5c9c96749831c70cfcee5fd5.jpeg"),
    Alex("shutiao020", "http://cdn.17zuoye.com/fs-resource/5c1082989831c74f317bcadd", "http://cdn.17zuoye.com/fs-resource/5c9b3a66b43327cd2d0cf7f2.jpeg"),
    James("shutiao030", "http://cdn.17zuoye.com/fs-resource/5c10829b9831c74f317bcae2", ""),
    Lucas("shutiao040", "http://cdn.17zuoye.com/fs-resource/5c10829d9831c74f317bcae7", "http://cdn.17zuoye.com/fs-resource/5c9b3a669831c70cfcee0204.jpeg"),
    Owen("shutiao050", "http://cdn.17zuoye.com/fs-resource/5c10828eb43327561cd34be1", "http://cdn.17zuoye.com/fs-resource/5c9c96749831c70cfcee5fca.jpeg"),
    Tom("shutiao060", "http://cdn.17zuoye.com/fs-resource/5c3c9853b433279a62f807db", "http://cdn.17zuoye.com/fs-resource/5c9b3a66b43327cd2d0cf802.jpeg"),
    Mike("shutiao070", "http://cdn.17zuoye.com/fs-resource/5c3c9852b433279a62f807b1", "http://cdn.17zuoye.com/fs-resource/5c9b3a66b43327cd2d0cf7ec.jpeg"),
    Mia("shutiao080", "http://cdn.17zuoye.com/fs-resource/5c3c9852b433279a62f807b7", "http://cdn.17zuoye.com/fs-resource/5c9c9662b43327cd2d0d5732.jpeg"),
    Emma("shutiao090", "http://cdn.17zuoye.com/fs-resource/5c3c9853b433279a62f807d5", ""),
    Zoe("shutiao10001", "http://cdn.17zuoye.com/fs-resource/5c3c9853b433279a62f807cf", ""),
    Camila("shutiao11001", "http://cdn.17zuoye.com/fs-resource/5c3c9853b433279a62f807c3", ""),
    Jason("shutiao12001", "http://cdn.17zuoye.com/fs-resource/5c3c9853b433279a62f807c9", "http://cdn.17zuoye.com/fs-resource/5c9c963cb43327cd2d0d56eb.jpeg"),
    Larry("shutiao130", "http://cdn.17zuoye.com/fs-resource/5c3c98529831c7916265520d", "http://cdn.17zuoye.com/fs-resource/5c9c964db43327cd2d0d570a.jpeg"),
    Sofia("shutiao140", "http://cdn.17zuoye.com/fs-resource/5c3c9851b433279a62f807a5", "http://cdn.17zuoye.com/fs-resource/5caaea729831c7ad0077859a.jpeg"),
    Anna("shutiao150", "http://cdn.17zuoye.com/fs-resource/5c3c9852b433279a62f807ab", ""),
    Lucy("shutiao160", "http://cdn.17zuoye.com/fs-resource/5c3c98529831c79162655207", "http://cdn.17zuoye.com/fs-resource/5c9c9662b43327cd2d0d572c.jpeg"),
    Bella("shutiao1701", "http://cdn.17zuoye.com/fs-resource/5c3c9852b433279a62f807bd", "http://cdn.17zuoye.com/fs-resource/5c9c96289831c70cfcee5f8a.jpeg"),
    Irene("shutiao180","http://cdn.17zuoye.com/fs-resource/5c4959f59831c751aa0e3d9e.jpeg", ""),
    Molly("shutiao190","http://cdn.17zuoye.com/fs-resource/5c4938a19831c751aa0e3307.png", ""),
    Steven("shutiao200","http://cdn.17zuoye.com/fs-resource/5c49389eb43327e1ce987b51.png", ""),
    John("shutiao2101","http://cdn.17zuoye.com/fs-resource/5c49389e9831c751aa0e32f5.jpeg", "http://cdn.17zuoye.com/fs-resource/5c9b3a669831c70cfcee01f9.jpeg"),
    Jessica("shutiao220","http://cdn.17zuoye.com/fs-resource/5c49389eb43327e1ce987b4b.png", "http://cdn.17zuoye.com/fs-resource/5c9c963cb43327cd2d0d56f6.jpeg"),
    Cici("shutiao230","http://cdn.17zuoye.com/fs-resource/5c49389e9831c751aa0e32ef.png", ""),
    Robert("shutiao240","http://cdn.17zuoye.com/fs-resource/5c49389f9831c751aa0e32fb.png", "http://cdn.17zuoye.com/fs-resource/5caaea729831c7ad0077858f.jpeg"),
    Albert("shutiao260","http://cdn.17zuoye.com/fs-resource/5c49389c9831c751aa0e32e9.png", "");
    @Getter
    private final String wxCode;
    @Getter
    private final String qrImage;

    @Getter
    private final String companyQrImage;//企业微信二维码

    public static ChipsEnglishTeacher safe(String name) {
        try {
            return ChipsEnglishTeacher.valueOf(name);
        } catch (Exception e) {
            return Winston;
        }
    }
}


