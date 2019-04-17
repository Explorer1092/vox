<#import "../../layout.ftl" as homeworkReport>
<@homeworkReport.page title="一键写评语" pageJs="quickremarks">
    <@sugar.capsule css=['homework','jbox'] />

<div class="mhw-syncExercises-feedback">
    <div class="mhw-header">
        <div class="header-inner">
            <div class="fl">一键写评语</div>
            <div class="fr"><a href="javascript:void(0)" class="switch-link J_templateText">使用模版</a></div>
        </div>
    </div>

    <div class="msf-main">
        <div class="J_filter msf-top">
            <ul>
                <li class="active" type="0">全部</li>
                <li type="1">已完成</li>
                <li type="2">未完成</li>
            </ul>
        </div>
        <div class="J_stuList"></div>
        <div class="msf-feedbak" style="position: relative;z-index: 10;">
            <textarea class="J_feedbak" placeholder="请填写您要发送的评语内容" maxlength="100"></textarea>
            <p class="num-tips">0/100</p>
        </div>
    </div>
    <div class="btns pad-30" style="position: relative;z-index: 10;">
        <a href="javascript:void(0)" class="w-btn J_submit">确定</a>
    </div>
</div>

<div class="J_slideBox mhw-slideBox" style="display:none;">
    <div class="mask" ></div>
    <div class="innerBox" >
        <div class="hd">评语模版<span class="close">×</span></div>
        <div class="mn">
            <div class="infoClass">
                <div class="right">
                    <ul class="J_material material"></ul>
                </div>
            </div>
        </div>
        <div class="mhw-btns">
            <a href="javascript:void(0)" class="J_selText w-btn">确认</a>
        </div>
    </div>
</div>

</@homeworkReport.page>