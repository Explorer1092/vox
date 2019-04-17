<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
title="一起-校长-测评"
pageJs=["dataPlatform", "jquery"]
pageJsFile={"dataPlatform": "public/script/adminteacher/dataplatform/dataplatform"}
pageCssFile={"index": ["/public/skin/adminteacher/css/dataplatform"]}>

<#include "./getversion.ftl">
<link rel="stylesheet" href="${getVersionUrl('public/plugin/swiper-3.4.2/swiper.min.css')}">
<style>

    input::-webkit-outer-spin-button,
    input::-webkit-inner-spin-button {
        -webkit-appearance: none;
    }
    input[type="number"] {
        -moz-appearance: textfield;
    }
</style>
<!-- @click="hide_tool_bar" -->
<div class="index-wrap" id="dataPlatform"  v-cloak>
    <div class="header">
        <div class="logo"></div>
        <div class="switch-box">
            <div class="switch-tab" @click="screen_full">{{is_full_screen ? '退出全屏' : '全屏显示'}}</div>
            <div class="switch-tab" @click="switch_concise_version">切换简版</div>
        </div>
        <div class="switch-login " >
            <p class="name">于校长</p>
            <ul>
                <#-- @click="jump_user_center" -->
                <li ><a href="/${idType!'schoolmaster'}/admincenter.vpage">个人中心</a></li>
                <#-- @click="exit_center" -->
                <li ><a href="${(ProductConfig.getMainSiteUcenterLogoutUrl())!''}">退出</a></li>
            </ul>
        </div>
        <div class="title-box">
            <h2 class="school-name">{{school_name}}</h2>
            <h2>AI智能教育数据监管平台</h2>
        </div>
    </div>
    <div class="container">
        <div class="main-box main-part1">
            <div class="content-box">
                <!--基础数据-->
                <div class="mod-wrap l-inner-box">
                    <!--四个边角-->
                    <div class="edgesBox">
                        <i class="edges01"></i>
                        <i class="edges02"></i>
                        <i class="edges03"></i>
                        <i class="edges04"></i>
                    </div>
                    <div class="line-top"></div>
                    <div class="title" >基础数据</div>
                    <ul class="dataBox01">
                        <li v-for="(base_data, index) in base_data_list">
                            <div class="dataInner">
                                <div class="dataLabel">{{base_data.title}}</div>
                                <div class="chartBoxWarp"><div class="chartBox baseDataChart"></div></div>
                                <div class="dataNumBox">
                                    <div class="dataNum">
                                        <div style="display: inline-block;">
                                            <i class="fl num">{{base_data.active_num}}/</i>
                                            <template v-if="!base_data.editing">
                                                <i class="fl">{{base_data.total_num === null ? '?' : base_data.total_num}}</i>
                                                <span class="fl editIcon" @click="edit_base_data(base_data)"></span>
                                            </template>
                                            <template v-else>
                                                <span class="fl edit-box" v-bind:class="{'error-box': is_edit_error}">
                                                    <input type="text" maxlength="6"
                                                       v-model.trim="base_data.edit_num"
                                                       v-on:keyup.enter="edit_sure(base_data, index)"
                                                       v-on:input="handle_input(base_data, base_data.active_num)"
                                                    >
                                                </span>
                                                <span class="fl input-btn" @click="edit_sure(base_data, index)">确定</span>
                                            </template>
                                        </div>
                                    </div>
                                    <div class="dataState">{{base_data.tip_name}}</div>
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
                <div class="mod-wrap r-inner-box">
                    <!--四个边角-->
                    <div class="edgesBox">
                        <i class="edges01"></i>
                        <i class="edges02"></i>
                        <i class="edges03"></i>
                        <i class="edges04"></i>
                    </div>
                    <div class="l-switch-box">
                        <h5>使用数据：{{current_date}}
                            <a href="javascript:void(0)">
                                <div class="q-layerTips">每月4日后显示当月的数据，4日前显示上月数据</div>
                            </a>
                        </h5>
                        <ul class="switch-inner">
                            <li v-bind:class="{active: homework_tab_index === 0}">
                                <div class="list" @click="switch_homework_tab(0)">
                                    <p><i>布置作业：</i><span class="num">{{homeWork_assigned_num}}</span></p>
                                    <b class="l-arrow"></b>
                                </div>
                            </li>
                            <li v-bind:class="{active: homework_tab_index === 1}">
                                <div class="list" @click="switch_homework_tab(1)">
                                    <p>资源下载：<span class="num">{{resource_download_num}}</span></p>
                                    <b class="l-arrow"></b>
                                </div>
                            </li>
                            <li v-bind:class="{active: homework_tab_index === 2}">
                                <div class="list" @click="switch_homework_tab(2)">
                                    <p>学科测评：<span class="num">{{exam_participate_num}}</span></p>
                                    <b class="l-arrow"></b>
                                </div>
                            </li>
                            <li v-bind:class="{active: homework_tab_index === 3}">
                                <div class="list" @click="switch_homework_tab(3)">
                                    <p>趣味活动：<span class="num">{{activity_articpate_num}}</span></p>
                                    <b class="l-arrow"></b>
                                </div>
                            </li>
                        </ul>
                    </div>
                    <div class="mod-wrap r-chart-box" id="chartBox">
                        <a class="detailLabel" href="javascript:void(0)" v-show="(homework_tab_index === 2) && exam_no_name_list.length">
                            测评明细
                            <div class="r-layer-box" >
                                <p class="layerTitle">测评明细</p>
                                <div class="scroll-box">
                                    <p v-for="item in exam_no_name_list">测评{{item.no}}：{{item.name}}</p>
                                </div>
                            </div>
                        </a>
                        <div class="swiper-container homework-swiper">
                            <div class="swiper-wrapper">
                                <div class="swiper-slide homework-slide" v-for="(slide, index) in homework_data_list">
                                    <!--图表占位-->
                                    <template v-show="!slide.is_empty">
                                        <div class="homeworkLeftChart"></div>
                                        <div class="homeworkRightChart"></div>
                                    </template>
                                    <!-- 暂无数据 -->
                                    <div class="data-empty-b" v-show="slide.is_empty">
                                        <h3>暂无数据</h3>
                                        <div class="txtState">
                                            <template v-if="homework_tab_index === 0">
                                                <p style="text-align: center">本月暂无老师在平台布置作业</p>
                                            </template>
                                            <template v-else-if="homework_tab_index === 1">
                                                <p>海量优质教学资源，老师更省心，教学效果更高效！请老师们使用起来吧~</p>
                                            </template>
                                            <template v-else-if="homework_tab_index === 2">
                                                <p>学科测评流程</p>
                                                <p>联系学校负责人-学科命题-试卷录入-学生在线作答-系统自动批改生成报告</p>
                                            </template>
                                            <template v-else-if="homework_tab_index === 3">
                                                <p>趣味活动流程</p>
                                                <p>打开一起小学老师app-布置趣味活动-学生在线完成-系统自动生成报告</p>
                                            </template>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="swiper-pagination homework-pagination"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="main-box main-part2">
            <!--实时动态-->
            <div class="mod-wrap l-inner-box">
                <div class="edgesBox">
                    <i class="edges01"></i>
                    <i class="edges02"></i>
                    <i class="edges03"></i>
                    <i class="edges04"></i>
                </div>
                <div class="line-top"></div>
                <div class="title">实时动态</div>
                <div class="l-chart-box">
                    <div class="giftBox">
                        <div class="center-box"></div>
                        <div class="center-dot"></div>
                        <div class="gift-layer gift-layer01"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer01_1.png'/>"></div>
                        <div class="gift-layer gift-layer02"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer02.png'/>"></div>
                        <div class="gift-layer gift-layer03"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer03.png'/>"></div>
                        <div class="gift-layer gift-layer04"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer04.png'/>"></div>
                        <div class="gift-layer gift-layer05"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer05.png'/>"></div>
                        <div class="gift-layer gift-layer06"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer06.png'/>"></div>
                        <div class="gift-layer gift-layer07"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer07.png'/>"></div>
                        <div class="gift-layer gift-layer08"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer08.png'/>"></div>
                        <div class="gift-layer gift-layer09"><img src="<@app.link href='public/skin/adminteacher/images/gift_layer09.png'/>"></div>
                        <div class="gift-txt gift-txt01">备</div>
                        <div class="gift-txt gift-txt02">讲</div>
                        <div class="gift-txt gift-txt03">练</div>
                        <div class="gift-txt gift-txt04">测</div>
                    </div>
                </div>
                <div class="r-dynamic-box" id="dynamicBox">
                    <template v-show="massage_list && massage_list.length">
                        <div class="dynamic-inner" id="dynamicList-1">
                            <#-- txt-yellow -->
                            <div class="dynamic-list" v-for="list in massage_list">
                                <span v-text="list"></span>
                            </div>
                        </div>
                        <div class="dynamic-inner" id="dynamicList-2">
                            <#-- txt-yellow -->
                            <div class="dynamic-list" v-for="list in massage_list">
                                <span v-text="list"></span>
                            </div>
                        </div>
                    </template>
                    <div class="noData-box" v-show="!massage_list || (massage_list.length === 0)">暂无动态</div>
                </div>
            </div>
            <!--学科能力养成-->
            <div class="mod-wrap m-inner-box">
                <div class="edgesBox">
                    <i class="edges01"></i>
                    <i class="edges02"></i>
                    <i class="edges03"></i>
                    <i class="edges04"></i>
                </div>
                <div class="line-top"></div>
                <div class="title">学科能力养成</div>
                <div class="termTime">{{ability_develop_term}}</div>
                <div class="switchGrade-box" v-bind:class="{showUp: show_subject_clazzs_tool}" v-click-outside-sc="click_outside_sc">
                    <p class="curGrade gradeLabel" @click="show_grade_list">{{ability_develop_grade_list[ability_develop_grade_index-1]}}</p>
                    <ul>
                        <li class="gradeLabel"
                            v-bind:class="{active: (index + 1) === ability_develop_grade_index}"
                            v-for="(list, index) in ability_develop_grade_list"
                            @click="switch_grade_list(index + 1)"
                        >{{list}}</li>
                    </ul>
                </div>
                <div class="m-chart-box">
                    <div class="swiper-container ad-subject-swiper">
                        <div class="swiper-wrapper">
                            <div class="swiper-slide" v-for="(slide, index) in ad_data_list">
                                <div class="subjectAbilityChart" v-show="!slide.is_empty"></div>
                                <div class="data-empty-s" v-show="slide.is_empty">
                                    <h3>暂无数据</h3>
                                    <div class="txtState">
                                        {{ability_develop_grade_list[ability_develop_grade_index-1]}}
                                        {{ability_develop_subject}}暂无数据。知识巩固练习，学科能力提升更快。
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="swiper-pagination subject-pagination"></div>
                    </div>
                </div>
            </div>
            <!--知识板块掌握-->
            <div class="mod-wrap r-inner-box">
                <div class="edgesBox">
                    <i class="edges01"></i>
                    <i class="edges02"></i>
                    <i class="edges03"></i>
                    <i class="edges04"></i>
                </div>
                <div class="line-top"></div>
                <div class="title">知识板块掌握</div>
                <div class="termTime">{{knowledge_swiper_term}}</div>
                <!--年级下拉框-->
                <div class="switchGrade-box " v-bind:class="{showUp: show_knowledge_grade_tool}" v-click-outside-kl="click_outside_kl">
                    <p class="curGrade gradeLabel" @click="show_knowledge_grade">{{knowledge_grade_list[knowledge_grade_index-1]}}</p>
                    <ul>
                        <li class="gradeLabel"
                            v-bind:class="{active: index + 1 === knowledge_grade_index}"
                            v-for="(grade, index) in knowledge_grade_list"
                            @click="switch_knowledge_grade(index + 1)"
                        >{{grade}}</li>
                    </ul>
                </div>
                <div class="r-chart-box" style="display:block;">
                    <!--knowledge-->
                    <div class="swiper-container knowledge-swiper">
                        <div class="swiper-wrapper">
                            <div class="swiper-slide" v-for="(slide, index) in knowledge_data_list ">
                                <div class="knowledgeChart" v-show="!slide.is_empty">{{index}}</div>
                                <div class="data-empty-s" v-show="slide.is_empty">
                                    <h3>暂无数据</h3>
                                    <div class="txtState">
                                        {{knowledge_grade_list[knowledge_grade_index-1]}}
                                        {{knowledge_subject}}暂无数据，多加布置作业，知识掌握更牢固。
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="swiper-pagination knowledge-pagination"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="footer"></div>
</div>

<script src="${getVersionUrl('public/plugin/swiper-3.4.2/swiper.min.js')}"></script>
<script>

</script>
</@layout.page>