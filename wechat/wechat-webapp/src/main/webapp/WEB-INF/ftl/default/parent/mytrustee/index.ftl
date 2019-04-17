<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='托管机构' pageJs="mytrusteeindex">
    <@sugar.capsule css=['mytrustee','jbox'] />
<div class="mc-wrap">
    <div data-bind='template: {name:"trusteeBannerTemplate"}'></div>
    <#if students?? && students?size gt 1>
        <div class="mc-childList js-childList">
            <ul>
                <#list students as stu>
                    <li data-sid="${stu.id!0}" class="js-childItem">
                        <img src="<@app.avatar href="${stu.img!}"/>"/>
                        <i class="icon"></i>
                        <p class="name textOverflow">${stu.name!""}</p>
                    </li>
                </#list>
            </ul>
        </div>
    </#if>
    <div class="mc-trustee">
        <div class="mc-trusteeList" data-bind="visible:showTrusteeList" style="display: none;">
            <ul data-bind='template: {name:"trusteeDetailTemplate",foreach: branchs}'></ul>
        </div>
        <div class="mc-trusteeEmpty emptys js-noneTrusteeService" data-bind="visible:noTrustee">
            <div class="bg"></div>
            <p>您孩子所在学校，当前还未开通此服务</p>
            <p class="">敬请期待！</p>
        </div>
        <input type="hidden" name="" id="singleChild" value="${(students[0].id)!0}"/>
        <input type="hidden" name="" id="singleChildName" value="${(students[0].name)!""}"/>
    </div>
</div>
<#--机构介绍单条模板-->
<script id='trusteeDetailTemplate' type='text/html'>
    <li class="li-cell js-trusteeDetailItem" data-bind="attr:{'data-bid': id}">
        <div class="cell-left"><img data-bind="attr:{src:(images[0].src)+'@1e_1c_0o_0l_108h_148w_90q'}"/></div>
        <div class="cell-right">
            <div class="line01 textOverflow" data-bind="text: name"></div>
            <div class="line02"><p class="intro textOverflow">距孩子<span class="js-stuName"></span>的学校<span class="js-distance" data-bind="text:distance+'m'"></span></p><div class="price"><span data-bind="text:'￥'+price" class="js-price"></span><span class="fontGrey"> 起</span></div></div>
            <div class="line03">
                <a href="javascript:void(0)" data-bind="text:tags[0]" class="js-tag"></a>
                <a href="javascript:void(0)" data-bind="text:tags[1]" class="js-tag"></a>
            </div>
        </div>
    </li>
</script>
<#--banner模板-->
<script id='trusteeBannerTemplate' type='text/html'>
    <div class="flexslider custom-flexslider" style="border: 0; margin: 0;" data-bind="visible:showBanner">
        <ul class="slides" data-bind='foreach: banners' style="overflow: hidden; width: 100%;">
            <li><a data-bind="attr:{data_href: link}" class="js-imgLinkBtn"><img data-bind="attr:{src:img+'@1e_1c_0o_0l_240h_640w_80q'}"/></a></li>
        </ul>
    </div>
</script>
<script>
    var selectIndex = "${selectIndex!0}";
</script>
</@trusteeMain.page>