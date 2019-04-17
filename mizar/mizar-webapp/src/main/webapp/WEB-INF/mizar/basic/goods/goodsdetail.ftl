<#import "../../module.ftl" as module>
<@module.page
title="课程管理"
pageJsFile={"siteJs" : "public/script/basic/goods"}
pageJs=["siteJs"]
leftMenu="课程管理"
>
<#if isNew!false>
    <div class="bread-nav">
        <a class="parent-dir" href="/basic/goods/index.vpage">课程列表</a>
        &gt;
        <a class="parent-dir goods-step1" href="javascript:void(0);">选择生效门店</a>
        &gt;
        <a class="current-dir" href="javascript:void(0);" style="cursor: default">新增课程</a>
    </div>
<#else>
    <div class="bread-nav">
        <a class="parent-dir" href="javascript:history.back();">课程列表</a>
        &gt;
        <a class="current-dir" href="javascript:void(0);" style="cursor: default">编辑课程</a>
    </div>
</#if>
<div id="selected-shop" class="op-wrapper clearfix">
    <#if shopInfo??>
        <#list shopInfo as s>
            <div class='shop-label' data-sid='${s.shopId!}'><label>${s.shopName!}</label></div>
        </#list>
    </#if>
</div>
<h3 class="h3-title">
    ${isNew?string("新增","编辑")}课程
    <span class="h6-title">带有 <span class="red-mark" style="margin-left:4px;">*</span> 为必填项</span>
</h3>
<form action="/common/uploadphoto.vpage" method="post" style="display: none;">
    <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
    <input id="upload-image" style="display:none;" class="file" name="file" type="file" accept="image/*"/>
    <input id="goods-id" value="${(goods.id)!}" name="gid" />
</form>
<form id="detail-form" action="${isNew?string("addgoods.vpage","editgoods.vpage")}" method="post">
    <input value="${(goods.id)!}" name="gid" style="display:none;">
    <input value="${(goods.shopId)!}" name="sid" id="sid" style="display:none;">
    <div style="float:left;">
        <div class="input-control">
            <label><span class="red-mark">*</span>课程名称：</label>
            <input name="goodsName" data-title="课程名称" class="require item" style="width: 300px;" value="${(goods.goodsName)!}" />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>课程标题：</label>
            <input name="title" class="require item" style="width: 300px;" value="${(goods.title)!}" data-title="课程标题" placeholder="请填写课程特色内容 " />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>课程简介：</label>
            <textarea name="desc" data-title="课程简介" class="require" style="width: 300px; resize: none" rows="3">${(goods.desc)!}</textarea>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>课程价格：</label>
            <input name="price" class="require item" style="width: 300px;" value="${(goods.price)!}" data-title="课程价格" placeholder="单位为元 " />
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>交易成功：</label>
            <select name="dealSuccess" class="require item" style="width: 300px;">
                <option value="1"  <#if goods?? && goods.dealSuccess == 1>selected</#if>>到课</option>
                <option value="2"  <#if goods?? && goods.dealSuccess == 2>selected</#if>>到课退费</option>
            </select>
        </div>
        <div class="input-control">
            <label>总名额：</label>
            <input name="totalLimit" class="item" style="width: 300px;" value="${(goods.totalLimit)!}" placeholder="不限制填写-1" />
        </div>
        <div class="input-control">
            <label>每天名额：</label>
            <input name="dayLimit" class="item" style="width: 300px;" value="${(goods.dayLimit)!}" placeholder="不限制填写-1" />
        </div>
        <div class="input-control">
            <label>短信文案：</label>
            <textarea name="smsMessage" class="" style="width: 300px; resize: none" rows="3" >${(goods.smsMessage)!}</textarea>
        </div>
        <div class="input-control">
            <label>年龄段：</label>
            <input name="target" class="item" style="width: 300px;" value="${(goods.target)!}" placeholder="例如6-8" />
        </div>
        <div class="input-control">
            <label>年级段：</label>
            <input name="clazzLevel" class="item" style="width: 300px;" value="${(goods.clazzLevel)!}" placeholder="例如1-6" />
        </div>
        <div class="input-control">
            <label>校区：</label>
            <textarea name="schoolAreas" placeholder="英文逗号分隔"  style="width: 300px; resize: none" rows="3">${(goods.schoolAreas)!}</textarea>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>学校：</label>
            <select name="requireSchool" class="require item" style="width: 300px;">
                <option value="2"  <#if goods?? && goods.requireSchool == 2>selected</#if>>否</option>
                <option value="1"  <#if goods?? && goods.requireSchool == 1>selected</#if>>是</option>
            </select>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>收货地址：</label>
            <select name="requireAddress" class="require item" style="width: 300px;">
                <option value="2"  <#if goods?? && goods.requireAddress == 2>selected</#if>>否</option>
                <option value="1"  <#if goods?? && goods.requireAddress == 1>selected</#if>>是</option>
            </select>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>孩子姓名：</label>
            <select name="requireStudentName" class="require item" style="width: 300px;">
                <option value="2"  <#if goods?? && goods.requireStudentName == 2>selected</#if>>否</option>
                <option value="1"  <#if goods?? && goods.requireStudentName == 1>selected</#if>>是</option>
            </select>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>地区：</label>
            <select name="requireRegion" class="require item" style="width: 300px;">
                <option value="2"  <#if goods?? && goods.requireRegion == 2>selected</#if>>否</option>
                <option value="1"  <#if goods?? && goods.requireRegion == 1>selected</#if>>是</option>
            </select>
        </div>

        <div class="input-control">
            <label><span class="red-mark">*</span>按钮背景：</label>
            <input name="buttonColor"  data-title="按钮背景色" class="require item" style="width: 300px;" value="${(goods.buttonColor)!}" placeholder="#000000"/>
        </div>
        <div class="input-control">
            <label>按钮文案：</label>
            <input name="buttonText" class="item" style="width: 300px;" value="${(goods.buttonText)!}"/>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>按钮文色：</label>
            <input name="buttonTextColor" data-title="按钮文案色" class="require item" style="width: 300px;" value="${(goods.buttonTextColor)!}" placeholder="#000000"/>
        </div>

        <div class="input-control">
            <label>成功文案：</label>
            <input name="successText" class="item" style="width: 300px;" value="${(goods.successText)!}"/>
        </div>

        <div class="input-control">
            <label>下线文言：</label>
            <input name="offlineText" class="item" style="width: 300px;" value="${(goods.offlineText)!}"/>
        </div>

        <div class="input-control">
            <label>输入区域背景色：</label>
            <input name="inputBGColor" class="item" style="width: 300px;" value="${(goods.inputBGColor)!}"/>
        </div>

    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>顶部图片：</label><span class="upload-tip">请上传顶部图片，不超过1张，不超过500kb</span>
            <#if isNew || (goods?? && goods.status=='OFFLINE')>
                <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
            </#if>
        </div>
        <div class="image-preview topImg clearfix">
            <input id="top-img" style="display: none;" name="topImg" />
            <#if (goods.topImage)??>
                <div class="image">
                    <img src="${goods.topImage}" />
                    <div class="del-btn">删除</div>
                </div>
            </#if>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>banner图：</label><span class="upload-tip">请上传课程头图，上传顺序即为页面展示顺序，不超过10张，每张不超过500kb</span>
            <#if isNew || (goods?? && goods.status=='OFFLINE')>
                <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
            </#if>
        </div>
        <div class="image-preview bannerImg clearfix">
            <input id="banner-img" style="display: none;" name="bannerImg" />
            <#if (goods.bannerPhoto)??>
                <#list goods.bannerPhoto as imgUrl>
                    <div class="image">
                        <img src="${imgUrl}" />
                        <div class="del-btn">删除</div>
                    </div>
                </#list>
            </#if>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>课程详情：</label><span class="upload-tip">请上传课程详情图片，上传顺序即为页面展示顺序，不超过10张，每张不超过500kb</span>
            <#if isNew || (goods?? && goods.status=='OFFLINE')>
                <a class="blue-btn upload-image" href="javascript:void(0)">上传照片</a>
            </#if>
        </div>
        <div class="image-preview detailImg clearfix">
            <input id="detail-img" style="display: none;" name="detailImg" />
            <#if (goods.detail)??>
                <#list goods.detail as imgUrl>
                    <div class="image">
                        <img src="${imgUrl}" />
                        <div class="del-btn">删除</div>
                    </div>
                </#list>
            </#if>
        </div>
    </div>
    <div class="clearfix submit-box">
        <a id="save-btn" data-type="${isNew?string("add","edit")}" class="submit-btn save-btn" href="javascript:void(0)">提交变更</a>
        <a id="abandon-btn" class="submit-btn abandon-btn" href="javascript:void(0);">放弃${isNew?string("新建","编辑")}</a>
    </div>
</form>
</@module.page>