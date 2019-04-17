<div class="class-module mt-20">
    <div class="module-head bg-f6 clearfix">
        <div class="title">我的资料</div>
    </div>
    <div class="personalData-list">
        <dl>
            <dt>姓名：</dt>
            <dd>
                <input id="realname" type="text" class="txt" placeholder="姓名" value="${(currentUser.profile.realname)!}">
                <span class="icon-arrow">（*必填）</span>
                <span class="w-form-misInfo w-form-info-error errorMsg"></span>
            </dd>
            <dt>所在学校：</dt>
            <dd>
                <input id="school_name" type="text" disabled="disabled" style="background-color:#e6e6e6; "class="txt" value="${(userShippingAddressMapper.schoolName)!}" placeholder="${(userShippingAddressMapper.schoolName)!}">
                <span class="icon-arrow">（*必填）</span>
                <p class="ped-info">学校信息不能随意修改，有问题请致电：400-160-1717</p>
            </dd>
            <#if (currentTeacherDetail.isOldJuniorTeacher())!false >
            <dt>所在地区：</dt>
            <dd>
                <select id="province" class="sel" data-regionId="${(userShippingAddressMapper.provinceCode)!}" data-regionText="${(userShippingAddressMapper.provinceName)!}">
                    <#list provinces as province>
                        <option value="${province.key}" <#if (userShippingAddressMapper.provinceCode == province.key)> selected="true" </#if> >${province.value}</option>
                    </#list>
                </select>
                <select id="city" class="sel" data-regionId="${(userShippingAddressMapper.cityCode)!}" data-regionText="${(userShippingAddressMapper.cityName)!}">
                    <option>${(userShippingAddressMapper.cityName)!}</option>
                </select>
                <select id="county" class="sel" data-regionId="${(userShippingAddressMapper.countyCode)!}" data-regionText="${(userShippingAddressMapper.countyName)!}">
                    <option>${(userShippingAddressMapper.countyName)!}</option>
                </select>
                <span class="icon-arrow">（*必填）</span>
            </dd>
                <dt>收货地址：</dt>
                <dd>
                    <span class="ped-area">
                        <span id="show-province">${(userShippingAddressMapper.provinceName)!}</span>
                        <span id="show-city">${(userShippingAddressMapper.cityName)!}</span>
                    </span>
                    <input type="text" id="detailAddress" class="txt width227" value="${(userShippingAddressMapper.detailAddress)!}" placeholder="${(userShippingAddressMapper.detailAddress)!}">
                    <span class="icon-arrow">（*必填）</span>
                    <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                    <p class="ped-info">学生兑换奖品收货地址</p>
                </dd>
                <dt>邮政编码：</dt>
                <dd>
                    <input id="post_code" type="text" class="txt" maxlength="6" value="${userShippingAddressMapper.postCode!}" placeholder="${(userShippingAddressMapper.postCode)!}" onkeyup="this.value=this.value.replace(/\D/g,'')" onafterpaste="this.value=this.value.replace(/\D/g,'')">
                </dd>
            </#if>
            <dt>联系电话：</dt>
            <dd>
                <style>
                    .a-hover:hover{color: #589d40;}
                </style>
                <#if mobile?has_content>
                    <div class="telephone">${mobile}已经绑定手机号，<a href="#/teacher/center/securitycenter.vpage" class="a-hover">去更换</a></div>
                <#else>
                    <div class="telephone">手机号码尚未验证，<a href="#/teacher/center/securitycenter.vpage" class="a-hover">去设置</a></div>
                </#if>
            </dd>
            <#if (currentTeacherDetail.isOldJuniorTeacher())!false >
                <dt>配送方式：</dt>
                <dd id="radios_father" data-value="${((userShippingAddressMapper.logisticType!'notavailable') == "notavailable")?string('express', userShippingAddressMapper.logisticType!'')}" placeholder="请选择您的奖品寄送方式">
                    <label data-value="express" class="radio-current">
                        <input type="radio" class="rad" <#if (userShippingAddressMapper.logisticType!'') != "express" && (userShippingAddressMapper.logisticType!'') != "ems">checked="checked"<#else>${(((userShippingAddressMapper.logisticType!'') == "express")?string('checked="checked"',''))!}</#if>> 普通快递（市、县可到达）</label>
                    <label data-value="ems" class="radio-current">
                        <input type="radio" class="rad" ${(((userShippingAddressMapper.logisticType!'') == "ems")?string('checked="checked"',''))!}> 邮局（乡、镇、村可到达，寄送时间较长）</label>
                    <p class="ped-info">学生兑换奖品配送方式</p>
                </dd>
            </#if>
        </dl>
    </div>
</div>
<div class="personalData-foot">
    <a id="submit_addr_but" href="javascript:;" class="green_btn">确定</a>
</div>
<#include "../validate.ftl"/>
<script type="text/javascript">
    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("myprofile");

        //省的下拉事件
        var $province = $("#province");
        $("#province").on("change",function(){
            var regionId = $(this).val();
            var regionText = $(this).find("option:selected").text();

            $province.attr("data-regionId", regionId);
            $province.attr("data-regionText", regionText);
            $("#show-province").text(regionText);
            fillRegionList("#city","#county",regionId);
        });

        //市的下拉事件
        var $city = $("#city");
        $("#city").on("change",function(){
            var regionId = $(this).val();
            var regionText = $(this).find("option:selected").text();

            $city.attr("data-regionId", regionId);
            $city.attr("data-regionText", regionText);
            $("#show-city").text(regionText);

            fillRegionList("#county", null, regionId);
        });

        //区的下拉事件
        var $county = $("#county");
        $("#county").on("change",function(){
            var regionId = $(this).val();
            var regionText = $(this).find("option:selected").text();

            $county.attr("data-regionId", regionId);
            $county.attr("data-regionText", regionText);
            $("#show-county").text(regionText);
        });

        //初始化省市区
        fillRegionList("#city", null, ${(userShippingAddressMapper.provinceCode)!});
        fillRegionList("#county", null, ${(userShippingAddressMapper.cityCode)!});

        $(".radio-current").on("click", function () {
            var $this = $(this);
            $this.siblings(".radio-current").find("input").attr("checked", false);
            $this.find("input").attr("checked", true);
            $("#radios_father").attr("data-value", $this.attr("data-value"));
        });

        $("#submit_addr_but").on("click", function(){
            var success = validate();
            if(success) {
                var data = {
                    userName        : $.trim($("#realname").val()),
                    userId          : '${currentUser.id!}',
                    provinceCode    : $province.attr("data-regionId"),
                    provinceName    : $province.attr("data-regionText"),
                    cityCode        : $city.attr("data-regionId"),
                    cityName        : $city.attr("data-regionText"),
                    countyCode      : $county.attr("data-regionId"),
                    countyName      : $county.attr("data-regionText"),
                    detailAddress   : $("#detailAddress").val(),
                    postCode        : $("#post_code").val(),
                    schoolId        : '${(userShippingAddressMapper.schoolId)!''}',
                    schoolName      : $("#school_name").val(),
                    id              : '${(userShippingAddressMapper.id)!}',
                    logisticType    : $("#radios_father").attr("data-value")
                };

                var $saveBtn = $(this);

                if($saveBtn.isFreezing()){
                    return false;
                }

                $saveBtn.freezing();
                App.postJSON('/teacher/center/modifyprofile.vpage', data, function(data){
                    $saveBtn.thaw();
                    if(data.success){
                        $17.alert(data.info,function(){
                            window.location.href = "#/teacher/center/basicinfo.vpage";
                            window.location.reload(); // 再刷新保证顶部title保持一致
                        });
                    }else{
                        $17.alert(data.info);
                    }
                });
            }
        });
    });

    /**
     * @param select                需要更新的目标区域控件id
     * @param childSelect           目标区域控件子区域id
     * @param parentRegionCode      当前作为父区域的区域编码
     * @returns {boolean}
     */
    function fillRegionList(select, childSelect, parentRegionCode) {
        if ($17.isBlank(parentRegionCode)) {
            return false;
        }

        var target = $(select);
        target.empty();

        $.getJSON('/getregion.vpage?regionCode=' + parentRegionCode,function(data){
           if (data.success) {
               if(data.rows.length > 0){

                   for(var i = 0; i < data.rows.length; i++){
                       var option = $("<option>").val(data.rows[i].key).text(data.rows[i].value);
                       target.append(option);
                   }

                   if (!$17.isBlank(childSelect)) {
                       fillRegionList(childSelect, null, data.rows[0].key);
                   }
               }
               $("#county").val("${(userShippingAddressMapper.countyCode)!}");
           }
        });
    }
</script>