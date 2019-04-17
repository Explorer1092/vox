<#import "../../../module.ftl" as module>
<@module.page
title="专辑文章管理"
leftMenu="专辑文章管理"
pageJsFile={"albumnewslist" : "public/script/basic/album/albumnewslist"}
pageJs=["albumnewslist"]
>

<div class="op-wrapper clearfix">
    <span class="title-h1">专辑文章管理</span>
</div>
<div class="op-wrapper marTop clearfix" style="margin-bottom: 10px;">
    <div class="item time-widAuto">
        <p>审核状态：</p>
        <select id="work_flow_status" class="v-select" style="width: 100px;">
            <option value="">全部</option>
            <option value="init" <#if workFlowStatus=='init'>selected="selected"</#if>>新建</option>
            <option value="lv1" <#if workFlowStatus=='lv1'>selected="selected"</#if>>待审核</option>
            <option value="processed" <#if workFlowStatus=='processed'>selected="selected"</#if>>通过</option>
            <option value="rejected" <#if workFlowStatus=='rejected'>selected="selected"</#if>>驳回</option>
        </select>
    </div>
    <div class="item time-widAuto marLeft15">
        <p>内容类型：</p>
        <div>
            <select id="content_type" title="" class="v-select" style="width: 110px;">
                <option value="">全部</option>
                <#list contentTypeList as type>
                    <option value="${type.name!''}"
                            <#if contentType?? && contentType == type.name>selected="selected"</#if>>${type.desc!''}</option>
                </#list>
            </select>
        </div>
    </div>
    <div class="matManage-box">
        <div class="input-control">
            <p>标题</p>
            <input id="newsTitle" placeholder="标题名称" maxlength="100" title="" class="item" style="width: 300px"
                   value="${newsTitle!''}">
            <input type="hidden" name="page" value="${pageIndex!1}" id="pageIndex">
            <a id="news_search" class="blue-btn" href="javascript:void(0)">查询</a>
        </div>
        <a name="edit_news" data-newsId="" class="blue-btn" href="javascript:void (0);"
           style="margin-top:27px;"
           <#if mizarUserAlbumList?? && mizarUserAlbumList?size gt 0>data-album_list="has"</#if>>新建文章</a>
    </div>
</div>
<table class="data-table">
    <thead>
    <tr>
        <th style="width: 120px;">标题</th>
        <th style="width: 50px;">内容<br/>类型</th>
        <th style="width: 100px;">所属专辑</th>
        <th style="width: 50px;">审核<br/>状态</th>
        <th style="width: 50px;">上线<br/>状态</th>
        <th style="width: auto;">驳回原因</th>
        <th style="width: 25%">操作</th>
    </tr>
    </thead>
    <tbody>
        <#if mizarUserNewsList?? && mizarUserNewsList?size gt 0>
            <#list mizarUserNewsList as jxtNews>
            <tr>
                <td>${jxtNews.title!''}</td>
                <td>${jxtNews.contentType!''}</td>
                <td>${jxtNews.albumName!''}</td>
                <td>${jxtNews.workFlowStatus!''}</td>
                <td>${jxtNews.onlineStatus!''}</td>
                <td>${jxtNews.rejectReason!''}</td>
                <td>
                    <#if jxtNews.workFlowStatus =="新建" || jxtNews.workFlowStatus =="驳回">
                        <a data-newsId="${jxtNews.id!''}" class="op-btn add_work_flow"
                           href="javascript:void(0);">提交审核</a>
                        <a name="edit_news" data-newsId="${jxtNews.id!''}" class="op-btn"
                           href="javascript:void(0);"
                           <#if mizarUserAlbumList?? && mizarUserAlbumList?size gt 0>data-album_list="has"</#if>>编辑</a>
                    </#if>
                    <a class="op-btn JS-viewBtn" href="javascript:void(0);" data-id="${jxtNews.id!''}">预览</a>
                </td>
            </tr>
            </#list>
        <#else>
        <tr>
            <td>暂无文章数据</td>
        </tr>
        </#if>
    </tbody>
</table>
<div id="paginator" pageIndex="${(pageIndex!1)}" class="paginator clearfix"
     totalPage="<#if totalPage??>${totalPage}<#else>1</#if>"></div>
</@module.page>