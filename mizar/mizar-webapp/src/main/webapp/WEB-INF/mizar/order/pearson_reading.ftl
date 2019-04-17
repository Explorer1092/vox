<#import "../module.ftl" as module>
<@module.page
title="阅读量查询"
leftMenu="阅读统计"
pageJsFile={"pearson_reading" : "public/script/basic/order/pearson_reading"}
pageJs=["pearson_reading"]
>
    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>
<style>
    a:hover {
        color: #ffffff !important;
    }

    .uploadBox .addBox img {
        width: 150px;
        height: 150px;
    }

    .form-horizontal .controls table th .control-label {
        float: left;
        margin-left: -180px;
        width: 160px;
        text-align: right;
    }

    .form-horizontal .controls table th label {
        text-align: left;
    }

    #colorpickerbox input {
        width: 50px;
    }

    input[type="text"] {
        height: 30px;
    }

    .articleClass a:hover {
        background: red
    }
</style>

<div class="op-wrapper clearfix">
    <span class="title-h1">培生绘本阅读量查询</span>
</div>

<div style="float: left;">
    <div class="item time-region" style="width: auto;">
        <div class="matManage-box">
            <div class="time-select input-control">
                <label style="width:64px;margin-right:5px;">开始时间:</label>
                <input title="" id="startTime" class="v-select" value="${startDate!''}" readonly="readonly"/>
                <label style="width:64px;margin-right:5px;">截止时间:</label>
                <input title="" id="endTime" class="v-select" value="${endDate!''}" readonly="readonly"/>
                <label style="width:60px;margin-left:5px;">绘本名称:</label>
                <input title="" id="book-name" class="v-select" maxlength="50" value="${queryBookName!''}" />
                <input type="hidden" name="page" value="${pageIndex!1}" id="pageIndex">
                <input type="text" hidden value="REN_JIAO" id="publisher_flag"/>
                <a id="detail_search" class="blue-btn" href="javascript:void(0)">查询</a>
            </div>
        </div>
    </div>

    <div>
        <table class="data-table">
            <thead>
            <tr>
                <th>绘本ID</th>
                <th>名称</th>
                <th>阅读量</th>
            </tr>
            </thead>
            <tbody>
                <#if records?? && records?size gt 0>
                    <#list records as record>
                    <tr>
                        <td>${record.bookId!''}</td>
                        <td>${record.name!''}</td>
                        <td>${record.quantity!0}</td>
                    </tr>
                    </#list>
                <#elseif errorInfo??>
                <tr>
                    <td colspan="3" style="color: red;">错误!原因：${errorInfo!'未知原因'}</td>
                </tr>
                <#else>
                <tr>
                    <td colspan="3">暂无阅读数据</td>
                </tr>
                </#if>
            </tbody>
        </table>
        <div id="paginator" pageIndex="${(pageIndex!1)}" class="paginator clearfix"
             totalPages="<#if totalPages??>${totalPages}<#else>1</#if>"></div>
    </div>
</div>
</@module.page>