<#import "../../module.ftl" as module>
<@module.page
title="专辑管理"
leftMenu="专辑管理"
pageJsFile={"albumlist" : "public/script/basic/album/albumlist"}
pageJs=["albumlist"]
>

<div class="op-wrapper clearfix">
    <span class="title-h1">专辑管理</span>
</div>
<div class="op-wrapper marTop clearfix" style="margin-bottom: 10px;">
    <div class="matManage-box">
        <div class="input-control">
            <p>专辑名称</p>
            <input id="albumName" placeholder="专辑名称" maxlength="100" title="" class="item" style="width: 300px" value="${albumName!''}">
            <input type="hidden" name="page" value="${pageIndex!1}" id="pageIndex">
            <a  id="album_search" class="blue-btn" href="javascript:void(0)">查询</a>
        </div>
    </div>
</div>
<table class="data-table">
    <thead>
    <tr>
        <th style="width: 120px;">专辑名称</th>
        <th style="width: auto;">创建时间</th>
        <th style="width: auto;">上线时间</th>
        <th width="77">订阅数</th>
        <th style="width: 35%">操作</th>
    </tr>
    </thead>
    <tbody>
        <#if mizarAlbumList?? && mizarAlbumList?size gt 0>
            <#list mizarAlbumList as mizarAlbum>
            <tr>
                <td>${mizarAlbum.title!''}</td>
                <td>${mizarAlbum.createTime!''}</td>
                <td>${mizarAlbum.onlineTime!''}</td>
                <td>${mizarUserAlbumSubCount[mizarAlbum.id]!0}</td>
                <td><a name="edit_album"  data-albumId="${mizarAlbum.id!''}" class="op-btn" href="javascript:void(0);">编辑</a></td>
            </tr>
            </#list>
        <#else>
        <tr>
            <td>暂无专辑数据</td>
        </tr>
        </#if>
    </tbody>
</table>
<div id="paginator" pageIndex="${(pageIndex!1)}" class="paginator clearfix" totalPage="<#if totalPage??>${totalPage}<#else>1</#if>"></div>
</@module.page>