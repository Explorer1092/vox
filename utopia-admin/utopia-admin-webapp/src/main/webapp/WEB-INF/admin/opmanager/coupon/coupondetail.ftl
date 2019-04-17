<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑广告" page_num=9 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑优惠券
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel"
           class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存优惠券"/>
        <#if accountId?has_content>
        <div style="float: right;">
            <a title="配置投放策略" href="couponconfig.vpage?couponId=${accountId!}"
               class="btn btn-warning">
                <i class="icon-cog icon-white"></i> 配置投放策略
            </a>
        </div>
        </#if>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="couponForm" name="detail_form" enctype="multipart/form-data" action="save.vpage"
                      method="post">
                    <input id="couponId" name="couponId" value="${accountId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <#if accountId?has_content>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">ID</label>
                            <div class="controls">
                                <input type="text" id="couponId" name="couponId" class="form-control" value="${accountId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        </#if>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="name" name="name" class="form-control js-postData" type="text" value="<#if coupon??>${coupon.name!''}</#if>" placeholder="请填写优惠券名称" style="width: 336px;" maxlength="30"/>
                                <span style="font-size: 10px;color: red">
                                    必填
                                </span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">描述  <span style="color: red">*</span></label>
                            <div class="controls">
                                <textarea id="desc" name="desc" class="form-control js-postData" style="width:336px;">${(coupon.desc)!}</textarea>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">类型  <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="couponType" name="couponType" style="width: 350px;" class="js-postData js-changeSelect" <#if accountId?has_content> disabled</#if> >
                                    <option value="">--请选择优惠券类型--</option>
                                    <#if couponTypes??>
                                        <#list couponTypes as c>
                                            <option <#if coupon?? && coupon.couponType??><#if coupon.couponType == c.name()> selected="selected"</#if></#if> value = ${c.name()!}>${c.getDesc()!}</option>
                                        </#list>
                                    </#if>
                                </select>
                                <span style="font-size: 10px;color:red">(生成后无法修改)</span>
                            </div>
                        </div>

                        <div class="control-group js-showText" style="display: none;">
                            <label class="col-sm-2 control-label">满减券条件额度  <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="voucherAmount" name="voucherAmount" class="form-control js-postData" value="<#if coupon??>${coupon.voucherAmount!''}</#if>" style="width: 336px" maxlength="20"/>
                                <span style="font-size: 10px;color: red">
                                    填写说明：例如，满100减20，那么这里填写100，折扣力度输入框填20
                                </span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">折扣力度  <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="typeValue" name="typeValue" class="form-control js-postData" value="<#if coupon??>${coupon.typeValue!''}</#if>" style="width: 336px" maxlength="20" />
                                <span style="font-size: 10px;color: red">
                                    填写说明：
                                    1. 直减券填写要减的金额，单位元
                                    2. 折扣券填写要进行的折扣，范围1-9.9
                                    3. 赠天券填写要赠送的服务天数
                                </span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">状态  <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="status" name="status" style="width: 350px;" class="js-postData">
                                <option value="">--请选择优惠券状态--</option>
                                <#if status??>
                                    <#list status as s>
                                        <option <#if coupon?? && coupon.status??><#if coupon.status == s.name()> selected="selected"</#if></#if> value = ${s.name()!}>${s.getDesc()!}</option>
                                    </#list>
                                </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">可使用次数  <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="usableCount" name="usableCount" class="form-control js-postData" value="<#if coupon??>${coupon.usableCount!''}</#if>" style="width: 336px" maxlength="20"/>
                                <span style="font-size: 10px;color: red">
                                    填写说明：0-不限制；1-一次（默认） ；多次的，请填写具体的使用次数
                                </span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">总发行数量  <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="totalCount" name="totalCount" class="form-control js-postData" value="<#if coupon??>${coupon.totalCount!''}</#if>" style="width: 336px" maxlength="20"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">剩余领取次数  <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="leftCount" name="leftCount" class="form-control js-postData" value="<#if coupon??>${coupon.leftCount!''}</#if>" style="width: 336px" maxlength="20"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">优惠券优惠时间类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <form action="">
                                    <input type="radio" class="select_radio" name="radio1" checked value="time1" style="width: 15px;height: 15px;"/>时长
                                    <input type="radio" class="select_radio" name="radio1" value="time2" style="width: 15px;height: 15px;"/>固定时间段
                                </form>
                            </div>
                        </div>
                        <div class="control-group js-time1">
                            <label class="col-sm-2 control-label">使用限制时长（天）<span style="color: red">*</span> </label>
                            <div class="controls">
                                <input type="text" id="effectiveDay" name="effectiveDay" class="form-control js-postData" value="<#if coupon??>${coupon.effectiveDay!''}</#if>" style="width: 336px" maxlength="20"/>
                                <span style="font-size: 10px;color:red">(优惠券领取后的有效时间)</span>
                            </div>
                        </div>
                        <div class="js-time2" style="display: none;">
                            <div class="control-group">
                                <label class="col-sm-2 control-label">开始使用时间 <span style="color: red">*</span> </label>
                                <div class="controls">
                                    <input type="text" id="useStartTime" name="useStartTime" class="form-control js-postData" value="<#if coupon??>${coupon.useStartTime!''}</#if>" style="width: 336px" readonly="readonly"/>
                                    <span style="font-size: 10px;color:red">(优惠券领取后的使用时间段)</span>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">使用截止时间 <span style="color: red">*</span> </label>
                                <div class="controls">
                                    <input type="text" id="useEndTime" name="useEndTime" class="form-control js-postData" value="<#if coupon??>${coupon.useEndTime!''}</#if>" style="width: 336px" readonly="readonly"/>
                                    <span style="font-size: 10px;color:red">(优惠券领取后的使用时间段)</span>
                                </div>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="col-sm-2 control-label">发券过期日期  <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="limitDate" name="limitDate" class="form-control js-postData" value="<#if coupon??>${coupon.limitDate!''}</#if>" style="width: 336px;" readonly="readonly"/>
                                <span style="font-size: 10px;color:red">(过期后优惠券将不能被领取)</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">跳转URL </label>
                            <div class="controls">
                                <input type="text" id="linkUrl" name="linkUrl" class="form-control js-postData" value="<#if coupon??>${coupon.linkUrl!''}</#if>" style="width: 336px" />
                                <span style="font-size: 10px;color:red">(配置用户点击立即使用跳转的链接)</span>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
$(function () {
    //编辑的时候进入页面
    var accountId = "${accountId!''}";
    if(accountId != ''){
        var couponUseStartTime = "${useStartTime!''}";
        if(couponUseStartTime != ''){
            $("input:radio[name=radio1][value='time2']").attr("checked",true);
            $(".js-time1").hide();
            $(".js-time2").show();
        }

        var couponType = "${couponType!''}";
        if(couponType == 'Voucher'){
            $(".js-showText").show();
        }
    }



    $('#limitDate').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',  //日期格式，自己设置
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        onSelect: function (selectedDate) {
        }
    });

    $('#useStartTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',  //日期格式，自己设置
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        onSelect: function (selectedDate) {
        }
    });

    $('#useEndTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',  //日期格式，自己设置
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        onSelect: function (selectedDate) {
        }
    });

    //验证表单
    var validateForm = function () {
        var msg = "";
        if($('#name').val() == ''){
            msg += "名称不能为空！\n";
        }
        if($('#couponType').val() == ''){
            msg += "请选择类型！\n";
        }
        if($('#couponType').val() == 'Voucher'){
            if($('#voucherAmount').val() == ''){
                msg += "满减券条件额度！\n";
            }
        }
        if($('#typeValue').val() == ''){
            msg += "请填写正确的折扣力度！\n";
        }
        if($('#status').val() == ''){
            msg += "请选择状态！\n";
        }
        if($('#usableCount').val() == ''){
            msg += "请填写可使用次数（0-不限制，默认1次）！\n";
        }
        if($('#totalCount').val() == ''){
            msg += "请填写发行量！\n";
        }
        if($('#leftCount').val() == ''){
            msg += "请填写剩余发行量！\n";
        }
        //选择时间段，还是时长
        var timeType = $("input[name='radio1']:checked").val();
        if(timeType == 'time1'){
            if($('#effectiveDay').val() == ''){
                msg += "请填写领取后有效使用时长！\n";
            }
        }else{
            if($('#useStartTime').val() == ''){
                msg += "请填写开始使用时间！\n";
            }

            if($('#useEndTime').val() == ''){
                msg += "请填写使用截止时间！\n";
            }
        }

        if($('#limitDate').val() == ''){
            msg += "请填写过期时间！\n";
        }
        if (msg.length > 0) {
            alert(msg);
            return false;
        }
        return true;
    }

    //保存提交
    $(document).on("click",'#save_ad_btn',function () {
        if(validateForm()){
            var post = {};
            $(".js-postData").each(function(i,item){
                post[item.name] = $(item).val();
            });
            console.log(post);
            $.post('save.vpage',post,function (res) {
                if(res.success){
                    alert(res.info);
                    location.href= 'index.vpage';
                }else{
                    alert(res.info);
                }
            });
        }
    });
    $(".select_radio").click(function () {
        $(this).attr('checked', 'checked');
        if ($(this).val() === 'time1') {
            $(".js-time1").show();
            $(".js-time2").hide();
        } else {
            $(".js-time1").hide();
            $(".js-time2").show();

        }
    });
    $(".js-changeSelect").change(function () {
        if ($(this).val() === 'Voucher') {
            $(".js-showText").show();
        } else {
            $(".js-showText").hide();
        }
    });
});
</script>
</@layout_default.page>