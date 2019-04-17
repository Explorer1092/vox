<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="" footerIndex=2>
<@sugar.capsule css=['home']/>
<#assign shortIconTail = "?x-oss-process=image/resize,w_300,h_375/auto-orient,1">
<style>
    .vacation_box{margin:.5rem 0;padding:.5rem .75rem;color:#636880;background-color:#fff;overflow:hidden}
    .vacation_box .vacTitle{position:relative;font-size:.75rem}
    .vacation_box .vacTitle .add_btn{position:absolute;top:-.25rem;right:-.25rem;width:1.25rem;height:1.25rem;text-align:center;color:#ff7d5a;font-size:1rem}
    .vacation_box .vacItem{padding:.25rem 0;font-size:.65rem;line-height:.95rem}
    .vacation_box .vacItem .time{color:#bababa}
    .vacation_box .vacItem img{display:block;width:5rem;height:5rem}
    .vacation_box .more_btn{float:right;font-size:.6rem;color:#898c91}
    .vacation_box .vacInfo{padding:0 0 .5rem 0;font-size:.65rem;color:#636880}
    .vacation_box .listNav{display:-webkit-box;display:-moz-box}
    .vacation_box .listNav li{-webkit-box-flex:1;-moz-box-flex:1;margin:0 .125rem;display:block;text-align:center;width:100%;height:1.25rem;font-size:.6rem;color:#636880;line-height:1.25rem;border:.05rem solid #636880;border-radius:.5rem}
    .vacation_box .listNav li.active{color:#ff7d5a;border:.05rem solid #ff7d5a}
    ._active{color:#ff7d5a;border-bottom:.1rem solid #ff7d5a}
    .school_data{border-radius:15px}
</style>
<div class="primary-box">
    <div class="res-top fixed-head">
        <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
        <span class="return-line"></span>
        <span class="res-title js-pageTitle">分科扫描数据</span>
    </div>
    <div class="c-main tab-main">
        <div class="pr-side">
            <table class="sideTable">
                <thead>
                <tr><td>科目</td><td>扫描学生数</td></tr>
                </thead>
                <#if singleSubjectAnshes?has_content>
                    <tbody>
                        <#list singleSubjectAnshes as item>
                        <tr class="hover line"><td>${item.subjectName!'--'}</td><td>${item.anshGte2StuCount!'0'}</td></tr>
                        </#list>
                    </tbody>
                </#if>
            </table>
        </div>
    </div>
</div>
</@layout.page>
