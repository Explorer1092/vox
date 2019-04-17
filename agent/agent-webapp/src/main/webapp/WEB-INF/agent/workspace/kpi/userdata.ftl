<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='新增认证用户数据' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-zoom-in"></i> 新增认证用户数据</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a class="btn btn-round" href="javascript:window.history.back();">
                    <i class="icon-chevron-left"></i>
                </a>&nbsp;
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable" id="dt_kpi_detail">
                    <thead>
                        <tr>
                            <th class="sorting" style="width: 60px;">地区</th>
                            <#if dates??>
                                <#list dates as date>
                                    <th class="sorting" style="width: 100px;">${date!}</th>
                                </#list>
                            </#if>
                            <th class="sorting" style="width: 200px;">市场人员</th>
                        </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if regions?has_content>
                            <#list regions as region>
                            <tr class="odd">
                                <#assign value = region+"_"+dates[0] />
                                <td class="center  sorting_1">
                                    <#if dataMap[value]?? && dataMap[value]["type"] == 'region'>
                                        <a href="index.vpage?regionCode=${dataMap[value]["code"]!}">${dataMap[value]["name"]!}</a>
                                    <#elseif dataMap[value]??>
                                        ${dataMap[value]["name"]!}
                                    </#if>
                                </td>
                                <#list dates as date>
                                    <td class="center  sorting_1">
                                        <#assign value = region+"_"+date />
                                        <#if dataMap[value]?? && dataMap[value]["studentAuth"]??>
                                            ${dataMap[value]["studentAuth"]!}
                                        <#else>
                                            0
                                        </#if>
                                    </td>
                                </#list>
                                <td class="center  sorting_1">
                                    <#if dataMap[value]??>
                                        ${dataMap[value]["user"]!}
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){

        $('#dt_kpi_detail').dataTable({
            "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
            "sPaginationType": "bootstrap",
            "aaSorting": [[0,'desc']],
            "oLanguage": {
                "sProcessing": "正在加载中......",
                "sLengthMenu": "每页显示 _MENU_ 条记录",
                "sZeroRecords": "对不起，查询不到相关数据！",
                "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
                "sInfoEmpty": "当前显示 0 到 0 条，共 0 条记录",
                "sInfoFiltered": "数据表中共为 _MAX_ 条记录",
                "sSearch": "搜索",
                "oPaginate": {
                    "sFirst": "首页",
                    "sPrevious": "上一页",
                    "sNext": "下一页",
                    "sLast": "末页"
                }
            }
        });
    });
</script>
</@layout_default.page>
