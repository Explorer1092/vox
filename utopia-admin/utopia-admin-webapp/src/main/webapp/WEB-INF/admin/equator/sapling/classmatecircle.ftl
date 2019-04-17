<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="青苗乐园" page_num=24>
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
    <form class="form-horizontal" action="/equator/newwonderland/sapling/classmatecircle.vpage" method="post">
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
    <#if clazzCircleList??>
        <div class="panel panel-info" style="width: 70%;display: inline-block;vertical-align: top;">
            <table class="table table-bordered">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        同班同学圈
                    </h4>
                </div>
                <tr>
                    <th style="text-align:center;vertical-align:middle;">序号</th>
                    <th style="text-align:center;vertical-align:middle;">学生id</th>
                    <th style="text-align:center;vertical-align:middle;">学生姓名</th>
                    <th style="text-align:center;vertical-align:middle;">树苗状态</th>
                    <th style="text-align:center;vertical-align:middle;">本周被照顾次数</th>
                </tr>
                <tbody id="tbody">
                    <#if (clazzCircleList?size>0)>
                        <#list clazzCircleList as classmate>
                            <#if classmate.studentId==studentId>
                                <tr style="color: red">
                            <#else >
                                <tr>
                            </#if>
                                <td style="text-align:center;vertical-align:middle;">
                                    ${classmate_index+1}
                                </td>
                                <td style="text-align:center;vertical-align:middle;">
                                    ${classmate.studentId?default('null')}
                                </td>
                                <td style="text-align:center;vertical-align:middle;">
                                    <img src="${(classmate.imgUrl)!}"alt="头像" width="40px" height="40px"></br>
                                    ${classmate.realname?default('null')}
                                </td>

                                <td style="text-align:center;vertical-align:middle;">
                                    <#if classmate.status??>
                                        <#if classmate.status== 'seedBefore'>
                                            无树
                                        <#elseif classmate.status== 'matching'>
                                            匹配中
                                        <#elseif classmate.status =='growing'>
                                            有树
                                        </#if>
                                    </#if>
                                </td>
                                <td style="text-align:center;vertical-align:middle;">
                                    ${classmate.lookafterNum?default(0)}
                                </td>

                            </tr>
                        </#list>
                    <#else>
                        <td colspan="5" style="text-align:center;vertical-align:middle;">暂无</td>
                    </#if>
                </tbody>
            </table>

        </div>
    </#if>


</span>
</@layout_default.page>