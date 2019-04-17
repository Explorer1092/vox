<#import "../../layout.ftl" as homeworkReport>
<@homeworkReport.page title="一键发奖励" pageJs="quickaward">
    <@sugar.capsule css=['homework','jbox'] />

<div class="mhw-syncExercises-feedback">
    <div class="mhw-header">
        <div class="header-inner">
            <h2 class="title">一键发奖励</h2>
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
    </div>
    <div class="msf-sendBean">
        <div class="fl-txt">给选中的学生发学豆<i class="bean-icon"></i></div>
        <div class="J_addBean fr-btn">
            <span class="subtraction">-</span><div class="J_sendBean sendBean">0</div><span class="add">+</span>
        </div>
    </div>
    <div class="btns pad-30" style="position: relative;z-index: 10;">
        <a href="javascript:void(0)" class="w-btn J_submit">确定</a>
    </div>
</div>

</@homeworkReport.page>