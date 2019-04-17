<#import "../module.ftl" as module>
<@module.page
title="编辑门店"
pageJsFile={"siteJs" : "public/script/bookstore/list"}
pageJs=["siteJs"]
leftMenu="门店列表"
>
    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>

<style>
    .input-control > label{width: 109px;}
    .input-control>.item{
        height:40px;
    }
    .h120{
        height:120px;!important;
    }
    .addImage{

    }
</style>
<div class="bread-nav">
    <a class="parent-dir" href="/bookstore/manager/update.vpage">门店列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">编辑门店</a>
</div>

<h3 class="h3-title">
    门店信息
</h3>

<form id="add-form" action="add.vpage" method="post">
    <div style="margin-top: 10px; padding:0 45px;">
        <input type="hidden" name="id" id="id" value="">
        <input type="hidden" name="mizarUserId" id="mizarUserId" value="">
        <input type="hidden" name="createMizarUserId" id="createMizarUserId" value="">
        <input type="hidden" name="updateMizarUserId" id="updateMizarUserId" value="">
        <input type="hidden" name="storeQrCode" id="storeQrCode" value="">
        <input type="hidden" name="identityPic" value="" id="title-image2">
        <div class="input-control">
            <label><span class="red-mark">*</span>门店名称：</label>
            <input name="bookStoreName" value="" id="bookStoreName" data-title="门店名称" maxlength="20" class="require item" placeholder="20字以内"/>
        </div>
             <div class="input-control">
            <label><span class="red-mark">*</span>联系人：</label>
            <input name="contactName" value="" id="contactName" data-title="联系人" maxlength="5" class="require item" placeholder="请输入联系人姓名"/>
        </div>
             <div class="input-control">
                 <label><span class="red-mark">*</span>手机号：</label>
                 <input name="mobile" value="" id="mobile" data-title="手机号" maxlength="11" class="require item" placeholder="此手机号作为登陆账号使用，请认真填写" readonly="readonly" style="border:none;"/>
             </div>

        <div class="input-control">
            <input type="hidden" name="storeAddress" id="address">
            <label><span class="red-mark">*</span>门店地址：</label>
            <div class="container">
                <select class="sel require" name="storeAddress1" style="width: 223px;" id="cmbProvinceE">
                </select>
                <select class="sel require" name="storeAddress1" style="width: 223px;" id="cmbCityE">
                </select>
            </div>
        </div>
             <div class="input-control">
                 <label><span class="red-mark">*</span>详细地址：</label>
                 <input id="detailArea" data-title="详细地址" maxlength="30" class="require item" style="height:90px;" placeholder="请输入详细地址，30字以内"/>
             </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>店铺面积：</label>
            <div style="padding:0 110px;width:60%;line-height:35px">

                <div style="display:inline-block;width:40%">
                    <input id="typeA" name="storeSizeType" type="radio" value="1" />
                    <label for="typeA">A.200平米以下</label>
                </div>
                <div style="display:inline-block;width:40%">
                    <input id="typeB" name="storeSizeType" type="radio" value="2" />
                    <label for="typeB">B.200-800平米以下</label>
                </div>
                <div style="display:inline-block;width:40%">
                    <input id="typeC" name="storeSizeType" type="radio" value="3" />
                    <label for="typeC">C.800-2000平米</label>
                </div>

               <div style="display:inline-block;width:40%">
                   <input id="typeD" name="storeSizeType" type="radio" value="4" />
                   <label for="typeD">D.2000平米以上</label>
               </div>

            </div>

        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>周边学校：</label>
            <input name="surroundingSchool" id="surroundingSchool" data-title="详细地址" maxlength="20" style="height:50px;" class="item" placeholder="请填写周边学校名称"/>
        </div>
        <div class="input-control">
            <p style="margin:30px 25px;">说明：为了方便结算，请认真填写以下信息</p>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>身份证号：</label>
            <input id="identityCardNumber" name="identityCardNumber" maxlength="18" class="item" style="height:50px;" placeholder="请填写身份证号"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>银行卡号：</label>
            <input id="bankCardNumber" name="bankCardNumber"  maxlength="20" class="item" style="height:50px;" placeholder="请填写银行卡号"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark"></span>开户行：</label>
            <input id="depositBank" name="depositBank" maxlength="30" class="item" style="height:50px;" placeholder="请填写银行卡号对应的开户行"/>
        </div>
        <div class="input-control" style="margin-left: 30px;padding-top: 10px">

            <div><span class="red-mark">*</span><span  style="font-size: 16px">上传本人手持身份证照片</span></div><br/>

            <p>1.请上传本人手持身份证照片</p>
            <p>2.照片要求可清晰看到身份证号及身份证正面本人照片</p>
            <p>3.照片大小不超过5M</p><br/>
            <div class="controls">
                <img id="preview-image2" style="display:inline-block;width:550px;height:auto">
                <div id="noneImage" style="width: 200px;height: 200px;border: 1px solid #cccccc; text-align: center; line-height: 200px; display: none">
                    <p>无图片！</p>
                </div>
            </div><br/>
            <div class="upload" id="upload" style="position: relative;width: 200px;height: 50px">
                <a id="imageSquareTrigger2" class="blue-btn submit-search" href="javascript:void(0)" style="float: none; display: inline-block; width: 100%;">+&nbsp&nbsp点击上传图片</a>

                <input type="file" id="imageSquare2" name="imageSquare2" accept="image/*" style="display: none;">

            </div>

        </div>
        <div class=" submit-box">
            <a id="add-save-btn" data-type="add" class="submit-btn save-btn" href="javascript:void(0)">提交</a>
            <a id="abandon-btn" class="submit-btn abandon-btn" href="/bookstore/manager/list.vpage">取消</a>
        </div>
    </div>
</form>


</@module.page>

<script>

    // edit page
    function GetQueryString(name)
    {
        var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if(r!=null)return  unescape(r[2]); return null;
    }
    $(document).ready(function(){
        var dataList = [],province={}, procode='',cityList=[],cName= '',p,c;

        $.post("detail.vpage", {id: GetQueryString('id')}, function (data) {
            if(data.success){
                var pdata = data.bookStoreBean;
                $('#id').val(pdata.id);
                $('#mizarUserId').val(pdata.mizarUserId);
                $('#updateMizarUserId').val(pdata.updateMizarUserId);
                $('#createMizarUserId').val(pdata.createMizarUserId);
                $('#storeQrCode').val(pdata.storeQrCode);
                $('#bookStoreName').val(pdata.bookStoreName);
                $('#contactName').val(pdata.contactName);
                $('#mobile').val(pdata.mobile);
                $('#surroundingSchool').val(pdata.surroundingSchool);
                $('#identityCardNumber').val(pdata.identityCardNumber);
                $('#bankCardNumber').val(pdata.bankCardNumber);
                $('#depositBank').val(pdata.depositBank);
                $('#title-image2').val(pdata.identityPic);

                if(pdata.identityPic && pdata.identityPic.length > 0){
                    $("#noneImage").hide();
                    $("#preview-image2").prop("src", pdata.identityPic + '?x-oss-process=image/resize,w_550/quality,q_80');
                }else{
                    $("#noneImage").show();
                    $("#preview-image2").hide();
                }

                p = pdata.storeAddressMap.provinceName;
                c = pdata.storeAddressMap.cityName;

                getProvinceEdit(); // get province

                $('#detailArea').val(pdata.storeAddressMap.detailAddress);
                if (pdata.storeSizeType == 1){
                    $('#typeA').attr("checked",true);
                } else if (pdata.storeSizeType ==2) {
                    $('#typeB').attr("checked",true);
                } else if (pdata.storeSizeType ==3) {
                    $('#typeC').attr("checked",true);
                } else {
                    $('#typeD').attr("checked",true);
                }

            }
        });
        function getProvinceEdit (procode){
            $.post("/bookstore/manager/provinces.vpage",function(res){
                if(res.success){
                    if (res.data.provinces) {
                        dataList = res.data.provinces;
                        for (var i = 0; i < dataList.length; i++) {
                            province = dataList[i];
                            procode = province.code;
                            var provinceOption = document.createElement("option");
                            $(provinceOption).attr('value',province.name);
                            $(provinceOption).attr('code',province.code);
                            $(provinceOption).text(province.name);
                            $('#cmbProvinceE').append(provinceOption);
                        }
                        if (p) {
                            $("#cmbProvinceE").find("option[value="+p+"]").attr("selected",true);
                        }

                        getCityEdit($("#cmbProvinceE").find("option:selected").attr('code')); // get city
                    }
                }

            });
        }
        function setAddress(){
            var cmbProvinceE = $('#cmbProvinceE').attr('value');
            var cmbProvinceCode = $('#cmbProvinceE').attr('code');
            var cmbCityE = $('#cmbCityE').attr('value');
            var cmbCityCode = $('#cmbCityE').attr('code');
            var detailArea = $('#detailArea').attr('value');
            var AResult = JSON.stringify({
                provinceCode:cmbProvinceCode,
                provinceName:cmbProvinceE,
                countryCode:cmbCityCode,
                cityName:cmbCityE,
                detailAddress:detailArea
            });
            $('#address').attr('value', AResult);

        }

        $('#cmbProvinceE').on('change',function () {
            $("#cmbCityE").empty();
            getCityEdit($("#cmbProvinceE").find("option:selected").attr('code'));
        })
        //
        function getCityEdit(procode) {
            if(procode){
                $.get("/bookstore/manager/regionlist.vpage?regionCode="+procode,function(res){
                    if(res.success){
                        if (res.data.regionList){
                            cityList = res.data.regionList;
                            for (var i = 0; i < cityList.length; i++) {
                                cName = cityList[i].name;
                                var cityOption = document.createElement("option");
                                $(cityOption).attr('value',cName);
                                $(cityOption).text(cName);
                                $('#cmbCityE').append(cityOption);
                            }
                            if (c) {
                                $("#cmbCityE").find("option[value="+c+"]").attr("selected",true)
                            }
                            setAddress();
                        }
                    }
                });
            }
        }

        // 赋值
        $('#cmbCityE').on('change',function () {
            setAddress();
        });
        $('#detailArea').on('keyup',function () {
            setAddress();
        });

        // 验输入
        var requireInputs = $(".require");
        function isEmptyInput() {
            var isTrue = false;
            requireInputs.each(function () {
                if ($(this).val() == '') {
                    $(this).addClass("error").val("请填写" + $(this).attr("data-title"));
                    isTrue = true;
                }
            });
            if ($(".require.error").length > 0) {
                return true;
            }
            return isTrue;
        }

        $(document).on("focus", ".require.error", function () {
            $(this).removeClass('error').val('');
        });
        // 提交
        $("#add-save-btn").on("click", function () {
            setAddress();
            if (isEmptyInput()) {
                return false;
            }
            if (!$("#cmbProvinceE").val()) {
                $.prompt("<div style='text-align:center;'>" + ( "请填写书店地址！") + "</div>", {
                    title: "提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function( e,v ){
                    },
                    useiframe: true
                });
                return;
            };
            if (!$("#title-image2").val()) {
                $.prompt("<div style='text-align:center;'>" + ( "请上传图片！") + "</div>", {
                    title: "提示",
                    buttons: {"确定": true},
                    focus: 1,
                    submit: function( e,v ){
                    },
                    useiframe: true
                });
                return;
            };
            $.prompt("<div style='text-align:center;'>是否确认保存？</div>", {
                title: "操作提示",
                buttons: {"取消":false, "确定": true},
                focus: 1,
                submit: function (e, v) {
                    if (v) {;
                        $("#add-form").ajaxSubmit(function (res) {
                            if (res.success) {
                                $.prompt("<div style='text-align:center;'>保存成功</div>", {
                                    title: "提示",
                                    buttons: {"确定": true},
                                    focus: 1,
                                    submit: function (e, v) {

                                        console.log(e,v)
                                        if(v) {
                                            location.href='/bookstore/manager/list.vpage';
                                        }
                                    },
                                    useiframe: true
                                });
                            } else {
                                $.prompt("<div style='text-align:center;'>" + (res.info || "保存失败！") + "</div>", {
                                    title: "提示",
                                    buttons: {"确定": true},
                                    focus: 1,
                                    useiframe: true
                                });
                            }
                        });
                    }
                },
                useiframe: true
            });
        });

        //点击上传图片
        $(document).on("click", "#imageSquareTrigger2", function () {
            $("#imageSquare2").click();
        });

        $('#imageSquare2').change(function(){
            $("#imageSquareTrigger2").text("图片上传中...");
            var $img = $("#preview-image2");
            var _this =  this;
            $("#noneImage").hide();
            $img.show();
            // 拼formData
            var formData = new FormData();
            var file = $(this)[0].files[0];

            if(!file) return ;
            $("#imageSquareTrigger2").val(file.name);
            if(file.size > 5242880){
                $.prompt("<div style='text-align:center;'>" +  "图片过大！" + "</div>", {
                    title: "提示",
                    buttons: {"确定": true},
                    focus: 1,
                    useiframe: true
                });
                $("#imageSquare2").val('');
                $("#imageSquareTrigger2").val('');
                $("#imageSquareTrigger2").html("+&nbsp&nbsp点击上传图片");
                return false;
            }
            formData.append('path', "bookStore");
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);


            $.ajax({
                url: '/bookstore/manager/tool/uploadphoto.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {

                        var file = $(_this)[0].files[0];
                        var reader = new FileReader();
                        reader.readAsDataURL(file);
                        reader.onload = function(e) {

                            // 图片base64化
                            var newUrl = this.result;
                            $("#preview-image2").prop("src", newUrl);
                            $("#imageSquareTrigger2").html("+&nbsp&nbsp点击上传图片");


                        };

                        $("#title-image2").val(res.info);

                    } else {
                        alert(res.info);
                    }
                }
            });

        });
    })
</script>
