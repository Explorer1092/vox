<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="审核平台" page_num=21>
<style>
    .product_content li:nth-child(4){
        width:250px;
    }
    .product_content li{
        text-decoration: none;
        list-style:none;
        float:left;
        width:200px;
    }
    #evaluate_table tr {
        border-bottom: 1px solid #e0e0e0;
    }
    #evaluate_table th {
        text-align:center;
        width:90px;
    }
    #evaluate_table th:nth-child(6){
        width:290px
    }
</style>

<div class="pic_alert" style="width:100%;height: 100%;position:fixed;top:0;background: #000;z-index:111;display:none">
    <img style="width:30%;vertical-align: middle;position:absolute;left:35%;top:10%" src="" alt="">
</div>
<div class="span9">
    <fieldset><legend>产品反馈</legend></fieldset>
    <#include 'product_content.ftl'>
    <div class="form-horizontal">
    <#if applyData?has_content && applyData.apply?has_content>
            <div class="modal-body" style="height: auto; overflow: visible;">
                <input type="hidden" id="schoolId" name="schoolId" value="${applyData.apply.schoolId!}"/>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学科</strong></label>
                    <div class="controls">
                        <select id="teacherSubject" name="teacherSubject">
                            <option value="" <#if !applyData.apply.teacherSubject?has_content>selected </#if>></option>
                            <#list subjectList as item>
                                <option value="${item.id}" <#if applyData.apply.teacherSubject?has_content && item.id == applyData.apply.teacherSubject.id >selected </#if>>${item.desc!}</option>
                            </#list>
                        </select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>反馈类型</strong></label>
                    <div class="controls">
                        <select id="feedbackType" name="feedbackType"  onchange="changeFeedbackType()">
                            <#list typeList as item>
                                <option value="${item.type}" <#if applyData.apply.feedbackType?has_content && item.type == applyData.apply.feedbackType.type >selected </#if>>${item.desc!}</option>
                            </#list>
                        </select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>三级分类</strong></label>
                    <div class="controls">
                        <select id="firstCategory" name="firstCategory" onchange="secondCategories()" <#if applyData.apply.firstCategory?has_content >category="${applyData.apply.firstCategory!}"</#if>></select>
                        <select id="secondCategory" name="secondCategory" onchange="thirdCategories()" <#if applyData.apply.secondCategory?has_content >category="${applyData.apply.secondCategory!}"</#if>></select>
                        <select id="thirdCategory" name="thirdCategory" <#if applyData.apply.thirdCategory?has_content >category="${applyData.apply.thirdCategory!}"</#if>></select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>处理意见</strong></label>
                    <div class="controls">
                        <#if processList?has_content>
                            <#list processList as item>
                                <input type="radio" name="processResult" value="${item.type}" onclick=""/> ${item.desc}
                            </#list>
                        </#if>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>回复内容</strong></label>
                    <div class="controls">
                        <textarea id="processNote" name="processNote" rows="5" style="width:400px" maxlength="500" placeholder="最多输入500字"></textarea>
                    </div>
                </div>

                <div  class="control-group pm_hide" style="display:none">
                    <label class="col-sm-2 control-label"><strong>指定PM</strong></label>
                    <div class="controls">
                        <select id="pmData" name="pmData">
                            <option value=""></option>
                            <#list pmList as item>
                                <option value="${item.account}" userPlatform="${item.userPlatform}">${item.accountName!}</option>
                            </#list>
                        </select>
                    </div>
                </div>

                <div class="control-group so_hide" style="display:none">
                    <label class="col-sm-2 control-label"><strong>指定销运</strong></label>
                    <div class="controls">
                        <select id="soData" name="soData">
                            <option value=""></option>
                            <#list soList as item>
                                <option value="${item.account}" userPlatform="${item.userPlatform}">${item.accountName!}</option>
                            </#list>
                        </select>
                    </div>
                </div>

                <#if applyData.apply.noticeFlag?has_content && applyData.apply.noticeFlag>
                    <div class="control-group">
                        <label class="col-sm-2 control-label"><strong>感谢老师</strong></label>
                        <div class="controls" style="margin-top:15px;">
                            <lable><input id="sendFlag"  name="sendFlag" type="checkbox" class="pop_condition" onclick="setNoticeContentStatus();"  style="margin-top: -3px;"/> 给老师发送感谢消息</lable>
                        </div>
                        <div class="controls" style="margin-top:15px;">
                            <input type="radio" name="thanksTeacher" value="感谢您的反馈，我们会认真考虑您提出的建议，您的声音是我们进步的源泉。" onclick="onThanks()" disabled="disabled"/> 感谢您的反馈，我们会认真考虑您提出的建议，您的声音是我们进步的源泉。
                        </div>
                        <div class="controls" style="margin-top:15px;">
                            <input type="radio" name="thanksTeacher" value="感谢您的反馈，我们正在积极处理中，近期会有所优化，请您耐心等待。" onclick="onThanks()" disabled="disabled"/> 感谢您的反馈，我们正在积极处理中，近期会有所优化，请您耐心等待。
                        </div>
                        <div class="controls" style="margin-top:15px;">
                            <input type="radio" class="thanksTeacher" name="thanksTeacher" value="" onclick="" disabled="disabled"/> <textarea id="noticeContent" name="noticeContent" maxlength="38" placeholder="最多输入38字" rows="2" style="width:500px" disabled="disabled"></textarea>
                        </div>
                    </div>
                </#if>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong></strong></label>
                    <div class="controls">
                        <button type="button" class="btn btn-default" data-dismiss="modal" onclick="processFunction(${applyData.apply.id}, ${applyData.apply.workflowId})">提交</button>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>处理意见区</strong></label>
                    <div class="controls">
                        <table class="table table-striped table-bordered" style="font-size: 14px;">
                            <thead>
                            <tr>
                                <th>审核日期</th>
                                <th>审核人</th>
                                <th>处理结果</th>
                                <th>处理意见</th>
                            </tr>
                            </thead>
                            <#if applyData.processResultList?has_content>
                                <#list applyData.processResultList as processResult>
                                    <tr>
                                        <td><#if processResult.processDate?has_content>${processResult.processDate?string("yyyy-MM-dd")}</#if></td>
                                        <td>${processResult.accountName!}</td>
                                        <td>${processResult.result!}</td>
                                        <td>${processResult.processNotes!}</td>
                                    </tr>
                                </#list>
                            </#if>
                        </table>
                    </div>
                </div>
            </div>
        </#if>
    </div>
</div>


<script type="text/javascript">
    $(document).on('click','input[name="processResult"]',function(){
        if($("input[name='processResult']:checked").val() == 2){
            $('.so_hide').hide();
            $('.pm_hide').hide();
        }else if($("input[name='processResult']:checked").val() == 1){
            $('.so_hide').hide();
            $('.pm_hide').show();
        }else if($("input[name='processResult']:checked").val() == 3){
            $('.pm_hide').hide();
            $('.so_hide').show();
        }
    });
    $(document).on('click','.pic_show',function(){
        var pic_src = $(this).find('img').attr('src');
        $('.pic_alert').show().find('img').attr('src',pic_src);
    });
    $(document).on('click','.pic_alert',function(){
        $(this).hide();
    });
    var CATEGORIES = ${category!''};

    $(function () {
        firstCategories();
    });

    function setNoticeContentStatus(){
        var sendFlag = $("#sendFlag").is(':checked');
        var thanksTeacher = $('.thanksTeacher').is(':checked');
        if(!sendFlag){
            $('input[name="thanksTeacher"]').attr('disabled',true);
            $("#noticeContent").attr("disabled",true);
        }else{
            $('input[name="thanksTeacher"]').attr('disabled',false);
        }
    }
    function onThanks(){
        $("#noticeContent").attr("disabled",true);
    }
    $(document).on('click','.thanksTeacher',function(){
        $("#noticeContent").attr("disabled",false);
    });
    $(document).on('blur','#noticeContent',function(){
        $('.thanksTeacher').val($(this).val());
        console.log($(this).val())
    });
    function changeFeedbackType(){
        var feedbackType = $("#feedbackType").val();
        $.post('/crm/productfeedback/load_category.vpage',{
            typeId:feedbackType
        },function(data){
            if(data.success){
                CATEGORIES = JSON.parse(data.category);
                firstCategories();
            }else{
                alert(data.info)
            }
        });
    }
    function firstCategories() {
        $("#firstCategory").empty();
        if (CATEGORIES != null) {
            $("#firstCategory").append("<option value=''>请选择</option>");
            for (var first in CATEGORIES) {
                $("#firstCategory").append("<option value='" + first + "'>" + first + "</option>");
            }
        }
        var category = $("#firstCategory").attr("category");
        if (!blankString(category)) {
            $("#firstCategory").val(category);
        }
        secondCategories();
    }
    function secondCategories() {
        $("#secondCategory").empty();
        var first = $("#firstCategory").val();
        if (CATEGORIES != null && !blankString(first)) {
            var seconds = CATEGORIES[first];
            $("#secondCategory").append("<option value=''>请选择</option>");
            for (var second in seconds) {
                $("#secondCategory").append("<option value='" + second + "'>" + second + "</option>");
            }
        }
        var firstDefault = $("#firstCategory").attr("category");
        var category = $("#secondCategory").attr("category");
        if (!blankString(firstDefault) && first== firstDefault && !blankString(category)) {
            $("#secondCategory").val(category);
        }
        thirdCategories();
    }
    function thirdCategories() {
        $("#thirdCategory").empty();
        var first = $("#firstCategory").val();
        var second = $("#secondCategory").val();
        if (CATEGORIES != null && !blankString(first) && !blankString(second)) {
            var thirds = CATEGORIES[first][second];
            $("#thirdCategory").append("<option value=''>请选择</option>");
            for (var i in thirds) {
                var third = thirds[i];
                $("#thirdCategory").append("<option value='" + third + "'>" + third + "</option>");
            }
        }
        var category = $("#thirdCategory").attr("category");
        if (!blankString(category)) {
            $("#thirdCategory").val(category);
        }
    }


    function processFunction(feedbackId, workflowId){
        var checkResult = checkInputData();
        if(checkResult ){
            var processResult = $("input[name='processResult']:checked").val();
            if(blankString(processResult)){
                alert("请选择处理意见！");
                return false;
            }
            var tip = "";
            if(processResult == 1){
                tip = "通过";
            }else if(processResult == 2){
                tip = "驳回";
            }else if(processResult == 3){
                tip = "转发";
            }
            if(confirm("确认" + tip + "该请求吗？")){
                var teacherSubject = $("#teacherSubject").val();
                var feedbackType = $("#feedbackType").val();
                var firstCategory = $("#firstCategory").val();
                var secondCategory = $("#secondCategory").val();
                var thirdCategory = $("#thirdCategory").val();
                var pmAccount = $("#pmData").val();
                var userPlatform = $("#pmData").find("option:selected").attr("userPlatform");
                var pmAccountName = $("#pmData").find("option:selected").text();

                var soAccount = $("#soData").val();
                var soPlatform = $("#soData").find("option:selected").attr("userPlatform");
                var soAccountName = $("#soData").find("option:selected").text();

                var sendFlag = $("#sendFlag").is(':checked');
                var noticeContent = "";
                if(sendFlag){
                    noticeContent = $("input[name='thanksTeacher']:checked").val();
                }

                var processNote = $("#processNote").val();

                var processUserList = new Array();
                if(processResult ==1){  // 同意的情况，指定PM
                    processUserList.push({"userPlatform":userPlatform, "account":pmAccount, "accountName":pmAccountName});
                }else if(processResult == 3){  // 转发的情况，指定销运
                    processUserList.push({"userPlatform":soPlatform, "account":soAccount, "accountName":soAccountName});
                }

                var processUsers = JSON.stringify(processUserList);
                var updateDate = {
                    feedbackId:feedbackId,
                    teacherSubject:teacherSubject,
                    feedbackType:feedbackType,
                    firstCategory:firstCategory,
                    secondCategory:secondCategory,
                    thirdCategory:thirdCategory,
                    sendFlag:sendFlag,
                    noticeContent:noticeContent
                };
                if(processResult ==1){
                    updateDate.pmAccount = pmAccount;
                    updateDate.pmAccountName = pmAccountName;
                }
                $.post('/crm/productfeedback/update.vpage', updateDate, function (data) {
                    if(data.success){
                        $.post('process.vpage',{
                            processResult:processResult,
                            workflowId:workflowId,
                            processNote:processNote,
                            processUsers: processUsers
                        },function(data){
                            if(data.success){
                                //location.href = "todo_list.vpage";
                                history.back();
                            }else{
                                alert('提交失败');
                            }
                        });
                    }else{
                        alert('提交失败');
                    }
                });
            }
        }
    }

    function checkInputData(){
       /* var teacherSubject = $("#teacherSubject").val();
        if(blankString(teacherSubject)){
            alert("请选择学科！");
            return false;
        }*/
        var feedbackType = $("#feedbackType").val();
        if(blankString(feedbackType)){
            alert("请选择反馈类型！");
            return false;
        }
        var firstCategory = $("#firstCategory").val();
        var secondCategory = $("#secondCategory").val();
        var thirdCategory = $("#thirdCategory").val();

        var processResult = $("input[name='processResult']:checked").val();
        if(blankString(processResult)){
            alert("请选择处理意见！");
            return false;
        }
        var processNote = $("#processNote").val();
        if(blankString(processNote)){
            alert("请填写回复内容！");
            return false;
        }

        if(processResult == 1){ // 采纳时必须指定PM
            var pmAccount = $("#pmData").val();
            if(blankString(pmAccount)){
                alert("请选择PM！");
                return false;
            }
        }else if(processResult == 3){ // 转发时必须指定销运
            var soAccount = $("#soData").val();
            if(blankString(soAccount)){
                alert("请选择销运！");
                return false;
            }
        }

        var sendFlag = $("#sendFlag").is(':checked');
        if(sendFlag){
            noticeContent = $("input[name='thanksTeacher']:checked").val();
            if(blankString(noticeContent)){
                alert("请填写感谢内容！");
                return false;
            }
        }
        return true;
    }


</script>
</@layout_default.page>
