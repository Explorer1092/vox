<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=3>
<style>
    body{
        text-shadow:none;
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 字典表调整申请</h2>
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
                        <label class="control-label">调整类别</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="radio" name="modifyType" value="1" />添加学校
                            </label>
                            <label class="control-label">
                                <input type="radio" name="modifyType" value="2" />删除学校
                            </label>
                            <label class="control-label">
                                <input type="radio" name="modifyType" value="3" />业务变更
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">选择学校</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="text" id="schoolId" name="schoolId" placeholder="输入学校ID"/>
                            </label>
                            <label class="control-label" style="margin-left:90px;width:50px">
                                <button id="queryBtn" type="button" class="btn btn-success" style="width: 50px;">查询</button>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学校ID</label>
                        <div class="controls">
                            <label class="control-label" id="schoolIdLabel" style="width:300px;text-align:left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学校名称</label>
                        <div class="controls">
                            <label class="control-label" id="schoolName" style="width:300px;text-align:left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">所属地区</label>
                        <div class="controls">
                            <label class="control-label" id="regionName" style="width:300px;text-align:left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学校阶段</label>
                        <div class="controls">
                            <label class="control-label" id="schoolLevel" style="width:300px;text-align:left"></label>
                        </div>
                    </div>
                    <div class="control-group schoolManager" hidden>
                        <label class="control-label">学校负责人</label>
                        <div class="controls">
                            <select id="schoolManager" name="phase">
                                <#if requestContext.getCurrentUser().isBusinessDeveloper()><option value="${requestContext.getCurrentUser().userId}" selected>${requestContext.getCurrentUser().realName}</option></#if>
                                <#if bdUserList?? && bdUserList?size gt 0>
                                    <option value="0" selected>暂不分配</option>
                                    <#list bdUserList as data>
                                        <option <#if data.selected?? && data.selected>selected</#if> value="${data.id!0}">${data.realName!''}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group schoolPopularity" style="display: none;">
                        <label class="control-label">学校等级</label>
                        <div class="controls">
                            <#if schoolPopularityList?has_content>
                                <#list schoolPopularityList as item>
                                    <label style="text-align: left;float: left;padding-right: 20px;top: 50px;padding-top: 5px;">
                                        <input type="radio" name="schoolPopularity" value="${item.level}" />${item.level!}<#if "E" != item.level >：${item.describe!}</#if>
                                    </label>
                                </#list>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls" style="margin-left:50px;color:red">提示：每月后5天提交的申请当月不一定能审核完成，如需计算在当月业绩中一定要提前申请！</div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">该校历史申请记录</label>
                    </div>

                    <div class="dataTables_wrapper" role="grid">
                        <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                            <thead>
                                <tr>
                                    <th class="sorting" style="width: 60px;">申请日期</th>
                                    <th class="sorting" style="width: 60px;">申请人</th>
                                    <th class="sorting" style="width: 60px;">申请类型</th>
                                    <th class="sorting" style="width: 60px;">备注</th>
                                    <th class="sorting" style="width: 60px;">审核结果</th>
                                    <th class="sorting" style="width: 60px;">调整原因</th>
                                    <th class="sorting" style="width: 120px;">审核情况</th>
                                </tr>
                            </thead>
                            <tbody>

                            </tbody>
                        </table>
                    </div>

                    <div class="control-group">
                        <label class="control-label">调整原因</label>
                        <div class="controls">
                            <textarea class="input-xlarge" id="comment" rows="5" style="width: 880px;" maxlength="180" placeholder="最多输入180字"></textarea>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="submitBtn" type="button" class="btn btn-primary">提交申请</button>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">

    $(document).on('click','input[name="modifyType"]',function(){
       var _this = $(this).val();
       if(_this == 1){
           $('.schoolManager').show();
       }else{
           $('.schoolManager').hide();
       }
       if(_this == 2){
           for (var i = 0; i< $('input[name="schoolPopularity"]').length;i++){
               if(typeof ($('input[name="schoolPopularity"]').eq(i).attr('checked')) == 'undefined') {
                   $('input[name="schoolPopularity"]').eq(i).parent().hide();
               }else{
                   $('input[name="schoolPopularity"]').eq(i).parent().show();
               }
           }
       }else{
           $('.schoolPopularity #uniform-undefined span').show();
        }
    });

    $(function(){
        $('#queryBtn').live('click',function(){
            $('.schoolPopularity').hide();
            var schoolId = $('#schoolId').val().trim();
            $.get('search_school.vpage',{
                schoolId : schoolId
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    var school = data.school;
                    var regionName = data.regionName;
                    var schoolPopularity = data.schoolPopularity == null? "" : data.schoolPopularity;
                    var historyApplyList = data.historyApplyList;
                    $('#targetSchoolId').val(school.id);
                    $('#schoolIdLabel').html(school.id);
                    $('#schoolName').html(school.cname);
                    $('#regionName').html(regionName);
                    if(school.level == 1){
                        $('#schoolLevel').html("小学");
                    }else if(school.level == 2){
                        $('#schoolLevel').html("初中");
                    }else if(school.level == 4){
                        $('#schoolLevel').html("高中");
                    }else if(school.level == 5){
                        $('#schoolLevel').html("学前");
                    }
                    $('#schoolLevel').attr("schoolLevel",school.level)

                    if(school.level == 2 || school.level == 4){
                        $('.schoolPopularity').show();
                    }
                    $("input[name='schoolPopularity'][value='" + schoolPopularity + "']").attr("checked","checked");

                    for (var i = 0; i< $('input[name="schoolPopularity"]').length;i++) {
                        if ($('input[name="schoolPopularity"]').eq(i).val() == schoolPopularity) {
                            $("input[name='schoolPopularity'][value='" + schoolPopularity + "']").eq(i).parent().addClass('checked');
                        } else {
                            $("input[name='schoolPopularity']").eq(i).parent().removeClass('checked');
                        }
                    }

                    if(historyApplyList.length != 0){
                        for(var i=0; i< historyApplyList.length; i++){
                            var trData = "<tr>";
                            trData += "<td>" + historyApplyList[i].createDatetime + "</td>";
                            trData += "<td>" + historyApplyList[i].accountName + "</td>";
                            if(historyApplyList[i].modifyType == 2){
                                trData += "<td class='tdList'>" + "删除学校" + "</td>";
                            }else if (historyApplyList[i].modifyType == 1) {
                                trData += "<td class='tdList'>" + "添加学校" + "</td>";
                            }else if (historyApplyList[i].modifyType == 3) {
                                trData += "<td class='tdList'>" + "业务变更" + "</td>";
                            }
                            if(historyApplyList[i].modifyDesc != null){
                                var desc = historyApplyList[i].modifyDesc;
                                desc = desc.replace(/\r\n/g, "<br/>");
                                trData += "<td>" + desc + "</td>";
                            }else{
                                trData += "<td>" + '' + "</td>";
                            }
                            trData += "<td>" + historyApplyList[i].status + "</td>";
                            trData += "<td>" + historyApplyList[i].comment + "</td>";
                            trData += "<td>" + historyApplyList[i].processFlow + "</td>";
                            trData += "</tr>";
                        }
                        $('#historyApplyTable tbody').html("");
                        $('#historyApplyTable tbody').append(trData);
                    }else{
                        $('#historyApplyTable tbody').html("");
                    }
                }
            });
        });
        console.log($('.tdList').val());

        $('#submitBtn').live('click',function(){
            var modifyType = $('input[name="modifyType"]:checked').val();
            if(modifyType != "1" && modifyType != "2" && modifyType != "3" ){
                alert("请选择调整类别！");
                return;
            }

            var targetSchoolId = $('#targetSchoolId').val().trim();
            if(targetSchoolId == ""){
                alert("请选择学校！");
                return;
            }

            var schoolPopularity = "";
            var schoolLevel = $('#schoolLevel').attr("schoolLevel")
            if ((schoolLevel == 2 || schoolLevel == 4) && modifyType != "2") {
                var selectRadio = $('input[name="schoolPopularity"]:checked');
                if(selectRadio.length < 1){
                    alert("请设置学校等级！");
                    return;
                }
                schoolPopularity = selectRadio.val();
            }

            var comment = $('#comment').val().trim();
            if(comment == ""){
                alert("请填写调整原因！");
                return ;
            }
            $.post('submit_dictschool_apply.vpage',{
                modifyType : modifyType,
                schoolId : targetSchoolId,
                schoolPopularity : schoolPopularity,
                targetUserId:$('#schoolManager>option:selected').val(),
                comment : comment
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    if(confirm("提交成功") == true){
                        window.location.href="/apply/view/list.vpage";
                    }
                }
            });
        });
    });
</script>

</@layout_default.page>
