<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="一起作业，中秋月更圆 - 感恩教师节">
    <@app.css href="public/skin/project/sendpkfashion/skin.css" />
<div class="main">
    <div class="banner">免费送价值50学豆的PK时装</div>
    <div class="content">
        <h1>下载一起作业家长通</h1>
        <div class="bind-fun-box">
            <div class="bindCode">
                <p class="code">
                    <#include "../../studentv3/apps/JZT_QR.ftl">
                    <img src="${get_JZT_QR("pk", false)}" width="144px" height="144px"/>
                </p>
                <p>扫描二维码下载一起作业家长通</p>
            </div>
        </div>
        <div class="bind-fun-box bind-reward-box">
            <div class="bindReward"><!--image--></div>
            <dl>
                <dt>下载后可获得奖励：</dt>
                <dd><span>●</span>10个PK活力值，当日有效</dd>
                <dd><span>●</span>PK时装：翅膀一对（见右图）</dd>
                <dd><span>●</span>下载成功后，刷新到PK馆【角色】领取</dd>
                <dd>特别提醒：请先创建PK角色后再下载家长通</dd>
            </dl>
        </div>
        <div class="bind-reward-box">
            <h4>下载家长通好处：</h4>
            <ul>
                <li><span>1</span>查作业</li>
                <li><span>2</span>查成绩</li>
                <li><span>3</span>查错题</li>
                <li><span>4</span>查学豆</li>
            </ul>
            <div style="clear: both;"></div>
        </div>
    </div>
</div>
</@temp.page>