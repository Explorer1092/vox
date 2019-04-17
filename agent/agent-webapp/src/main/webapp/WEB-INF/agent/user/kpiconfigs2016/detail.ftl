<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='用户KPI更新用户预算' page_num=5>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加/编辑用户KPI更新用户预算</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>出错啦！ ${error!}</strong>
            </div>
        </#if>
        <div class="box-content">
            <form id="edit_config_form" class="form-horizontal" method="post" action="saveconfig.vpage" enctype="multipart/form-data" >
                <fieldset>
                    <input id="mode" name="mode" type="hidden" value="${mode}">
                    <input id="configId" name="configId" type="hidden" value="<#if kpiConfig??>${kpiConfig.id}</#if>">
                    <input id="userId" name="userId" type="hidden" value="<#if kpiConfig??>${kpiConfig.userId}</#if>">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户账户</label>
                        <div class="controls">
                            <input id="account" name="account" type="text" onblur="loadUserByAccount();" onkeypress="if(event.keyCode==13){loadUserByAccount();}" <#if userAccount??>value="${userAccount!''}" disabled</#if>>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户姓名</label>
                        <div class="controls">
                            <input id="userName" name="userName" type="text" disabled value="<#if kpiConfig??>${kpiConfig.userName}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户角色</label>
                        <div class="controls">
                            <select id="userRole" name="userRole">
                                <option value="">-请选择-</option>
                                <option value="学校专员" <#if kpiConfig??><#if kpiConfig.userRole == '学校专员'> selected</#if></#if>>学校专员</option>
                                <#--<option value="市经理" <#if kpiConfig??><#if kpiConfig.userRole == '市经理'> selected</#if></#if>>市经理</option>-->
                                <option value="市代理" <#if kpiConfig??><#if kpiConfig.userRole == '市代理'> selected</#if></#if>>市代理</option>
                                <option value="直营城市经理" <#if kpiConfig??><#if kpiConfig.userRole == '直营城市经理' || kpiConfig.userRole == '市经理'> selected</#if></#if>>直营城市经理</option>
                                <option value="代理城市经理" <#if kpiConfig??><#if kpiConfig.userRole == '代理城市经理'> selected</#if></#if>>代理城市经理</option>
                            </select>
                            <span style="color:red;">(必填)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">小学/中学</label>
                        <div class="controls">
                            <select id="marketStuLevel" name="marketStuLevel">
                                <option value="">-请选择-</option>
                                <option value="小学" <#if kpiConfig??><#if kpiConfig.marketStuLevel == '小学'> selected</#if></#if>>小学</option>
                                <option value="中学" <#if kpiConfig??><#if kpiConfig.marketStuLevel == '中学'> selected</#if></#if>>中学</option>
                            </select>
                            <span style="color:red;">(必填)</span>
                        </div>
                    </div>
                    <div id="settleDiv" class="control-group">
                        <label class="control-label" for="focusedInput">渗透策略</label>
                        <div class="controls">
                            <select id="settlementType" name="settlementType" <#if kpiConfig??><#if kpiConfig.marketStuLevel == '中学'>disabled</#if></#if>>
                                <option value="">-请选择-</option>
                                <option value="低渗" <#if kpiConfig??><#if kpiConfig.settlementType == '低渗'> selected</#if></#if>>低渗</option>
                                <option value="高渗" <#if kpiConfig??><#if kpiConfig.settlementType == '高渗'> selected</#if></#if>>高渗</option>
                                <#--<option value="双渗" <#if kpiConfig??><#if kpiConfig.settlementType == '双渗'> selected</#if></#if>>双渗</option>-->
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">结算开始日期</label>
                        <div class="controls">
                            <input id="startDate" name="startDate" class="input-large focused" type="text" value="<#if kpiConfig??>${kpiConfig.salaryStartDate?string('yyyy-MM-dd')}<#else>2016-03-01</#if>">
                            <span style="color:red;">(必填)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">结算结束日期</label>
                        <div class="controls">
                            <input id="endDate" name="endDate" class="input-large focused" type="text" value="<#if kpiConfig??>${kpiConfig.salaryEndDate?string('yyyy-MM-dd')}<#else>2016-06-30</#if>">
                            <span style="color:red;">(必填)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">地区编码</label>
                        <div class="controls">
                            <input id="region" name="region" class="input-large focused" type="text" maxlength="6" onblur="loadRegionInfo();" onkeypress="if(event.keyCode==13){loadRegionInfo();}" <#if kpiConfig??>value="${kpiConfig.regionCode!}"</#if>>
                            <span style="color:red;">(必填)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">地区名称</label>
                        <div class="controls">
                            <input id="regionName" name="regionName" class="input-large focused" type="text" <#if kpiConfig??>value="${kpiConfig.regionName!}"</#if>>
                            <span style="color:red;">(必填)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">新增认证</label>
                        <div class="controls">
                            <input id="newAuth" name="newAuth" class="input-large focused" type="number" min="0" value="<#if kpiConfig??>${kpiConfig.newAuthTarget!0}<#else>0</#if>" <#if kpiConfig??><#if kpiConfig.marketStuLevel == '中学'>disabled</#if></#if>>
                        </div>
                    </div>
                    <div class="control-group">
                        <label id="slMarLb" class="control-label" for="focusedInput"><#if kpiConfig??><#if kpiConfig.marketStuLevel == '中学'>3月份新增认证<#else>3月份高覆盖</#if><#else>3月份高覆盖</#if></label>
                        <div class="controls">
                            <input id="slMar" name="slMar" class="input-large focused" type="number" min="0" value="<#if kpiConfig??>${kpiConfig.marSlTarget!0}<#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label id="slAprLb" class="control-label" for="focusedInput"><#if kpiConfig??><#if kpiConfig.marketStuLevel == '中学'>4月份新增认证<#else>4月份高覆盖</#if><#else>4月份高覆盖</#if></label>
                        <div class="controls">
                            <input id="slApr" name="slApr" class="input-large focused" type="number" min="0" value="<#if kpiConfig??>${kpiConfig.aprSlTarget!0}<#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label id="slMayLb" class="control-label" for="focusedInput"><#if kpiConfig??><#if kpiConfig.marketStuLevel == '中学'>5月份新增认证<#else>5月份高覆盖</#if><#else>5月份高覆盖</#if></label>
                        <div class="controls">
                            <input id="slMay" name="slMay" class="input-large focused" type="number" min="0"  value="<#if kpiConfig??>${kpiConfig.maySlTarget!0}<#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label id="slJunLb" class="control-label" for="focusedInput"><#if kpiConfig??><#if kpiConfig.marketStuLevel == '中学'>6月份新增认证<#else>6月份高覆盖</#if><#else>6月份高覆盖</#if></label>
                        <div class="controls">
                            <input id="slJun" name="slJun" class="input-large focused" type="number" min="0" value="<#if kpiConfig??>${kpiConfig.junSlTarget!0}<#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">双科认证</label>
                        <div class="controls">
                            <input id="stuDsa" name="stuDsa" class="input-large focused" type="number" min="0" value="<#if kpiConfig??>${kpiConfig.slDsaTarget!0}<#else>0</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">1~2年级数学新增认证数</label>
                        <div class="controls">
                            <input id="mathAuth" name="mathAuth" class="input-large focused" type="number" min="0" value="<#if kpiConfig??>${kpiConfig.authGradeMathTarget!0}<#else>0</#if>" <#if kpiConfig??><#if kpiConfig.marketStuLevel == '中学'>disabled</#if></#if>>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">计算系数</label>
                        <div class="controls">
                            <input id="factor" name="factor" class="input-large focused" type="number" min="0" value="<#if kpiConfig??>${kpiConfig.userCpaFactor!100}<#else>100</#if>">
                            <span>%</span>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="save_config_btn" type="button" class="btn btn-primary"> 保 存 </button>&nbsp;&nbsp;
                        <a id="cancel_btn" class="btn" href="index.vpage?userId=<#if kpiConfig??>${kpiConfig.userId}</#if>"> 取 消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function() {
        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $('#marketStuLevel').on('change',function() {
            var level = $('#marketStuLevel').find('option:selected').val();
            if(level == '中学'){
                $('#settlementType').attr("disabled", true);
                $('#region').val('');
                $('#regionName').val('');
                $('#newAuth').attr("disabled" ,true);
                $('#newAuth').val(0);
                $('#slMarLb').html('3月份新增认证');
                $('#slAprLb').html('4月份新增认证');
                $('#slMayLb').html('5月份新增认证');
                $('#slJunLb').html('6月份新增认证');
                $('#slMar').val(0);
                $('#slApr').val(0);
                $('#slMay').val(0);
                $('#slJun').val(0);
                $('#stuDsa').attr("disabled" ,true);
                $('#stuDsa').val(0);
                $('#mathAuth').attr("disabled" ,true);
                $('#mathAuth').val(0);
            } else {
                $('#settlementType').attr("disabled", false);
                $('#region').val('');
                $('#regionName').val('');
                $('#newAuth').attr("disabled" ,false);
                $('#slMarLb').html('3月份高覆盖');
                $('#slAprLb').html('4月份高覆盖');
                $('#slMayLb').html('5月份高覆盖');
                $('#slJunLb').html('6月份高覆盖');
                $('#slMar').val(0);
                $('#slApr').val(0);
                $('#slMay').val(0);
                $('#slJun').val(0);
                $('#stuDsa').attr("disabled" ,false);
                $('#stuDsa').val(0);
                $('#mathAuth').attr("disabled" ,false);
                $('#mathAuth').val(0);
            }
        });

        $('#save_config_btn').live('click', function () {
            // 获取数据
            var configInfo = {
                // 基础信息
                mode: $('#mode').val(),
                configId: $('#configId').val(),
                userId: $('#userId').val(),
                userName: $('#userName').val(),
                userRole: $('#userRole').val(),
                startDate: $('#startDate').val(),
                endDate: $('#endDate').val(),
                region: $('#region').val(),
                regionName: $('#regionName').val(),
                marketStuLevel: $('#marketStuLevel').val(),
                settlementType: $('#settlementType').val(),
                // 目标信息
                newAuth: $('#newAuth').val(),
                slMar: $('#slMar').val(),
                slApr: $('#slApr').val(),
                slMay: $('#slMay').val(),
                slJun: $('#slJun').val(),
                stuDsa: $('#stuDsa').val(),
                mathAuth: $('#mathAuth').val(),
                factor: $('#factor').val()
            };

            // 前端校验
            var msg = validateConfig(configInfo);
            if (msg.length == 0) {
                if (confirm("是否确认信息正确并保存？")) {
                    $.post('saveconfig.vpage', configInfo, function (data) {
                        if (data.success) {
                            alert("保存成功!");
                            window.location.href = 'index.vpage?userId='+configInfo.userId;
                        } else {
                            alert(data.info);
                        }
                    });
                }
            }else {
                alert(msg);
            }
        });

    });

    function loadUserByAccount() {
        var accName = $('#account').val();
        if (accName != '') {
            $.getJSON('getuserbyaccount.vpage', {accName : accName}, function(data) {
                if (data.success) {
                    $('#userId').val(data.user.userId);
                    $('#userName').val(data.user.userName);
                } else {
                    alert(data.info);
                    $('#account').val('');
                    $('#userId').val('');
                    $('#userName').val('');
                }
            });
        }
    }

    function loadRegionInfo() {
        var regionCode = $('#region').val();
        if (regionCode != '') {
            $.getJSON('getregioninfo.vpage', {regionCode: regionCode}, function(data) {
                if (data.success) {
                    $('#regionName').val(data.regionName);
                } else {
                    alert(data.info);
                    $('#region').val('');
                    $('#regionName').val('');
                }
            });
        }
    }

    function validateConfig(configInfo) {
        var msg = "";
        if (configInfo.userId == '' || configInfo.userName == '') {
            msg += "请输入用户！\r\n";
        }
        if (configInfo.userRole == '') {
            msg += "请选择用户角色！\r\n";
        }
        if (configInfo.marketStuLevel == '') {
            msg += "请选择小学/中学！\r\n";
        }
        if (configInfo.startDate == '' || configInfo.endDate == '') {
            msg += "请选择结算开始/结束日期！\r\n";
        }
        if (configInfo.region == '' || configInfo.regionName == '') {
            msg += "请正确填写区域信息！\r\n";
        }
        return msg;
    }

</script>

</@layout_default.page>                                                   