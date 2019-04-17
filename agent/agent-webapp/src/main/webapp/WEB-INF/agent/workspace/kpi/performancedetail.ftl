<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='市场数据明细' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-zoom-in"></i> 市场数据明细</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <div class="pull-right">
                    <a class="btn btn-round" href="javascript:window.history.back();">
                        <i class="icon-chevron-left"></i>
                    </a>&nbsp;
                </div>
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable" id="dt_kpi_detail">
                    <thead>
                        <tr>
                            <th class="sorting" style="width: 60px;">日期</th>
                            <th class="sorting" style="width: 200px;">地区</th>
                            <th class="sorting" style="width: 100px;" title="只要系统中建立了账号，就算做新增注册老师。">老师新增注册 <i class="icon-question-sign"></i></th>
                            <th class="sorting" style="width: 100px;" title="1、填写真实姓名；2、绑定手机；3、8名同学完成3次作业或测验。4、3名同学绑定手机。">老师新增认证<i class="icon-question-sign"></i></th>
                            <th class="sorting" style="width: 90px;" >老师当日使用</th>
                            <th class="sorting" style="width: 100px;" title="注册班级内必须至少有一位学生做过一次作业或测验，则此班所有学生都成为新增注册学生。">学生新增注册<i class="icon-question-sign"></i></th>
                            <th class="sorting" style="width: 100px;" title="所在班级老师完成认证，如果某学生累计完成3次作业（与认证老师科目一致），则此学生成为新增认证学生。">学生新增认证<i class="icon-question-sign"></i></th>
                            <th class="sorting" style="width: 90px;" >学生当日使用</th>
                        </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if performancedetailList?has_content>
                            <#list performancedetailList as detail>
                                <tr class="odd">
                                    <td class="center  sorting_1">${detail["date"]}</td>
                                    <td class="center  sorting_1">
                                        <#if detail["type"] == 'region' && detail["code"] gt 0>
                                            <a href="performancedetail.vpage?regionCode=${(detail["code"])!}&dayCnt=${dayCnt}">${detail["name"]!}</a>
                                        <#else>
                                            <#if detail["name"]??>${detail["name"]}<#else>区域未知</#if>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if detail["teacherRegister"]?has_content && detail["teacherRegister"] gt 0>
                                            <#if detail["type"] == 'region'>
                                                <a id="view_reg_teachers_${detail['date']!}_${detail['code']}"  href="viewregteachers.vpage?startDate=${detail['date']}&endDate=${detail['date']}&region=${detail['code']}" target="_blank">${detail["teacherRegister"]!}</a>
                                            <#elseif detail["type"] == 'school'>
                                                <a id="view_reg_teachers_${detail['date']!}_${detail['code']}"  href="viewregteachers.vpage?startDate=${detail['date']}&endDate=${detail['date']}&school=${detail['code']}" target="_blank">${detail["teacherRegister"]!}</a>
                                            <#else>
                                                ${detail["teacherRegister"]}
                                            </#if>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if detail["teacherAuth"]?has_content && detail["teacherAuth"] gt 0>
                                            <#if detail["type"] == 'region'>
                                                <a id="view_auth_teachers_${detail['date']!}_${detail['code']}"  href="viewauthteachers.vpage?startDate=${detail['date']}&endDate=${detail['date']}&region=${detail['code']}" target="_blank">${detail["teacherAuth"]!}</a>
                                            <#elseif detail["type"] == 'school'>
                                                <a id="view_auth_teachers_${detail['date']!}_${detail['code']}"  href="viewauthteachers.vpage?startDate=${detail['date']}&endDate=${detail['date']}&school=${detail['code']}" target="_blank">${detail["teacherAuth"]!}</a>
                                            <#else>
                                                ${detail["teacherAuth"]}
                                            </#if>
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if detail["teacherActive"]?has_content && detail["teacherActive"] gt 0>
                                            ${detail["teacherActive"]}
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if detail["studentRegister"]?has_content && detail["studentRegister"] gt 0>
                                            ${detail["studentRegister"]}
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if  detail["studentAuth"]?has_content && detail["studentAuth"] gt 0>
                                            ${detail["studentAuth"]}
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                        <#if detail["studentActive"]?has_content && detail["studentActive"] gt 0>
                                            ${detail["studentActive"]}
                                        </#if>
                                    </td>
                                   </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <#if ((dayCnt!7) + 7) lte 84>
                    <div class="pagination pagination-centered">
                        <ul>
                            <li>
                                <a href="performancedetail.vpage?dayCnt=${(dayCnt!7) + 7}"><i class="icon-arrow-down"></i>查看${(dayCnt!7) + 7}天内数据</a>
                            </li>
                        </ul>
                    </div>
                </#if>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var sEmptyTable = "";
        if (${performancedetailList?size} == 0) {
            sEmptyTable = ${dayCnt!7} + "天内暂无数据";
        }
        $('#dt_kpi_detail').dataTable({
            "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
            "sPaginationType": "bootstrap",
            "aaSorting": [[0,'desc']],
            "oLanguage": {
                "sProcessing": "正在加载中......",
                "sLengthMenu": "每页显示 _MENU_ 条记录",
                "sZeroRecords": "对不起，查询不到相关数据！",
                "sEmptyTable": sEmptyTable,
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

//        $("a[id^='view_auth_teachers_']").live('click',function(){
//            var id = $(this).attr("id").substring("view_auth_teachers_".length);
//            var date = id.substr(0, 8);
//            var region = id.substr(9, 6);
//            $.post('viewauthteachers.vpage',{
//                date: date,
//                region: parseInt(region)
//            },function(data){
//                if(!data.success){
//                    alert(data.info);
//                }else{
//                    $(window.location).attr('href', 'index.vpage');
//                }
//            });
//        });

    });
</script>
</@layout_default.page>
