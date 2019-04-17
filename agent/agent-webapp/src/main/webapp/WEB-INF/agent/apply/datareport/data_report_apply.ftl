<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='大数据报告申请' page_num=3>
<script src="/public/rebuildRes/js/common/category.js"></script>
<style>
    body{
        text-shadow:none;
    }
    /*不显示日期面板*/
    .ui-datepicker-calendar {
        display: none;
    }
</style>
<div class="row-fluid">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 大数据报告申请</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <form class="form-horizontal">
                <input type="hidden" id="targetSchoolId" name="targetSchoolId" />
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">学科</label>
                        <div class="controls">
                            <select id="subject" name="subject" class="checkData"  data-info="请选择级别">
                                <#list subjectList as item>
                                    <option value="${item}">
                                        <#if item == 1>小学英语
                                        <#elseif item == 2>小学数学
                                        </#if>
                                    </option>
                                </#list>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">级别</label>
                        <div class="controls">
                            <#list reportLevelList as item>
                                <label class="control-label" style="text-align:left">
                                    <input type="radio" name="reportLevel" value="${item}" class="checkData reportLevel" data-info="请选择级别"/>
                                    <#if item == 1>市级
                                    <#elseif item == 2>区级
                                    <#elseif item == 3>校级
                                    </#if>
                                </label>
                            </#list>
                        </div>
                    </div>
                    <div class="control-group rgion" <#--style="display: none;"-->>
                        <label class="control-label city_county">区域</label>
                        <div class="controls city_county">
                        <#if requestContext.getCurrentUser().isBusinessDeveloper()>
                            <select id="county" class="firstCategory checkData"  data-info="请选择区域" onchange="secondCategories()" category=""></select>
                        </#if>
                        <#if requestContext.getCurrentUser().isCityManager() || requestContext.getCurrentUser().isRegionManager()>
                            <select id="city" class="firstCategory checkData"  data-info="请选择区域" onchange="secondCategories()" category=""></select>
                            <select id="county" class="secondCategory checkData"  data-info="请选择区域" onchange="thirdCategories()" category=""></select>
                        </#if>
                        </div>
                        <div class="schoolShow" style="display: none">
                            <label class="control-label">学校</label>
                            <div class="controls">
                                <input id="addSchoolBtn" value="添加" type="button" class="checkData"  data-info="请选择学校"/>
                            </div>
                            <div id="schoolinfo_div" style="display: none;">
                                <table class="table table-bordered table-striped" style="width: 50%;margin-left: 10%">
                                    <tbody id="schoolinfo_tbody">
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="control-group schoolShow" style="display:none;">
                        <label class="control-label">英语起始年级</label>
                        <div class="controls">
                            <label class="control-label" style="text-align:left"><input class="englishStartGrade" type="radio" name="englishStartGrade"
                                                                value="1"/>一年级</label>
                            <label class="control-label" style="text-align:left"><input class="englishStartGrade" type="radio" name="englishStartGrade"
                                                                value="3"/>三年级</label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">时间维度</label>
                        <div class="controls">
                            <label class="control-label" style="text-align:left">
                                <input class="reportType" type="radio" checked="checked" disabled="disabled" name="reportType" value="1"/>
                                学期报告
                            </label>
<#--                            <label class="control-label" style="text-align:left">
                                <input class="reportType" type="radio" name="reportType" value="2"/>
                                月度报告
                            </label>-->
                        </div>
                    </div>
                    <div class="control-group schoolTerm" <#--style="display: none"-->>
                        <label class="control-label">学期</label>
                        <div class="controls">
                            <select id="semester" name="semester">
                                <option value="" data-value="1">2016年9-12月</option>
                                <option value="" data-value="2">2017年1-6月</option>
                                <option value="" data-value="3">2017年7-12月</option>
                                <option value="" data-value="4">2018年1-6月</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group monthly" style="display:none;">
                        <label class="control-label">月份</label>
                        <div class="controls">
                            <input type="text" class="reportMonth input-small checkData" id="startDate" name="startDate" value="" data-info="请选择月份">
                        </div>
                    </div>

                    <div class="control-group sampleSchool">
                        <label class="control-label" style="margin-bottom:0;padding-top:2.5px">样本校</label>
                        <div class="controls">（选填：最多可添加一所，添加样本校后报告内容会增加样本校数据分析）<input id="addSampleSchoolBtn" value="添加" type="button"/></div>
                        <div id="sampleSchool" style="display: none;">
                            <table class="table table-bordered table-striped" style="width: 50%;margin-left: 10%">
                                <tbody id="sampleSchool_tbody">
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="dataTables_wrapper" role="grid">
                        <table class="table table-striped table-bordered bootstrap-datatable" id="sample_schools"
                               style="width: 1000px;margin-left: 50px;">
                            <thead>
                                <tr>
                                    <th class="sorting" style="width: 60px;">学校ID</th>
                                    <th class="sorting" style="width: 60px;">学校名称</th>
                                    <th class="sorting" style="width: 60px;">操作</th>
                                </tr>
                            </thead>
                            <tbody>
                            </tbody>
                        </table>
                    </div>

                    <div class="control-group">
                        <label class="control-label">申请原因</label>
                        <div class="controls">
                            <textarea class="input-xlarge checkData" id="comment" rows="5" style="width: 880px;" maxlength="180"
                                      placeholder="在此填写申请原因，用途" data-info="请填写申请原因"></textarea>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="submitBtn" type="button" class="btn btn-primary">提交申请</button>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
    <div id="region_select_dialog" class="modal fade hide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">选择学校</h4>
                    <input id="selectSchoolType" type="hidden" >
                </div>
                <div class="modal-body">
                    <div class="control-group">
                        <textarea id="selectSids"  class="controls" style="width:80%" placeholder="只能填写一所学校"></textarea>
                        <button class="btn btn-large btn-primary" type="button" id="addSchooleBtn">查询</button>
                    </div>
                    <div class="control-group" id="alertInfoInDialog" style="color: red;display: none;">
                    </div>
                    <div class="control-group">
                        <div id="schoolTable"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div class="pull-left">
                        <button id="add_school_submit_btn" type="button" class="btn btn-large btn-primary">确定</button>
                        <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    var templateSchool;

    $(document).on('change',"#subject",function(){
        var subjectVal = $(this).val();
        var radioBtn = $('input[name="reportLevel"][value="3"]');
        if(subjectVal == 2){ // 小学数学的情形下，不允许选择校级报告
            radioBtn.parent().removeClass("checked");
            radioBtn.attr("checked", false);
            radioBtn.parents("label").hide();
            $('.schoolShow').hide();
            $('.city_county').show();
        }else {
            radioBtn.parents("label").show();
        }
    });

    $(document).on('change','input[name="reportLevel"]',function(){
        console.log($(this).val());
        if($(this).val() == 3){
            $('.schoolShow').show();
            $('.city_county').hide();
            $('.sampleSchool').hide();
        }else if($(this).val() == 2){
            $('.schoolShow').hide();
            $('.city_county').show();
            $('.sampleSchool').show();
            $('#county').show();
        }else if($(this).val() == 1){
            $('.schoolShow').hide();
            $('#city').show();
            $('#county').hide();
            $('.city_county').show();
            $('.sampleSchool').show();
        }
    });

    var CATEGORIES = ${regionCategory!""};
    $(function () {
        firstCategories();
    });
    $("#startDate").datepicker({
        dateFormat      : 'yy-mm',  //日期格式，自己设置
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : new Date(),
        prevText: '上月',         // 前选按钮提示
        nextText: '下月',
        showButtonPanel: true,        // 显示按钮面板
        minDate:'11-7-280',
        maxDate:new Date(),
        showMonthAfterYear: true,
        currentText: "本月",  // 当前日期按钮提示文字
        closeText: "关闭",
        numberOfMonths  : 1,
        changeMonth: true,
        changeYear: true,
        onSelect : function (selectedDate){},
        onClose: function(dateText, inst) {// 关闭事件
            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
            $(this).datepicker('setDate', new Date(year, month, 1));
        }
    });
    //添加学校dialog
    $(document).on("click","#addSchoolBtn",function(){
        templateSchool = 'addSchool';
        var uid = $(this).data("uid");
        var gid = $(this).data("gpid");
        $("#selectSids").val("");
        $("#schoolTable").html("");
        $("#selectSchoolType").val($(this).attr("data-type"));
        $("#region_select_dialog").modal('show');
        $("#alertInfoInDialog").hide();
    });
    $(document).on("click","#addSampleSchoolBtn",function(){
        templateSchool = 'addSampleSchool';
        console.log(templateSchool);
        var uid = $(this).data("uid");
        var gid = $(this).data("gpid");
        $("#selectSids").val("");
        $("#schoolTable").html("");
        $("#selectSchoolType").val($(this).attr("data-type"));
        $("#region_select_dialog").modal('show');
        $("#alertInfoInDialog").hide();
    });
    //获取焦点事件
    $("#selectSids").on("click",function (){
        $(this).focus();
    });
    $(document).on('change','input[name="reportType"]',function(){
        console.log($(this).val());
        if($(this).val() == 1){
        $('.schoolTerm').show();
        $('.monthly').hide()
        }else if($(this).val() == 2){
            $('.schoolTerm').hide();
            $('.monthly').show()
        }
    });
    //添加学校Btn
    $(document).on("click","#addSchooleBtn",function(){
        var schoolIds = $("#selectSids").val().split(',');
        if(schoolIds.length>1){
            alert("只能添加一所学校");
            return false;
        }
        if(!schoolIds){
            alert("请输入要查询学校的id");
            return false;
        }
        if(schoolIds.length != 0 ){
            $.ajax({
                url: "search_school.vpage",
                contentType: 'application/json;charset=UTF-8',
                data: JSON.stringify(schoolIds),
                dataType: "json",
                type: "POST",
                success:function(res){
                    if(res.success){
                        var dataTemp = {},errorList=[];
                        var dataTempt = [],dataInfo;
                        dataTemp.schoolData = res.successSchool;
                        errorList = res.errorSchool;
                        dataTempt = res.notLevel;
                        renderDepartment('#chooseSchoolTableTemp',dataTemp,"#schoolTable");
                        var errorSchool = "以下学校无操作权限："+errorList;
                        var notLevel = "以下学校不是小学："+dataTempt;
                        var dataInfo = errorSchool + '\n'+ notLevel;
                        if(errorList.length >= 1 || dataTempt.length >= 1){
                            alert(dataInfo);
                        }
                    }else{
                        alert(res.info);
                    }
                }
            });
        }else{
            alert("请输入学校ID,并以英文格式逗号分隔");
        }
    });
    //渲染模板
    var renderDepartment = function(tempSelector,data,container){
        var source   = $(tempSelector).html();
        var template = Handlebars.compile(source);

        $(container).html(template(data));
    };

    $("#add_school_submit_btn").on("click",function(){
        var $schoolIds =  $("#schoolTable").find(".js-schoolIds");
        var schoolId = '';
        var schoolView = '';
        if($schoolIds){
            for(var ii = 0 ; ii < $schoolIds.length ; ii++ ){
                var id = $($schoolIds[ii]).attr("data-id");
                var name = $($schoolIds[ii]).attr("data-name");
                schoolView += "<tr class='schoolinfo' id='schoolInfo_"+ id +"' data-id='"+id+"'><td>"+ id+" </td><td >"+name+"</td><td class='delete-schoolinfo' data-id='"+id+"'><a class='removeSchool' data-id='"+id+"' href=' javascript:void(0)'>删除</a></td></tr>"
                schoolId += "," +$($schoolIds[ii]).attr("data-id");
            if(templateSchool == 'addSchool'){
                $("#schoolinfo_tbody").append(schoolView);
                $("#schoolinfo_div").show();
                $('#addSchoolBtn').hide();
                $("#schoolinfo_div table").show();
            }
            if(templateSchool == 'addSampleSchool'){
                $("#sampleSchool_tbody").append(schoolView);
                $("#sampleSchool").show();
                $("#sampleSchool table").show();
                $('#addSampleSchoolBtn').hide();
            }
            $("#region_select_dialog").modal('hide');
            }
        }else{
            alert("请输入要添加的学校");
            return false;
        }
    });
        $(document).on('click','.removeSchool',function(){
            $(this).closest('table').hide();
            $(this).closest('tr').remove();
            console.log($(this));
            var sl = $(".schoolinfo").length;
            if(!sl || sl == 0){
                $("#schoolinfo_div").hide();
            }
            $('#addSchoolBtn').show();
        });

//    提交校验
//    var checkData = function () {
//        var flag = true;
//        $.each($(".checkData"), function (i, item) {
//            if (!($(item).val())) {
//                alert($(item).data("info"));
//                flag = false;
//                return false;
//            }
//        });
//        return flag;
//    };

    $(document).on('click','#submitBtn',function(){
        if($('#schoolinfo_tbody .schoolinfo').length > 1){
            alert("只能添加一所学校");
            return false ;
        }
        if($('#sampleSchool_tbody .schoolinfo').length > 1){
            alert("只能添加一所样本校");
            return false ;
        }
        var comment = $('#comment').val().trim();
        if(comment.length > 130){
            comment = comment.substring(0, 130);
        }
//        if(checkData()){
            var data = {
                 subject : $('#subject').val(),
                 reportLevel : $("input[name='reportLevel']:checked").val(),
                 city : $('#city').val(),
                 county : $('#county').val(),
                 schoolId : $('#schoolinfo_tbody .schoolinfo').data('id'),
                 englishStartGrade : $("input[name='englishStartGrade']:checked").val(),
                 reportType : $("input[name='reportType']:checked").val(),
                 reportTerm : $('#semester option:selected').data('value'),
                 reportMonth : $('.reportMonth').val().replace(/-/g,''),
                 sampleSchoolId : $('#sampleSchool_tbody .schoolinfo').data('id'),
                 comment : comment
            };
            $.post("add_data_report.vpage",data,function(res){
                if(res.success){
                    location.href="/apply/view/list.vpage";
                }else{
                    alert(res.info);
                }
            })
//        }
    })
</script>
<script id="chooseSchoolTableTemp" type="text/x-handlebars-template">
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>学校id</th>
            <th>学校名称</th>
        </tr>
        </thead>
        <tbody>
        {{#each schoolData}}
        <tr>
            <td class="js-schoolIds" data-id="{{id}}" data-name ="{{shortName}}">{{id}}</td>
            <td >{{shortName}}</td>
        </tr>
        {{/each}}
        </tbody>
    </table>
    <#--<div class="pull-left" style="padding-top: 10px;">共计 <span style="color: red;">{{#if totalNo}}{{totalNo}}{{else}}0{{/if}}</span> 所学校</div>-->
</script>
</@layout_default.page>
