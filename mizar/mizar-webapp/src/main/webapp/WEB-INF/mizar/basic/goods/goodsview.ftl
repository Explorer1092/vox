<#import "../../module.ftl" as module>
<@module.page
title="课程管理"
pageJsFile={"siteJs" : "public/script/basic/goods"}
pageJs=["siteJs"]
leftMenu="课程管理"
>

<div class="bread-nav">
    <a class="parent-dir" href="javascript:history.back();">课程列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">课程详情</a>
</div>
<div id="selected-shop" class="op-wrapper clearfix">
    <#if shopInfo??>
        <#list shopInfo as s>
            <div id='' class='shop-label' data-sid='${s.shopId!}'><label>${s.shopName!}</label></div>
        </#list>
    </#if>
</div>

<h3 class="h3-title">
    课程详情
</h3>
<div class="input-control" >
    <textarea name="mobiles"  id = "accessMobiles"  style="width: 400px; height: 500px;" placeholder="批量到课，一行一个手机号，不超过100个"></textarea>
    <div class="clearfix">
        <a class="submit-btn access-btn shop-label"  style="width: 350px;" href="javascript:;" data-goodsId='${(goods.id)!}'>批量到课</a>
    </div>
</div>
<div>
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
            <input name="totalLimit" class="item" style="width: 300px;" value="${(goods.totalLimit)!}" placeholder="不限制不填写" />
        </div>
        <div class="input-control">
            <label>每天名额：</label>
            <input name="dayLimit" class="item" style="width: 300px;" value="${(goods.dayLimit)!}" placeholder="不限制不填写" />
        </div>
        <div class="input-control">
            <label>短信文案：</label>
            <textarea name="smsMessage" class="" style="width: 300px; resize: none" rows="3" >${(goods.smsMessage)!}</textarea>
        </div>
        <div class="input-control">
            <label>年龄段：</label>
            <input name="target" class="item" style="width: 300px;" value="${(goods.target)!}" placeholder="例如6-8岁" />
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
                <option value="1"  <#if goods?? && goods.requireSchool == 1>selected</#if>>否</option>
                <option value="2"  <#if goods?? && goods.requireSchool == 2>selected</#if>>是</option>
            </select>
        </div>
        <div class="input-control">
            <label><span class="red-mark">*</span>收货地址：</label>
            <select name="requireAddress" class="require item" style="width: 300px;">
                <option value="1"  <#if goods?? && goods.requireAddress == 1>selected</#if>>否</option>
                <option value="2"  <#if goods?? && goods.requireAddress == 2>selected</#if>>是</option>
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
            <label>输入区域背景色：</label>
            <input name="inputBGColor" class="item" style="width: 300px;" value="${(goods.inputBGColor)!}"/>
        </div>

    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>顶部图片：</label>
        </div>
        <div class="image-preview bannerImg clearfix">
            <input id="top-img" style="display: none;" name="topImage" />
            <#if (goods.topImage)??>
                <div class="image">
                    <img src="${goods.topImage}" />
                </div>
            </#if>
        </div>
    </div>
    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>课程头图：</label>
        </div>
        <div class="image-preview bannerImg clearfix">
            <input id="banner-img" style="display: none;" name="bannerImg" />
            <#if (goods.bannerPhoto)??>
                <#list goods.bannerPhoto as imgUrl>
                    <div class="image">
                        <img src="${imgUrl}" />
                    </div>
                </#list>
            </#if>
        </div>
    </div>

    <div class="clearfix" style="clear:both;">
        <div class="input-control">
            <label>课程详情：</label>
        </div>
        <div class="image-preview detailImg clearfix">
            <input id="detail-img" style="display: none;" name="detailImg" />
            <#if (goods.detail)??>
                <#list goods.detail as imgUrl>
                    <div class="image">
                        <img src="${imgUrl}" />
                        <#if isNew || (goods?? && goods.status=='OFFLINE')>
                            <div class="del-btn">删除</div>
                        </#if>
                    </div>
                </#list>
            </#if>
        </div>
    </div>
    <div class="clearfix submit-box">
        <a class="submit-btn abandon-btn" href="javascript:history.back();">返回</a>
    </div>

</div>



</@module.page>