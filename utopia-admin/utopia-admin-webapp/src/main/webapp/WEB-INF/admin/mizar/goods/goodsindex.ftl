<#import "../../layout_default.ftl" as layout_default />
<#import "../pager.ftl" as pager />
<@layout_default.page page_title="Mizar Manager" page_num=17>
<div id="main_container" class="span9">
    <legend>
        <strong>机构管理</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <ul class="inline">
                        <li>
                            机构ID：<input type="text" id="sid" name="sid" value="<#if sid??>${sid}</#if>" placeholder="输入机构ID">
                        </li>
                        <li>
                            课程名称：<input type="text" id="goods" name="goods" value="<#if goods??>${goods}</#if>" placeholder="输入课程名称">
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">
                                <i class="icon-search icon-white"></i> 查  询
                            </button>
                        </li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="text-align: center; width: 100px;">课程名称</th>
                        <th style="text-align: center;">课程标题</th>
                        <th style="text-align: center; width: 400px;">课程简介</th>
                        <th style="text-align: center; width: 80px;">课程分类</th>
                        <th style="text-align: center; width: 80px;">课程状态</th>
                        <th style="text-align: center; width: 100px;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if goodsList?? && goodsList?has_content>
                            <#list goodsList as goods>
                            <tr>
                                <td>${goods.goodsName!''}</td>
                                <td><pre>${goods.title!''}</pre></td>
                                <td><pre>${goods.desc!''}</pre></td>
                                <td>${goods.category!''}</td>
                                <td><#if goods.status??>${goods.status.getDesc()!''}<#else>离线</#if></td>
                                <td>
                                    <a href="info.vpage?gid=${goods.id}" class="btn btn-info">
                                        <i class="icon-pencil icon-white"></i> 编  辑
                                    </a>
                                    <#if goods.status?? && (goods.status == 'PENDING' || goods.status == 'OFFLINE' )>
                                        <a title="审核通过" href="javascript:void(0);" class="btn btn-success" id="approve_info" data-gid="${goods.id!}">
                                            <i class="icon-ok icon-white"></i> 审核通过
                                        </a>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr><td colspan="5" style="text-align: center;"><strong>No Data Found</strong></td></tr>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>
</div>
<script>
    $(function () {
        $('#approve_info').on('click', function () {
            if (confirm("是否确认通过审核并上线？")) {
                var gid = $(this).data("gid");
                $.post("approvegoods.vpage", {gid:gid},function(res) {
                    if (res.success) {
                        alert("审核通过");
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                });
            }
        });
    });
</script>
</@layout_default.page>