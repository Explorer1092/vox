/* global define : true, PM : true, $:true */
/**
 *  @date 2015/9/16
 *  @auto liluwei
 *  @description 该模块主要负责作业模块 送花逻辑 等等
 */
define(['template', 'tab', 'redo', 'homework'], function(template){

    'use strict';

    // build secondInfo
    var sid = PM.sid;

    var urlCommonSuffix = 'sid='+sid+'&subject=',
        url = {
            errorReport : '/parentMobile/homework/wrongquestionlist.vpage?' + urlCommonSuffix
        };

    var common_second_tab = [
        [
            "错题本",
            "errorReport"
        ]
    ];


    var secondInfo = {
        MATH : {
            display : "数学",
            secondTab : common_second_tab
        },
        ENGLISH : {
            display : "英语",
            secondTab : common_second_tab
        },
        CHINESE : {
            display : "语文",
            secondTab : common_second_tab
        }
    };

    var buildSecondTabInfo = function(text, tabTemplateElId, subject){
        return  {
            text : text,
            //tabLocal : "#willFollow",
            ajaxUrl  : url[tabTemplateElId] + subject,
            tabTargetEl : '#tabContent',
            tabTemplateEl : "#" + tabTemplateElId
        };
    };

    $.each(secondInfo, function(subject, info){
        info.secondTab = info.secondTab.map(function(second){
            return buildSecondTabInfo.apply(null, second.concat(subject));
        });
    });

    var $tabContent,
        $willFollow,
        $secondTabDom;

    $(function(){
        $tabContent = $("#tabContent");
        $willFollow = $("#willFollow");
        $secondTabDom = $("#doFullSecondTab");
    });


    $(".doTop .doTab").data(
        "tab_change_fn",
        function(){

            $tabContent.html("");

            var secondHtml = template(
                "secondTabTemp",
                {
                    secondTabInfo : secondInfo[$(this).data("subject")].secondTab,
                    window : window
                }
            );

            secondHtml || (secondHtml = $willFollow.text());

            var $secondDom = $(secondHtml);

            $secondTabDom.html($secondDom);

            $secondDom
            .find(".doTab")
            .data("tab_change_fn", function(){
                $(document).trigger("checkCanShowRedoBtn");
            })
            .filter(":first").click();
        }
    ).eq(0).click();

    //ui2.0 版本大于1.6的title样式修改
    (function(){

        if($("#J-do-homeworkReport-page").length>0){

            var version=(PM.client_params&&PM.client_params.app_version)||PM.app_version||'0.0';

            if(version>='1.6'){
                $("#J-do-title-topBox").attr("style","padding: 36px 0 0;");
            }
        }
    })();
});
