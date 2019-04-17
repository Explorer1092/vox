<#import "../layout.ftl" as layout>
<@layout.page title="我的老师">
    <@sugar.capsule css=['chipsTeacher'] />

<style>
    html, body, input{
        font: inherit;
    }
    .teacherWrap .teacherHead {
        padding: 1.425rem .5rem 1.5rem 1.55rem;
        height: 4.7rem;
    }
    .codeBox .codeBtn{
        display: inline-block;
        margin-left: 0.3rem;
    }
    .wxNumber_span {
        -webkit-user-select: all !important;
    }
</style>

<div class="teacherWrap">
    <div class="teacherHead" style="display: none;">
        <div class="titleText">我的老师</div>
    </div>
    <div class="teacherMain">
        <div class="addTeacher">如何添加老师</div>
        <div class="method">方法一：保存二维码，在微信中识别二维码添加老师</div>
        <div class="codeBox" style="position: relative;">
            <div class="codeImg"></div>
            <#if qrCode?? && qrCode != '' >
                <img class="codeImg" src="${qrCode!''}" alt="" style="position: absolute;top: 0;z-index: 999;"/>
            <#else>
                <img class="codeImg" src="/public/images/parent/chips/teacher.jpg" alt="" style="position: absolute;top: 0;z-index: 999;"/>
            </#if>
        </div>
        <div class="method">方法二：复制老师微信号，在微信中添加老师</div>
        <div class="wxBox">
            <div class="wxNumber">微信号：
                <span class="wxNumber_span">
                    <#if wxCode?? && wxCode != ''>
                        ${wxCode}
                    <#else>
                        shutiao004
                    </#if>
                </span>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function pageLog(){
        require(['logger'], function(logger) {
            // 我的老师页_被加载
            logger.log({
                module: 'm_XzBS7Wlh',
                op: 'myteacher_load'
            })
        })
    }
</script>

</@layout.page>

