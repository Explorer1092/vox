<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="vitalityLogList" type="java.util.List<com.voxlearning.utopia.admin.data.VitalityMapper>" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>学生<a href="../student/studenthomepage.vpage?studentId=${userId!}">${userName!}</a>PK转职记录
            </legend>
        </fieldset>
        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr>
                    <td> 转职日期</td>
                    <td> 转职方式</td>
                    <td> 转职前职业</td>
                    <td> 转职后职业</td>
                </tr>
                <#if logs?has_content>
                    <#list logs as log>
                        <tr>
                            <td>${log.date?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>
                                <#if log.type??>
                                    <#if log.type == 'FREE'>
                                        免费
                                    <#elseif log.type == 'BEAN'>
                                        学豆
                                    </#if>
                                </#if>
                            </td>
                            <td>
                                <#if log.source??>
                                    <#if log.source == 'WARRIOR'>
                                        勇士
                                    <#elseif log.source == 'SAGE'>
                                        智者
                                    <#elseif log.source == 'BARD'>
                                        奇才
                                    </#if>
                                </#if>
                            </td>
                            <td>
                                <#if log.target??>
                                    <#if log.target == 'WARRIOR'>
                                        勇士
                                    <#elseif log.target == 'SAGE'>
                                        智者
                                    <#elseif log.target == 'BARD'>
                                        奇才
                                    </#if>
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