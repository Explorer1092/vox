<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="数据下载" page_num=1>

<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<style>
    label{ display: inline-block}
    .top_margin li{margin-top:15px}
</style>
<#assign isBdOrCityManager = requestContext.getCurrentUser().isBusinessDeveloper()|| requestContext.getCurrentUser().isCityManager()/>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 数据下载</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <style>
            select {width: 120px;position: relative;z-index: 3} /*label会遮住select的点击区域*/
        </style>
        <div class="box-content ">
            <ul class="top_margin" style="list-style:none;">
                <li>
                    <input type="radio" name="mode" checked style="margin-left: 4px" value="1" id="online_mode"><label for="online_mode">online模式数据下载</label>
                    <input type="radio" name="mode" style="margin-left: 4px" value="2" id="offline_mode"><label for="offline_mode">offline模式数据下载</label>
                    <#if isJuniorSchool>
                        <input type="radio" name="mode" style="margin-left: 4px" value="3" id="parent_mode"><label for="offline_mode">家长数据下载</label>
                    </#if>
                    <input type="radio" name="mode" style="margin-left: 4px" value="4" id="xtest_mode"><label for="xtest_mode">x测试数据下载</label>
                </li>
                <li id="defaul_mode_li">
                    选择日期：<input type="text" id="startDate" name="day" style="cursor: default;" value="${date}" readonly>
                </li>
                <li id="xtest_mode_li" hidden="hidden">
                    考试开始时间：<input type="text" id="xtestBeginDate" name="day" style="cursor: default;" value="${xtestBrginDate}" readonly> 至 <input type="text" id="xtestEndDate" name="day" style="cursor: default;" value="${xtestEndDate}" readonly>
                </li>
                <li id="dic_school_type_li">
                    字典表学校：
                    <span class="dic_school_type"><input type="radio" name="schoolType" style="margin-left: 4px" value="1" id="schoolType1"><label for="schoolType1">字典表学校</label></span>
                    <span class="no_dic_school_type"><input type="radio" name="schoolType" style="margin-left: 4px" value="2" id="schoolType2"><label for="schoolType2">非字典表学校</label></span>
                </li>
                <li>
                    数据类型：
                    <span class="dic_school"><input type="radio" name="dataType" checked style="margin-left: 4px" value="1" id="school"><label id="school_lab" for="school">学校</label></span>
                    <span class="no_dic_school"><input type="radio" name="dataType" style="margin-left: 4px;" value="2" id="teacher"><label id="teacher_lab" for="teacher">老师</label></span>
                    <span class="no_dic_school"><input type="radio" name="dataType" style="margin-left: 4px;" value="3" id="class"><label id="class_lab" for="class">班级</label></span>
                </li>
                <#if !isBdOrCityManager>
                    <li>
                        选择部门：<input type="text" id="updateDepBtn" value="选择部门" style="cursor: default;" readonly/>
                    </li>
                </#if>
                <li>
                    <button type="button" id="download_btn" class="btn btn-success">下载</button>
                </li>
            </ul>


        </div>
    </div>
</div>

<#--调整部门弹窗-->
<div id="userUpdateDep_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">选择部门</h4>
            </div>
            <div class="modal-body">
                <div class="control-group">
                    <div class="row-fluid">
                        <div id="useUpdateDep_con_dialog" class="span10"></div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="updateDepSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="application/javascript">

    var isBdOrCityManager = ${isBdOrCityManager?string("true","false")};


    $(document).on("click","#xtest_mode",function(){
        $("#xtest_mode_li").show();
        $("#defaul_mode_li").hide();

        $("#school_lab").text("考试");
        $("#teacher_lab").text("学校");
        $("#class_lab").text("班级");

        $("#dic_school_type_li").hide();

        $(".no_dic_school").show();


    });
    $(document).on("click","#offline_mode",function(){
        $("#defaul_mode_li").show();
        $("#xtest_mode_li").hide();

        $("#school_lab").text("学校");
        $("#teacher_lab").text("老师");
        $("#class_lab").text("班级");

        $("#dic_school_type_li").show();

        var _this = $(this),dic_school = $('.dic_school'),no_dic_school = $('.no_dic_school');
        if(_this.val() == 1){
            no_dic_school.show();
        }else{
            no_dic_school.hide();
            dic_school.find('input').prop('checked',true);
            dic_school.find('span').addClass('checked');
            no_dic_school.find('span').removeClass('checked');
        }
    });
    $(document).on("click","#online_mode",function(){
        $("#defaul_mode_li").show();
        $("#xtest_mode_li").hide();

        $("#school_lab").text("学校");
        $("#teacher_lab").text("老师");
        $("#class_lab").text("班级");

        $("#dic_school_type_li").show();

        var _this = $(this),dic_school = $('.dic_school'),no_dic_school = $('.no_dic_school');
        if(_this.val() == 1){
            no_dic_school.show();
        }else{
            no_dic_school.hide();
            dic_school.find('input').prop('checked',true);
            dic_school.find('span').addClass('checked');
            no_dic_school.find('span').removeClass('checked');
        }
    });
    $(document).on("click","#parent_mode",function(){
        $("#defaul_mode_li").show();
        $("#xtest_mode_li").hide();

        $("#school_lab").text("学校");
        $("#teacher_lab").text("老师");
        $("#class_lab").text("班级");

        $("#dic_school_type_li").show();

        var _this = $(this),dic_school = $('.dic_school'),no_dic_school = $('.no_dic_school');
        if(_this.val() == 1){
            no_dic_school.show();
        }else{
            no_dic_school.hide();
            dic_school.find('input').prop('checked',true);
            dic_school.find('span').addClass('checked');
            no_dic_school.find('span').removeClass('checked');
        }
    });



    $(document).on("click","#updateDepSubmitBtn",function(){
        var tree = $("#useUpdateDep_con_dialog").fancytree("getTree");
        if(tree.getActiveNode()){
            var node = tree.getActiveNode();
            $("#updateDepBtn").val(node.title);
        }

        $("#userUpdateDep_dialog").modal("hide");
    });
    $('#schoolType1').prop('checked',true);//默认选中字典表学校
    $(document).on('change','input[name="schoolType"]',function () {
        var _this = $(this),dic_school = $('.dic_school'),no_dic_school = $('.no_dic_school');
        if(_this.val() == 1){
            no_dic_school.show();
        }else{
            no_dic_school.hide();
            dic_school.find('input').prop('checked',true);
            dic_school.find('span').addClass('checked');
            no_dic_school.find('span').removeClass('checked');
        }
    });
    $(document).on("click","#download_btn",function(){
        var mode = $("input[name=mode]:checked").val();
        var day = $("#startDate").val();
        var xtestBeginDay = $("#xtestBeginDate").val();
        var xtestEndDay = $("#xtestEndDate").val();
        var schoolType = $("input[name=schoolType]:checked").val();
        var dataType = $("input[name=dataType]:checked").val();
        var tree = $("#useUpdateDep_con_dialog").fancytree("getTree");
        var groupId = 0;
        if (!isBdOrCityManager){
            if(xtestBeginDay==null || xtestBeginDay=="") {
                alert("请选择考试开始时间区域");
                return;
            }
            if(xtestEndDay==null || xtestEndDay=="") {
                alert("请选择考试开始时间区域");
                return;
            }
            if(tree.getActiveNode()){
                var node = tree.getActiveNode();
                $("#updateDepBtn").val(node.title);
                groupId = node.key;
                if (dataType != '1'){
                    if (node.data && node.data.role == 'City'){

                    }else {
                        alert("请选择角色为分区部门");
                        return;
                    }
                }
            }else{
                alert("请选择分区");
                return;
            }
        }

        location.href = "exportDataReport.vpage?mode="+mode +"&day="+day + "&schoolType="+schoolType +"&dataType="+dataType +"&groupId="+groupId + "&xtestBeginDay=" + xtestBeginDay + "&xtestEndDay=" + xtestEndDay;

    });


    //调整部门
    $(document).on("click","#updateDepBtn",function(){
        $("#userUpdateDep_dialog").modal('show');
    });

    $(document).ready(function () {
        $("#useUpdateDep_con_dialog").fancytree("destroy");
        $("#newRoleName_dialog").remove();
        $("#useUpdateDep_con_dialog").fancytree({
            source: {
                url: "/user/orgconfig/getNewDepartmentTree.vpage",
                cache:true
            },
            checkbox: false,
            autoCollapse:true,
            selectMode: 1
        });

        $("#startDate").datepicker({
            dateFormat      : 'yymmdd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false
        });
        $("#xtestBeginDate").datepicker({
            dateFormat      : 'yymmdd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : lastMnoth(1),
            viewDate        : lastMnoth(1),
            startDate       : lastMnoth(1),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){
                var xtestEndDate =  $("#xtestEndDate").val();
                if(xtestEndDate){
                    if(selectedDate > xtestEndDate){
                        alert("结束日期必须晚于开始日期");
                        $("#xtestBeginDate").val(null);//头疼的写法  return false; 竟然不管用~~~
                        // return false;
                    }
                    var _xtestEndDate = xtestEndDate.substr(0,4) + "-" + xtestEndDate.substr(4,2) + "-" + xtestEndDate.substr(6,2);
                    if (lastMnoth(_xtestEndDate, 3) > selectedDate) {
                        alert("只能查询三个月的数据");
                        $("#xtestBeginDate").val(null);//头疼的写法  return false; 竟然不管用~~~
                        // return false;
                    }
                }
                return true;
            }
        });
        $("#xtestEndDate").datepicker({
            dateFormat      : 'yymmdd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){
                var xtestBeginDate =  $("#xtestBeginDate").val();
                if(xtestBeginDate){
                    if(selectedDate < xtestBeginDate){
                        alert("结束日期必须晚于开始日期");
                        $("#xtestEndDate").val(null);//头疼的写法  return false; 竟然不管用~~~
                        // return false;
                    }
                    var _xtestBeginDate = xtestBeginDate.substr(0,4) + "-" + xtestBeginDate.substr(4,2) + "-" + xtestBeginDate.substr(6,2);
                    if (lastMnoth(_xtestBeginDate, -3) < selectedDate) {
                        alert("只能查询三个月的数据");
                        $("#xtestEndDate").val(null);//头疼的写法  return false; 竟然不管用~~~
                        // return false;
                    }
                }
                return true;
            }
        });
    });

    function lastMnoth(num) {
        var time = new Date();
        time.setMonth(time.getMonth()-num);
        var timeStr = format(time, 'yyyyMMdd');
        return timeStr;
    }

    function lastMnoth(time, num) {
        var time = new Date(time);
        time.setMonth(time.getMonth()-num);
        var timeStr = format(time, 'yyyyMMdd');
        return timeStr;
    }

    var format = function(time, format){
        var t = new Date(time);
        var tf = function(i){return (i < 10 ? '0' : '') + i};
        return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function(a){
            switch(a){
                case 'yyyy':
                    return tf(t.getFullYear());
                    break;
                case 'MM':
                    return tf(t.getMonth() + 1);
                    break;
                case 'mm':
                    return tf(t.getMinutes());
                    break;
                case 'dd':
                    return tf(t.getDate());
                    break;
                case 'HH':
                    return tf(t.getHours());
                    break;
                case 'ss':
                    return tf(t.getSeconds());
                    break;
            }
        })
    }
</script>
</@layout_default.page>
