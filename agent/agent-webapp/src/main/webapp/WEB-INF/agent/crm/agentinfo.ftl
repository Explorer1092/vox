<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM-市场信息' page_num=3>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-eye-open"></i> 地区覆盖信息</h2>
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
                <table class="table table-striped table-bordered" id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                        <tr>
                            <th class="sorting" style="width: 200px;">地区</th>
                            <th class="sorting" style="width: 150px;">市场人员姓名</th>
                            <th class="sorting" style="width: 150px;">覆盖情况</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#if dataList?has_content>
                            <#list dataList as detail>
                                <tr class="odd">
                                    <td class="center  sorting_1">
                                        <#if detail["hasChild"]>
                                           <a href="agentinfodetail.vpage?code=${detail["regionCode"]}">${detail["regionName"]}</a>
                                        <#else>
                                            ${detail["regionName"]}
                                        </#if>
                                    </td>
                                    <td class="center  sorting_1">
                                         ${detail["agentUser"]}
                                    </td>
                                    <td class="center  sorting_1">
                                         ${detail["coverage"]}
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

</script>
</@layout_default.page>
