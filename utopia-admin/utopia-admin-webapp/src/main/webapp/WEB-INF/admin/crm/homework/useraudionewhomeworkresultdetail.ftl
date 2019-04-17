<#--
&lt;#&ndash; @ftlvariable name="stResultDetailList" type="java.util.Map<String, Object>" &ndash;&gt;
&lt;#&ndash; @ftlvariable name="homeworkId" type="java.lang.Long" &ndash;&gt;
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<style>
    blockquote {
        margin: 0;
    }
</style>
<div id="main_container" class="span9">
    <fieldset>
        <legend>
            ${sname}:${sid}的语音作业
        </legend>
    </fieldset>

    <div id="content">

    </div>

</div>


<script id="movieTemplate" type="text/html">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>作业开始时间</th>
            <th>作业完成时间</th>
            <th>作业id</th>
            <th>总分</th>
            <th>详情</th>
        </tr>
        <%for(var i = 0; i < stResultDetailList.length; i++){%>
        <tr>
            <td><%=stResultDetailList[i].startTime%></td>
            <td><%=stResultDetailList[i].finishedTime%></td>
            <td><%=stResultDetailList[i].homeworkId%></td>
            <td><%=stResultDetailList[i].score%></td>
            <td>
                <a target="_blank" href="/crm/homework/usernewhomeworkresultdetail.vpage?userId=<%=sid%>&homeworkId=<%=stResultDetailList[i].homeworkId%>"><%=stResultDetailList[i].homeworkId%>详情</a>
            </td>
        </tr>
        <%}%>
    </table>
</script>

<script>
    $(function () {

        var stResultDetailList =${crmAudioNewhomework};
        var sid = ${sid};
        $("#content").html(template("movieTemplate", {
            stResultDetailList: stResultDetailList,
            sid:sid
        }));
    });
</script>

</@layout_default.page>

-->









<#-- @ftlvariable name="stResultDetailList" type="java.util.Map<String, Object>" -->
<#-- @ftlvariable name="homeworkId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<style>
    blockquote {
        margin: 0;
    }
</style>
<div id="main_container" class="span9">
    <fieldset>
        <legend>
        ${sname}:${sid}的语音作业
        </legend>
    </fieldset>

    <div id="content">

    </div>

    <div>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th style="width: 150px;">作业开始时间</th>
                <th style="width: 150px;">作业完成时间</th>
                <th>作业id</th>
                <th style="width: 300px;">总分</th>
                <th>详情</th>
            </tr>
            <#if crmAudioNewhomework?? && crmAudioNewhomework?size gt 0>
                <#list crmAudioNewhomework as detail>
                    <tr>
                        <td>${detail.startTime}</td>
                        <td>${detail.finishedTime}</td>
                        <td>${(detail.homeworkId)!""}</td>
                        <td>${(detail.score)!""}</td>
                        <td>
                            <a target="_blank"
                               href="/crm/homework/usernewhomeworkresultdetail.vpage?userId=${(sid)!""}&homeworkId=${(detail.homeworkId)!""}">
                                详情</a>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>
</@layout_default.page>