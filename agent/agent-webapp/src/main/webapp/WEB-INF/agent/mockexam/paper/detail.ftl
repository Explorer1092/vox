<#--此功能移植自agent 统考申请-->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='试卷详情' page_num=3>
<style>
    body{
        text-shadow:none;
    }
    #dataTable_length, #dataTable_paginate, .dataTables_filter, .dataTables_info{display: none}
    .row-fluid .form-horizontal .span5 {  margin-left: 0;  }
    select.s_time{
        width: 60px;;
    }
    .radio input[type="radio"], .checkbox input[type="checkbox"]{
        float: left;
        margin-left: -7px;
    }
    .form-horizontal .controls{
        padding-top: 5px;
    }
    .apply_input_time{
        width: 80px;
    }
    .show{
        display: none;}
    .achievement td{margin:0 6px 5px 6px;}
</style>
<div class="row-fluid">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i>试卷基本信息</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
            <div class="box-content">
                <div class="form-horizontal" id="container">
                    <div class="control-group">
                        <label class="control-label">试卷ID</label>
                        <div class="controls">
                            ${paper.paperId!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">试卷名称</label>
                        <div class="controls">
                        ${paper.paperName!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">试卷来源</label>
                        <div class="controls">
                        ${paper.source!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">试卷类型</label>
                        <div class="controls">
                        ${paper.type!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">所属区域</label>
                        <div class="controls">
                        ${paper.region!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">所属学科</label>
                        <div class="controls">
                        ${paper._subject!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">所属教材</label>
                        <div class="controls">
                        ${paper.bookName!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">试卷题数</label>
                        <div class="controls">
                        ${paper.topicNum!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">试卷总分</label>
                        <div class="controls">
                        ${paper.totalScore!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">录入人</label>
                        <div class="controls">
                        ${paper.creator!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">试卷状态</label>
                        <div class="controls">
                        ${paper.status!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">测评次数</label>
                        <div class="controls">
                        ${paper.planTimes!''}
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">是否开放给其它地区</label>
                        <div class="controls">
                            <#if paper.isPublic?? && paper.isPublic == 'Y'>
                            是
                            <#else>
                            否
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="form-horizontal examLogs">
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label">测评记录</label>
                        </div>
                    </fieldset>
                </div>
            </div>
            <div id="schoolinfo_div">
                <table class="table table-bordered table-striped" id="dataTable">
                    <thead id="tableHead">
                    <tr>
                        <th class="sorting">序号</th>
                        <th class="sorting" style="width:10%">测评ID</th>
                        <th class="sorting" style="width:25%">测评名称</th>
                        <th class="sorting" style="width:12%">申请人</th>
                        <th class="sorting">测评级别</th>
                        <th class="sorting">测评形式</th>
                        <th class="sorting">测评学科</th>
                        <th class="sorting">测评创建时间</th>
                        <th class="sorting">测评开始时间</th>
                        <th class="sorting">测评截止时间</th>
                        <th class="sorting">测评状态</th>
                    </tr>
                    </thead>
                    <tbody id="schoolinfo_tbody">
                    </tbody>
                </table>
                <div id="page_id" class="pagination"></div>
            </div>
    </div>
</div>
<script type="text/javascript">
//    function page_id(total,currentPage){
//        $('#page_id').jqPaginator({
//            totalPages: total,
//            visiblePages: 10,
//            currentPage: currentPage || 1,
//            onPageChange: function (num, type) {
//                $('#text').html('当前第' + num + '页');
//                if(type == 'change'){
//                    get_data(num);
//                    _page = num ;
//                }
//            }
//        });
//    }
    function get_data(){
        var subjects = {
            'MATH':'数学',
            'ENGLISH':'英语',
            'CHINESE':'语文'
        };
        $.ajax({
            url: '/mockexam/plan/queryPage.vpage',
            type: "POST",
            datType: "JSON",
            contentType: "application/json",
            data: JSON.stringify({paperId:getUrlParam('paperId'),subject:getUrlParam('subject'),withCreator:false}),
            async: false,
            success: function (res) {
                if (!res.success) {
                    alert(res.info);
                } else {
                    var dataTable = $('#dataTable').dataTable();
                    var data = res.data;
                    var dataList = [];
                    if(data.length == 0 && res.page ==1){
                        $('.examLogs').hide();
                    }else{
                        for(var i = 0;i < data.length;i++){
                            var item = data[i];
                            if(item.status == 'EXAM_PUBLISHED'){
                                for(var j = 0;j < item.paperIds.length;j++){
                                    var v = item.paperIds[j];
                                    dataList.push([i+1||0 ,item.id||0,item.name||0,item.creatorName||'',item._regionLeve||'',item._form||'',item._subject,new Date(item.createDatetime).Format('yyyy.MM.dd hh:mm'),new Date(item.createDatetime).Format('yyyy.MM.dd hh:mm'),new Date(item.createDatetime).Format('yyyy.MM.dd hh:mm'),item._status])
                                }
                            }
                        }
                        dataTable.fnClearTable();
                        dataTable.fnAddData(dataList);
//                        var total = Math.ceil(res.totalSize/10);
//                        if(data.length > 0){
//                            page_id(total,res.page||1)
//                        }
                    }

                }
            }
        });
    }
    get_data();

</script>
</@layout_default.page>
