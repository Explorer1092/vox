<#import "../../module.ftl" as module>
<@module.page
title="用户详情查询"
leftMenu="用户详情查询"
pageJsFile={"orderdetailcount" : "public/script/basic/order/thirdparty/thirdpartyuserinfo"}
pageJs=["orderdetailcount"]
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
    <span class="title-h1">一起作业—${(thirdPartyName!'')}数据查询</span>
</div>

<div style="float: left;">
    <div class="item time-region" style="width: auto;">
        <div class="matManage-box">
            <div class="time-select input-control">
                <input type="hidden" name="page" value="${pageIndex!1}" id="pageIndex">
                <a id="detail_search" class="blue-btn" href="javascript:void(0)">查询</a>
                <a id="download_data" class="blue-btn" href="javascript:void(0)">下载数据</a>
            </div>
        </div>
    </div>

    <div>
        <table class="data-table">
            <thead>
            <tr>
                <th>序号</th>
                <th>手机号</th>
                <th>学生姓名</th>
                <th>学生年龄</th>
                <th>学生年级</th>
                <th>学生地区</th>
                <th>报名时间</th>
                <th>学习设备</th>
            </tr>
            </thead>
            <tbody>
                <#if returnList?? && returnList?size gt 0>
                    <#list returnList as orderDetail>
                    <tr>
                        <td>${orderDetail.index!''}</td>
                        <td>${orderDetail.mobile!''}</td>
                        <td>${orderDetail.childName!''}</td>
                        <td>${orderDetail.childAge!''}</td>
                        <td>${orderDetail.clazzLevel!''}</td>
                        <td>${orderDetail.region!''}</td>
                        <td>${orderDetail.date!''}</td>
                        <td>${orderDetail.device!''}</td>
                    </tr>
                    </#list>
                <#else>
                <tr>
                    <td>暂无订单数据</td>
                </tr>
                </#if>
            </tbody>
        </table>
        <div id="paginator" pageIndex="${(pageIndex!1)}" class="paginator clearfix"
             totalPages="<#if totalPages??>${totalPages}<#else>1</#if>"></div>
    </div>
</div>
</@module.page>