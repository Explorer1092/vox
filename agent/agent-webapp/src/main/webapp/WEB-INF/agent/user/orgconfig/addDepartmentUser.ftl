<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='部门' page_num=5>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 添加用户</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin() || requestContext.getCurrentUser().isCityAgent() >

            </#if>
        </div>

        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">部门</label>
                        <div class="controls">
                            <input id="group_name" class="input-xlarge focused" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">姓名</label>
                        <div class="controls">
                            <input id="real_name" class="input-xlarge focused js-needed" type="text" value="" data-needInfo="姓名不能为空">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">账号</label>
                        <div class="controls">
                            <input id="account_name" class="input-xlarge focused js-needed" type="text" value="" data-needInfo="账号不能为空">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">角色</label>
                        <div class="controls">
                            <select id="role_name" class="form-control js-needed" data-needInfo="请选择角色">
                                <option value="0">请选择</option>
                                <option value="1">全国总监</option>
                                <option value="2">大区经理</option>
                                <option value="3">市经理</option>
                                <option value="4">代理</option>
                                <option value="5">合作代理</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">电话</label>
                        <div class="controls">
                            <input id="phone_no" class="input-xlarge focused js-needed" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">保证金</label>
                        <div class="controls">
                            <input id="promise_cash" class="input-xlarge focused" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">开户行</label>
                        <div class="controls">
                            <input id="bank_name" class="input-xlarge focused" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">开户人</label>
                        <div class="controls">
                            <input id="bank_hostname" class="input-xlarge focused" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">银行帐号</label>
                        <div class="controls">
                            <input id="bank_account" class="input-xlarge focused" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">合同开始日期</label>
                        <div class="controls">
                            <input id="contract_start_date" class="input-xlarge focused js-needed" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">合同结束日期</label>
                        <div class="controls">
                            <input id="contract_end_date" class="input-xlarge focused" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">合同编号</label>
                        <div class="controls">
                            <input id="contract_number" class="input-xlarge focused" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">地址</label>
                        <div class="controls">
                            <input id="address" class="input-xlarge focused" type="text" value="">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">简介</label>
                        <div class="controls">
                            <textarea id="introDesc" class="input-xlarge focused"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">负责区域</label>
                        <div class="controls">
                            <button id="add_respon_area_btn" type="button" class="btn btn-primary">添加</button>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button id="add_sys_user_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
    <!--/span-->

</div>
<script id="departmentDetailTemp" type="text/x-handlebars-template">

</script>


<script type="text/javascript">
    $(function(){

        $(document).on("click","#add_respon_area_btn",function(){

        });

    });
</script>
</@layout_default.page>
