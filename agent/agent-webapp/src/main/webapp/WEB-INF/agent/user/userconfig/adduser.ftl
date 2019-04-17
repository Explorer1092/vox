<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加/编辑系统用户' page_num=5>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加/编辑系统用户</h2>
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
                            <input id="account_name" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.accountName!}</#if>"
                                   <#if userId??>disabled</#if>>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">真实姓名</label>
                        <div class="controls">
                            <input id="real_name" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.realName!}</#if>">
                        </div>
                    </div>
                    <#if !userId??>
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">密码</label>
                            <div class="controls">
                                <input id="password" class="input-xlarge focused" type="password" value="">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">确认密码</label>
                            <div class="controls">
                                <input id="re_password" class="input-xlarge focused" type="password" value="">
                            </div>
                        </div>
                    </#if>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户简介</label>
                        <div class="controls">
                            <textarea id="user_comment" class="input-xlarge focused"><#if agentSysUser??>${agentSysUser.userComment!}</#if></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户电话</label>
                        <div class="controls">
                            <input id="tel" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.tel!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户邮箱</label>
                        <div class="controls">
                            <input id="email" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.email!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户QQ</label>
                        <div class="controls">
                            <input id="imAccount" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.imAccount!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">用户地址</label>
                        <div class="controls">
                            <input id="address" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.address!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">保证金金额</label>
                        <div class="controls">
                            <input id="cash_deposit" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.cashDeposit!}</#if>"
                                   <#if agentSysUser?? && agentSysUser.cashDepositReceived?? && agentSysUser.cashDepositReceived>disabled</#if>>
                            <#if agentSysUser?? && agentSysUser.cashDepositReceived?? && agentSysUser.cashDepositReceived>
                                <a id="addCashDeposit" class="btn btn-info" href="#">
                                    追加保证金
                                </a>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">开户行名称</label>
                        <div class="controls">
                            <input id="bank_name" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.bankName!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">开户人姓名</label>
                        <div class="controls">
                            <input id="bank_hostname" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.bankHostName!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">银行帐号</label>
                        <div class="controls">
                            <input id="bank_account" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.bankAccount!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">合同开始日期</label>
                        <div class="controls">
                            <input id="contract_start_date" class="input-xlarge focused" type="text" value="<#if agentSysUser??&&agentSysUser.contractStartDate??>${agentSysUser.contractStartDate?string('yyyy-MM-dd')}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">合同结束日期</label>
                        <div class="controls">
                            <input id="contract_end_date" class="input-xlarge focused" type="text" value="<#if agentSysUser??&&agentSysUser.contractEndDate??>${agentSysUser.contractEndDate?string('yyyy-MM-dd')}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">合同编号</label>
                        <div class="controls">
                            <input id="contract_number" class="input-xlarge focused" type="text" value="<#if agentSysUser??>${agentSysUser.contractNumber!}</#if>">
                        </div>
                    </div>
                    <!--
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">校园业务级别</label>
                        <div class="controls">
                            <select name="select" id="school_level" class="xla_k">
                                <option value="1" <#if (agentSysUser.schoolLevel)?? && agentSysUser.schoolLevel == 1>selected="selected"</#if>>小学</option>
                                <option value="2" <#if (agentSysUser.schoolLevel)?? && agentSysUser.schoolLevel == 2>selected="selected"</#if>>中学</option>
                                <option value="12" <#if (agentSysUser.schoolLevel)?? && agentSysUser.schoolLevel == 12>selected="selected"</#if>>小学&中学</option>
                            </select>
                        </div>
                    </div>
                    -->
                    <div class="control-group" id="group_region_tree">
                        <label class="control-label">所属代理区域</label>
                        <div id="groupTree" name="groupTree" class="controls" style="width: 280px;height: 300px">
                        </div>
                        <input type="hidden" name="groupIds" value="" id="groupIds">
                    </div>

                    <div class="form-actions">
                        <button id="add_sys_user_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<input type="hidden" id="userId" value="${userId!}">
<input type="hidden" id="schoolMap" value='${schoolMap!}'>

<div id="addCashDepositDialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">追加保证金</h4>
            </div>
            <form class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">保证金金额</label>
                        <div class="controls">
                            <input id="modalCashDeposit" class="input-xlarge focused" type="text">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">保证金说明</label>
                        <div class="controls">
                            <textarea id="modalCashDepositDesc" style="width: 270px;" rows="4"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="saveCashDepositBtn" type="button" class="btn btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){

        $("#contract_start_date").datepicker({
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

        $("#contract_end_date").datepicker({
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

        $("#groupTree").fancytree({
            extensions: ["filter"],
            source: {
                url: "loadgrouptree.vpage?userId=" + $("#userId").val(),
                cache:true
            },
            checkbox: true,
            selectMode: 2,

            init: function(event, data, flag) {
                var tree = $("#groupTree").fancytree("getTree");
                tree.visit(function(node){
                    $("input[name='group']").each(function(){
                        if (node.key == $(this).attr("value")) {
                            node.setSelected(true);
                            node.setActive();
                        }
                    });
                });
            }
        });

        $('#add_sys_user_btn').live('click',function(){
            var accountName = $('#account_name').val().trim();
            var realName = $('#real_name').val().trim();
            var userComment = $('#user_comment').val().trim();
            var password ="";
            var rePassword = "";
            if($('#userId').val() == ''){
                password = $('#password').val().trim();
                rePassword = $('#re_password').val().trim();
            }
            var tel = $('#tel').val().trim();
            var email = $('#email').val().trim();
            var imAccount = $('#imAccount').val().trim();
            var address = $('#address').val().trim();

            var cashDeposit = $('#cash_deposit').val().trim();
            var bankName = $('#bank_name').val().trim();
            var bankHostname = $('#bank_hostname').val().trim();
            var bankAccount = $('#bank_account').val().trim();

            var contractStartDate = $('#contract_start_date').val();
            var contractEndDate = $('#contract_end_date').val();
            var contractNumber = $('#contract_number').val();

            var groups = new Array();
            var schools = new Array();

            var groupTree = $("#groupTree").fancytree("getTree");
            var groupNodes = groupTree.getSelectedNodes();

            var schoolLevel = "";

            $.map(groupNodes, function(node){
                if (node.data.type == 'group') {
                    groups.push(node.key);
                }
                if (node.data.type == 'school') {
                    schools.push(node.key);
                }
            });

            if (groups.length > 1) {
                if(!confirm("该用户有2个以上代理区域，确定吗?")){
                    return false;
                }
            }

            if(groups.length == 0) {
                alert("请选择代理区域!");
                return false;
            }

            if(schools.length == 0) schools.push("");
            if(!checkAddSysUser(accountName,realName,password,rePassword)){
                return false;
            }

//            if (!schoolLevel) {
//                alert("必须勾选一项校园业务级别!");
//                return false;
//            }

            $.post('addsysuser.vpage',{
                accountName : accountName,
                realName : realName,
                userComment : userComment,
                password : password,
                tel : tel,
                email : email,
                imAccount : imAccount,
                address : address,
                cashDeposit : cashDeposit,
                bankName : bankName,
                bankHostname : bankHostname,
                bankAccount : bankAccount,
                contractStartDate:contractStartDate,
                contractEndDate:contractEndDate,
                contractNumber:contractNumber,
                groups:groups,
                schools:schools,
                userId:$('#userId').val(),
                schoolLevel:schoolLevel
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $(window.location).attr('href', 'index.vpage');
                }
            });
        });

    });

    function checkAddSysUser(accountName,realName,password,rePassword){
        if(accountName.trim() == ''){
            alert("请输入用户名!");
            return false;
        }
        if(realName.trim() == ''){
            alert("请输入真实姓名!");
            return false;
        }

        if ($('#userId').val() == '') {
            if(password.trim() == ''){
                alert("请输入密码!");
                return false;
            }
            if(password.trim() != rePassword.trim()){
                alert("两次输入的密码不匹配！");
                return false;
            }
        }

        return true;
    }

    function initSchool(){
        $('#group').change();
    }

    $('#addCashDeposit').live('click',function(){
        $('#modalCashDeposit').val('');
        $('#modalCashDepositDesc').val("");
        $('#addCashDepositDialog').modal('show');
    });

    $('#saveCashDepositBtn').live('click',function(){
        saveCashDeposit();
    });

    function saveCashDeposit() {
        var userId = $('#userId').val();
        var modalCashDeposit = $('#modalCashDeposit').val();
        var modalCashDepositDesc = $('#modalCashDepositDesc').val();

        if (modalCashDeposit == '' || !$.isNumeric(modalCashDeposit)) {
            alert("请输入正确的保证金金额!");
            return false;
        }

        if (modalCashDepositDesc == '') {
            alert("请输入追加保证金原因说明以方便财务审核!");
            return false;
        }

        $.post('addcashdepoist.vpage',{
            userId: $('#userId').val(),
            cashDeposit: modalCashDeposit,
            cashDepositDesc: modalCashDepositDesc
        },function(data){
            if(!data.success){
                alert(data.info);
            } else {
                $('#addCashDepositDialog').modal('hide');
            }
        });
    }

</script>

</@layout_default.page>