<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='大考统计' page_num=15>
<style type="text/css">
    .control-group{display: inline-block;margin-left: 10px;}
    input,select{width: 100%;}
    .ui-datepicker-calendar {display: none;}
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>大考统计</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">考试月份</label>
                        <div class="controls">
                            <input type="text" id="month" name="month" readonly>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">城市</label>
                        <div class="controls">
                            <input type="text" id="cityName">
                            <input type="hidden" id="cityCode" name="cityCode">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">负责人</label>
                        <div class="controls">
                            <input type="text" id="userName" name="userName" value="">
                            <input type="hidden" id="userId" name="userId" value="" class="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学校</label>
                        <div class="controls">
                            <input type="text" id="school" name="school" value="">
                            <input type="hidden" id="schoolId" name="schoolId">
                        </div>
                    </div>
                    <div class="control-group">
                        <button id="queryOralBtn" type="button" class="btn btn-primary">查询</button>
                        <button id="exportOralBtn" type="button" class="btn btn-primary">导出</button>
                    </div>
                </fieldset>
            </form>
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable"
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 60px;">月份</th>
                        <th class="sorting" style="width: 60px;">城市</th>
                        <th class="sorting" style="width: 60px;">学校ID</th>
                        <th class="sorting" style="width: 160px;">学校名称</th>
                        <th class="sorting" style="width: 60px;">负责人</th>
                        <th class="sorting" style="width: 100px;">年级</th>
                        <th class="sorting" style="width: 60px;">大考3科</th>
                        <th class="sorting" style="width: 60px;">大考6科</th>
                        <th class="sorting" style="width: 50px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<div id="targetHistory_dialog" class="modal fade hide" style="width: 960px;margin-left: -480px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">数据统计：</h4>
            </div>
            <div class="modal-body">
                <div id="targetHistory_con_dialog" class="form-horizontal">

                </div>
            </div>
        </div>
    </div>
</div>
<script>
    template.helper('toFlag', function (val) {
        return val ? '合格' : '不合格';
    });
    template.helper('toPercent', function (val) {
        return val + '%';
    });
    template.helper('hasContact', function (val) {
        return val ? '有' : '无';
    });
    template.helper('examType', function (val) {
        return val === 0 ? '自动' : '提报';
    });
    template.helper('toArtScienceType', function (val) {
        switch(val){
            case 'SCIENCE':
                return '理科';
            case 'ART':
                return '文科';
            default:
                return '--';
        }
    });
</script>
<script id="TargetHistoryDialogTemp" type="text/html">
    <div class="row-fluid" style="max-height: 200px;">
        <p><%=res.schoolName%>-<%=res.gradeDes%></p>
        <div class="areaDetailTitle span3">自动/提报统计：</div>
        <div class="areaDetailContent span6" style="float: none;">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" rowspan="1">月份</th>
                        <th class="sorting" colspan="1">自动/提报</th>  <#--0 自动 1 提报-->
                        <th class="sorting" colspan="1">大考3科</th>
                        <th class="sorting" colspan="1">大考6科</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <%if(res.gradeDetails.agentAutoApplyStatistics){%>
                    <%for(var i = 0; i< res.gradeDetails.agentAutoApplyStatistics.length; i++){%>
                    <%var resLen = res.gradeDetails.agentAutoApplyStatistics[i].autoApplyStatistic%>
                    <tr>
                        <td class="center sorting_1"><%=resLen.month%></td>
                        <td class="center sorting_1"><%=examType(resLen.type)%></td>
                        <td class="center sorting_1"><%=resLen.bgExamGte3StuCount%></td>
                        <td class="center sorting_1"><%=resLen.bgExamGte6StuCount%></td>
                    </tr>
                    <%}%>
                    <%}%>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="areaDetailTitle span3">文理班设置：</div>
        <div class="areaDetailContent span6" style="float: none;">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" rowspan="1">年级文理班分布</th>
                        <th class="sorting" colspan="1">班级文理班设置</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <tr>
                        <td><%=toFlag(res.gradeDetails.agentArtScienceCondition.isMeetGradeArtsci)%></td>
                        <td><%=toFlag(res.gradeDetails.agentArtScienceCondition.isMeetClassArtsci)%></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="areaDetailTitle span3">扫描明细：</div>
        <div class="areaDetailContent span11">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" rowspan="1">月份</th>
                        <th class="sorting" colspan="1">自动/提报</th>  <#--0 提报 1 自动-->
                        <th class="sorting" colspan="1">科目</th>
                        <th class="sorting" colspan="1">试卷</th>
                        <th class="sorting" colspan="1">题型题量</th>
                        <th class="sorting" colspan="1">文科/理科</th>
                        <th class="sorting" colspan="1">班级最低渗透率</th>
                        <th class="sorting" colspan="1">参考人数</th>
                        <th class="sorting" colspan="1">学生渗透率</th>
                        <th class="sorting" colspan="1">班级渗透率</th>
                        <th class="sorting" colspan="1">有无合同</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <%if(res.gradeDetails.agentScanDetails){%>
                    <%for(var i = 0; i< res.gradeDetails.agentScanDetails.length; i++){%>
                    <%var resLen = res.gradeDetails.agentScanDetails[i]%>
                    <%var resLenDet = res.gradeDetails.agentScanDetails[i].scanDetails%>
                    <tr>
                        <td class="center sorting_1"><%=resLenDet.month%></td>
                        <td class="center sorting_1"><%=examType(resLenDet.type)%></td>
                        <td class="center sorting_1"><%=resLen.subjectDes%></td>
                        <td class="center sorting_1"><%=resLen.paperName%></td>
                        <td class="center sorting_1"><%=toFlag(resLenDet.isMeetQuestion)%></td>
                        <td class="center sorting_1"><%=toArtScienceType(resLenDet.artScienceType)%></td>
                        <td class="center sorting_1"><%=toPercent(resLenDet.classMinPermeability)%></td>
                        <td class="center sorting_1"><%=resLenDet.examStuCount%>人</td>
                        <td class="center sorting_1"><%=toPercent(resLenDet.stuPermeability)%></td>
                        <td class="center sorting_1"><%=toPercent(resLenDet.classPermeability)%></td>
                        <td class="center sorting_1"><%=hasContact(resLenDet.isSignContract)%></td>
                    </tr>
                    <%}%>
                    <%}%>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
    template.helper("Math",Math);
    var schoolId = 0;
    $(function(){
        /*城市联想查询*/
        $('#cityName').autocomplete({
            delay :600,
            source:function(request,response){
                if(!request.term||request.term.trim()==''){
                    return;
                }
                $.get("search_city.vpage",{cityKey: request.term},function(result){
                    response( $.map( result.dataList, function( item ) {
                        return {
                            label: item.cityName,
                            value: item.cityName,
                            cityCode: item.cityCode
                        }
                    }));
                });
            },
            select: function( event, ui ) {
                $('#cityName').val(ui.item.value);
                $('#cityCode').val(ui.item.cityCode);
            }
        });
        /*学校联想查询*/
        $('#school').autocomplete({
            delay :600,
            source:function(request,response){
                if(!request.term||request.term.trim()==''){
                    return;
                }
                $.get("/exam/contractmanage/search_school.vpage",{schoolKey: request.term},function(result){
                    response( $.map( result.dataList, function( item ) {
                        return {
                            label: item.cmainName,
                            value: item.cmainName,
                            id: item.id
                        }
                    }));
                });
            },
            select: function( event, ui ) {
                schoolId = ui.item.id;
                $('#school').val(ui.item.value);
                $('#schoolId').val(ui.item.id)
            }
        });
        /*负责人联想查询*/
        $('#userName').autocomplete({
            delay :600,
            source:function(request,response){
                if(!request.term||request.term.trim()==''){
                    return;
                }
                $.get("/exam/contractmanage/search_user.vpage",{userKey: request.term},function(result){
                    response( $.map( result.dataList, function( item ) {
                        return {
                            label: item.realName,
                            value: item.realName,
                            id: item.id
                        }
                    }));
                });
            },
            select: function( event, ui ) {
                $('#userId').val(ui.item.id) ;
                $('#userName').val(ui.item.realName) ;
            }
        });
        /*考试月份选择*/
        $("#month").datepicker({
            dateFormat      : 'yy-mm',  //日期格式，自己设置
            closeText       : "确定",
            currentText     : "本月",
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            numberOfMonths  : 1,
            changeMonth: true,
            changeYear: true,
            showButtonPanel: true,
            onChangeMonthYear:function () {
                var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
                var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
                $(this).datepicker('setDate', new Date(year, month, 1));
            },
            beforeShow : function( input ) {
                setTimeout(function() {
                    var buttonPane = $(input)
                            .datepicker( "widget" )
                            .find( ".ui-datepicker-buttonpane" );
                    $( "<button>", {
                        text: "清空",
                        click: function() {
                            $.datepicker._clearDate(input);
                        }
                    }).addClass("ui-state-default ui-priority-primary ui-corner-all").appendTo( buttonPane );
                }, 1 );
            }
        }).val(new Date().Format('yyyy-MM'));
        $("#queryOralBtn").on("click",function(){
            if($('#userName').val() == ''){
                $('#userId').val('');
            }
            if($('#school').val() == ''){
                $('#schoolId').val('');
            }

            var dataObj = {
                month:$('#month').val(),
                cityCode:$('#cityCode').val(),
                userId:$('#userId').val(),
                schoolId:$('#schoolId').val(),
                type:1
            };
            $.get("search_exam.vpage",dataObj,function (res) {
                if(res.success && res.dataList){
                    var dataTableList = [];
                    for(var i=0;i < res.dataList.length;i++){
                        var item = res.dataList[i];
                        var arr = [];
                        var subjectList = item.examGradeVOList;
                        for(var j = 0; j < subjectList.length ;j++){
                            subject = subjectList[j];
                            var operator = "<span class='show_exam_info' style='cursor:pointer;color:blue;' data-gradedes='" + subject.gradeDes + "' data-schoolname='" + item.schoolName + "' data-detail='" + JSON.stringify(subject.agentGradeDetails) + "'>数据统计</span>";
                            arr = [item.monthStr, item.cityName, item.schoolId, item.schoolName, item.ownerName, subject.gradeDes,subject.bgExamGte3StuCount+'人',subject.bgExamGte6StuCount+'人', operator];
                            dataTableList.push(arr);
                        }
                    }

                    var reloadDataTable = function () {
                        var table = $('#datatable').dataTable();
                        table.fnClearTable();
                        table.fnAddData(dataTableList); //添加添加新数据
                    };
                    setTimeout(reloadDataTable(),0);
                }else{
                    alert(res.info);
                }
            });

        });
        $('#exportOralBtn').on('click',function () {
            var dataObj = {
                month:$('#month').val(),
                cityCode:$('#cityCode').val(),
                userId:$('#userId').val(),
                schoolId:$('#schoolId').val(),
                type:1
            };
            window.open("export_exam.vpage?month="+dataObj.month+"&cityCode="+dataObj.cityCode+"&userId="+dataObj.userId+"&schoolId="+dataObj.schoolId+"&type="+dataObj.type,'_blank');

        });
        $(document).on("click",'.show_exam_info',function () {
            var res = {};
            res.gradeDes = $(this).data('gradedes');
            res.schoolName = $(this).data('schoolname');
            res.gradeDetails = $(this).data('detail');
            $("#targetHistory_dialog").modal("show");
            $('#targetHistory_con_dialog').html(template("TargetHistoryDialogTemp",{res:res}));
        });

        $("#queryOralBtn").trigger("click");
    });

</script>
</@layout_default.page>
