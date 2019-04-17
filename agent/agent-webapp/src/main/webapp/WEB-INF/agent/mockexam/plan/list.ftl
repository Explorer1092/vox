<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='测评列表' page_num=3>
<style>
    #dataTable_length, #dataTable_paginate, .dataTables_filter, .dataTables_info{display: none}
    .row-fluid .form-horizontal .span5 {  margin-left: 0;  }
    #topRegisterDataTab td{word-break: break-all;}
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 测评列表</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <div class="controls">
                    <a id="createBtn" class="btn btn-success" href="javascript:;">
                        <i class="icon-plus icon-white"></i>
                        创建测评
                    </a>
                </div>
            </div>
        </div>
        <div class="box-content">
            <div class="form-horizontal">
                <fieldset>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评ID</label>
                        <div class="controls">
                            <input type="text" onkeyup="this.value=this.value.replace(/[^\d]/g,'');" class="input-small" id="planId" name="planId">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评名称</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="name" name="name">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评级别</label>
                        <div class="controls">
                            <select id="type" name="type">
                                <option value="" <#if !selectType?has_content>selected="selected" </#if>>全部</option>
                                <#if regionLevel??>
                                    <#list regionLevel as item>
                                        <option value="${item.key!}">${item.value!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <#--<div class="control-group span5">-->
                        <#--<label class="control-label" for="selectError3">测评形式</label>-->
                        <#--<div class="controls">-->
                            <#--<select id="form" name="form">-->
                                <#--<option value="" <#if !selectType?has_content>selected="selected" </#if>>请选择</option>-->
                                <#--<#if form??>-->
                                    <#--<#list form as item>-->
                                        <#--<option value="${item.key!}">${item.value!}</option>-->
                                    <#--</#list>-->
                                <#--</#if>-->
                            <#--</select>-->
                        <#--</div>-->
                    <#--</div>-->
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评年级</label>
                        <div class="controls">
                            <select id="grade" name="grade">
                                <option value="" selected="selected">全部</option>
                                <#if form??>
                                    <#list grade as item>
                                        <option value="${item.key!}">${item.value!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评学科</label>
                        <div class="controls">
                            <select id="subject" name="subject">
                                <option value="" <#if !selectType?has_content>selected="selected" </#if>>全部</option>
                                <#if subject??>
                                    <#list subject as item>
                                        <option value="${item.key!}">${item.value!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">申请人</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="contractor" name="contractor" value="">
                            <input class="input-small" id="creatorName" name="creatorName" value="" type="hidden">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">试卷ID</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="paperId" name="paperId" value="">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">起始创建申请时间：</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="createStartTime" name="createStartTime">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">结束创建申请时间：</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="createEndTime" name="createEndTime">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评开始大于时间：</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="planStartTime" name="planStartTime">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评开始小于时间：</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="planEndTime" name="planEndTime">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评截止大于时间：</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="deadlineStartTime" name="deadlineStartTime">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评截止小于时间：</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="deadlineEndTime" name="deadlineEndTime">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">测评状态</label>
                        <div class="controls">
                            <select id="status" name="status">
                                <option value="" <#if !selectType?has_content>selected="selected" </#if>>全部</option>
                                <#if status??>
                                    <#list status as item>
                                        <option value="${item.key!}">${item.value!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span5">
                        <div class="controls">
                            <button id="queryOralBtn" type="button" class="btn btn-primary">查询</button>
                            &nbsp;
                            <#if isAdmin?? && isAdmin == true>
                                <button id="sure_audit" type="button" class="btn btn-primary">批量审核</button>
                            </#if>

                        </div>
                    </div>
                </fieldset>
            </div>
            <div id="editDepInfo_dialog" class="modal fade hide">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                            <h4 class="modal-title changeTitle"></h4>
                        </div>
                        <div class="modal-body render_temp">
                        </div>
                        <div class="modal-footer">
                            <div>
                                <button id="editDepSubmitBtn" type="button" class="btn btn-large btn-primary" data-id="">确定</button>
                                <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="score_dialog" class="modal fade hide">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                            <h4 class="modal-title changeTitle"></h4>
                        </div>
                        <div class="modal-body score_temp">
                        </div>
                        <div class="modal-footer">
                            <div>
                                <button id="sure_download" type="button" class="btn btn-large btn-primary" data-id="">确定</button>
                                <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div id="report_dialog" class="modal fade hide">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                            <h4 class="modal-title changeTitle"></h4>
                        </div>
                        <div class="modal-body report_dialog">
                        </div>
                        <div class="modal-footer">
                            <div>
                                <button id="report_sure_download" type="button" class="btn btn-large btn-primary" data-id="">查看</button>
                                <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <input type="hidden" id="isAdmin" name="isAdmin" value="<#if isAdmin?? && isAdmin == true>true<#else>false</#if>">
            <div id="topRegisterDataTab" class="dataTables_wrapper">

                <table class="table table-striped table-bordered bootstrap-datatable" id="dataTable">
                    <thead id="tableHead">
                        <tr id="trHead" style="width:100%;">
                            <#if isAdmin?? && isAdmin == true>
                                <th class="unSorting" style="width:3%"><input type="checkbox" class="all-select">全选</th>
                            </#if>
                            <th class="sorting" style="width:4%">序号</th>
                            <th class="sorting" style="width:5%">测评ID</th>
                            <th class="sorting" style="width:15%">测评名称</th>
                            <th class="sorting" style="width:14%">试卷ID</th>
                            <th class="sorting" style="width:5%">申请人</th>
                            <th class="sorting" style="width:6%">测评级别</th>
                            <th class="sorting" style="width:6%">测评年级</th>
                            <th class="sorting" style="width:6%">测评学科</th>
                            <th class="sorting" style="width:8%">测评创建时间</th>
                            <th class="sorting" style="width:8%">测评开始时间</th>
                            <th class="sorting" style="width:8%">测评截止时间</th>
                            <th class="sorting" style="width:6%">测评状态</th>
                            <th class="sorting" style="width:15%">操作</th>
                        </tr>
                    </thead>
                    <tbody id="tbody">
                    </tbody>
                </table>
            </div>
            <div id="page_id" class="pagination"></div>
        </div>
    </div>
</div>
<div id="targetHistory_dialog" class="modal fade hide" style="width: 960px;margin-left: -480px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">成绩查询：</h4>
            </div>
            <div class="modal-body render_search">

            </div>
        </div>
    </div>
</div>
<script id="modifyInfo" type="text/html">
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">审核:</label>
            </div>
            <div class="controls">
                <input class="option" name="option" type="radio" value="APPROVE">通过
                <input class="option" name="option" type="radio" value="REJECT">驳回
            </div>
        </div>
    </div>
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">审核备注:</label>
            </div>
            <textarea name="note" id="note" cols="30" rows="10"></textarea>
        </div>
    </div>
</script>
<script id="score_download" type="text/html">
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">成绩单下载:</label>
            </div>
            <div class="controls">
                <input class="sure_down" name="sure_down" type="radio" value="1" checked>系统分成绩
                <input class="sure_down" name="sure_down" type="radio" value="2">人工分成绩
            </div>
        </div>
    </div>
</script>
<script id="report_download" type="text/html">
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">测评报告:</label>
            </div>
            <div class="controls">
                <%for(var i = 0; i< dataList.length ; i++){%>
                <%var data = dataList[i]%>
                    <input class="report_down" name="report_down" type="radio" value="<%=data.regionCode%>" <%if(i == 0){%>checked<%}%>><%=data.regionName%>
                <%}%>
            </div>
        </div>
    </div>
</script>
<script id="render_search" type="text/html">
    <div class="control-group span5">
        <label class="control-label" for="selectError3">当前参考总人数：
            <span id="total_student"></span></label>
        <label class="control-label" for="selectError3">学生ID：</label>
        <div class="controls">
            <input id="studentId" type="number">
            <input type="button" id="search_result" value="搜索">
        </div>
    </div>
    <div id="targetHistory_con_dialog" class="form-horizontal">

    </div>
</script>
<script id="makeup" type="text/html">
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">补考ID:</label>
            </div>
            <textarea name="note" id="note" cols="30" rows="10" placeholder="逗号分隔"></textarea>
        </div>
        <div id="error_info" class="row-fluid" style="display: none">
            <div class="span3">
                <label for="">信息:</label>
            </div>
            <div class="error_info"></div>
        </div>
    </div>
</script>
<script id="replenish" type="text/html">
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">重考ID:</label>
            </div>
            <textarea name="note" id="note" cols="30" rows="10" placeholder="逗号分隔"></textarea>
        </div>
        <div id="error_info" class="row-fluid" style="display: none">
            <div class="span3">
                <label for="">信息:</label>
            </div>
            <div class="error_info"></div>
        </div>
    </div>
</script>

<script id="TargetHistoryDialogTemp" type="text/html">
    <div class="row-fluid" style="max-height: 200px;">
        <div class="areaDetailContent span11">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" colspan="1">学生ID</th>  <#--0 提报 1 自动-->
                        <th class="sorting" colspan="1">学生姓名</th>
                        <th class="sorting" colspan="1">所属区域</th>
                        <th class="sorting" colspan="1">所属学校</th>
                        <th class="sorting" colspan="1">所属班级</th>
                        <th class="sorting" colspan="1">开始测评时间</th>
                        <th class="sorting" colspan="1">测评总时长</th>
                        <th class="sorting" colspan="1">交卷时间</th>
                        <th class="sorting" colspan="1">成绩</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <%if(res){%>
                    <tr>
                        <td class="center sorting_1"><%=res.studentId%></td>
                        <td class="center sorting_1"><%=res.studentName%></td>
                        <td class="center sorting_1"><%=res.region%></td>
                        <td class="center sorting_1"><%=res.school%></td>
                        <td class="center sorting_1"><%=res.className%></td>
                        <td class="center sorting_1"><%=res.startAt%></td>
                        <td class="center sorting_1"><%=res.duration%></td>
                        <td class="center sorting_1"><%=res.submitAt%></td>
                        <td class="center sorting_1"><%=res.score%></td>
                    </tr>
                    <%}%>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var _total = 0;
        var _page = 1;   // 记录当前页， 从 1 开始
        $('#createBtn').click(function() {
            window.location.href = '/mockexam/plan/forcreate.vpage';
        });
        var _origin = location.origin;

        var _url = '';
        if(_origin.indexOf('test') > -1){
            _url = 'http://www.test.17zuoye.net';
        }else if(_origin.indexOf('staging') > -1){
            _url = 'http://www.17zuoye.com';
        }else{
            _url = 'http://www.17zuoye.com';
        }

//        $('#contractor').autocomplete({
//            delay :600,
//            source:function(request,response){
//                if(!request.term||request.term.trim()==''){
//                    return;
//                }
//                $.get("search_user.vpage",{userKey: request.term},function(result){
//                    response( $.map( result.dataList, function( item ) {
//                        return {
//                            label: item.realName,
//                            value: item.realName,
//                            id: item.id
//                        }
//                    }));
//                });
//            },
//            select: function( event, ui ) {
//                $('#creatorName').val(ui.item.id) ;
//                $('#contractor').val(ui.item.realName) ;
//            }
//        });

        $("#createStartTime").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#createEndTime").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
        $("#planStartTime").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
        $("#planEndTime").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
        $("#deadlineStartTime").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
        $("#deadlineEndTime").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        // currentPage  从 1 开始
        function renderPage(totalPage, currentPage){
            $('#page_id').jqPaginator({
                totalPages: totalPage || 1,
                visiblePages: 10,
                currentPage: currentPage||1,
                onPageChange: function (num, type) {
                    $('#text').html('当前第' + num + '页');
                    if(type == 'change'){
                        get_data(num);
                        _page = num;
                    }
                }
            });
        }

        var buttonObj = {
            PLAN_DETAIL: '<a class="go_detail" href="javascript:void(0)">'+'查看'+'</a>&nbsp;&nbsp;&nbsp;',
            PLAN_MODIFY: '<a class="edit_info" href="javascript:void(0)">'+'编辑'+'</a>&nbsp;&nbsp;&nbsp;',
            PLAN_WITHDRAW: '<a class="withdraw" href="javascript:void(0)">'+'撤回'+'</a>&nbsp;&nbsp;&nbsp;',
            PLAN_COPY: '<a class="copy" href="javascript:void(0)">'+'复制'+'</a>&nbsp;&nbsp;&nbsp;',
            PLAN_AUDIT: '<a class="modify" href="javascript:void(0)">'+'审核'+'</a>&nbsp;&nbsp;&nbsp;',
            PLAN_ONLINE: '<a href="javascript:void(0)" class="exam_online" href="javascript:void(0)">'+'上线'+'</a>&nbsp;&nbsp;&nbsp;',
            PLAN_OFFLINE: '<a href="javascript:void(0)" class="offline">'+'下线'+'</a>&nbsp;&nbsp;&nbsp;',
            EXAM_REPLENISH: '<a href="javascript:void(0)" class="replenish">'+'重考'+'</a>&nbsp;&nbsp;&nbsp;',
            EXAM_MAKEUP: '<a href="javascript:void(0)" class="makeup">'+'补考'+'</a>&nbsp;&nbsp;&nbsp;',
            EXAM_SCORE: '<a href="javascript:void(0)" class="search">'+'成绩查询'+'</a>&nbsp;&nbsp;&nbsp;',
            EXAM_SCORE_DONWLOAD: '<a href="javascript:void(0)" class="score_download">'+'成绩单下载'+'</a>&nbsp;&nbsp;&nbsp;',
            EXAM_REPORT_DONWLOAD: '<a href="javascript:void(0)" class="report_download">'+'测评报告'+'</a>&nbsp;&nbsp;&nbsp;',
            PLAN_DOWNLOAD_ATTACHMENT:'<a href="javascript:void(0)" class="attachment_download">'+'下载凭证'+'</a>&nbsp;&nbsp;&nbsp;'
        };
        function get_data(page){
            if(page - 1 < 0){
                page = 1;
            }

            var post_data ={
                size:10,
                page:page - 1,
                planId:$('#planId').val(),
                name:$('#name').val(),
                type:$('#type').val(),
//                form:$('#form').val(),
                grade:$('#grade').val(),
                subject:$('#subject').val(),
                creatorName:$('#contractor').val(),
                paperId:$('#paperId').val(),
                status:$('#status').val(),
                createStartTime:new Date($('#createStartTime').val()).getTime(),
                createEndTime:new Date($('#createEndTime').val()).getTime(),
                planStartTime:new Date($('#planStartTime').val()).getTime(),
                planEndTime:new Date($('#planEndTime').val()).getTime(),
                deadlineStartTime:new Date($('#deadlineStartTime').val()).getTime(),
                deadlineEndTime:new Date($('#deadlineEndTime').val()).getTime()
            };
            $.ajax({
                url: '/mockexam/plan/queryPage.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify(post_data),
                async: false,
                success: function (res) {
                    if (!res.success) {
                        alert(res.info);
                    } else {
                        var dataTable = $('#dataTable').dataTable();
                        var data = res.data;
                        var dataList = [];
                        for(var i = 0;i < data.length;i++){
                            var item = data[i];
                            var dom =  '';
                            var audit =  '';
                                (item.actions || []).forEach(function (v) {
                                    if (v == 'PLAN_DOWNLOAD_ATTACHMENT') {
                                        if (item.status == 'PLAN_AUDITING' && item.attachmentFiles) {
                                            dom += buttonObj[v];
                                        }
                                    } else {
                                        dom += buttonObj[v];
                                    }
                                });
                                dom += '<input type="hidden" data-id ="'+item.id+'" data-examid="'+item.examId+'" data-level="'+item.regionLevel+'"><div style="display: none;">'+ JSON.stringify(item.attachmentFiles) +'</div>';

                                //管理员批量审核添加input
                                var isAdmin = $('#isAdmin').val() == "true";
                                var status = $('#status').val()

                                if(isAdmin && 'PLAN_AUDITING'==status && item.paperType != "NEW"){
                                    audit =  '<td class="center sorting_1">' +
                                            '<input type="checkbox" class="product-apply-item" value="'+ item.id +'">' +
                                            '</td>';
                                }else{
                                    audit = '<td class="center sorting_1"></td>';
                                }
                                var adminTr = [audit,i+1 ||0 ,item.id||0,item.name||'',item.paperIds||'',item.creatorName||'',item._regionLeve||'',item._grade||'',item._subject||'',new Date(item.createDatetime).Format('yyyy.MM.dd hh:mm')||'',new Date(item.startTime).Format('yyyy.MM.dd hh:mm')||'',new Date(item.endTime).Format('yyyy.MM.dd hh:mm')||'',item._status||'',dom];
                                var tr = [i+1 ||0 ,item.id||0,item.name||'',item.paperIds||'',item.creatorName||'',item._regionLeve||'',item._grade||'',item._subject||'',new Date(item.createDatetime).Format('yyyy.MM.dd hh:mm')||'',new Date(item.startTime).Format('yyyy.MM.dd hh:mm')||'',new Date(item.endTime).Format('yyyy.MM.dd hh:mm')||'',item._status||'',dom];
                                if (isAdmin) {
                                    dataList.push(adminTr)
                                } else {
                                    dataList.push(tr)
                                }
                        }
                        dataTable.fnClearTable();
                        dataTable.fnAddData(dataList);
                        var total = Math.ceil(res.totalSize/10);
                        renderPage(total, res.page + 1);
                    }
                }
            });
        }
        //批量审核功能
        $(".product-apply-item").click(function () {
            var allSelect = true;
            $(".product-apply-item").each(function (index, element) {
                if (!$(element).attr("checked")) {
                    allSelect = false;
                }
            });
            if (allSelect) {
                $(".all-select").attr("checked", allSelect);
                $(".all-select").parent("span").addClass("checked");
            } else {
                $(".all-select").attr("checked", false);
                $(".all-select").parent("span").removeClass("checked");
            }
        });

        $(".all-select").click(function () {
            var allSelect = $(this).attr("checked");
            $(".product-apply-item").each(function (index, element) {
                if (allSelect) {
                    $(element).attr("checked", allSelect);
                } else {
                    $(element).attr("checked", false);
                }
            });
        });
        $('#sure_audit').on('click',function () {
            var ids = [];
            $(".product-apply-item").each(function (index, element) {
                if ($(element).attr("checked")) {
                    ids.push($(element).val())
                }
            });
            if(ids.length === 0){
                layer.alert('请选择需要审核的条目');
                return false;
            }
            var post_data =JSON.stringify({
                "ids": ids,        // 测评ID列表
                "option":"APPROVE"           // 审核选项：APPROVE：批准，REJECT：拒绝
            });
            layer.confirm("是否确定通过?",{
                btn: ['确认','取消'] //按钮
            },function () {
                $.ajax({
                    url: '/mockexam/plan/batchAudit.vpage',
                    type: "POST",
                    dataType: "JSON",
                    contentType: 'application/json',
                    data:post_data,
                    async: false,
                    success:function (res) {
                        if (!res.success){
                            layer.alert('审核失败，请联系管理员');
                        } else{
                            layer.alert(res.data,
                                    function () {
                                        get_data(_page);
                                        layer.close(layer.index);
                                        $("#editDepInfo_dialog").modal('hide');
                                    }
                            );

                        }
                    }
                });
            });
        });

        $("#sure_audit").hide();//默认批量审批隐藏
        $("#queryOralBtn").on("click",function(){
            var isAdmin = $("#isAdmin").val();
            var status = $("#status").val();
            if(isAdmin && 'PLAN_AUDITING'==status) {
                $("#sure_audit").show();
            } else {
                $("#sure_audit").hide();
            }
            get_data(1);
        });
        $(document).on("click",".exam_online",function () {
            _id = $(this).siblings('input').data('id');
            $.ajax({
                url: '/mockexam/plan/online.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({id:_id}),
                async: false,
                success:function (res) {
                    if(res.success){
                        layer.alert('操作成功',
                                function () {
                                    get_data(_page);
                                    layer.close(layer.index);
                                    $("#editDepInfo_dialog").modal('hide');
                                }
                        );
                    }else{
                        layer.alert(res.info)
                    }
                }
            });
        });

        $(document).on("click",".score_download",function () {
            examId = $(this).siblings('input').data('examid');
            $('#score_dialog').modal('show');
            $('.score_temp').html(template('score_download',{}))
        });
        var _regionLevel = '';
        $(document).on("click",".report_download",function () {
            id = $(this).siblings('input').data('id');
            examId = $(this).siblings('input').data('examid');
            _regionLevel = ($(this).siblings('input').data('level') || '').toLowerCase();
            $.get('regions.vpage',{id:id},function (res) {
                if(res.success){
                    $('#report_dialog').modal('show');
                    $('.report_dialog').html(template('report_download',{dataList:res.data}))
                }else{
                    layer.alert(res.info)
                }
            })
        });

        //下载附件
        $(document).on("click",".attachment_download",function () {
            console.log($(this).siblings('div').text())
            var attachmentList = JSON.parse($(this).siblings('div').text());
            if(attachmentList.length === 1){
                window.location.href = attachmentList[0].fileUrl;
            }else{
                var dom = '';
                $.each(attachmentList,function (i,item) {
                    dom += '<a style="display: block;text-indent: 20px; " href="' + item.fileUrl +'">'+ item.fileName +'</a>'
                });
                layer.open({
                    title:'下载附件',
                    type: 1,
                    area: ['350px', '150px'], //宽高
                    content: dom
                });
            }
        });
        $(document).on("click","#sure_download",function () {
            var _type = $('input[name=sure_down]:checked').val();
            window.location.href = _url + "/container/loadstudentachievementv2.vpage?exam_id=" + examId + "&type=" + _type;
        });
        $(document).on("click","#report_sure_download",function () {
            var _regionCode = $('input[name=report_down]:checked').val();
            $.ajax({
                url: '/mockexam/plan/querySign.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({examId:examId,regionLevel:_regionLevel,regionCode:_regionCode}),
                async: false,
                success:function (res) {
                    if(res.success){
                        var sig = res.data;
                        $.ajax({
                            url: '/mockexam/plan/queryReportIsExist.vpage',
                            type: "POST",
                            datType: "JSON",
                            contentType: "application/json",
                            data: JSON.stringify({examId:examId,regionLevel:_regionLevel,regionCode:_regionCode}),
                            async: false,
                            success:function (res1) {
                                if(res1.success){
                                    if(res1.data.isExist){
                                        window.open(_url + "/exam/evaluationReport/marketingtestreport.vpage?examId=" + examId + "&regionLevel=" + _regionLevel + "&regionCode=" + _regionCode + "&sig=" + sig);
                                    }else{
                                        layer.alert(res1.data.info);
                                    }
                                }else{
                                    layer.alert(res1.info);
                                }
                            }
                        });
                    }else{
                        layer.alert(res.info);
                    }
                }
            });
        });
        $(document).on("click",".offline",function () {
            _id = $(this).siblings('input').data('id');
            $.ajax({
                url: '/mockexam/plan/offline.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({id:_id}),
                async: false,
                success:function (res) {
                    if(res.success){
                        layer.alert('操作成功',
                                function () {
                                    get_data(_page);
                                    layer.close(layer.index);
                                    $("#editDepInfo_dialog").modal('hide');
                                }
                        );
                    }else{
                        layer.alert(res.info)
                    }
                }
            });
        });
        $(document).on("click",".withdraw",function () {
            _id = $(this).siblings('input').data('id');
            layer.confirm('确定撤回吗？', {
                btn: ['确定','取消'] //按钮
            }, function(){
                $.ajax({
                    url: '/mockexam/plan/withdraw.vpage',
                    type: "POST",
                    datType: "JSON",
                    contentType: "application/json",
                    data: JSON.stringify({id:_id}),
                    async: false,
                    success:function (res) {
                        if(res.success){
                            layer.alert('操作成功',
                                    function () {
                                        get_data(_page);
                                        layer.close(layer.index);
                                        $("#editDepInfo_dialog").modal('hide');
                                    }
                            );
                        }else{
                            layer.alert(res.info)
                        }
                    }
                });
            }, function(){
            });
        });
        var search_result_id ;
        $(document).on("click",".search",function () {
            search_result_id = $(this).siblings('input').data('id');
            $.get('/mockexam/exam/student.vpage?id='+ search_result_id,function (res) {
                if(res.success){
                    $('#total_student').html(res.data);
                }
            });
            $('#targetHistory_dialog').modal('show');
            $('.render_search').html(template("render_search",{}));
        });
        $(document).on("click","#search_result",function () {
            $.ajax({
                url: '/mockexam/exam/scores.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({id:search_result_id , studentId:$('#studentId').val()}),
                async: false,
                success:function (res) {
                    if(res.success && res.data.score){
//                        $("#editDepInfo_dialog").modal('hide');
                        res.data.startAt = new Date(res.data.startAt).Format('yyyy.MM.dd hh:mm');
                        res.data.submitAt = new Date(res.data.submitAt).Format('yyyy.MM.dd hh:mm');
                        $('#targetHistory_con_dialog').html(template("TargetHistoryDialogTemp",{res:res.data}));
                    }else{
                        layer.alert(res.info || '请检查查询条件')
                    }
                }
            });
        });
        var _id = "";
        var _type = "";
        $(document).on("click",".modify",function () {
            _id = $(this).siblings('input').data('id');
            window.location.href = "/mockexam/plan/foraudit.vpage?id="+_id;
        });
        $(document).on("click",".go_detail",function () {
            _id = $(this).siblings('input').data('id');
            window.open('/mockexam/plan/fordetail.vpage?id='+_id);
        });
        $(document).on("click",".edit_info",function () {
            _id = $(this).siblings('input').data('id');
            window.open('/mockexam/plan/forupdate.vpage?id='+_id);
        });
        $(document).on("click",".copy",function () {
            _id = $(this).siblings('input').data('id');
            window.open('/mockexam/plan/forcreate.vpage?id='+_id);
        });
        $(document).on("click","#editDepSubmitBtn",function () {
            $("#editDepInfo_dialog").modal('show');
            if(_type == 'modify'){
                var option = $("input[name='option']:checked").val();
                var note = $("#note").val();
                $.ajax({
                    url: '/mockexam/plan/audit.vpage',
                    type: "POST",
                    datType: "JSON",
                    contentType: "application/json",
                    data: JSON.stringify({id:_id,option:option,note:note}),
                    async: false,
                    success:function (res) {
                        if(res.success){
                            layer.alert('操作成功',
                                    function () {
                                        get_data(_page);
                                        layer.close(layer.index);
                                        $("#editDepInfo_dialog").modal('hide');
                                    }
                            );
                        }else{
                            layer.alert(res.info)
                        }
                    }
                });
            }
            if(_type == 'makeup' || _type == 'replenish'){
                var note = $("#note").val();
                $.ajax({
                    url: '/mockexam/exam/'+ _type +'.vpage',
                    type: "POST",
                    datType: "JSON",
                    contentType: "application/json",
                    data: JSON.stringify({examId:examId,id:_id,studentIds:note}),
                    async: false,
                    success:function (res) {
                        if(res.success){
                            var _info = '';
                            var keys = Object.keys(res.data);
                            for(var i = 0;i < keys.length;i++){
                                _info += (res.data[keys[i]] + '：' + keys[i]  + '</br>');
                            }
                            layer.alert(_info,function () {
                                layer.close(layer.index);
                                $("#editDepInfo_dialog").modal('hide');
                            });
                        }else{
                            $('#error_info').show();
                            $('.error_info').html(res.info);
                        }
                    }
                });
            }

        });
        var examId = "";
        $(document).on("click",".makeup",function () {
            _type = 'makeup';
            examId = $(this).siblings('input').data('examid');
            _id = $(this).siblings('input').data('id');
            $('.render_temp').html(template("makeup",{res:''}));
            $("#editDepInfo_dialog").modal('show');
        });
        $(document).on("click",".replenish",function () {
            _type = 'replenish';
            examId = $(this).siblings('input').data('examid');
            _id = $(this).siblings('input').data('id');
            $('.render_temp').html(template("replenish",{res:''}));
            $("#editDepInfo_dialog").modal('show');
        });
    });
</script>
</@layout_default.page>
