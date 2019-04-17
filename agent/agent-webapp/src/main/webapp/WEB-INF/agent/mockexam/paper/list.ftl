<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='试卷列表' page_num=3>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<style>
    .ui-autocomplete {  max-height: 200px;  overflow-y: auto;  overflow-x: hidden;  }
    #dataTable_length, #dataTable_paginate, .dataTables_filter, .dataTables_info{display: none}
    .row-fluid .form-horizontal .span5 {  margin-left: 0;  }
    .ui-datepicker-calendar {
        display: none;// 不显示日期面板
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 试卷列表</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>
        <div class="box-content">
            <div class="form-horizontal">
                <fieldset>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">试卷ID</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="paperId" name="paperId" style="width:220px">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">试卷名称</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="paperName" name="paperName" style="width:220px">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">试卷来源</label>
                        <div class="controls">
                            <select id="source" name="source">
                                <option value="" <#if !selectType?has_content>selected="selected" </#if>>请选择</option>
                                <#if source?? && source?size gt 0>
                                    <#list source as item>
                                        <option value="${item.key!}">${item.value!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">所属地区</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="region" name="region" value="" style="width:220px"> <a id="delRegion" style="color:red;" hidden="hidden">✖</a>
                            <input class="input-small" id="regionCode" name="region" value="" type="hidden">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">所属学科</label>
                        <div class="controls">
                            <select id="subject" name="subject">
                                <#if subject?? && subject?size gt 0>
                                    <#list subject as item>
                                        <option value="${item.key}" <#if item.key == 'MATH'>selected</#if>>${item.value!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <div id="teaching_material_div" style="display: inline-block;">
                            <div style="display: inline-block;">
                                <label class="control-label">使用教材</label>
                                <div class="controls">
                                    <select id = "bookCatalogId" >
                                        <option value="" <#if !selectType?has_content>selected="selected" </#if>>全部</option>
                                    </select>
                                </div>
                            </div>
                            <div style="display: inline-block;">
                                <input id="searchBookName" placeholder="输入教材名称快速定位">
                            </div>
                        </div>
                    </div>

                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">试卷类型</label>
                        <div class="controls">
                            <select id="partType" name="partType">
                                <option value="" <#if !selectType?has_content>selected="selected" </#if>>全部</option>
                                <option value="normal">普通</option>
                                <option value="oral">口语</option>
                                <option value="listening">听力</option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">使用月份</label>
                        <div class="controls">
                            <input type="text" id="usageMonth" name ="usageMonth" class="apply_input_time">
                        </div>
                    </div>

                    <div class="control-group span5">
                        <div class="controls">
                            <button id="queryOralBtn" type="button" class="btn btn-primary">查询</button>
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
                        <div class="modal-body">
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
            <div id="topRegisterDataTab" class="dataTables_wrapper">
                <table class="table table-striped table-bordered bootstrap-datatable" id="dataTable">
                    <thead id="tableHead">
                    <tr>
                        <th class="sorting">序号</th>
                        <th class="sorting">试卷ID</th>
                        <th class="sorting">试卷名称</th>
                        <th class="sorting">试卷来源</th>
                        <th class="sorting">所属区域</th>
                        <th class="sorting">所属教材</th>
                        <th class="sorting">试卷类型</th>
                        <th class="sorting"style="width:10%">试卷题数 ↿⇂</th>
                        <th class="sorting"style="width:10%">试卷总分 ↿⇂</th>
                        <th class="sorting"style="width:10%">测评次数 ↿⇂</th>
                        <th class="sorting">测评形式</th>
                        <th class="sorting">是否开放</th>
                        <th class="sorting">试卷创建时间</th>
                        <th class="sorting" style="width:12%">操作</th>
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
<div id="addDepartment_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择所属地区</h4>
            </div>
            <div class="modal-body">
                <div class="row-fluid">
                    <div class="span7">
                        <div id="dialogAreaTree"></div>
                    </div>
                </div>
                <div class="control-group">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="chooseRegionBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
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
<script id="makeup" type="text/html">
    <div class="control-group">
        <div class="row-fluid">
            <div class="span3">
                <label for="">补考ID:</label>
            </div>
            <textarea name="note" id="note" cols="30" rows="10" placeholder="逗号分隔"></textarea>
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
    </div>
</script>
<script type="text/javascript">
    $(function(){

        $("#usageMonth").datepicker({
            dateFormat      : 'yy-mm',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            changeMonth: true,
            changeYear: true,
            onChangeMonthYear: function(stateDate) {
                var month = $("#ui-datepicker-div .ui-datepicker-month option:selected").val();//得到选中的月份值
                var year = $("#ui-datepicker-div .ui-datepicker-year option:selected").val();//得到选中的年份值
                if(parseInt(month) + 1 < 10 ){
                    var temp = '0'+ (parseInt(month) + 1);
                }else{
                    var temp  = parseInt(month) + 1;
                }
                $('#usageMonth').val(year+'-'+temp);//给input赋值，其中要对月值加1才是实际的月份
            }
        });

        function setPartType() {
            var subject = $("#subject").val();
            if(subject != 'ENGLISH') {
                $("#partType").empty();
                $("#partType").prepend("<option value='normal'>普通</option>");   //为Select插入一个Option
            }
        }

        setPartType();

        $("#subject").on("change",function(){
            var subject = $("#subject").val();
            if(subject != 'ENGLISH') {
                $("#partType").empty();
                $("#partType").prepend("<option value='normal'>普通</option>");   //为Select插入一个Option
            } else {
                $("#partType").empty();
                $("#partType").append('<option value="" selected="selected" >全部</option>');   //为Select插入一个Option
                $("#partType").append('<option value="normal" >普通</option>');
                $("#partType").append('<option value="oral" >口语</option>');
                $("#partType").append('<option value="listening" >听力</option>');
            }
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
        var _page = 0;
        var _total = 0;
        var _subjects = {
            'MATH':103,
            'ENGLISH':102,
            'CHINESE':101
        };
        $('#createBtn').click(function() {
            window.location.href = '/mockexam/plan/forcreate.vpage';
        });
        function page_id(total,currentPage){
            $('#page_id').jqPaginator({
                totalPages: total,
                visiblePages: 10,
                currentPage: currentPage || 1,
                onPageChange: function (num, type) {
                    $('#text').html('当前第' + num + '页');
                    if(type == 'change'){
                        get_data(num);
                        _page = num ;
                    }
                }
            });
        }
        function get_data(page){
            var mon = $("#usageMonth").val().trim();
            var post_data ={
                size:10,
                page:page||1,
                paperName:$('#paperName').val(),
                paperId:$('#paperId').val(),
                source:$('#source').val(),
                form:$('#form').val(),
                subject:$('#subject').val(),
                regionCode:$('#regionCode').val(),
                status:$('#status').val(),
                bookId: $("#bookCatalogId option:selected").val(),
                bookName: $("#bookCatalogId option:selected").text().trim(),
                partType: $("#partType option:selected").val().trim(),
                usageMonth: mon,
            };
            if(post_data.bookName == '' || post_data.bookName == '全部'){
                post_data.bookId = "";
                post_data.bookName = '';
            }
            $.ajax({
                url: '/mockexam/paper/querypage.vpage',
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
                            var isPublic = '',
                                    do_Public = '';
                            if(item.isPublic == 'Y'){
                                isPublic = '已开放';
                            }else if(item.isPublic == 'N'){
                                isPublic = '未开放';
                            }
//                            if(item.subject == 'MATH'){
//                                var _paperId = '<a target="_blank" href="'+item.paperPreviewUrl+'">'+item.paperId+'</a> &nbsp;&nbsp;';
//                            }else{
//                                var _paperId = '<a class="un_clicked" href="javascript:void(0)">'+item.paperId+'</a> &nbsp;&nbsp;';
//                            }
                            var dom = "";
//                            dom += '<a target="_blank" href="/mockexam/paper/fordetail.vpage?paperId='+item.paperId+"&subject=" + item.subject +'">'+'查看'+'</a> &nbsp;&nbsp;';
                            if(item.subject == 'MATH' && item.planTimes){
                                dom += '<a style="cursor:pointer" id="report_sure_download" data-id="'+item.paperId +'">'+'题目质量分析'+'</a> &nbsp;&nbsp;';
                            }
                            dom += '<a target="_blank" href="'+item.paperPreviewUrl+'">'+ '试卷预览' + '</a> &nbsp;&nbsp;';
                            //待审核
                            <@apptag.pageElement elementCode="ba3622da4871493c">
                                if(item.source != '通用'){
                                    if(item.isPublic == 'Y'){
                                        dom += '<a style="cursor: pointer" class="publish"  data-id="'+item.paperId+'">'+'关闭'+'</a>'
                                    }else if(item.isPublic == 'N'){
                                        dom += '<a style="cursor: pointer" class="publish" data-id="'+item.paperId+'">'+'开放'+'</a>'
                                    }
                                }
                            </@apptag.pageElement>
                            dataList.push([i+1 ||0 ,item.paperId,item.paperName||'',item.source||'',item.region||'',item.bookName, item.partTypes,item.topicNum||'',item.totalScore||'',item.planTimes, item.planForm, isPublic,new Date(item.createDatetime).Format('yyyy.MM.dd hh:mm')||'',dom])
                        }
                        dataTable.fnClearTable();
                        dataTable.fnAddData(dataList);
                        var total = Math.ceil(res.totalSize/10);
                        _total = total;
                        page_id(_total,_page)
                    }
                }
            });
        }
        $("#queryOralBtn").on("click",function(){
            _page = 0;
            get_data();
        });
        $(document).on("click","#report_sure_download",function () {
            var paperId = $(this).data('id');
            $.ajax({
                url: '/mockexam/plan/querySign.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({paperId:paperId}),
                async: false,
                success:function (res) {
                    if(res.success){
                        var sig = res.data;
                        $.ajax({
                            url: '/mockexam/plan/queryReportIsExist.vpage',
                            type: "POST",
                            datType: "JSON",
                            contentType: "application/json",
                            data: JSON.stringify({paperId:paperId}),
                            async: false,
                            success:function (res1) {
                                if(res1.success){
                                    if(res1.data.isExist){
                                        window.open(_url + "/exam/evaluationReport/subjectquality.vpage?paperId=" + paperId + "&sig=" + sig);
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
        $(document).on("click",".publish",function () {
            var _id = $(this).data('id');
            $.ajax({
                url: '/mockexam/paper/openclose.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({paperId:_id}),
                async: false,
                success:function (res) {
                    if(res.success){
                        layer.alert('操作成功',function () {
                            get_data(_page);
                            layer.close(layer.index);
                        }
                    );
                    }else{
                        layer.alert(res.info)
                    }
                }
            });
        });
        var getAreaFunc = function(item,arr){
            item.forEach(function(i){
                if(i.selected){
                    arr.push({data:i.data,cityName:i.title,key:i.key})
                } else if(i.children && i.children.length>0){
                    getAreaFunc(i.children,arr)
                }
            })
        };
        var selectArr = [];
        var viewDepartment = function () {
            var subDialogTree = $("#dialogAreaTree");
            var selectTree = subDialogTree.fancytree("getTree").rootNode.children;
            selectArr = [];
            getAreaFunc(selectTree,selectArr);

        };

        //初始化负责区域dialog数据
        var initSubDialogData = function(){
            var  subDialogTree = $("#dialogAreaTree");
            $.get("/user/orgconfig/getGroupRegionTree.vpage?",function (res) {
                res.forEach(function (v) {
                    v.selected = false;
                    v.children.forEach(function(t){
                        t.selected = false;
                        t.children.forEach(function (z) {
                            z.selected = false;
                        })
                    })
                });
                subDialogTree.fancytree("destroy");
                subDialogTree.fancytree({
                    extensions: ["filter"],
                    source: res,
                    checkbox: true,
                    selectMode: 1,
                    autoCollapse:true,
                    select:function () {
                        viewDepartment()
                    },
                    init:function(){
                        var tree = $("#dialogAreaTree").fancytree("getTree");
                        tree.visit(function(node){
                            if(node.data.selectFlag){
                                node.setSelected(true);
                            }
                        });
                    }
                });
                viewDepartment();
            });
        };
        $(document).on("focus","#region",function(){
            initSubDialogData();
            $("#addDepartment_dialog").modal('show');

        });
        $(document).on("click",".un_clicked",function(){
            layer.alert('暂不支持小学英语、小学语文学科试卷预览功能');
        });
        $(document).on("click","#chooseRegionBtn",function(){
            $('#region').val((selectArr[0]||[]).cityName);
            $('#regionCode').val((selectArr[0] ||[]).key);
            $("#addDepartment_dialog").modal('hide');
            if($('#region').val() != '') {
                $("#delRegion").show();
            }
        });

        $(document).on("click","#delRegion",function(){
            $('#region').val("");
            $('#regionCode').val("");
            $("#delRegion").hide();
        });

        $(document).on("click",".offline",function () {
            var _id = $(this).data('id');
            $.ajax({
                url: '/mockexam/plan/offline.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({id:_id}),
                async: false,
                success:function (res) {
                    if(res.success){
                        layer.alert('操作成功');
                    }else{
                        layer.alert(res.info)
                    }
                }
            });
        });
        $(document).on("click",".search",function () {
            var _id = $(this).data('id');
            $.ajax({
                url: '/mockexam/plan/offline.vpage',
                type: "POST",
                datType: "JSON",
                contentType: "application/json",
                data: JSON.stringify({id:_id}),
                async: false,
                success:function (res) {
                    if(res.success){
                        layer.alert('操作成功');
                    }else{
                        layer.alert(res.info)
                    }
                }
            });
        });
        var _id = "";
        var _type = "";
        $(document).on("click",".modify",function () {
            _id = $(this).data('id');
            _type = "modify";
            $('.modal-body').html(template("modifyInfo",{res:''}));
            $("#editDepInfo_dialog").modal('show');
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
                            layer.alert('操作成功');
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
                            layer.alert('操作成功');
                        }else{
                            layer.alert(res.info)
                        }
                    }
                });
            }

        });
        var examId = "";
        $(document).on("click",".makeup",function () {
            _type = 'makeup';
            examId = $(this).data('examId');
            _id = $(this).data('id');
            $('.modal-body').html(template("makeup",{res:''}));
            $("#editDepInfo_dialog").modal('show');
        });
        $(document).on("click",".replenish",function () {
            _type = 'replenish';
            examId = $(this).data('examId');
            _id = $(this).data('id');
            $('.modal-body').html(template("replenish",{res:''}));
            $("#editDepInfo_dialog").modal('show');
        });

        $('#subject').on("change",function(){
            getBooks($('#subject').val(),$('#searchBookName').val())
        });
        $('#searchBookName').on("change",function(){
            getBooks($('#subject').val(),$('#searchBookName').val())
        });
        getBooks($('#subject').val(),$('#searchBookName').val());
    });

    function getBooks(subject,name){
        $.ajax({
            url: '/mockexam/refer/books.vpage',
            type: "POST",
            datType: "JSON",
            contentType: "application/json",
            data: JSON.stringify({subject:subject,q:name}),
            async: false,
            success: function (data) {
                if (!data.success) {
                    alert(data.errorDesc);
                } else {
                    if($('#searchBookName').val() == ''){
                        data.data.unshift({id: "", name: "全部"})
                    }
                    $('#bookCatalogId').html(template('books',{res:data.data}));
                    $("#bookCatalogId option:first").prop("selected", 'selected');
                }
            }
        });
    };
</script>
<script type="text/html" id="books">
    <%for(var i = 0;i< res.length;i++){%>
    <option name="bookCatalogId" value = "<%=res[i].id%>" <%if(res[i].name == ""){%>selected<%}%> ><%=res[i].name%></option>
    <%}%>
</script>
</@layout_default.page>
