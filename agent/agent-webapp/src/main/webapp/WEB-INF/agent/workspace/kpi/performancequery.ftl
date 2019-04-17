<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='用户管理' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-search"></i> 业绩查询</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                &nbsp;
            </div>
        </div>

        <div class="box-content">
            <form id="user_kpi_form"  action="loaduserkpi.vpage" method="post">
                <fieldset>
                    <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isRegionManager()>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">省级以上区域</label>
                        <div class="controls">
                            <select id="groupSelect" name="groupSelect">
                                <#if groupList?has_content>
                                    <#list groupList as group>
                                        <option value="${group.id}">${group.groupName}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    </#if>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">市场人员姓名</label>
                        <div class="controls">
                            <select id="userSelect" name="userSelect">
                                <#if userList?has_content>
                                    <#list userList as user>
                                        <option value="${user.id}">${user.realName}</option>
                                    </#list>
                                </#if>
                            </select>
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">绩效指标</label>
                        <div class="controls">
                            <select id="kpiSelect" name="kpiSelect"></select>
                        </div>
                    </div>
                    <div class="control-group span3">
                        <label class="control-label" for="selectError3">绩效周期</label>
                        <div class="controls">
                            <select id="kpiEvalSelect" name="kpiEvalSelect"></select>
                        </div>
                    </div>
                </fieldset>
            </form>
            <div id="userKpiDataTable" class="dataTables_wrapper">
                <table class="table table-striped table-bordered">
                    <thead>
                        <tr>
                            <th class="sorting" style="width: 280px;">绩效考核指标(KPI)</th>
                            <th class="sorting" style="width: 160px;">时间</th>
                            <th class="sorting" style="width: 80px;">完成数量</th>
                        </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var userKpiCache = {};

    <#-- 省代区域变更，刷新用户列表 -->
    $("#groupSelect").live("change",function(){
        var $this = $(this);
        loadUser($this.val());
    });

    <#--用户列表-->
    $("#userSelect").live("change",function(){
        var $this = $(this);
        addKpiList($this.val());
    });
    <#--绩效目标下拉框-->
    $("#kpiSelect").live("change",function(){
        var kpiId = $(this).val();
        fillKpiEvalSelect(kpiId, userKpiCache[0]);
    });

    <#--考核审核期间--->
    $("#kpiEvalSelect").live("change",function(){
        queryFormResult($("#userSelect").val(),$(this).val());
    });

    $(function(){
        addKpiList($("#userSelect").val());
    });

    <#--- Load Group Users -->
    function loadUser(groupId){
        $.post('getgroupuser.vpage',{
            groupId:groupId
        },function(data){
            if(!data.success){
                alert(data.info);
            }else{
                $('#userSelect').html('');
                for(var i=0; i<data.userList.length; i++){
                    $('<option value="'+data.userList[i].id+'">'+data.userList[i].realName+'</option>').appendTo($('#userSelect'));
                }

                addKpiList($('#userSelect').val());
            }
        });
    }

    <#---渲染绩效-->
    function addKpiList(userId){
        $.getJSON("getuserkpi.vpage",{userId : userId},function(data){
            if(data.success){
                userKpiCache[0] = data.userKpi;
                fillKpiSelect(data.userKpi);
            }
        });
    }

    function fillKpiSelect(kpiList){
        if(!$.isArray(kpiList)){
            return;
        }
        $("#kpiSelect").empty();
        $("#kpiEvalSelect").empty();
        if(kpiList.length > 0){
            for(var i = 0; i < kpiList.length; i++){
                var option = $("<option>");
                option.val(kpiList[i].id);
                option.text(kpiList[i].name);
                $("#kpiSelect").append(option);
            }
            fillKpiEvalSelect(kpiList[0].id, kpiList);
        }
    }

    function fillKpiEvalSelect(kpiId, kpiList){
        for(var i = 0; i < kpiList.length; i++){
            if(kpiId == kpiList[i].id){
               $("#kpiEvalSelect").empty();
               for(var j = 0; j < kpiList[i].kpiEvalList.length; j++){
                   var option = $("<option>");
                   option.val(kpiList[i].kpiEvalList[j].id);
                   option.text(kpiList[i].kpiEvalList[j].evalDurationFromString + "~" + kpiList[i].kpiEvalList[j].evalDurationToString);
                   $("#kpiEvalSelect").append(option);
               }
               queryFormResult($("#userSelect").val(),kpiList[i].kpiEvalList[0].id);
               break;
            }
        }

    }

    function queryFormResult(userId,kpiEvalId){
        $.post('performancequerychip.vpage',{
            userId:userId,
            kpiEvalId:kpiEvalId
        },function(data){
            $("#userKpiDataTable").html(data);
        });
    }
</script>
</@layout_default.page>
