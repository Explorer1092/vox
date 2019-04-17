<#-- @ftlvariable name="feedbackType" type="java.lang.String" -->
<#-- @ftlvariable name="feedbackTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="startDate" type="java.lang.String" -->
<#-- @ftlvariable name="content" type="java.lang.String" -->
<#-- @ftlvariable name="unmask" type="java.lang.String" -->
<#-- @ftlvariable name="feedbackQuickReplyList" type="java.util.List" -->
<#-- @ftlvariable name="feedbackStateMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
 <#-- @ftlvariable name="feedbackInfoList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="内容查询" page_num=3>
<div class="span9">
    <div>
        <fieldset>
            <legend>内容查询</legend>
        </fieldset>
    </div>
    <div>
        <fieldset><legend>查询结果</legend></fieldset>
        <#if result.lesson??>
            <table class="table table-striped table-bordered" style="font-size: 14px;">
                <thead>
                <tr>
                    <th>LessonID</th>
                    <th>课本名称</th>
                    <th>单元名称</th>
                    <th>Lesson名称</th>
                    <th>请求Json</th>
                </tr>
                </thead>
                <tr>
                    <td style="width: 90px;max-width:90px;">
                        <#if result.lesson??>${result.lesson.id}</#if>
                    </td>
                    <td style="width: 90px;max-width:90px;">
                        <#if result.book??>${result.book.cname}</#if>
                    </td>
                    <td nowrap style="width: 90px;max-width:90px;"><#if result.unit??>${result.unit.cname}</#if></td>
                    <td><div style="width: 100px;max-width:100px;"><#if result.lesson??>${result.lesson.cname}</#if></div></td>
                    <td><div><#if result.flashData??>${result.flashData}</#if></div></td>
                </tr>
            </table>
        <#else>
            无数据
        </#if>

    </div>
</div>

<script type="text/javascript">

    $(function(){

    });

</script>
</@layout_default.page>