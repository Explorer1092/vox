<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='学校字典表设置' page_num=6>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在删除，请等待……</p>
</div>
<div class="row-fluid sortable ui-sortable">
    <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>出错啦！ ${error!}</strong>
        </div>
    </#if>
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 市场结算指标</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right">
                <#-- <a id="add_dict" class="btn btn-success" href="updateSchoolDictInfo.vpage">
                     <i class="icon-plus icon-white"></i>
                     添加
                 </a>
                 <a href="exportSchoolDictInfo.vpage" class="btn btn-primary">导出市场结算数</a>-->
                    <a href="import_agent_payments.vpage?type=${type!0}" class="btn btn-primary">批量导入结算数</a>
                </div>
            </#if>
        </div>

        <div class="box-content ">

            <form id="paymentsTypeSearch" action="/sysconfig/payments/index.vpage" method="get">
                <label for="regionCode">结算指标类型：</label>
                <select id="paymentsType" name="type" style="width: 100%">
                    <option value="1"  <#if type??&&type==1>selected</#if>>大区经理结算指标</option>
                    <option value="2"  <#if type??&&type==2>selected</#if>>代理结算指标</option>
                </select>
            </form>
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper " role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>

                    <tr>
                        <th class="sorting" style="width: 80px;">部门</th>
                        <th class="sorting" style="width: 90px;">姓名</th>
                        <th class="sorting" style="width: 50px;">帐号</th>
                        <th class="sorting" style="width: 50px;">角色</th>
                        <th class="sorting" style="width: 60px;">结算月份</th>
                        <#if type??&&type==1>
                            <th class="sorting" style="width: 85px;">绩效指标1</th>
                            <th class="sorting" style="width: 85px;">绩效1分数</th>
                            <th class="sorting" style="width: 85px;">绩效指标2</th>
                            <th class="sorting" style="width: 85px;">绩效2分数</th>

                        </#if>
                        <#if type??&&type==2>
                            <th class="sorting" style="width: 75px;">小学市级专场</th>
                            <th class="sorting" style="width: 75px;">小学区级专场</th>
                            <th class="sorting" style="width: 75px;">小学插播组会</th>
                            <th class="sorting" style="width: 75px;">小学本月线索</th>
                            <th class="sorting" style="width: 75px;">中学市级专场</th>
                            <th class="sorting" style="width: 75px;">中学区级专场</th>
                            <th class="sorting" style="width: 75px;">中学插播组会</th>
                            <th class="sorting" style="width: 75px;">中学本月线索</th>
                        </#if>
                        <th class="sorting" style="width: 60px;">操作</th>
                    </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if payments??>
                            <#list payments as p>
                            <tr class="odd">
                                <td class="center">${p.departmentName!''}</td>
                                <td class="center">${p.userName!''}</td>
                                <td class="center">${p.account!''}</td>
                                <td class="center">${p.role!''}</td>
                                <td class="center">${p.settlementMonth!''}</td>
                                <#if type??&&type==1>
                                    <td class="center">${p.indicator1Name!''}</td>
                                    <td class="center">${p.indicator1!''}</td>
                                    <td class="center">${p.indicator2Name!''}</td>
                                    <td class="center">${p.indicator2!''}</td>
                                </#if>
                                <#if type??&&type==2>
                                    <td class="center">${p.cityJuniorMeet!''}</td>
                                    <td class="center">${p.countyJuniorMeet!''}</td>
                                    <td class="center">${p.interCutJuniorMeet!''}</td>
                                    <td class="center">${p.juniorTheMothClue!''}</td>
                                    <td class="center">${p.cityMiddleMeet!''}</td>
                                    <td class="center">${p.countyMiddleMeet!''}</td>
                                    <td class="center">${p.interCutMiddleMeet!''}</td>
                                    <td class="center">${p.middleTheMothClue!''}</td>
                                </#if>
                                <#if requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCountryManager()>
                                    <td class="center ">
                                    <#--  <a id="edit_${p.id}" class="btn btn-info"
                                         href="updateSchoolDictInfo.vpage?dictId=${p.id}">
                                          <i class="icon-edit icon-white"></i>
                                          编 辑
                                      </a>-->
                                        <a id="delete_${p.id}" class="btn btn-danger" href="javascript:void(0);">
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
            if (confirm("是否确认删除该条结算数据？")) {
                $("#loadingDiv").show();
                $.post('remove_agent_payments.vpage', {
                    id: $(this).attr("id").substring("delete_".length)
                }, function (data) {
                    if (data.success) {
                        alert("删除成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                        $("#loadingDiv").hide();
                    }
                });
            }
        });
    });

    $("#paymentsType").live("change",function () {
        $("#paymentsTypeSearch").submit();
    });
</script>
</@layout_default.page>