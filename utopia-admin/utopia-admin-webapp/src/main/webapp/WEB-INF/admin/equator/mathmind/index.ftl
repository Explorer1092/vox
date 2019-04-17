<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="数学思维" page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<style xmlns="http://www.w3.org/1999/html">
    .panel-info {
        border-color: #bce8f1;
    }

    .panel {
        margin-bottom: 10px;
        background-color: #fff;
        border: 1px solid transparent;
        border-radius: 4px;
        -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
        box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
    }

    .panel-info > .panel-heading {
        color: #31708f;
        background-color: #d9edf7;
        border-color: #bce8f1;
    }

    .panel-heading {
        padding: 10px 15px;
        border-bottom: 1px solid transparent;
        border-top-left-radius: 3px;
        border-top-right-radius: 3px;
    }

    .panel-title {
        margin-top: 0;
        margin-bottom: 0;
        font-size: 16px;
        color: inherit;
    }

    .panel-body {
        padding: 15px;
    }

    .btn {
        height: 25px;
        font-size: 6px;
    }

    .table {
        width: 100%;
        margin-bottom: 0px;
    }

</style>

<span class="span9" style="font-size: 14px">
    <#include '../userinfotitle.ftl' />
    <form class="form-horizontal" action="/equator/newwonderland/mathmind/userInfo.vpage" method="post">
        <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
        </#if>
        <ul class="inline selectbtn">
            学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus"
                        placeholder="输入学生ID" onkeyup="value=value.replace(/[^\d]/g,'')"/>
            <input type="submit" value="查询" class="btn btn-primary"/>
        </ul>
    </form>
    <#if lightModuleUnit??>
        <div class="panel panel-info" style="width:70%;display: inline-block;vertical-align: top;">
            <div class="panel-heading">
                <h3 class="panel-title">数学思维进度
                </h3>
            </div>
            <div class="panel-body" ">
                 <p>最新解锁单元：<strong>${lightModuleUnit?default("null")}</strong></p>
                <table class="table table-bordered">
                    <div class="panel-heading">
                        <h4 class="panel-title">已参加的单元列表</h4>
                    </div>
                    <tr>
                        <th style="text-align:center;vertical-align:middle;">洲</th>
                        <th style="text-align:center;vertical-align:middle;">模块</th>
                        <th style="text-align:center;vertical-align:middle;">单元</th>
                        <th style="text-align:center;vertical-align:middle;">是否过关</th>
                        <th style="text-align:center;vertical-align:middle;">获得的奖杯数量</th>
                    </tr>
                    <tbody id="tbody">
                        <#if locationList??>
                            <#list locationList as location>
                                <tr>
                                    <td style="text-align:center;vertical-align:middle;">${location.continent?default("null")}</td>
                                    <td style="text-align:center;vertical-align:middle;">
                                        ${location.module?default("null")}</td>
                                    <td style="text-align:center;vertical-align:middle;">${location.unit?default("null")}</td>
                                    <td style="text-align:center;vertical-align:middle;"><#if location.finished>过关<#else>
                                        未过关</#if> </td>
                                    <td style="text-align:center;vertical-align:middle;">${location.trophy?default(0)} </td>
                                </tr>
                            </#list>
                        <#else>
                            <tr>
                                <td colspan="5" style="text-align:center;vertical-align:middle;">暂无</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="panel panel-info" style="width: 50%;display: inline-block;vertical-align: top;">
            <table class="table table-bordered">
                <div class="panel-heading">
                    <h4 class="panel-title">已解锁的漫画查看情况</h4>
                </div>
                <tr>
                    <th style="text-align:center;vertical-align:middle;">漫画编号</th>
                    <th style="text-align:center;vertical-align:middle;">是否看过</th>
                </tr>
                <tbody id="tbody">
                    <#if cartoonMap?? && (cartoonMap?size>0)>
                        <#list cartoonMap?keys as key>
                            <tr>
                                <td style="text-align:center;vertical-align:middle;">
                                    ${key?default('null')}
                                </td>
                                <td style="text-align:center;vertical-align:middle;">
                                    <#if cartoonMap[key]>已看过<#else>未看过</#if>
                                </td>
                            </tr>
                        </#list>
                    <#else>
                        <td colspan="2" style="text-align:center;vertical-align:middle;">暂无</td>
                    </#if>
                </tbody>
            </table>
        </div>
    </#if>
</span>
</@layout_default.page>