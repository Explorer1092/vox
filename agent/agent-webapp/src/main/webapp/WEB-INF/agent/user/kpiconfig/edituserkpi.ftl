<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加系统权限' page_num=5>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加/编辑用户绩效指标</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户名</label>
                        <div class="controls">
                            <input id="account_name" class="input-xlarge focused" type="text" value="${(memberKpiConfig.user.realName)!}" readonly="readonly">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">绩效指标</label>
                        <div class="controls">
                            <input id="kpiName" class="input-xlarge focused" type="text" value="${(memberKpiConfig.kpiEval.kpiDef.kpiName)!}" readonly="readonly">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="focusedInput">考核目标</label>
                        <div class="controls">
                            <input id="user_kpitarget" class="input-xlarge focused" type="text" value="${(memberKpiConfig.kpiTarget)!}">
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="add_user_kpi_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="${requestContext.webAppContextPath}/user/kpiconfig/index.vpage"> 取消 </a>
                    </div>
                    <input type="hidden" id="userId" value="${(memberKpiConfig.user.id)!}">
                    <input type="hidden" id="kpiEvalId" value='${(memberKpiConfig.kpiEval.id)!}'>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>

<script type="text/javascript">
    //验证是否未定义或null或空字符串
    function isBlank(str){
        return str == 'undefined' || String(str) == 'null' || $.trim( str ) == '';
    }

    $(function(){
        $('#add_user_kpi_btn').live('click',function(){
            var accountName = $('#account_name').val().trim();
            var kpiName = $('#kpiName').val().trim();
            var userKpiTarget = $('#user_kpitarget').val().trim();
            if(isBlank(accountName)){
                alert("用户名不能为空");
                return false;
            }
            if(isBlank(kpiName)){
                alert("绩效名称不能为空");
                return false;
            }
            if(isBlank(userKpiTarget)){
                alert("绩效目标值不能为空");
                return false;
            }

            $.post('saveuserkpi.vpage',{
                userId    : $("#userId").val(),
                kpiEvalId : $("#kpiEvalId").val(),
                kpiTarget : userKpiTarget
            },function(data){
                if(data.success){
                    alert(data.info);
                    setTimeout(function(){
                        window.location.href = "${requestContext.webAppContextPath}/user/kpiconfig/index.vpage";
                    },800);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });
    });
</script>

</@layout_default.page>