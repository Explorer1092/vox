<#import "../module.ftl" as module>
<@module.page
title="新建门店"
pageJsFile={"siteJs" : "public/script/bookstore/list"}
pageJs=["siteJs"]
leftMenu="门店列表"
>
<style>
    .input-control > label{width: 109px;}
    .input-control>.item{
        height:40px;
    }
    .h120{
        height:120px;!important;
    }
    .controls{

    }
</style>
<div class="bread-nav">
    <a class="parent-dir" href="/bookstore/manager/update.vpage">门店列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">新建门店</a>
</div>

<h3 class="h3-title">
    门店信息
</h3>

    <form id="add-form" action="add.vpage" method="post">
        <div style="margin-top: 10px; padding:0 45px;">
            <input type="hidden" name="mizarUserId" id="mizarUserId" value="">
            <input type="hidden" name="createMizarUserId" id="createMizarUserId" value="">
            <input type="hidden" name="updateMizarUserId" id="updateMizarUserId" value="">
            <input type="hidden" name="identityPic" value="" id="title-image">
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
                <input name="mobile" value="" id="mobile" data-title="手机号" maxlength="11" class="require item" placeholder="此手机号作为登陆账号使用，请认真填写"/>
            </div>

            <div class="input-control">
                <input type="hidden" name="storeAddress" id="address">
                <label><span class="red-mark">*</span>门店地址：</label>
                <div class="container">
                    <select class="sel require" name="storeAddress1" style="width: 223px;" data-title="选择省" id="cmbProvince">
                    </select>
                    <select class="sel require" name="storeAddress2" style="width: 223px;" data-title="选择市" id="cmbCity">
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
                        <input type="radio" name="storeSizeType" value="1" id="male"/>
                        <label id="storeSizeType" for="male">A.200平米以下</label>
                    </div>
                    <div style="display:inline-block;width:40%">
                        <input type="radio"  name="storeSizeType" value="2" id="emale"/>
                        <label  id="storeSizeType" for="emale">B.200-800平米以下</label>
                    </div>
                    <div style="display:inline-block;width:40%">
                        <input type="radio"  name="storeSizeType" value="3" id="amale"/>
                        <label  id="storeSizeType" for="amale">C.800-2000平米</label>
                    </div>
                    <div style="display:inline-block;width:40%">
                        <input type="radio" name="storeSizeType" value="4" id="formale"/>
                        <label id="storeSizeType" for="formale">D.2000平米以上</label>
                    </div>
                </div>

            </div>
            <div class="input-control">
                <label><span class="red-mark"></span>周边学校：</label>
                <input name="surroundingSchool" id="surroundingSchool" maxlength="20" style="height:50px;" class="item" placeholder="请填写周边学校名称"/>
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
                    <img id="preview-image" style="display:inline-block;width:550px;height:auto">

                </div><br/>
                <div class="upload" id="upload">
                    <a id="imageSquareTrigger" class="blue-btn submit-search" href="javascript:void(0)" style="float: none; display: inline-block; width: auto;">+&nbsp&nbsp点击上传图片</a>
                    <input type="file" id="imageSquare" accept="image/*" style="display: none;">
                </div>

            </div>
            <div class=" submit-box">
                <a id="add-save-newpage" data-type="add" class="submit-btn save-btn" href="javascript:void(0)">提交</a>
                <a id="abandon-btn" class="submit-btn abandon-btn" href="/bookstore/manager/list.vpage">取消</a>
            </div>

        </div>
    </form>


</@module.page>



