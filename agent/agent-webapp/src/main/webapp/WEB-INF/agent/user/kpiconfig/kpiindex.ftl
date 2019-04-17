<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='用户管理' page_num=5>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-star"></i> 用户绩效目标管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="user_kpi_form" class="form-horizontal" action="index.vpage" method="post">
                <fieldset>
                    <div class="control-group span4">
                        <label class="control-label">绩效指标</label>
                        <div class="controls">
                            <select id="kpiSelect" name="kpiSelect">
                                <#if kpiList?has_content>
                                    <#list kpiList as kpi>
                                        <option value="${kpi.id}" <#if kpiSelect?? && kpiSelect == kpi.id>selected</#if>>${kpi.kpiName}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                </fieldset>
            </form>
            <div id="userKpiDataTable" class="dataTables_wrapper">
                <#if memberList?has_content>
                    <table class="table table-striped table-bordered">
                        <thead>
                        <tr>
                            <th class="sorting" style="width: 160px;">用户姓名</th>
                            <th class="sorting" style="width: 200px;">绩效考核指标(KPI)</th>
                            <th class="sorting" style="width: 100px;">区域</th>
                            <#if kpiEvalList?has_content>
                                <#list kpiEvalList as kpiEval>
                                    <th class="sorting" style="width: 100px; text-align: center">
                                        考核期间<br/>
                                        ${(kpiEval.getEvalDurationFromString())!""} ~ ${(kpiEval.getEvalDurationToString())!""}
                                    </th>
                                </#list>
                            </#if>
                            <th class="sorting" style="width: 80px;">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                            <#list memberList as member>
                                <#list memberRegion[member.id?string]?keys as regionInfo>
                                <tr class="odd">
                                    <td class="center  sorting_1">${(member.realName)!}(${(member.accountName)!})</td>
                                    <td class="center  sorting_1">
                                        <#list kpiList as kpi>
                                            <#if kpiSelect?? && kpiSelect == kpi.id>${kpi.kpiName}</#if>
                                        </#list>
                                    </td>
                                    <td class="center  sorting_1">
                                        ${memberRegion[member.id?string][regionInfo]}
                                    </td>
                                    <#if kpiEvalList?has_content>
                                        <#list kpiEvalList as kpiEval>
                                            <#assign kpiKey = member.id + "_" + kpiEval.id + "_" + regionInfo />
                                            <td class="center  sorting_1">
                                                <#if memberKpiConfig[kpiKey]?? && memberKpiConfig[kpiKey].kpiTarget??>
                                                    ${memberKpiConfig[kpiKey].kpiTarget!}
                                                <#else>
                                                </#if>
                                            </td>
                                        </#list>
                                    </#if>
                                    <td class="center ">
                                        <#if requestContext.getCurrentUser().isCountryManager()>
                                        <a id="edit_user_kpi_${member.id!}_${kpiSelect}_${regionInfo}" class="btn btn-info" href="#">
                                            <i class="icon-edit icon-white"></i>
                                            编辑
                                        </a>
                                        </#if>
                                    </td>
                                </tr>
                                </#list>
                            </#list>
                        </tbody>
                    </table>
                <#else>
                    暂无数据
                </#if>
            </div>
        </div>
    </div>
</div>

<input type="hidden" id="userId" value="0">
<input type="hidden" id="regionCode" value="0">

<div id="userKpiConfigDialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">编辑用户绩效目标</h4>
            </div>
            <form class="form-horizontal">
                <div id="user_kpi_edit_panel" class="modal-body" style="height: auto; overflow: visible; width: auto">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="saveUserKpiBtn" type="button" class="btn btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
</div>


<script type="text/javascript">

    $("#kpiSelect").live("change",function(){
        $("#user_kpi_form").submit();
    });

    $("a[id^='edit_user_kpi_']").live('click',function(){
        var userKpiKey = $(this).attr("id").substring("edit_user_kpi_".length);
        var userKpiInfo = userKpiKey.split("_");
        editUserKpi(userKpiInfo[0], userKpiInfo[1], userKpiInfo[2]);
    });

    $('#saveUserKpiBtn').live('click',function(){
        saveUserKpi();
    });

    function saveUserKpi() {
        var kpiList = new Array();
        $("input[id^='kpi_target_']").each(function(){
            var kpiTargetId = $(this).attr("id");
            var kpiKey = kpiTargetId.substring("kpi_target_".length);
            var kpiValue =  $(this).val();
            if (kpiValue != '' && !$.isNumeric(kpiValue)) {
                alert("绩效目标必须输入并且为数字类型!");
                return false;
            }
            kpiList.push(kpiKey + "_" + kpiValue)
        });
        var userKpiList = kpiList.join('#');

        $.post('saveuserkpi.vpage',{
            userKpiList: userKpiList
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                $('#userKpiConfigDialog').modal('hide');
                // $("#user_kpi_form").submit();
            }
        });
    }

    function editUserKpi(userId, kpiId, regionCode) {
        $.post('getuserkpi.vpage',{
            userId:userId,
            kpiId:kpiId,
            regionCode:regionCode
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                var editHtml = "";
                editHtml += '<div class="control-group">';
                editHtml += '<label class="control-label" for="focusedInput">用户名</label>';
                editHtml += '<div class="controls">';
                editHtml += '<input id="modalUserName" class="input-xlarge focused" type="text" value="';
                editHtml += data.user.realName + '(' + data.user.accountName + ')" readonly>';
                editHtml += '</div>';
                editHtml += '</div>';
                editHtml += '<div class="control-group">';
                editHtml += '<label class="control-label" for="focusedInput">绩效指标</label>';
                editHtml += '<div class="controls">';
                editHtml += '<input id="modalKpiName" class="input-xlarge focused" type="text" value="';
                editHtml += data.kpiDef.kpiName + '" readonly>';
                editHtml += '</div>';
                editHtml += '</div>';

                for(var i =0; i<data.kpiEvalList.length; i++){
                    editHtml += '<div class="control-group">';
                    editHtml += '<label class="control-label" for="focusedInput">考核周期(';
                    editHtml += data.kpiEvalList[i].evalDurationFromString + '~' + data.kpiEvalList[i].evalDurationToString + ')</label>';
                    editHtml += '<div class="controls">';

                    kpiKey = data.user.id + "_" + data.kpiEvalList[i].id + "_" + data.regionCode;
                    kpiTarget = '';
                    if (data.kpiMap[kpiKey]) {
                        kpiTarget = data.kpiMap[kpiKey].kpiTarget;
                    }
                    kpiTargetId = "kpi_target_" + kpiKey;

                    editHtml += '<input id="' + kpiTargetId +'" class="input-xlarge focused" type="text" value="' + kpiTarget + '">';
                    editHtml += '</div>';
                    editHtml += '</div>';
                }
                $('#user_kpi_edit_panel').html(editHtml);
                $('#regionCode').val(data.regionCode);
                $('#userKpiConfigDialog').modal('show');
            }
        });
    }

</script>
</@layout_default.page>
