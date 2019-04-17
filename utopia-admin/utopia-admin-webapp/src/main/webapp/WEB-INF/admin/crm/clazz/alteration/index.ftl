<#import "../../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM换班" page_num=3>
<div>
    <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
    </#if>
</div>
<div>
    <form id="frm" action="downloadalteration.vpage" method="post" class="inline form-horizontal" onsubmit= window.location.reload()>
    <#--各种精确查询-->
        老师ID<input type="text" id="input-teacherId" style="width:120px;"/>
        老师手机<input type="text" id="input-teacherMobile" style="width:150px;"/>
        <button id="btn-search" class="btn">查询</button>
        &nbsp;&nbsp;&nbsp;创建时间：
        <input type="text" id="startDate" name="startDate" style="width:120px;"/> -
        <input type="text" id="endDate" name="endDate" style="width:120px;"/>
        <button id="btn-excel" type="submit" class="btn">导出excel</button>
        <button id="btn-record" class="btn btn-warning">查看操作记录</button>
    </form>
    <br>
</div>
<div style="margin-bottom: 20px;">
    <input type="checkbox" id="need-manual-check">需要人工帮助
</div>
<div id="allRegionProvince"></div>
<div id="allSchoolList"></div>
<div id="allClazzRollOutList" class="span9"></div>
<div id="alterationRecord"></div>
<div id="fake-confirm-dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>判定假老师</h3>
    </div>
    <div class="modal-body">
        <input id="fake-teacher-id" type="hidden" />
        <input id="fake-teacher-type" type="hidden" />
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>原因</dt>
                    <dd>
                        <textarea id="fake-teacher-desc" name="fake-teacher-desc" rows="5">换班外呼排假</textarea>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="fake_confirm_btn" onclick="fakeTeacher()" class="btn btn-danger">判定为假</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        var allRegionProvince = $("#allRegionProvince");
        var allSchoolList = $("#allSchoolList");
        var allClazzRollOutList = $("#allClazzRollOutList");
        var alterationRecord = $('#alterationRecord');
        var recordAllSchool = null;
        var recordAllClass = null; // by wyc 2016-01-16
        var searchTeacherId = null;
        //加载所有省
        $.getJSON("${requestContext.webAppContextPath}/legacy/map/nodes-0.vpage", function(data) {
            allRegionProvince.html( template("T:全国地区列表", { list : data}) );
        });

        $('#startDate').datepicker({
                                       maxDate: 0,
                                       dateFormat: "yy-mm-dd",
                                       onSelect: function(dateText){
                                           $('#endDate').datepicker("option","minDate",dateText);
                                       }
                                   });
        $('#endDate').datepicker({
                                     maxDate: 0,
                                     dateFormat: 'yy-mm-dd',
                                     onSelect: function(dateText){
                                         $('#startDate').datepicker("option","maxDate",dateText);
                                     }
                                 });

        //加载学校 getschools by wyc 2016-01-16
        allRegionProvince.on("click", "a", function(){
            var $this = $(this);
            $this.addClass("btn-primary").siblings().removeClass("btn-primary");
            searchTeacherId = null;
            allClazzRollOutList.html("");
            alterationRecord.html("");
            $.get("${requestContext.webAppContextPath}/crm/clazz/alteration/getschools.vpage", {pcode: $this.data("id") }, function(data){
                if(data.success){
                    recordAllSchool = data.json;
                    allSchoolList.html( template("T:当前地区所有学校", { list : recordAllSchool}) )
                }else{
                    alert(data.info);
                }
            });
        });

        // 加载当前学校的班级 getclasses by wyc 2016-01-16
        allSchoolList.on("click", "a", function () {
            var $this = $(this);
            $this.css({
                          'background-color': "#666",
                          'color': "#fff"
                      }).siblings().css({
                                            'background-color': "inherit",
                                            'color': "inherit"
                                        });

            allSchoolList.html(template("T:当前地区所有学校", {list: recordAllSchool}));
            alterationRecord.html("");
            $.get("${requestContext.webAppContextPath}/crm/clazz/alteration/getclasses.vpage", {schoolId: $this.data("schoolid"), teacherId: searchTeacherId}, function (data) {
                if (data.success) {
                    recordAllClass = data.json;
                    allClazzRollOutList.html(template("T:当前学校所有班级", {list: recordAllClass}))
                } else {
                    alert(data.info);
                }
            });

        });

        $("#need-manual-check").change(function(){
            var $this = $(this);
            $this.css({
                          'background-color': "#666",
                          'color': "#fff"
                      }).siblings().css({
                                            'background-color': "inherit",
                                            'color': "inherit"
                                        });

            var selected = $(this).is(":checked");
            if(selected){
                $("#allRegionProvince").hide();
                refreshManualTypeTpl(searchTeacherId);
            }else {
                $("#allRegionProvince").show();
                $("#allClazzRollOutList").html('');
            }
        });

        function refreshManualTypeTpl(searchTeacherId){
            $.get("${requestContext.webAppContextPath}/crm/clazz/alteration/getmanualclasses.vpage", {teacherId: searchTeacherId}, function (data) {
                if (data.success) {
                    recordAllClass = data.json;
                    allClazzRollOutList.html(template("T:当前学校所有班级", {list: recordAllClass}))
                } else {
                    alert(data.info);
                }
            });
        }

        //查看操作记录
        $("#btn-record").on("click", function() {
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();

            allSchoolList.html("");
            allClazzRollOutList.html("");
            $.get("${requestContext.webAppContextPath}/crm/clazz/alteration/alterationrecords.vpage", {startDate: startDate, endDate: endDate }, function(data){
                if(data.success){
                    alterationRecord.html(template("T:换班操作记录", {list: data.result}));
                }else{
                    alert(data.info);
                }
            });
            return false;
        });

        //查询
        $("#btn-search").on("click", function() {
            var teacherId = $("#input-teacherId").val();
            var teacherMobile = $("#input-teacherMobile").val();

            allSchoolList.html("");
            alterationRecord.html("");
            $.get("${requestContext.webAppContextPath}/crm/clazz/alteration/searchdata.vpage", {teacherId: teacherId, teacherMobile: teacherMobile }, function(data){
                if(data.success){
                    recordAllClass = data.json;
                    allClazzRollOutList.html(template("T:当前学校所有班级", {list: recordAllClass}))
                }else{
                    alert(data.info);
                }
            });
            return false;
        });

        // 判断假老师
        allClazzRollOutList.on("click", ".v-confirmFake", function(){
            var $this = $(this);
            if($this.hasClass("disabled")){
                return false;
            }
            var type = $this.data("type");
            var teacherId = $this.data("teacherid");
            $('#fake-teacher-id').val(teacherId);
            $('#fake-teacher-type').val(type);
            $('#fake-confirm-dialog').modal('show');
        });

        //确认换班
        allClazzRollOutList.on("click", ".v-confirmShift", function(){
            var $this = $(this);
            var $postLink = "${requestContext.webAppContextPath}/crm/clazz/alteration/approvereplace.vpage";//SUBSTITUTE

            if($this.hasClass("disabled")){
                return false;
            }

            var result = confirm('确定换班吗？');
            if(!result){
                return false;
            }

            if($this.data("type") == "TRANSFER"){
                $postLink = "${requestContext.webAppContextPath}/crm/clazz/alteration/approvetransfer.vpage";//HAND_OVER
            }

            if($this.data("type") == "LINK"){
                $postLink = "${requestContext.webAppContextPath}/crm/clazz/alteration/approvelink.vpage";//JOIN
            }

            $.post($postLink, {
                respondentId : $this.data("respondentid"),
                applicantId : $this.data("applicantid"),
                clazzId : $this.data("clazzid"),
                recordId : $this.data("recordid"),
                type : $this.data("type")
            }, function(data){
                if(data.success){
                    //成功
                    $this.addClass("disabled").text("换班成功");
                    if($("#need-manual-check").is(":checked")){
                        refreshManualTypeTpl();
                    }
                }else{
                    alert(data.info);
                }
            });
        });

        //取消
        allClazzRollOutList.on("click", ".v-confirmCancel", function(){
            var $this = $(this);
            var $postLink = "${requestContext.webAppContextPath}/crm/clazz/alteration/cancelreplace.vpage";//SUBSTITUTE

            if($this.hasClass("disabled")){
                return false;
            }

            var result = confirm('确定拒绝吗？');
            if(!result){
                return false;
            }

            if($this.data("type") == "TRANSFER"){
                $postLink = "${requestContext.webAppContextPath}/crm/clazz/alteration/canceltransfer.vpage";//HAND_OVER
            }

            if($this.data("type") == "LINK"){
                $postLink = "${requestContext.webAppContextPath}/crm/clazz/alteration/cancellink.vpage";//JOIN
            }

            $.post($postLink, {
                applicantId : $this.data("applicantid"),
                recordId : $this.data("recordid")
            }, function(data){
                if(data.success){
                    //成功
                    $this.addClass("disabled").text("拒绝成功");
                }else{
                    alert(data.info);
                }
            });
        });
    });

    function fakeTeacher(){
        if (!confirm("老师如存在未处理的换班申请，判假后将取消这些申请。\r\n确认判假？")) {
            return false;
        }
        var teacherId = $('#fake-teacher-id').val();
        var desc = $('#fake-teacher-desc').val();
        var type = $('#fake-teacher-type').val();

        if (desc == '') {
            alert('请输入原因！');
            return false;
        }
        $.post("faketeacher.vpage", {teacherId: teacherId, desc:desc}, function(data) {
            if(data.success){
                alert("操作成功!");
                $('#fake-confirm-dialog').modal('hide');
                if(type=='APPLICANT') {
                    $('#applicant-' + teacherId).addClass("disabled").text("判假成功");
                } else if (type=='RESPONDENT') {
                    $('#respondent-'+ teacherId).hide();
                } else {
                    window.location.reload();
                }
            } else {
                alert(data.info);
            }
        });
    }

</script>
<#--template-->
<script type="text/html" id="T:全国地区列表">
    <div style="border: 1px solid #e3e3e3; padding:15px; margin: 0 0 20px;">
        <%for(var i = 0, len = list.length; i < len; i++){%>
        <a href="javascript:void(0);" style="margin: 5px; width: 53px;" class="btn" data-id="<%=list[i].id%>"><%=list[i].text%></a>
        <%}%>
    </div>
</script>
<script type="text/html" id="T:当前地区所有学校">
    <div style="border: 1px solid #e3e3e3; padding:15px; margin: 0 0 20px;">
        <%if(list.length > 0){%>
        <%for(var i = 0, len = list.length; i < len; i++){%>
        <a href="javascript:void(0);" data-index="<%=i%>" data-schoolId="<%=list[i].schoolId%>" style="display: inline-block; margin: 5px;padding: 4px 8px; border-radius: 4px;"><%=list[i].schoolName%></a>
        <%}%>
        <%}else{%>
        <p style="text-align: center; font-size: 20px;">本地区没有转让班级</p>
        <%}%>
    </div>
</script>
<script type="text/html" id="T:当前学校所有班级">
    <%if(list != null){%>
    <%for(var i = 0, len = list.length; i < len; i++){%>
    <%if(list[i].records.length > 0){%>
    <table class="table table-bordered" id="respondent-<%=list[i].respondentId%>">
        <thead>
        <tr style="background-color: #d9edf7;">
            <th colspan="4"><%=list[i].respondentName%>
                <%if(list[i].respondentAuth == 1){%><span class="label label-important">认证</span><%}%>
                （<%if(list[i].respondentId){%>账号：<a href="${requestContext.webAppContextPath}/crm/teachernew/teacherdetail.vpage?teacherId=<%=list[i].respondentId%>" target="_blank"><%=list[i].respondentId%></a><%}%>）
                <a href="javascript:void(0);" class="btn btn-danger v-confirmFake" data-teacherId="<%=list[i].respondentId%>" data-type="RESPONDENT" >判定为假</a>
            </th>
        </tr>
        </thead>
        <thead>
        <tr>
            <th style="width: 20%">班级</th>
            <th style="width: 20%">申请转让/接管/关联</th>
            <th>申请人</th>
            <th style="width: 30%;">操作</th>
        </tr>
        </thead>
        <tbody>
        <%for(var r = 0, records = list[i].records; r < records.length; r++){%>
        <tr>
            <td><%=records[r].clazzName%> (<a href="${requestContext.webAppContextPath}/crm/clazz/groupinfo.vpage?clazzId=<%=records[r].clazzId%>" target="_blank"><%=records[r].clazzId%></a>)</td>
            <td><%=records[r].typeName%></td>
            <td>
                <%=records[r].applicantName%>
                <%if(records[r].applicant == 1){%><span class="label label-important">认证</span><%}%>
                <a href="${requestContext.webAppContextPath}/crm/teachernew/teacherdetail.vpage?teacherId=<%=records[r].applicantId%>" target="_blank"><%=records[r].applicantId%></a>
            </td>
            <td>
                <a href="javascript:void(0);" id="applicant-<%=records[r].applicantId%>" class="btn btn-danger v-confirmFake" data-teacherId="<%=records[r].applicantId%>" data-type="APPLICANT">判定为假</a>
                <a href="javascript:void(0);" class="btn btn-success v-confirmShift" data-applicantId="<%=records[r].applicantId%>" data-respondentId="<%=list[i].respondentId%>" data-clazzId="<%=records[r].clazzId%>" data-recordId="<%=records[r].recordId%>" data-type="<%=records[r].type%>">确认换班</a>
                <a href="javascript:void(0);" class="btn v-confirmCancel" data-applicantId="<%=records[r].applicantId%>" data-recordId="<%=records[r].recordId%>" data-respondentId="<%=list[i].respondentId%>" data-type="<%=records[r].type%>">拒绝</a>
            </td>
        </tr>
        <%}%>
        </tbody>
        <%}%>
    </table>
    <%}%>
    <%}else{%>
    <p style="text-align: center; font-size: 20px;">没有符合条件的转让班级</p>
    <%}%>
</script>
<script type="text/html" id="T:换班操作记录">
    <div class="well">
        <div id="data_table_journal">
            <table class="table table-bordered table-striped" style="width: 80%; margin: auto;">
                <thead>
                <tr style="background-color: #d9edf7;">
                    <th style="text-align: center;">日期</th>
                    <th style="text-align: center;">转让操作数</th>
                    <th style="text-align: center;">转让通过数</th>
                    <th style="text-align: center;">转让通过率(%)</th>
                    <th style="text-align: center;">接管操作数</th>
                    <th style="text-align: center;">接管通过数</th>
                    <th style="text-align: center;">接管通过率(%)</th>
                    <th style="text-align: center;">关联操作数</th>
                    <th style="text-align: center;">关联通过数</th>
                    <th style="text-align: center;">关联通过率(%)</th>
                    <th style="text-align: center;">判假数</th>
                </tr>
                </thead>
                <%for(var i = 0; i < list.length; ++i){%>
                <tbody>
                <tr <%if(list[i].tranRatio==100 || list[i].repRatio==100 || list[i].lnkRatio==100){%> class="success"  <%}else{%> class="warning" <%}%> >
                <td style="text-align: center;"><%=list[i].recordDate%></td>
                <td style="text-align: center;"><%=list[i].tranTotal%></td>
                <td style="text-align: center;"><%=list[i].tranSucCnt%></td>
                <td style="text-align: center;"><%=list[i].tranRatio%></td>
                <td style="text-align: center;"><%=list[i].repTotal%></td>
                <td style="text-align: center;"><%=list[i].repSucCnt%></td>
                <td style="text-align: center;"><%=list[i].repRatio%></td>
                <td style="text-align: center;"><%=list[i].lnkTotal%></td>
                <td style="text-align: center;"><%=list[i].lnkSucCnt%></td>
                <td style="text-align: center;"><%=list[i].lnkRatio%></td>
                <td style="text-align: center;"><%=list[i].fakeCnt%></td>
                </tr>
                </tbody>
                <%}%>
            </table>
        </div>
    </div>
</script>
</@layout_default.page>