<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='地域字典表设置' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>地区字典表维护</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right">
                    <a class="btn btn-info" href="index.vpage">
                        <i class="icon-search icon-white"></i>
                        查看全部
                    </a>&nbsp;
                    <a id="add_dict" class="btn btn-success" href="addregiondict.vpage">
                        <i class="icon-plus icon-white"></i>
                        添加
                    </a>
                    &nbsp;
                </div>
            </#if>
        </div>

        <div class="box-content">

            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper">

                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable"
                       id="DataTables_Table_0">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 70px;">地区编码</th>
                        <th class="sorting" style="width: 100px;">地区名称</th>
                        <th class="sorting" style="width: 80px;">直营/代理</th>
                        <th class="sorting" style="width: 120px;">城市类别（A/B/C）</th>
                        <th class="sorting" style="width: 100px;">主城/非主城区</th>
                        <th class="sorting" style="width: 80px;">低渗/高渗</th>
                        <th class="sorting" style="width: 80px;">小学/中学</th>
                        <th class="sorting" style="width: 80px;">新增认证目标</th>
                        <th class="sorting" style="width: 80px;">三月月活目标</th>
                        <th class="sorting" style="width: 80px;">四月月活目标</th>
                        <th class="sorting" style="width: 80px;">五月月活目标</th>
                        <th class="sorting" style="width: 80px;">六月月活目标</th>
                        <th class="sorting" style="width: 80px;">双科认证目标</th>
                        <#--<th class="sorting" style="width: 80px;">排除毕业班的学生数</th>-->
                        <th class="sorting" style="width: 80px;">1~2年级数学认证目标数</th>
                        <th class="sorting" style="width: 145px;">操作</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if regionList??>
                            <#list regionList as r>
                            <tr class="odd">
                                <td id="region_${r.id}" class="center">${r.regionCode!}</td>
                                <td id="region_${r.id}" class="center">${r.regionName!}</td>
                                <td id="model_${r.id}" class="center">${r.cityModel!'--'}</td>
                                <td id="level_${r.id}" class="center">${r.cityLevel!'--'}</td>
                                <td id="maincity_${r.id}" class="center">${r.springMainCity!'--'}</td>
                                <td id="settle_${r.id}" class="center">${r.citySettlement!'--'}</td>
                                <td id="stu_${r.id}" class="center">${r.marketStuLevel!'--'}</td>
                                <td id="add_${r.id}" class="center">${r.addBudget!0}</td>
                                <td id="mar_${r.id}" class="center">${r.marBudget!0}</td>
                                <td id="apr_${r.id}" class="center">${r.aprBudget!0}</td>
                                <td id="may_${r.id}" class="center">${r.mayBudget!0}</td>
                                <td id="jun_${r.id}" class="center">${r.junBudget!0}</td>
                                <td id="dsa_${r.id}" class="center">${r.doubleSubjectBudget!0}</td>
                                <#--<td id="expsix_${r.id}" class="center">${r.stuNumExpSix!0}</td>-->
                                <td id="mathAdd_${r.id}" class="center">${r.gradeMathAddBudget!0}</td>
                                <#if requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCountryManager()>
                                    <td class="center ">
                                        <a id="edit_${r.id}" class="btn btn-info"
                                           href="addregiondict.vpage?dictId=${r.id}">
                                            <i class="icon-edit icon-white"></i>
                                            编 辑
                                        </a>
                                        <a id="delete_${r.id}" class="btn btn-danger" href="javascript:void(0);">
                                            <i class="icon-trash icon-white"></i>
                                            删 除
                                        </a>
                                    </td>
                                </#if>
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
    $(function () {
        $("a[id^='delete_").live('click', function () {
            var id=$(this).attr("id").substring("delete_".length);
            if (confirm("是否确认删除该条字典表数据？")) {
                $.post('deleteregiondict.vpage', {
                    dictId:id
                }, function (data) {
                    if (data.success) {
                        alert("删除成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

    });

</script>
</@layout_default.page>
