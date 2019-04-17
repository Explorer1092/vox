<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='管理员后台任务' page_num=5>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 2016春季市场KPI目标管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
            <#--<#if requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCountryManager()>-->
                <form action="exportconfigexcel.vpage" method="post">
                    <button id="export_btn" class="btn btn-success" type="submit">
                        <i class="icon-file icon-white"></i>
                        导出Excel
                    </button
                </form>
                <a class="btn btn-info" href="index.vpage">
                    <i class="icon-search icon-white"></i>
                    查看全部
                </a>&nbsp;
                <a class="btn btn-primary" href="detail.vpage?mode=new">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>&nbsp;
            <#--</#if>-->
            </div>
        </div>
        <div class="box-content">
            <#--<form id="query-form" class="form-horizontal" method="post" action="#">-->
                <#--<fieldset>-->
                <#--<div class="control-group span3">-->
                    <#--<label class="control-label" for="focusedInput">用户ID或名称</label>-->
                    <#--<div class="controls">-->
                        <#--<input type="text" class="input-medium" id="userInfo" name="userInfo" <#if userInfo??> value="${userInfo}"</#if>>-->
                    <#--</div>-->
                <#--</div>-->
                <#--<div class="control-group span3">-->
                    <#--<label class="control-label" for="focusedInput">地区编码</label>-->
                    <#--<div class="controls">-->
                        <#--<input type="number" max="999999" min="0" class="input-medium" id="region" name="region" <#if region??> value="${region}"</#if>>-->
                    <#--</div>-->
                <#--</div>-->
                    <#--<a id="kpi-search-btn" class="btn btn-success" href="#">-->
                        <#--<i class="icon-ok icon-white"></i>-->
                        <#--查询-->
                    <#--</a>-->
                <#--</fieldset>-->
            <#--</form>-->
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper span11">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th style="display: none;">用户ID</th>
                        <th class="sorting" width="100px">用户信息</th>
                        <th class="sorting" width="100px">地区信息</th>
                        <th class="sorting" width="150px">结算区间</th>
                        <th class="sorting" width="50px">小学/中学</th>
                        <th title="小学新增认证目标，中学此项无意义">新增认证<i class="icon-question-sign"></i></th>
                        <th title="小学此项为高覆盖结算目标，中学此项为新增认证目标">3月目标<i class="icon-question-sign"></i></th>
                        <th title="小学此项为高覆盖结算目标，中学此项为新增认证目标">4月目标<i class="icon-question-sign"></i></th>
                        <th title="小学此项为高覆盖结算目标，中学此项为新增认证目标">5月目标<i class="icon-question-sign"></i></th>
                        <th title="小学此项为高覆盖结算目标，中学此项为新增认证目标">6月目标<i class="icon-question-sign"></i></th>
                        <th>双科认证</th>
                        <th>1-2年级新增</th>
                        <th>计算系数(%)</th>
                        <#--<#if requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCountryManager()>-->
                            <th style="width: 145px;" >操作 </th>
                        <#--</#if>-->
                    </tr>
                    </thead>
                    <tbody>
                        <#if kpiConfigList??>
                            <#list kpiConfigList as kpiConfig>
                            <tr class="odd">
                                <td style="display: none;" id="userId_${kpiConfig.id!0}" class="center">
                                    ${kpiConfig.userId!''}
                                </td>
                                <td class="center">
                                    ${kpiConfig.userName!''}(${kpiConfig.userId!0})<br>
                                    ${kpiConfig.userAccount!''}<br>
                                    ${kpiConfig.userRole!''}
                                </td>
                                <td class="center">
                                    ${kpiConfig.regionName!''}<br>
                                    ${kpiConfig.regionCode!''}
                                </td>
                                <td class="center">
                                    ${kpiConfig.salaryStartDate?string('yyyy-MM-dd')}<br>
                                     ${kpiConfig.salaryEndDate?string('yyyy-MM-dd')}
                                </td>
                                <td class="center">
                                    ${kpiConfig.marketStuLevel!''}
                                </td>
                                <td class="center">
                                    ${kpiConfig.newAuthTarget!0}
                                </td>
                                <td class="center">
                                    ${kpiConfig.marSlTarget!0}
                                </td>
                                <td class="center">
                                    ${kpiConfig.aprSlTarget!0}
                                </td>
                                <td class="center">
                                    ${kpiConfig.maySlTarget!0}
                                </td>
                                <td class="center">
                                    ${kpiConfig.junSlTarget!0}
                                </td>
                                <td class="center">
                                    ${kpiConfig.slDsaTarget!0}
                                </td>
                                <td class="center">
                                    ${kpiConfig.authGradeMathTarget!0}
                                </td>
                                <td class="center">
                                    ${kpiConfig.userCpaFactor!0}
                                </td>
                                <#--<#if requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCountryManager()>-->
                                    <td class="center ">
                                        <a class="btn btn-info" href="detail.vpage?id=${kpiConfig.id}&mode=edit">
                                            <i class="icon-edit icon-white"></i>
                                            编 辑
                                        </a>
                                        <a id="delete_${kpiConfig.id}" class="btn btn-danger" href="javascript:void(0);">
                                            <i class="icon-trash icon-white"></i>
                                            删 除
                                        </a>
                                    </td>
                                <#--</#if>-->
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
        $('#kpi-search-btn').live('click', function () {
            var userInfo = $('#userInfo').val();
            var region = $('#region').val();
//            if (userInfo == '' && region == 0) {
//                alert("查询条件不能都为空");
//                return;
//            }
            $('#query-form').submit();
        });

        $("a[id^='delete_']").live('click', function () {
            var id = $(this).attr("id").substring("delete_".length);
            if (confirm("是否确认删除该人员的此条KPI预算配置？")) {
                $.post('deleteconfig.vpage', {
                    configId:id
                }, function(data) {
                   if (data.success) {
                       alert("删除成功");
                       var userId = $('#userId_'+id).html().trim();
                       window.location.href='index.vpage?userId='+userId;
                   } else {
                       alert(data.info);
                   }
                });
            }
        });
    });

</script>

</@layout_default.page>