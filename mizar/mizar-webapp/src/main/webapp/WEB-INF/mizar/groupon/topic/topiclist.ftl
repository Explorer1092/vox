<#import "../../module.ftl" as module>
<@module.page
title="专题管理"
pageJsFile={"siteJs" : "public/script/basic/topic"}
pageJs=["siteJs"]
leftMenu="专题管理"
>
<div class="op-wrapper clearfix">
    <form id="filter-form" action="/groupon/topic/index.vpage" method="get">
        <input id="page" name="page" type="hidden" value="${pageNum!1}">
        <div class="item shop-region" style="width: 120px;">
            <p>专题信息</p>
            <input value="${token!}" name="token" class="v-select" placeholder="专题ID或名称"/>
        </div>
        <div class="item shop-region" style="width: 120px;">
            <p>展示位置</p>
            <select name="pos" class="v-select">
                <option value="">全部</option>
                <option value="BANNER" <#if pos?? && pos == 'BANNER'> selected</#if>>顶部头图</option>
                <option value="LEFT" <#if pos?? && pos == 'LEFT'> selected</#if>>左侧图</option>
                <option value="RIGHT_TOP" <#if pos?? && pos == 'RIGHT_TOP'> selected</#if>>右上图</option>
                <option value="RIGHT_BOTTOM" <#if pos?? && pos == 'RIGHT_BOTTOM'> selected</#if>>右下图</option>
            </select>
        </div>
        <div class="item time-region">
            <p>开始时间</p>
            <div>
                <div class="time-select">
                    <input id="startTime" value="${start!}" name="start" autocomplete="off" class="v-select"  style="width: 170px;" />
                    <div style="margin:0 5px;line-height:30px;">至</div>
                    <input id="endTime" value="${end!}" name="end" autocomplete="off" class="v-select"  style="width: 170px;"/>
                </div>
            </div>
        </div>
        <div class="item time-region" style="width: auto;">
            <p>结束时间</p>
            <div>
                <div class="time-select">
                    <input id="endStart" value="${endStart!}" name="endStart" autocomplete="off" class="v-select" style="width: 170px;" />
                    <div style="margin:0 5px;line-height:30px;">至</div>
                    <input id="endEnd" value="${endEnd!}" name="endEnd" autocomplete="off" class="v-select"  style="width: 170px;"/>
                </div>
                <a id="js-filter" class="blue-btn submit-search" href="javascript:void(0)" style="margin-left:20px;">搜索</a>
            </div>
        </div>
        <#--<div class="item" style="width:auto;margin-right:0;">-->
            <#--<p style="color:transparent;">.</p>-->
            <#--<a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>-->
        <#--</div>-->
    </form>
    <div class="item" style="width:auto; float: right;">
        <p style="color:transparent;">.</p>
        <a class="blue-btn" href="detail.vpage">新增专题</a>
    </div>
</div>

<table class="data-table one-page displayed">
    <thead>
    <tr>
        <th>专题名称</th>
        <th style="width: 90px;">专题类型</th>
        <th>展示位置</th>
        <th style="width: 65px;">优先级</th>
        <th>开始时间</th>
        <th>结束时间</th>
        <th style="width: 50px;">状态</th>
        <th style="width:100px;">操作</th>
    </tr>
    </thead>
    <tbody>
    <#if topicList?? && topicList?size gt 0>
        <#list topicList as topic>
        <tr>
            <td><i class="id-tag" title="${topic.id!}">ID</i>${topic.name!''}</td>
            <td><#if topic.type??><#if topic.type=='to_detail'>商品专题<#elseif  topic.type=='outer_url'>链接专题</#if></#if></td>
            <td>
            <#if topic.position??>
                <#if topic.position == 'BANNER'>顶部头图
                <#elseif topic.position == 'LEFT'>左侧图
                <#elseif topic.position == 'RIGHT_TOP'>右上图
                <#elseif topic.position == 'RIGHT_BOTTOM'>右下图
              </#if>
            </#if>
            </td>
            <td>${topic.orderIndex!''}</td>
            <td><#if topic.startTime??>${topic.startTime?string('yyyy-MM-dd hh:mm:ss')}</#if></td>
            <td><#if topic.endTime??>${topic.endTime?string('yyyy-MM-dd hh:mm:ss')}</#if></td>
            <td><#if topic.status??><#if topic.status=="ONLINE">上线<#else>下线</#if></#if></td>
            <td>
                <#switch topic.status!>
                    <#case "ONLINE">
                        <a class="op-btn" href="/groupon/topic/view.vpage?tid=${topic.id!''}" style="margin-right:0;">查看</a>
                        <a class="op-btn op-status" data-status="OFFLINE" data-tid="${topic.id!''}" href="javascript:void(0);" style="margin-right:0;float:right;">下线</a>
                        <#break />
                    <#case "OFFLINE">
                        <a class="op-btn" href="/groupon/topic/detail.vpage?tid=${topic.id!''}" style="margin-right:0;">编辑</a>
                        <a class="op-btn op-status" data-status="ONLINE" data-tid="${topic.id!''}" href="javascript:void(0);" style="margin-right:0;float:right;">上线</a>
                        <#break />
                    <#default>
                        <#break />
                </#switch>
            </td>
        </tr>
        </#list>
    <#else>
        <tr>
            <td colspan="6" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下没有数据"}</td>
        </tr>
    </#if>
    </tbody>
</table>
<div id="paginator" data-startPage="${pageNum!1}"  data-totalPage="${totalPage!1}" class="paginator clearfix"></div>
</@module.page>