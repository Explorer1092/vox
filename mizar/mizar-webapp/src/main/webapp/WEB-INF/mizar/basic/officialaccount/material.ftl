<#import "../../module.ftl" as module>
<@module.page
title="公众号"
leftMenu="素材管理"
pageJsFile={"materiallist" : "public/script/basic/officialaccount/materiallist"}
pageJs=["materiallist"]
>

<div class="op-wrapper clearfix">
    <span class="title-h1">一起作业公众号—素材管理</span>
</div>

<div class="matManage-box">
    <div class="input-control">
        <p>标题名称：</p>
        <input data-bind="value: titleVal" placeholder="标题名称" maxlength="100" title="" class="item" style="width: 300px">
        <a data-bind="click: searchBtn" class="blue-btn" href="javascript:void(0)">查询</a>
    </div>
    <a class="blue-btn" href="/basic/officialaccount/materialedit.vpage" style="margin-top:29px;">新建文章</a>
</div>

<table class="data-table">
    <thead>
    <tr>
        <th style="width: 130px;">标题</th>
        <th style="width: 240px;">生成的链接</th>
        <th style="width: 100px;">生成时间</th>
        <th style="width: 100px;">修改时间</th>
        <th>状态</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
    <!-- ko foreach : {data : showDetail, as : '_sd'} -->
    <tr>
        <td>
            <!--ko text: _sd.title--><!--/ko-->
        </td>
        <td><a style="word-break: break-all; width: 310px; display: block;" href="javascript:void(0);"><!--ko text: _sd.generateUrl--><!--/ko--></a></td>
        <td><!--ko text: _sd.createTime--><!--/ko--></td>
        <td><!--ko text: _sd.updateTime--><!--/ko--></td>
        <td>
            <!--ko if: _sd.submitted-->
            已投稿
            <!--/ko-->

            <!--ko if: !_sd.submitted-->
            未投稿
            <!--/ko-->

        </td>
        <td>
            <a data-bind="click: $root.editBtn" class="op-btn" href="javascript:void(0);">编辑</a><br />
            <a class="op-btn copyBtn" data-bind="attr:{'data-content':  _sd.generateUrl, 'data-clipboard-text': _sd.generateUrl}" href="javascript:void(0);">复制链接</a><br />
            <!--ko if: !_sd.submitted-->
            <a class="op-btn" data-bind="click: $root.submittedBtn" href="javascript:void(0);">
                <!--ko if: !_sd.submitted-->
                投稿
                <!--/ko-->
            </a><br />
            <!--/ko-->
            <a data-bind="click: $root.viewBtn" class="op-btn" href="javascript:void(0);">预览</a>
        </td>
    </tr>
    <!--/ko-->
    </tbody>
</table>
<div id="paginator" pageIndex="1" title="" class="paginator clearfix" totalPage="1"></div>
</@module.page>