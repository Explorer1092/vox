<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='业绩详情' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 查询结果</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered" id="dt_kpi_detail">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">日期</th>
                        <th class="sorting" style="width: 60px;">地区</th>
                        <th class="sorting" style="width: 140px;">学生认证</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if myKpiPerformanceDetail?has_content>
                            <#list myKpiPerformanceDetail as detail>
                                <tr class="odd">
                                    <td class="center  sorting_1">${detail["date"]}</td>
                                    <td class="center  sorting_1">
                                        <#if detail["school_name"]?has_content>
                                            ${detail["school_name"]}
                                        <#elseif detail["areaname"]?has_content>
                                            <#if detail["areaname"] == "合计">
                                                合计
                                            <#else>
                                                <a href="myperformancedetail.vpage?regionCode=${(detail["areacode"])!}">${detail["areaname"]}</a>
                                            </#if>
                                        <#elseif detail["cityname"]?has_content>
                                            <#if detail["cityname"] == "合计">
                                                合计
                                            <#else>
                                                <a href="myperformancedetail.vpage?regionCode=${(detail["citycode"])!}"> ${detail["cityname"]}</a>
                                            </#if>
                                        <#else>
                                            <#if detail["proname"]?has_content && detail["proname"] == "合计">
                                               合计
                                            <#else>
                                                <a href="myperformancedetail.vpage?regionCode=${(detail["procode"])!}">${(detail["proname"])!""}</a>
                                            </#if>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if detail["student_auth"]?has_content && detail["student_auth"] gt 0>
                                             ${detail["student_auth"]}
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                        <#else>
                            <tr class="odd">
                                <td class="center  sorting_1" colspan="10">${dayCnt!7}天内暂无数据</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
                <#if ((dayCnt!7) + 7) lte 84>
                    <div class="pagination pagination-centered">
                        <ul>
                            <li>
                                <a href="myperformancedetail.vpage?dayCnt=${(dayCnt!7) + 7}"><i class="icon-arrow-down"></i>查看${(dayCnt!7) + 7}天内数据</a>
                            </li>
                        </ul>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
//    $(function(){
//
//        $('#dt_kpi_detail').dataTable({
//            "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
//            "sPaginationType": "bootstrap",
//            "aaSorting": [[0,'desc']],
//            "oLanguage": {
//                "sProcessing": "正在加载中......",
//                "sLengthMenu": "每页显示 _MENU_ 条记录",
//                "sZeroRecords": "对不起，查询不到相关数据！",
//                "sEmptyTable": "表中无数据存在！",
//                "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
//                "sInfoEmpty": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
//                "sInfoFiltered": "数据表中共为 _MAX_ 条记录",
//                "sSearch": "搜索",
//                "oPaginate": {
//                    "sFirst": "首页",
//                    "sPrevious": "上一页",
//                    "sNext": "下一页",
//                    "sLast": "末页"
//                }
//            }
//        });
//    });
</script>
</@layout_default.page>
