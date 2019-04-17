<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>学生<a href="../student/studenthomepage.vpage?studentId=${userId!}">${userName!}</a>获得PK武器记录
            </legend>
        </fieldset>
        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr>
                    <td> 武器ID</td>
                    <td> 武器实例ID</td>
                    <td> 武器名称</td>
                    <td> 武器品质</td>
                    <td> 获得日期</td>
                </tr>
                <#if logs?has_content>
                    <#list logs as log>
                        <tr>
                            <td>${log.originalId}</td>
                            <td>${log.equipId}</td>
                            <td>${log.equipName}</td>
                            <td>
                                <#if log.quality??>
                                    <#if log.quality == 1>
                                        白
                                    <#elseif log.quality == 2>
                                        绿
                                    <#elseif log.quality == 3>
                                        蓝
                                    <#elseif log.quality == 4>
                                        紫
                                    <#elseif log.quality == 5>
                                        金
                                    </#if>
                                </#if>
                            </td>
                            <td>
                                <#if log.date??>
                                    ${log.date?string("yyyy-MM-dd HH:mm:ss")}
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </div>
</div>
</@layout_default.page>