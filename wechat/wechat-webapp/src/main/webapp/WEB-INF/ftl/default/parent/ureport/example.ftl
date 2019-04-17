<#import "../layout.ftl" as unitExample>
<@unitExample.page title="重点知识及例题" pageJs="unitExample">
<@sugar.capsule css=['unitReport'] />
<div id="loading" style="padding:50px 0; text-align:center">数据加载中...</div>
<div id="reportExample" class="unitReports-box" style="display:none;">
    <div id="example">
        <div class="caption">
            <h1 style="width:70%">
                <span class="subHead" style="padding:15px 0 0 0;margin-right:-40%" data-bind="html:countInfo">
                </span>
                <span data-bind="text:title"></span>
            </h1>
        </div>
        <div class="title">
            <i class="icon icon-1"></i>
            易考例题
        </div>
    </div>
    <div class="container container-1">
        <div class="content">
            <p class="prompt">题目：</p>
            <div id="questionContent"></div>
        </div>
    </div>
    <div class="container container-1">
        <div class="content">
            <p class="prompt">解析：</p>
            <div id="questionContentAnalysis"></div>
        </div>
    </div>
    <div class="info">
        <h2><a href="javascript:download()" class="btn-view">下载</a>下载一起作业APP即可直接免费训练</h2>
    </div>
</div>
<#include "../menu.ftl">
</@unitExample.page>