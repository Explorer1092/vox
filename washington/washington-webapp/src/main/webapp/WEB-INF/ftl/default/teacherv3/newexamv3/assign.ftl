<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
<@sugar.capsule js=["vue","datepicker","plugin.newexamv3"] css=["plugin.datepicker","new_teacher.carts","homeworkv3.homework"] />
<style type="text/css">
    /*单行文本省略号*/
    .line-txt-ellipsis{overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
    /*鼠标手势*/
    .m-pointer{cursor: pointer;}
    /*单行线*/
    .paper-container .s-line{
        width : 98%;
        height: 1px;
        line-height: 1px;
        background-color: #dae6ee;
        margin: 2px auto;
    }
    /*模块信息表格*/
    .table-box{
        margin-top: 10px;
        font-size: 14px;
        overflow: hidden;
        padding: 0 15px;
        text-align: center;
    }
    .table-box table{ width: 100%;border: 1px solid #ebe9e4; border-radius: 6px;border-collapse: collapse;border-spacing: 0;}
    .table-box table thead{background-color: #f6f6f6;}
    .table-box table td{vertical-align: middle;padding: 0 10px; height: 50px;border: 1px solid #ebe9e4; border-bottom: 0; border-top: 0;}
    /* 题目内容样式*/
    .h-content .subject-type{padding: 20px 15px 0; font-size: 14px;line-height: normal;}
    .h-content .subject-type .sub-title{font-size: 14px;}
    .h-content .subject-type .question-number{font-style:normal;color: #fff;font-size: 12px;width: 20px;height: 20px;line-height: 20px;border-radius: 50%;text-align: center;background-color: #489bf4;margin: 5px;flex-shrink: 0;}
    .answeBox, .answeBox-2{margin-top: 20px;padding: 12px 24px;background: #f4f6f8;}
    .freya .line .container{margin-left: -50px;}
    [v-cloak]{
        display: none;
    }
</style>

<div id="assignmockexam" v-cloak>
    <div class="w-base" style="position: relative; zoom: 1;  z-index: 5;">
        <div class="w-base-title" style="clear: both; *zoom:1; overflow: hidden;">
            <h3>布置单元检测</h3>
        </div>
        <div class="w-base-container">
            <#--年级班级-->
            <level-clazzs v-if="clazzList && clazzList.length > 0" :clazz-list="clazzList" v-on:level-click="levelClickCb" v-on:clazz-click="clazzClickCb"></level-clazzs>
            <#--课本单元-->
            <book-units v-if="book" v-bind="book" v-on:exchange-book="exchangeBook" v-on:exchange-unit="exchangeUnit"></book-units>
        </div>
    </div>
    <div class="w-base h-baseTab">
        <div class="homework-way" v-show="focusLevel > 0 && focusClazzs && focusClazzs.length > 0">
            <div class="subtitle-bar h-switch">
                <div class="h-arrow h-arrow-L" style="display: none;"><i class="h-arrow-icon h-arrow-iconLhover"></i></div>
                <div class="h-arrow h-arrow-R" style="display: none;"><i class="h-arrow-icon h-arrow-iconR h-arrow-iconRhover"></i></div>
                <ul class="topBar">
                    <li class="line-txt-ellipsis m-pointer" v-for="(paperTypeObj,index) in unitTestPaperInfos" v-on:click="tabClick(paperTypeObj)" v-bind:class="{'active':paperTypeObj.paperTypeId == focusPaperType}" v-text="paperTypeObj.paperType">单元检测</li>
                </ul>
            </div>
            <div class="home-content">
                <paper-list :papers="paperMap[focusPaperType]" v-on:paper-click="paperClickCb"></paper-list>
                <paper-info v-if="paperInfo" v-bind="paperInfo" v-on:go-assign="goAssignCb"></paper-info>
                <div class="h-set-homework current" v-if="!paperMap[focusPaperType] || paperMap[focusPaperType].length == 0">
                    <div class="seth-mn">
                        <div class="testPaper-info">
                            <div class="inner" style="padding: 15px 10px; text-align: center;">
                                <p>没有试卷信息，请切换其他单元查看</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="h-set-homework current" v-show="focusLevel > 0 && (!focusClazzs || focusClazzs.length == 0)">
            <div class="seth-mn">
                <div class="testPaper-info">
                    <div class="inner" style="padding: 15px 10px; text-align: center;">
                        <p>请选择相应的班级</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<#include "../templates/levelandclazzs_vue.ftl">
<#include "../templates/bookandunits_vue.ftl">
<#include "../templates/newexamv3/papercontent.ftl">
<#include "../templates/newexamv3/confirm.ftl">
<script type="text/javascript">
    var constantObj = {
        currentDate : "${currentDate!}",
        startDateTime : "${startDateTime!}",
        endDateTime : "${endDateTime!}",
        imgDomain   : "${imgDomain!}",
        env : <@ftlmacro.getCurrentProductDevelopment />
    };

    $(function(){
        var subject = $17.getQuery("subject");
        window.LeftMenu && (LeftMenu.focus(subject + "_mockexam"));

        //初始化题目渲染库
        Freya.init({
            isDev: ["staging","prod","test"].indexOf(constantObj.env) === -1,
            cheat : ["staging","test"].indexOf(constantObj.env) !== -1,
            cdnHost:constantObj.imgDomain
        }).then(function(){
            $17.info("Freya init completed");
        });

    });
</script>
<@sugar.capsule js=["newexamv3.assign"] />
</@shell.page>