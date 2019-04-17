<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="审核平台" page_num=21>
<style>
    .product_content li:nth-child(1){
        margin-left:50px
    }
    .product_content li{
        text-decoration: none;
        list-style:none;
        float:left;
        margin-left:100px
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
    <input type="hidden" id="currentAccount" value="${currentAccount!}" />
    <#include 'product_content.ftl'>
    <div class="form-horizontal">
    <#if applyData?has_content && applyData.apply?has_content>
            <div class="modal-body" style="height: auto; overflow: visible;">
                <input type="hidden" id="schoolId" name="schoolId" value="${applyData.apply.schoolId!}"/>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学科</strong></label>
                    <div class="controls">
                        <label class="control-label" id="regionName" style="text-align: left;"><#if applyData.apply.teacherSubject?has_content>${applyData.apply.teacherSubject.desc!}</#if></label>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>反馈类型</strong></label>
                    <div class="controls">
                        <label class="control-label" id="regionName" style="text-align: left;"><#if applyData.apply.feedbackType?has_content>${applyData.apply.feedbackType.desc!}</#if></label>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>三级分类</strong></label>
                    <div class="controls">
                        <label class="control-label" id="regionName" style="text-align: left;">
                            <#if applyData.apply.firstCategory?has_content>${applyData.apply.firstCategory!}</#if>
                            <#if applyData.apply.secondCategory?has_content>/${applyData.apply.secondCategory!}</#if>
                            <#if applyData.apply.thirdCategory?has_content>/${applyData.apply.thirdCategory!}</#if>
                        </label>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>处理意见</strong></label>
                    <div class="controls">
                        <#if processList?has_content>
                            <#list processList as item>
                                <input type="radio" name="processResult" value="${item.type}" /> ${item.desc}
                            </#list>
                            <input type="radio" name="processResult" value="31" /> 需评估（评估完成后可在待处理中再回复）
                        </#if>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>回复内容</strong></label>
                    <div class="controls">
                        <textarea id="processNote" name="processNote" rows="3" style="width:400px" maxlength="最多输入1000字" placeholder="最多输入1000字"></textarea>
                    </div>
                </div>

                <div id="pmDiv" class="control-group" style="display: none">
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

                <div id="maybeDay" class="control-group"  style="display: none">
                    <label  class="col-sm-2 control-label"><strong>预计上线日期</strong></label>
                    <div class="controls">
                        <input id="onlineEstimateDate" class="input-medium" type="text" value="${applyData.apply.onlineEstimateDate!''}">
                    </div>
                </div>

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
        if($("input[name='processResult']:checked").val() == 1){
            $('#maybeDay').show();
        }else{
            $('#maybeDay').hide();
        }

    if($("input[name='processResult']:checked").val() == 3){
        $('#pmDiv').show();
    }else{
        $('#pmDiv').hide();
    }
});
$(document).on('click','.pic_show',function(){
    var pic_src = $(this).find('img').attr('src');
    $('.pic_alert').show().find('img').attr('src',pic_src);
});
$(document).on('click','.pic_alert',function(){
    $(this).hide();
});


    $(function () {
        var date = new Date();
        $("#onlineEstimateDate").datepicker({
            dateFormat: 'yy-mm',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            changeMonth: true,
            changeYear: true,
            minDate: new Date(),
            onClose: function (dateText, inst) {
                    var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
                    var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
                $(this).datepicker('setDate', new Date(year, month, 1));
            },
            beforeShow: function () {
                $(".ui-datepicker-calendar").hide();
            }
        });
    });

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
            }else if(processResult == 31){
                tip = "评估";
            }
            if(confirm("确认" + tip + "该请求吗？")){
                var processNote = $("#processNote").val();
                var onlineEstimateDate = $("#onlineEstimateDate").val();

                if(processResult == 2){ // PM驳回, 任务流向销运，做最终确认
                    var processUsers = "";
                    var currentAccount = $("#currentAccount").val();
                    <#if applyData?? && applyData.processResultList?? && applyData.processResultList?size gt 0>
                        <#assign key_index = applyData.processResultList?size>
                        var account = '${applyData.processResultList[key_index-1].account!''}';
                        var accountName = '${applyData.processResultList[key_index-1].accountName!''}';
                        var processUserList = new Array();
                        processUserList.push({"userPlatform":'admin', "account":account, "accountName":accountName});
                        processUsers = JSON.stringify(processUserList);
                    </#if>

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
                            alert(data.info);
                        }
                    });
                }else if(processResult == 1) { // 反馈被采纳的情况下， 更新预计上线时间
                    $.post('/crm/productfeedback/update_online_data.vpage',{
                        feedbackId:feedbackId,
                        onlineEstimateDate:onlineEstimateDate
                    },function(data){
                        if(data.success){
                            $.post('process.vpage',{
                                processResult:processResult,
                                workflowId:workflowId,
                                processNote:processNote
                            },function(data){
                                if(data.success){
                                    //location.href = "todo_list.vpage";
                                    history.back();
                                }else{
                                    alert(data.info);
                                }
                            });
                        }else{
                            alert(data.info);
                        }
                    });
                }else if(processResult == 3) { // 反馈被转发的情况下， 更新预计上线时间
                    var pmAccount = $("#pmData").val();
                    var userPlatform = $("#pmData").find("option:selected").attr("userPlatform");
                    var pmAccountName = $("#pmData").find("option:selected").text();
                    var processUserList = new Array();
                    processUserList.push({"userPlatform":userPlatform, "account":pmAccount, "accountName":pmAccountName});
                    var processUsers = JSON.stringify(processUserList);
                    $.post('/crm/productfeedback/update_pm_data.vpage',{
                        feedbackId:feedbackId,
                        pmAccount:pmAccount,
                        pmAccountName:pmAccountName
                    },function(data){
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
                                    alert(data.info);
                                }
                            });
                        }else{
                            alert(data.info);
                        }
                    });
                }else if(processResult == 31) { // 需评估， 待评估的情况下进行任务转发操作， 提交一次转发操作，并且将任务转发给自己

                    var processUsers = "";
                    var currentAccount = $("#currentAccount").val();
                    if(currentAccount != ""){
                        var option = $("#pmData option[value='" + currentAccount + "']");
                        if(option.length > 0){
                            var userPlatform = option.attr("userPlatform");
                            var pmAccountName = option.text();
                            var processUserList = new Array();
                            processUserList.push({"userPlatform":userPlatform, "account":currentAccount, "accountName":pmAccountName});
                            processUsers = JSON.stringify(processUserList);
                        }
                    }

                    processResult = 3; // 执行转发操作
                    $.post('/crm/productfeedback/update_feedback_status.vpage',{
                        feedbackId:feedbackId,
                        feedbackStatus:6
                    },function(data){
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
                                    alert(data.info);
                                }
                            });
                        }else{
                            alert(data.info);
                        }
                    });
                }
            }
        }
    }

    function checkInputData(){

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

        if(processResult == 1){ // 处理意见为“采纳”的，需要选择“预计上线日期”（格式：yyyy-MM）
            var onlineEstimateDate = $("#onlineEstimateDate").val();
            if(blankString(onlineEstimateDate)){
                alert("请设置预计上线日期！");
                return false;
            }
        }else if(processResult == 3){ // 处理意见为“转发”的，需要选择 PM
            var pmAccount = $("#pmData").val();
            if(blankString(pmAccount)){
                alert("请选择PM！");
                return false;
            }
        }
        return true;
    }


</script>
</@layout_default.page>
