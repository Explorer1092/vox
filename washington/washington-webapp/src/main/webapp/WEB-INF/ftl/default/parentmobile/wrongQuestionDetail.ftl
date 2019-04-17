<#import './layout.ftl' as layout>

<@layout.page className='WrongQuestionDetail' title="错题详情" pageJs="unitReportDetail" globalJs = []>

    <#escape x as x?html>

        <#if result.success>

			<#include "./constants.ftl">
            <#assign homeworkType = homeworkType!'ENGLISH'>

            <div class="parentApp-error-link doAutoTrack" data-track="faultnotes|faultdetail_open">
                <#if jump_afenti?? >
                    <a href="javascript:void(0);" class="do-goto-game doTrack"
                       data-appKey="${jump_afenti.appKey!'link'}"
                       data-launchUrl="${jump_afenti.launchUrl!''}"
                       data-browser="${jump_afenti.browser!'system'}"
                       data-orientation="${jump_afenti.orientation!'sensor'}"

                       data-track="faultnotes|
                       <#if homeworkType?index_of("ENGLISH") gt -1 >faultdetail_en_afenti_click</#if>
                       <#if homeworkType?index_of("MATH") gt -1 >faultdetail_math_afenti_click</#if>
                       <#if homeworkType?index_of("CHINESE") gt -1 >faultdetail_chinese_afenti_click</#if>
                    ">
                        <div class="green">查看</div>
                        <div class="text">本次错题已放入阿分题“错题精灵”，快让宝贝去练习吧！</div>
                    </a>
                </#if>
            </div>




            <#assign eids = result.eids![]>

            <#if eids?size == 0>
                <div class="parentApp-messageNull">
                    暂无错题
                </div>
            <#else>
                <style>
                    .container{
                        padding: 0 15px;
                        overflow: hidden;
                        position: relative;
                        width: 95.313%;
                    }
                    .container h3{
                        font-weight: bolder;
                    }
                </style>
                <div class="container">
                    <div class="doRenderQuestionByIds" data-eids="${eids?join(",")}" data-complete_url="${result.completeUrl!''}" > </div>
                    <h3 class="">正确答案与解析</h3>
                    <div class="doRenderRightAnswer" > </div>
                </div>
                <div class="foot_btn_box foot_btn_box_fixed">
                    <#assign questionAction = [
                    {
                        "display" : "上一题",
                        "className" : "doGetPreviousQuestion",
                        "style" : "display:none;",
                        "track" : "faultdetail_prevs_click"
                    },
                    {
                        "display" : "下一题",
                        "className" : "doGetNextQuestion",
                        "style" : (eids?size == 0)?string("display:none;", ""),
                        "track" : "faultdetail_next_click"
                    }
                    ]>
                    <div class="inner">
                        <#list questionAction as action>
                            <a class="${action.className} btn_mark btn_mark_block doTrack" data-track="faultnotes|${action.track}" href="javascript:;" style="${action.style}"><span style="color: #FFFFFF; font-weight: normal;">${action.display}</span></a>
                        </#list>
                    </div>
                </div>
            </#if>
        <#else>
            <em class="doAutoTrack" data-track="faultnotes|faultdetail_error"></em>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>
    </#escape>

</@layout.page>
