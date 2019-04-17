<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='认证老师' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-zoom-in"></i> 认证老师信息</h2>

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
                <table class="table table-striped table-bordered bootstrap-datatable" id="dt_kpi_detail">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 200px;">地区</th>
                        <th class="sorting" style="width: 100px;">学校</th>
                        <th class="sorting" style="width: 100px;">班级</th>
                        <th class="sorting" style="width: 60px;">学科</th>
                        <th class="sorting" style="width: 60px;">学号</th>
                        <th class="sorting" style="width: 60px;">姓名</th>
                        <th class="sorting" style="width: 100px;">邮箱</th>
                        <th class="sorting" style="width: 100px;">电话</th>
                        <th class="sorting" style="width: 100px;">认证时间</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if teachers?has_content>
                            <#list teachers as teacher>
                            <tr class="odd">
                                <td class="center  sorting_1">${teacher["region"]!}</td>
                                <td class="center  sorting_1">${teacher["schoolName"]!}</td>
                                <td class="center  sorting_1">
                                    <#if teacher.clazzList?? && teacher.clazzList?size gt 0>
                                        <#list  teacher.clazzList as clazz>
                                        ${clazz["clazzName"]!''}<br/>
                                        </#list>
                                    </#if>
                                </td>
                                <td class="center  sorting_1">${teacher["subject"]!}</td>
                                <td class="center  sorting_1">${teacher["teacherId"]!}</td>
                                <td class="center  sorting_1">${teacher["teacherName"]!}</td>
                                <td class="center  sorting_1">${teacher["email"]!}</td>
                                <td class="center  sorting_1">${teacher["mobile"]!}</td>
                                <td class="center  sorting_1">${teacher["authtime"]!}</td>
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


    });
</script>
</@layout_default.page>
