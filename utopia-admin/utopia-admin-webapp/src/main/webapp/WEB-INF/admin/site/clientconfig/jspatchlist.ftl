<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='Web manage' page_num=4>
<#--<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>-->
<style>
    span { font: "arial"; }
    .tron {background-color: #f4fff3
    }
    td {
        border-bottom: 1px solid #F4FFF3;
    }
</style>
<div id="main_container" class="span9">
    <@h.head/>

    <hr>

    <div>
        <form id="s_form" action="?" method="post" class="form-horizontal">
            <fieldset>
                <legend>CRM-JSPatch
                    <a href="toeditpage.vpage?type=JSPATCH" type="button" class="btn btn-info" >添加</a>
                    <a href="totestpage.vpage?type=JSPATCH" type="button" class="btn btn-warning">测试</a>
                </legend>
            </fieldset>

        </form>
    </div>
    <#if patchList??>
        <div class="row-fluid">
            <div class="span12">
                <div >
                    <div id="data_table_journal" >
                        <table class="table table-striped table-bordered" style=" white-space:nowrap; overflow:hidden;align=:center;">
                            <tr>
                                <td width="5%">产品ID</td>
                                <td width="10%">包版本号</td>
                                <td width="15%">系统版本</td>
                                <td width="15%">用户ID</td>
                                <td width="15%">手机型号</td>
                                <td width="5%">中小学</td>
                                <td width="5%">是否启用</td>
                                <td width="5%">状态</td>
                                <td>操作</td>
                            </tr>
                            <#list patchList as patch >
                                <tr>
                                    <td style="display:none">${patch.id!}</td>
                                    <td><a href="toeditpage.vpage?type=JSPATCH&id=${patch.id }">${patch.productId!}</a></td>
                                    <td>${patch.apkVer!}</td>
                                    <td>${patch.sysVer!}</td>
                                    <td>${patch.user!}</td>
                                    <td>${patch.model!}</td>
                                    <td>${patch.ktwelve!}</td>
                                    <td>
                                        <#if patch.isOpen>启用
                                        <#else >禁用
                                        </#if>
                                    </td>
                                    <td>
                                        <#if !patch.status?? || patch.status == 'draft'>草稿
                                        <#elseif patch.status == 'published'>已发布
                                        </#if>
                                    </td>
                                    <td>
                                        <#if !patch.status?? || patch.status == 'draft'><a href="topublish.vpage?type=JSPATCH&id=${patch.id }" type="button" class="btn btn-success">发布</a>
                                        <#elseif patch.status == 'published'><a href="todraft.vpage?type=JSPATCH&id=${patch.id }" type="button" class="btn btn-danger">下线</a>
                                        </#if>
                                        <input type="button" value="删除" productId= "${patch.id }" class="btn btn-danger deleteBtn">
                                    </td>
                                </tr>
                            </#list>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </#if>
</div>

<div id="add_dialog" class="modal fade hide" style="width: 60%; left: 40%;">
    <div class="modal-dialog">
        <div class="modal-content" style="padding: 10px 10px;" >


        </div>
    </div>
</div>

<script type="text/javascript">
    $(function() {
        $("table tr:gt(0)").hover(function() {
            $(this).addClass("tron");
        }, function() {
            $(this).removeClass("tron");
        })

        $(".deleteBtn").on("click", function() {
            if (!confirm("确定删除这个配置？")) {
                return false;
            }

            $.post("toremove.vpage", {
                id: $(this).attr("productId"),
                type:"JSPATCH"
            },
                function () {
                    window.location.reload();
                }
            )
        })
    });
</script>

</@layout_default.page>