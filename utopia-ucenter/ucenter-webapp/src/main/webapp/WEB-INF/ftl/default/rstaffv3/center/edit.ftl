<#-- @ftlvariable name="userShippingAddressMapper" type="com.voxlearning.utopia.entity.user.UserShippingAddress" -->
<#-- @ftlvariable name="currentResearchStaffDetail" type="com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail" -->
<#-- @ftlvariable name="currentUser" type="com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff" -->
<#import "../researchstaffv3.ftl" as com>
<@com.page menuType="normal">
<ul class="breadcrumb_vox">
    <li><a href="/rstaff/center/index.vpage">个人信息</a> <span class="divider">/</span></li>
    <li class="active">修改信息</li>
</ul>
<div id="edit_user_info" class="testpaperBox">
    <div class="sAvatar row_vox_left text_center" style="width: 120px; padding-top: 30px;">
        <div class="sar spacing_vox_tb row_vox_right">
        <#--fixme : img url 添加默认头像样式-->
            <i class="avatar" style="border: 1px solid #ddd; display: inline-block; width: 90px;height: 90px;"><img
                    id="user_avatar" src="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>" alt="" width="90"
                    height="90"></i><br/><br/>
            <a id="change_avatar" href="javascript:void(0)" class="btn_vox btn_vox_primary" uid="${(currentUser.id)!}"
               face="<@app.avatar href='${(currentUser.fetchImageUrl())!}'/>">修改头像</a>
        </div>
    </div>
    <div class="row_vox_left" style="width: 460px;">
        <dl class="horizontal_vox">
            <dt class="text_big">姓名：</dt>
            <dd><input id="realname" type="text" data-label="姓名" class="require int_vox"
                       value="${(currentUser.profile.realname)!}"/><span></span></dd>
        </dl>
        <dl class="horizontal_vox">
            <#if (currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForCity()) && currentUser.subject != "MATH">
                <dt class="text_big">收货信息：</dt>
                <dd>&nbsp;</dd>
                <dt id="district" class="text_normal">所在地区：</dt>
                <dd>
                    <select id="provinces" next_level="citys" class="int_vox district_select">
                        <#list provinces as p>
                            <option value="${p.key}">${p.value}</option>
                        </#list>
                    </select>

                    <select id="citys"
                            defaultValue="<#if ((userShippingAddressMapper.cityCode)!0) != 0 >${(userShippingAddressMapper.cityCode)!}<#else>${(currentUser.region.cityCode)!}</#if>"
                            next_level="countys" class="int_vox district_select" style="width: 140px">
                        <option value="0">市</option>
                    </select>

                    <select id="countys"
                            defaultValue="<#if ((userShippingAddressMapper.countyCode)!0) != 0>${(userShippingAddressMapper.countyCode)!}<#else>${(currentUser.region.countyCode)!}</#if>"
                            class="int_vox district_select" style="width: 140px">
                        <option value="0">区</option>
                    </select>

                    <span></span>
                </dd>
                <dt class="text_normal">详细地址：</dt>
                <dd><input type="text" class="require int_vox"
                           value="${(userShippingAddressMapper.detailAddress?html)!}" id="detail_address"
                           data-label="详细地址"/><span></span></dd>
                <dt class="text_normal">邮政编码：</dt>
                <dd><input type="text" class="require int_vox" value="${(userShippingAddressMapper.postCode)!}"
                           id="post_code" data-label="邮政编码"/><span></span></dd>
                <dt class="text_normal">联系电话：</dt>
                <dd><input type="text" class="require int_vox" value="${(mobile)!}" id="phone"
                           data-label="联系电话"/><span></span></dd>
                <dt class="text_normal" style="display: none;">配送方式：</dt>
                <dd style="display: none;">
                    <input type="radio" style="width: 20px;" id="express" name="logisticType" value="express"
                           checked><label style="text-align: left; width: auto;" for="express">普通快递（市、县可到达）</label>
                    <input type="radio" style="width: 20px;" id="ems" name="logisticType"
                           value="ems" ${((userShippingAddressMapper.logisticType=="ems")?string('checked',''))!}><label
                        style="text-align: left; width: auto;" for="ems">邮局（乡、镇、村可到达）</label>
                </dd>
            </#if>
            <dt>&nbsp;</dt>
            <dd>
                <a href="/rstaff/center/index.vpage" class="btn_vox  "><strong>取消</strong></a>
                <a id="submit_addr_but" class="btn_vox btn_vox_primary " href="javascript:void(0);"><strong>提交</strong></a>
            </dd>
        </dl>
    </div>
    <div class="clear"></div>
</div>
<div style="display: none;">
    <div id="uploadFaceWindow" title="上传头像" style="width:660px;height:470px;background:#FFF; margin:0 auto;">
        <div id="uploadFaceFlash"></div>
    </div>
</div>

<script id="t:编辑成功提示" type="text/html">
    <div class="text_center spacing_vox">
        <p><strong class="text_green text_big">个人资料修改成功！</strong></p>
        <#if currentUser.subject != "MATH" && (currentUser.isResearchStaffForCounty()|| currentUser.isResearchStaffForCity())>
            <p>您的收货地址将作为教学用品中心的发送地址。</p>
        </#if>
    </div>
    <div class="text_center spacing_vox">
        <a class="btn_vox" href="javascript:$.prompt.close();"><strong>取消</strong></a>
        <a class="btn_vox btn_vox_primary" href="/rstaff/center/index.vpage"><strong>确定</strong></a>
    </div>
</script>

<script type="text/javascript">
    function validate() {
        var errorCount = 0;
        $(".require").each(function () {
            if ($17.isBlank($(this).val())) {
                $(this).closest('li').addClass('err');
                var errorMessage = $(this).data("label") + '不可为空';
                $(this).siblings('span').html("<i></i>" + errorMessage);
                errorCount++;
            }
        });
        return $("li.err").size() == 0 && errorCount == 0;
    }

    // validate
    $(function () {
        $("input").on("focus blur change", function (e) {
            var _this = $(this);
            var notice = "";
            var row = _this.closest('li');
            var _type = _this.attr("id");
            var span = _this.siblings("span");
            var condition = true;
            var errorMessage = "";
            if (e.type != "blur") {
                switch (_type) {
                    case "realname":
                        var value = _this.val().replace(/\s+/g, "");
                        condition = !(value.match(/[^\u4e00-\u9fa5]/g));
                        errorMessage = "请输入您的真实姓名,须为中文";
                        if (value.length < 2 || value.length > 10) {
                            errorMessage = "请输入2-10位中文名字";
                            condition = false;
                        }
                        notice = "请输入真实姓名";
                        break;
                    case "phone":
                        condition = $17.isMobile(_this.val());
                        errorMessage = "请填写正确的手机号码";
                        notice = "电话用于快递配送时确认，请确保号码畅通可用";
                        break;
                    case "post_code":
                        condition = $17.isZipCode(_this.val());
                        errorMessage = "请填写正确格式邮编";
                        notice = "请输入邮编";
                        break;
                    case "email":
                        condition = $17.isEmail(_this.val());
                        errorMessage = "请填写正确格式的邮箱";
                        notice = "请输入常用邮箱，验证通过后可用于登录和找回密码";
                        break;
                    default:
                        break;
                }
            }

            if (e.type == "focus") {
                if (!row.hasClass("err") && _this.val() == "") {
                    span.html(notice)
                }
            } else if (e.type == "blur") {
                if (!row.hasClass("err") && _this.val() == "") {
                    span.html("");
                }
            } else if (e.type == "change") {
                if (!$17.isBlank(_this.val())) {
                    if (!condition) {
                    <#--fixme : cor ,err 两个样式类-->
                        row.removeClass("cor").addClass("err");
                    } else {
                        row.removeClass("err").addClass("cor");
                        errorMessage = "";
                    }
                    span.html("<i class='icon_new_all'></i>" + errorMessage);
                } else {
                    if (_this.hasClass("require")) {
                        errorMessage = _this.data("label") + '不可为空';
                        row.removeClass("cor").addClass("err");
                    <#--fixme : icom_new_all也是一个样式类-->
                        span.html("<i class='icon_new_all'></i>" + errorMessage);
                    } else {
                        row.removeClass("err");
                        span.html("");
                    }
                }
            }
        });
    })
</script>
<script>
    function Avatar_callback(data) {
        data = eval("(" + data + ")");
        if ($17.isBlank(data)) {
            setTimeout(function () {
                window.location.reload();
            }, 200);
        } else if (data) {
            $("#user_avatar").attr("src", '<@app.avatar href="'+data.row+'"/>');
            $.prompt.close();
            return false;
        }
    }

    function Avatar_Cancel() {
        $.prompt.close();
    }

    function checkDifferent(d1, d2) {
        return (
                d1.userName != d2.userName
                        || d1.provinceCode != d2.provinceCode
                        || d1.cityCode != d2.cityCode
                        || d1.countyCode != d2.countyCode
                        || d1.detailAddress != d2.detailAddress
                        || d1.postCode != d2.postCode
                        || d1.logisticType != d2.logisticType
                        || d1.phone != d2.phone
                );
    }

    $(function () {
        var oldData = null;

        function init() {

            var $provinces = $("#provinces");
            var $citys = $('#citys');
            var $countys = $('#countys');

            <#if ((userShippingAddressMapper.provinceCode)!0) != 0>
                $provinces.val(${(userShippingAddressMapper.provinceCode)!});
            <#else>
                $provinces.val(${(currentUser.region.provinceCode)!});
            </#if>

            App.districtSelect.init($provinces);

            setTimeout(function () {
                oldData = {
                    userName: $("#realname").val(),
                    provinceCode: $provinces.val(),
                    cityCode: $citys.val(),
                    countyCode: $countys.val(),
                    detailAddress: $("#detail_address").val(),
                    postCode: $("#post_code").val(),
                    phone: $('#phone').val(),
                    logisticType: '${(userShippingAddressMapper.logisticType)!}'
                };
            }, 1000);
        }

        $("#change_avatar").on("click", function () {
            var $date = '<iframe src="/ucenter/avatar.vpage?avatar_cancel=parent.Avatar_Cancel&avatar_callback=parent.Avatar_callback" width="660" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>'
            $.prompt($date, {
                title: "上传头像",
                buttons: {},
                position: { width: 670 }
            });
            return false;
        });

        $("#submit_addr_but").lock("click", function (_this) {
            var success = validate();
            var $provinces = $("#provinces");
            var $citys = $('#citys');
            var $countys = $('#countys');
            var wind = template("t:编辑成功提示", {});
            if (success) {
                var userName = $("#realname").val();
                var provinceCode = $provinces.val();
                var cityCode = $citys.val();
                var countyCode = $countys.val();
                var detailAddress = $("#detail_address").val();
                var postCode = $("#post_code").val();
                var phone = $("#phone").val();
                var logisticType = $('input[name="logisticType"]:checked').val();
                var data = {
                    userName: userName,
                    provinceCode: provinceCode,
                    cityCode: cityCode,
                    countyCode: countyCode,
                    detailAddress: detailAddress,
                    postCode: postCode,
                    phone: phone,
                    logisticType: logisticType
                };
                if (checkDifferent(data, oldData)) {
                    var postData = {
                        userName: userName,
                        provinceCode: provinceCode,
                        provinceName: $provinces.find('option:selected').text(),
                        cityCode: cityCode,
                        cityName: $citys.find('option:selected').text(),
                        countyCode: countyCode,
                        countyName: $countys.find('option:selected').text(),
                        detailAddress: detailAddress,
                        postCode: postCode,
                        phone: phone,
                        logisticType: logisticType
                    };
                    _this.postJSON('/rstaff/center/submitrstaffinfo.vpage', postData, function (data) {
                        if (data.success) {
                            $('#nav_real_name').text(data.userName);
                            $.prompt(wind, {
                                title: "提示",
                                buttons: {}
                            });
                        } else {
                            $.prompt(data.info);
                        }
                    });
                } else {
                    $.prompt(wind, {
                        title: "提示",
                        buttons: {}
                    });
                }
            }
        });

        init();
    })
</script>
</@com.page>