<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="青苗乐园" page_num=24>
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

    .table {
        width: 100%;
        margin-bottom: 0px;
    }

</style>

<span class="span9" style="font-size: 14px">
    <#include '../userinfotitle.ftl' />
    <form class="form-horizontal" action="/equator/newwonderland/sapling/letterproanswer.vpage" method="post">
        <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
        </#if>
        <ul class="inline selectbtn">
            学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus"
                        placeholder="输入学生ID" onkeyup="value=value.replace(/[^\d]/g,'')"/>
            树木id：<input type="text" id="saplingCommId" name="saplingCommId" value="${saplingCommId!''}" autofocus="autofocus"
                        placeholder="输入树木ID"/>
            <input type="submit" value="查询" class="btn btn-primary"/>
        </ul>
    </form>
    <div class="panel panel-info" style="width: 80%; display: inline-block;vertical-align: top;">
        <table class="table table-bordered">
            <div class="panel-heading">
                <h4 class="panel-title">
                    给好友去信的问题及答案
                </h4>
            </div>
            <tr>
                <th style="text-align:center;vertical-align:middle;">信id</th>
                <th style="text-align:center;vertical-align:middle;">阶段</th>
                <th style="text-align:center;vertical-align:middle;">问题id</th>
                <th style="text-align:center;vertical-align:middle;">问题内容</th>
                <th style="text-align:center;vertical-align:middle;">答案</th>
                <th style="text-align:center;vertical-align:middle;">是否随机答案</th>
                <th style="text-align:center;vertical-align:middle;">随机答案</th>
            </tr>
            <tbody id="tbody">
                <#if letterProAnswer??>
                    <#list letterProAnswer as letter>
                        <tr>
                            <td style="text-align:center;vertical-align:middle;">${letter.letterId?default("null")}</td>
                            <td style="text-align:center;vertical-align:middle;">${letter.stage?default(0)} </td>
                            <td style="text-align:center;vertical-align:middle;">${letter.proId?default("null")}</td>
                            <td style="text-align:center;vertical-align:middle;">${letter.proContent?default("null")}</td>
                            <td style="text-align:center;vertical-align:middle;">${letter.answerContent?default("null")}</td>
                            <td style="text-align:center;vertical-align:middle;">${letter.randomFlag?string('是','否')}</td>
                            <td style="text-align:center;vertical-align:middle;"><#if letter.randomFlag>${letter.randomAnswer?default('')}</#if></td>
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="6" style="text-align:center;vertical-align:middle;">暂无</td>
                    </tr>
                </#if>
            </tbody>
        </table>
    </div>
</span>
</@layout_default.page>