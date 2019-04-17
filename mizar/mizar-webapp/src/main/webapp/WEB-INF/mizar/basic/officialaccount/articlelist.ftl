<#import "../../module.ftl" as module>
<@module.page
title="公众号"
leftMenu="发布管理"
pageJsFile={"articlelist" : "public/script/basic/officialaccount/articlelist"}
pageJs=["articlelist"]
>

<div class="op-wrapper clearfix">
    <span class="title-h1">一起作业公众—发布管理</span>
</div>
<div class="op-wrapper marTop clearfix" style="margin-bottom: 10px;">
    <div class="item time-widAuto">
        <p>公众号：</p>

        <select class="v-select" style="width: 140px;" title="" data-bind="options:accountList,optionsText:'name', optionsValue:'id',value: accountValue, event:{ change: accountChanged}">
        </select>
    </div>
    <div class="item time-region" style="width: auto;">
        <p>发布时间</p>
        <div>
            <div class="time-select">
                <input title="" id="startTime" class="v-select" />
                <div style="margin:0 5px;line-height:30px;">至</div>
                <input title="" id="endTime" class="v-select" />
            </div>
        </div>
    </div>
    <div class="item time-widAuto marLeft15">
        <p>状态</p>
        <div>
            <a data-bind="click: searchBtn" class="blue-btn marLeft15" href="javascript:void(0)">查询</a>
            <select title="" class="v-select" style="width: 110px;" data-bind="options:statusList,optionsText:'name', optionsValue:'value',value: statusValue, event:{ change: statusChanged}">
            </select>
        </div>
    </div>
    <a data-bind="click: $root.editBtn" class="blue-btn" href="javascript:void (0);" style="margin-top:27px;">新建发布</a>
</div>
<div style="margin-bottom: 10px; color: red; font-weight: bold;">
    今日还可发布：<!--ko text: publishLeftNumsD--><!--/ko-->次， 本月还可发布： <!--ko text: publishLeftNumsM--><!--/ko-->次
</div>

<table class="data-table conTable" data-bind="visible: showDetail" style="display: none;">
    <thead>
    <tr>
        <th style="width: 405px;">文章预览</th>
        <th style="width: 105px;">发布时间</th>
        <th>发布人</th>
        <th style="width: 65px;">状态</th>
        <th style="min-width: 95px;">操作</th>
    </tr>
    </thead>
    <tbody>
    <!-- ko foreach : {data : showDetail, as : '_sd'} -->
    <tr>
        <td>
            <!-- ko foreach : {data : _sd.articles, as : '_at'} -->
            <div class="con-box">
                <img src="" data-bind="attr: {'src': _at.simgUrl}, click: $root.viewImgBtn" title="点击预览" class="img1">
                <div class="info"><!--ko text: $index()+1--><!--/ko-->.<!--ko text: _at.articleTitle--><!--/ko--></div>
            </div>
            <!--/ko-->
        </td>
        <td><!--ko text: _sd.publishTime--><!--/ko--></td>
        <td><!--ko text: _sd.publishUser--><!--/ko--></td>
        <td>
            <!--ko if: _sd.status == 'Published'-->
            已发布
            <!--/ko-->
            <!--ko if: _sd.status == 'Online'-->
            未发布
            <!--/ko-->
            <!--ko if: _sd.status == 'Offline'-->
            撤回
            <!--/ko-->

        </td>
        <td>
            <!--ko if: _sd.status == 'Published'-->
            <a data-bind="click: $root.offlineBtn" class="op-btn" href="javascript:void(0);">撤回</a>
            <!--/ko-->
            <!--ko if: _sd.status == 'Online'-->
            <a data-bind="click: $root.pushBtn" class="op-btn" href="javascript:void(0);">发布</a>
            <!--/ko-->


            <a data-bind="click: $root.editBtn" class="op-btn" href="javascript:void(0);">编辑</a>
        </td>
    </tr>
    <!--/ko-->
    <tr data-bind="visible: contentDetail().length == 0" style="display: none;">
        <td colspan="5">暂无文章列表</td>
    </tr>
    </tbody>
</table>
<div id="paginator" pageIndex="1" title="" class="paginator clearfix" totalPage="1"></div>
</@module.page>