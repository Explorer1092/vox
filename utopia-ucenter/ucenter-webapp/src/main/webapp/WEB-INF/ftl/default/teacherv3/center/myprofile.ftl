<div class="w-base">
    <div class="w-base-title">
        <h3>我的资料</h3>
    </div>
    <div class="t-security">
        <div class="myData">
            <div class="w-form-table">
                <dl>
                    <dt>姓名：</dt>
                    <dd>
                        <input type="text" id="realname" class="w-int" value="${(currentUser.profile.realname)!}" placeholder="请输入您的真实姓名，须为中文">
                        <span class="w-form-misInfo w-form-info-error"><strong class="info">（*必填）</strong></span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                    </dd>
                    <dt>所在学校：</dt>
                    <dd>
                        <input id="school_name" style="background-color:#e6e6e6; " disabled="disabled" type="text" class="w-int" value="${(userShippingAddressMapper.schoolName)!}" placeholder="${(userShippingAddressMapper.schoolName)!}"><span class="w-form-misInfo w-form-info-error"><strong class="info">（*必填）</strong></span>
                        <p><span class="w-form-misInfo w-form-info-error"><strong class="info">学校信息不能随意修改，有问题请致电：<@ftlmacro.hotline phoneType="teacher"/></strong></span></p>
                    </dd>
                    <dt style="width:96%;margin-top:5px;padding:20px 0 16px 4%;border-top:1px solid #bfbfbf;text-align:left;font-size:18px;font-weight:bold;color:#4e5656">收货地址：</dt>
                    <dt>收件人：</dt>
                    <dd>
                        <input type="text" id="receivename" class="w-int" value="${(userShippingAddressMapper.receiver)!}" placeholder="请输入正取的收件人姓名">
                        <span class="w-form-misInfo w-form-info-error"><strong class="info">（*必填）</strong></span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                    </dd>
                    <dt style="display: ;">所在地区：</dt>
                    <dd style="display: ;">
                        <div id="province" class="w-select">
                            <div class="current"><span class="content" data-value="${(userShippingAddressMapper.provinceCode)!}">${(userShippingAddressMapper.provinceName)!}</span><span class="w-icon w-icon-arrow"></span></div>
                            <ul id="provinceList" data-size="8">
                                <#list provinces as province>
                                    <li><a href="javascript:void(0);" data-region_id="${province.key}">${province.value}</a></li>
                                </#list>
                            </ul>
                        </div>
                        <div id="city" class="w-select">
                            <div class="current"><span class="content" data-value="${(userShippingAddressMapper.cityCode)!}">${(userShippingAddressMapper.cityName)!}</span><span class="w-icon w-icon-arrow"></span></div>
                            <ul id="cityList" data-size="8"></ul>
                        </div>
                        <div id="county" class="w-select">
                            <div class="current"><span class="content" data-value="${(userShippingAddressMapper.countyCode)!}">${(userShippingAddressMapper.countyName)!}</span><span class="w-icon w-icon-arrow"></span></div>
                            <ul id="countyList" data-size="8"></ul>
                        </div>
                        <span class="w-form-misInfo w-form-info-error"><strong class="info">（*必填）</strong></span>
                    </dd>
                    <dt>收货地址：</dt>
                    <dd>
                        <span style="background-color: #eee; display: inline-block; padding: 6px 3px 5px; vertical-align: middle;">
                            <span id="show-province">${(userShippingAddressMapper.provinceName)!}</span><span id="show-city">${(userShippingAddressMapper.cityName)!}</span><span id="show-county">${(userShippingAddressMapper.countyName)!}</span>
                        </span>
                        <input type="text" id="detailAddress" class="w-int" maxlength="40" value="${(userShippingAddressMapper.detailAddress)!}" placeholder="${(userShippingAddressMapper.detailAddress)!'请填写学校地址，作为您和学生的收货地址'}" style="width: 160px;">
                        <span class="w-form-misInfo w-form-info-error"><strong class="info">（*必填）</strong></span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                        <p><span class="w-form-misInfo w-form-info-error"><strong class="info">
                            <#if (currentTeacherDetail.isPrimarySchool())!false>
                                老师/学生共用收货地址
                            <#else>
                                学生兑换奖品收货地址
                            </#if>
                        </strong></span></p>
                    </dd>
                    <dt>邮政编码：</dt>
                    <dd><input type="text" class="w-int" id="post_code" maxlength="6" value="${userShippingAddressMapper.postCode!}" placeholder="${(userShippingAddressMapper.postCode)!}" onkeyup="this.value=this.value.replace(/\D/g,'')" onafterpaste="this.value=this.value.replace(/\D/g,'')"></dd>
                    <dt> 联系电话：</dt>
                    <#--<dd>
                        <div class="w-form-text">
                            <#if mobile?has_content>
                                ${mobile} 已经绑定手机号，<a href="#/teacher/center/securitycenter.vpage">去更换</a>
                            <#else>
                                手机号码尚未验证，<a href="#/teacher/center/securitycenter.vpage">去设置</a>
                            </#if>
                        </div>
                     </dd>-->
                    <dd>
                        <input type="text" id="receivetel" class="w-int" value="${receiverPhone!''}" placeholder="请您输入有效号码">
                        <span class="w-form-misInfo w-form-info-error"><strong class="info">（*必填）</strong></span>
                        <span class="w-form-misInfo w-form-info-error errorMsg"></span>
                    </dd>
                    <dt> 配送方式：</dt>
                    <dd id="radios_father" data-value="${((userShippingAddressMapper.logisticType!'notavailable') == "notavailable")?string('express', userShippingAddressMapper.logisticType!'')}" placeholder="请选择您的奖品寄送方式">
                        <label data-value="express" class="<#if (userShippingAddressMapper.logisticType!'') != "express" && (userShippingAddressMapper.logisticType!'') != "ems">w-radio-current<#else>${(((userShippingAddressMapper.logisticType!'') == "express")?string('w-radio-current',''))!}</#if>"><span class="w-radio"></span> 普通快递（市、县可到达）</label>
                        <label data-value="ems" class="${(((userShippingAddressMapper.logisticType!'') == "ems")?string('w-radio-current',''))!}"><span class="w-radio rad"></span> 邮局（乡、镇、村可到达，寄送时间较长）</label>
                        <#if (currentTeacherDetail.isJuniorTeacher())!false>
                            <p><span class="w-form-misInfo w-form-info-error"><strong class="info">
                                学生兑换奖品配送方式
                            </strong></span></p>
                        </#if>
                    </dd>

                </dl>
            </div>
        </div>
    </div>
</div>
<div class="w-foot-btn">
    <a id="submit_addr_but" class="w-btn" href="javascript:void(0);">确定</a>
</div>
<#include "validate.ftl"/>
<script type="text/javascript">
    if(location.pathname == "/teacher/center/myprofile.vpage"){
        location.href = "/teacher/center/index.vpage#/teacher/center/myprofile.vpage";
    }

    $(function(){
        LeftMenu.changeMenu();
        LeftMenu.focus("myprofile");


        //省的下拉事件
        var $province = $17.modules.select("#province");
        $("ul[id='provinceList'] a").on("click",function(){

            $province.set($(this).data("region_id"),$(this).text());
            $("#show-province").text($(this).text());
            fillRegionList("#city","#cityList",$(this).data("region_id"),"#county","#countyList");
        });

        //市的下拉事件
        var $city = $17.modules.select("#city");
        $("ul[id='cityList'] a").die().live("click",function(){
            $city.set($(this).data("region_id"),$(this).text());
            $("#show-city").text($(this).text());

            fillRegionList("#county","#countyList",$(this).data("region_id"));
        });

        //区的下拉事件
        var $county = $17.modules.select("#county");
        $("ul[id='countyList'] a").die().live("click",function(){
            $county.set($(this).data("region_id"),$(this).text());
            $("#show-county").text($(this).text());
        });

        //初始化省市区
        fillRegionList("${(userShippingAddressMapper.cityCode)!}","#cityList","${(userShippingAddressMapper.provinceCode)!}","${(userShippingAddressMapper.countyCode)!}","#countyList");
        //快递
        $17.modules.radios("#radios_father", "label");

        $("#submit_addr_but").on("click", function(){
            var success = validate();
            if(success) {
                var data = {
                    userName        : $.trim($("#realname").val()),
                    userId          : '${currentUser.id!}',
                    provinceCode    : $province.get(),
                    provinceName    : $("#province").find("span.content").text(),
                    cityCode        : $city.get(),
                    cityName        : $("#city").find("span.content").text(),
                    countyCode      : $county.get(),
                    countyName      : $("#county").find("span.content").text(),
                    detailAddress   : $("#detailAddress").val(),
                    postCode        : $("#post_code").val(),
                    schoolId        : '${(userShippingAddressMapper.schoolId)!''}',
                    schoolName      : $("#school_name").val(),
                    id              : '${(userShippingAddressMapper.id)!}',
                    logisticType    : $("#radios_father").attr("data-value"),
                    receiverPhone   : $.trim($("#receivetel").val()),
                    receiver        : $.trim($("#receivename").val())
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
                            if(location.href.indexOf("productId=")>-1){
                                var params = {
                                    productId: $17.getQuery("productId"),
                                    skuId: $17.getQuery("skuId"),
                                    num: $17.getQuery("num"),
                                    discountPrice: $17.getQuery("discountPrice"),
                                    couponNumber: $17.getQuery("couponNumber"),
                                    RefId: $17.getQuery("RefId")
                                };
                                window.location.href = "${(ProductConfig.getMainSiteBaseUrl())!''}/reward/product/orderconfirm.vpage?" + $.param(params);
                            }else{
                                window.location.href = "/teacher/center/index.vpage";
                            }
                        });
                    }else{
                        $17.alert(data.info);
                    }
                });
            }
        });
    });

    /**
     *
     * @param select 页面下拉框样式的DIV的属性ID
     * @param target 填充区域数据的页面元素属性ID
     * @param parentRegionCode 上级区域编码
     * @param childSelect 下一级下拉框样式的DIV的属性ID
     * @param childTarget 下一级区域数据的页面元素属性ID
     * @returns {boolean} 返回值
     */
    function fillRegionList(select,target,parentRegionCode,childSelect,childTarget){
        var $target = $(target);
        $target.empty();
        if($17.isBlank(parentRegionCode)){
            return false;
        }
        $.getJSON('/getregion.vpage?regionCode=' + parentRegionCode,function(data){
            if(data.success){
               if(data.rows.length > 0){
                   for(var i = 0; i < data.rows.length; i++){
                       var $li = $("<li></li>");
                       var $a = $("<a href='javascript:void(0);'></a>");
                       $a.attr("data-region_id",data.rows[i].key);
                       $a.text(data.rows[i].value);
                       $li.append($a);
                       $target.append($li);
                   }
                   if(!$17.isBlank(select)){
                       var $dark = $(select).find("span.content");
                       $dark.attr("data-value", data.rows[0].key);
                       $dark.text(data.rows[0].value);
                   }

                   if(!$17.isBlank(childTarget) && !$17.isBlank(childSelect)){
                       fillRegionList(childSelect,childTarget, (select != "#city" ? select : data.rows[0].key));
                   }
               }else{
                   if(!$17.isBlank(select)){
                       var $dark = $(select).find("span.content");
                       $dark.attr("data-value", "");
                       $dark.text("");
                   }
               }
                var cityText = $("#city").find(".content").text();
                var countyText = $("#county").find(".content").text();
                $("#show-city").text(cityText);
               $("#show-county").text(countyText);
            }
        });
        return false;
    }
</script>