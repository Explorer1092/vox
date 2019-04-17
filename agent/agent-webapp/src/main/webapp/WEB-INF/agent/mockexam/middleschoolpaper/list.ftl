<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='中学试卷列表' page_num=3>
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
            <h2><i class="icon-search"></i> 中学试卷列表</h2>
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
                        <label class="control-label" for="selectError3">考试名称</label>
                        <div class="controls">
                            <input type="text" class="input-small" id="paperName" name="paperName" style="width:220px">
                        </div>
                    </div>
                    <div class="control-group span5">
                        <label class="control-label" for="selectError3">试卷类型</label>
                        <div class="controls">
                            <select id="paperTag" name="paperTag">
                                <option value="" <#if !selectType?has_content>selected="selected" </#if>>全部</option>
                                <option value="2">笔试（普通）</option>
                                <option value="1">口语</option>
                                <option value="0">听力</option>
                                <option value="3">听说</option>
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
                        <th class="sorting">考试名称</th>
                        <th class="sorting">试卷类型</th>
                        <th class="sorting">所属区域</th>
                        <th class="sorting">所属教材</th>
                        <th class="sorting"style="width:10%">试卷题数 ↿⇂</th>
                        <th class="sorting"style="width:10%">试卷总分 ↿⇂</th>
                        <th class="sorting"style="width:10%">使用次数 ↿⇂</th>
                        <th class="sorting">使用月份</th>
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
<script type="text/javascript">
    $(function(){

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
                examName:$('#paperName').val(),
                paperId:$('#paperId').val(),
                regionId:$('#regionCode').val(),
                bookId: $("#bookCatalogId option:selected").val(),
                usageMonth: mon,
                paperTag: $("#paperTag option:selected").val().trim(),
            };
            if(post_data.bookName == '' || post_data.bookName == '全部'){
                post_data.bookId = "";
                post_data.bookName = '';
            }
            $.ajax({
                url: '/middleschool/mockexam/paper/querypage.vpage',
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
                            var booName = "";
                            var mon = "";
                            var dom = '<a target="_blank" href="'+item.paperPreviewUrl+'">'+ '预览' + '</a> &nbsp;&nbsp;';
                            dataList.push([i+1 ||0 ,item.id,item.title||'',item.paperTagText||'',item.regions||'',item.books, item.totalNum,item.totalScore||'', item.examTimes, item.usageMonth, dom])
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

        $(document).on("click","#delRegion",function(){
            $('#region').val("");
            $('#regionCode').val("");
            $("#delRegion").hide();
        });

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

        $(document).on("click","#chooseRegionBtn",function(){
            $('#region').val((selectArr[0]||[]).cityName);
            $('#regionCode').val((selectArr[0] ||[]).key);
            $("#addDepartment_dialog").modal('hide');
            if($('#region').val() != '') {
                $("#delRegion").show();
            }
        });
        $('#searchBookName').on("change",function(){
            getBooks($('#searchBookName').val());
        });
        getBooks($('#searchBookName').val());
    });

    function getBooks(name){
        $.ajax({
            url: '/middleschool/mockexam/paper/books.vpage',
            type: "POST",
            datType: "JSON",
            data: JSON.stringify({p:name}),
            contentType: "application/json",
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
