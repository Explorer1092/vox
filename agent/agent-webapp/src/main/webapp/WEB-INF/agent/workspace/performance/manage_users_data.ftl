<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的申请' page_num=1>
<style>
    .active{
        background:#eaeaea
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 业绩统计</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="query_form"  action="manage_users_data.vpage" method="get" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3" style="width:95%;padding-left:5%">
                        <ul class="nav nav-tabs">
                            <li class="tab-list1"><a href="my_data.vpage">我的业绩</a></li>
                            <li class="tab-list1"><a href="manage_users_data.vpage">下属业绩</a></li>
                            <li class="tab-list1"><a href="manage_groups_data.vpage">部门业绩</a></li>
                        </ul>
                    </div>
                </fieldset>
                <fieldset>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">选择月份</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="month" name="month" value="${month!}">
                            <button type="submit" id="search_btn" class="btn btn-success">确定</button>
                        </div>
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button type="button" id="search_btn" class="btn btn-success" onclick="downloadData();">下载</button>
                        </div>
                    </div>
                </fieldset>
            </form>

            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 10%;">月份</th>
                        <th class="sorting" style="width: 10%;">大区</th>
                        <th class="sorting" style="width: 10%;">部门</th>
                        <th class="sorting" style="width: 10%;">负责人</th>
                        <th class="sorting" style="width: 10%;">角色</th>
                        <th class="sorting" style="width: 10%;">类别</th>
                        <th class="sorting" style="width: 10%;">目标</th>
                        <th class="sorting" style="width: 10%;">完成</th>
                        <th class="sorting" style="width: 10%;">完成率</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if dataList??>
                            <#list dataList as item>
                                <tr class="odd tbody01">
                                    <td class="center  sorting_1">${item.month!}</td>
                                    <td class="center  sorting_1">${item.regionGroupName!}</td>
                                    <td class="center  sorting_1">${item.cityGroupName!}</td>
                                    <td class="center  sorting_1">${item.userName!}</td>
                                    <td class="center  sorting_1"><#if item.userRoleType??>${item.userRoleType.roleName!}</#if></td>
                                    <td class="center  sorting_1"><#if item.performanceKpiType??>${item.performanceKpiType.desc!}</#if></td>
                                    <td class="center  sorting_1">${item.budget!}</td>
                                    <td class="center  sorting_1">${item.complete!}</td>
                                    <td class="center  sorting_1">${item.completeRate!}</td>
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
        $("#month").datepicker({
            dateFormat      : 'yymm',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
    });

    function downloadData(){
        var month = $("#month").val();
        window.location.href = "downloadperformance.vpage?type=2&month=" + month;
    }

</script>
</@layout_default.page>
