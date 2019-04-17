<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='库存变动明细' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 库存变动明细</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>

            <div class="pull-right">
                <a class="btn btn-success" href="index.vpage">
                    <i class="icon-plus icon-white"></i>
                    返回
                </a>
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">

                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable" id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;">日期</th>
                        <th class="sorting" style="width: 100px;">操作人</th>
                        <th class="sorting" style="width: 70px;">商品名称</th>
                        <th class="sorting" style="width: 70px;">变动值</th>
                        <th class="sorting" style="width: 70px;">变动原因</th>
                        <th class="sorting" style="width: 70px;">变动前库存</th>
                        <th class="sorting" style="width: 145px;">变动后库存</th>
                    </tr>
                    </thead>

                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if recordList??>
                            <#list recordList as record>
                                <tr class="odd">
                                    <td class="center  sorting_1">${record.createTime!}</td>
                                    <td class="center  sorting_1">${record.userName!}</td>
                                    <td class="center  sorting_1">${record.productName!}</td>
                                    <td class="center  sorting_1">${record.quantityChange!}</td>
                                    <td class="center  sorting_1">${record.comment!}</td>
                                    <td class="center  sorting_1">${record.preQuantity!}</td>
                                    <td class="center  sorting_1">${record.afterQuantity!}</td>
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
