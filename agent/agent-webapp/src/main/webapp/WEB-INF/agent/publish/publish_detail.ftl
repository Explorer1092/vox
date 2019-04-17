<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='数据详情' page_num=page_num>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>数据详情</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if data?has_content && data.allowDownload?has_content && data.allowDownload>
                <div class="pull-right">
                    <a class="btn btn-success exportBtn" href="publish_export.vpage?id=${data.publishId!''}">
                        <i class="icon-plus icon-white"></i>
                        原样导出
                    </a>
                </div>
            </#if>
        </div>
        <div class="box-content">
            <div class="publish_con" style="line-height: 2.5">
                <span style="margin-right: 10px;">标题：${data.title!''}</span>
                <span style="margin-right: 10px;">更新时间：${data.updateTime!''}</span>
                <span style="margin-right: 10px;">发布人：${data.operatorName!''}</span>
            </div>
            <div class="publish_data" style="max-height: 500px;overflow: scroll;">
                <div class="dataInfo" style="line-height: 2.5"></div>
                <table class="table table-striped table-bordered bootstrap-datatable">
                    <thead>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
            <div class="publish_comment">
                <p>备注说明：</p>
                <div>${data.comment!''}</div>
            </div>
        </div>
    </div>
</div>
<script type="application/javascript">
$(function () {
    var publishId = getUrlParam('publishId');
    var thead = $('.publish_data table thead'),tbody = $('.publish_data table tbody');
    $.get("publish_data_list.vpage",{'publishId':publishId},function (res) {
        if(res.success){
            var data = res.data;
            $('.dataInfo').text('共'+ data.dataList.length +'行，共'+ data.dataTitleList.length +'列');
            thead.html('');
            tbody.html('');
            var headerString = '';
            data.dataTitleList.forEach(function (val) {
                headerString += '<th style="min-width: 100px;">'+ val +'</th>';
            });
            thead.append("<tr><th style='width: 50px;min-width: auto;'>序号</th>"+ headerString +"</tr>");
            data.dataList.forEach(function (arr,index) {
                var bodyString = '';
                arr.forEach(function (val) {
                    bodyString += '<td>'+ val +'</td>';
                });
                index = index + 1;
                tbody.append("<tr><td>"+ index +"</td>"+ bodyString +"</tr>");
            });
        }else{
            $('.dataInfo').text('');
            // alert(res.info);
        }
    });

});
</script>
</@layout_default.page>

