<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=12>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<#-- 使用已改过的 kindeditor-all.js-->
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<style>
    .field-title {
        font-weight: bold;
    }

    .prompt-content {
        margin: 0px;
        height: 57px;
        width: 338px;
    }

</style>

<div id="main_container" class="span9">
    <legend>添加奖品</legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <legend class="field-title">基础信息</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName">奖品名称：<span
                                style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <input type="text" <#if product??>value="${product.productName!''}"</#if> name="productName"
                                   id="productName" class="input">
                        </div>
                    </div>

                    <div class="control-group" id="singleItemMain">
                        <label class="control-label">单品属性：<span style="color: red;font-size: 20px;">*</span></label>
                        <#if skus?has_content && skus?size gt 1>
                            <#list skus as s>
                                <div class="controls singleItemBox" style="margin-top: 5px;">
                                    <input type="hidden" name="vitality" value="${s.id!''}" class="input idCtn">
                                    名称：<input type="text" name="vitality" value="${s.skuName!''}" class="input nameCtn">
                                    库存：<input type="text" name="vitality" value="${s.inventorySellable!''}"
                                              class="input stockCtn" style="width: 80px;">
                                    <#if s_index = 0>
                                        <input type="button" value="添加" class="btn btn-primary" id="addRowSingle"> <br/>
                                    <#else>
                                        <input type="button" value="删除" class="btn thisDelete"
                                               data-val="${s_index}"><br/>
                                    </#if>
                                </div>
                            </#list>
                        <#else>
                            <div class="controls singleItemBox" style="margin-top: 5px;">
                                <input type="hidden" name="vitality" value="${(skus[0].id)!''}" class="input idCtn">
                                名称：<input type="text" name="vitality" value="${(skus[0].skuName)!''}"
                                          class="input nameCtn">
                                库存：<input type="text" name="vitality" value="${(skus[0].inventorySellable)!''}"
                                          class="input stockCtn" style="width: 80px;">
                                <input type="button" value="添加" class="btn btn-primary" id="addRowSingle">
                            </div>
                        </#if>
                        <div id="newAddSingleItem"></div>
                    </div>


                    <div class="control-group">
                        <label class="control-label">一级分类：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <#if types??>
                                <select id="productType" name="productType">
                                    <#list types as t >
                                        <option value="${t.name()!}" <#if product?? && (product.productType == t.name())>
                                                selected </#if>>${t.getDescription()!}</option>
                                    </#list>
                                </select>
                            </#if>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">二级分类：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <div id="productTypeContent"></div>
                        <#--<input type="hidden" id="categoryIds" name="categoryIds" value=""/>-->
                            <select id="categoryId" name="categoryId"></select>
                        </div>
                    </div>

                    <div class="control-group category-field" id="prompt-type" style="display:none">
                        <label class="control-label">兑换成功提醒方式：</label>
                        <div class="controls">
                            <label style="display: inline-block">
                                <input name="prompt-option" id="send-msg" data-target="msg" value="1" type="checkbox">
                                站内信提醒
                            </label>
                            <label style="display: inline-block">
                                <input name="prompt-option" id="send-sms" data-target="sms" value="2" type="checkbox">
                                短信提醒
                            </label>
                        </div>
                    </div>

                    <div class="control-group category-field" id="prompt-sms" style="display:none">
                        <label class="control-label">短信提醒文案：</label>
                        <div class="controls">
                            <textarea id="sms-content" class="prompt-content"
                                      placeholder="提示：如果文案中包含兑换码，需要用变量$couponNo代替">
                            </textarea>
                        </div>
                    </div>

                    <div class="control-group category-field" id="prompt-msg" style="display:none">
                        <label class="control-label">站内信提醒文案：</label>
                        <div class="controls">
                            <textarea id="msg-content" class="prompt-content"
                                      placeholder="提示：如果文案中包含兑换码，需要用变量$couponNo代替">
                            </textarea>
                        </div>
                    </div>

                    <div class="control-group category-field" id="coupon-resource" style="display:none">
                        <label class="control-label">优惠券来源：</label>
                        <div class="controls">
                            <#if couponResourceTypes??>
                                <select id="couponResource" name="couponResource">
                                    <#list couponResourceTypes as t >
                                        <option value="${t.name()!}" <#if product?? && (product.couponResource == t.name())>
                                                selected </#if>>${t.getDescription()!}</option>
                                    </#list>
                                </select>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group category-field" id="isp-field" style="display:none" data-init="0">
                        <label class="control-label">运营商：</label>
                        <div class="controls">
                            <label style="display: inline-block;">
                                <input type="radio" name="isp-option" value="UNICOM"
                                       <#if (product??) && (product.relateVirtualItemId!'') == "UNICOM">checked="checked"</#if>>
                                联通
                            </label>
                            <label style="display: inline-block;">
                                <input type="radio" name="isp-option" value="MOBILE"
                                       <#if (product??) && (product.relateVirtualItemId!'') == "MOBILE">checked="checked"</#if>>
                                移动
                            </label>
                            <label style="display: inline-block;">
                                <input type="radio" name="isp-option" value="TELECOM"
                                       <#if (product??) && (product.relateVirtualItemId!'') == "TELECOM">checked="checked"</#if>>
                                电信
                            </label>
                            <label style="display: inline-block;">
                                <input type="radio" name="isp-option" value="ALL"
                                       <#if (product??) && (product.relateVirtualItemId!'') == "ALL">checked="checked"</#if>>
                                三网通
                            </label>
                        </div>
                    </div>

                    <div class="control-group category-field" id="flow-packet-type" style="display:none" data-init="0">
                        <label class="control-label">流量包大小（MB）：</label>
                        <div class="controls">
                            <select>
                                <option value="30">30</option>
                                <option value="50">50</option>
                                <option value="100">100</option>
                                <option value="500">500</option>
                                <option value="1024">1024</option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group category-field" id="head-wear-list" style="display:none" data-init="0">
                        <label class="control-label">头饰：</label>
                        <div class="controls">
                            <select>
                            </select>
                        </div>
                    </div>

                    <div class="control-group category-field" id="toby-list" style="display:none" data-init="0">
                        <label class="control-label">托比装扮：</label>
                        <div class="controls">
                            <select>
                            </select>
                        </div>
                    </div>

                    <div class="control-group category-field" id="expiry-field" style="display:none">
                        <label class="control-label">有效期(天)：</label>
                        <div class="controls">
                            <input type="text" id="expiry-date" name="expiry-date"
                                   <#if product??>value="${product.expiryDate!''}"<#else>value="15"</#if>/>
                        </div>
                    </div>

                    <div class="control-group category-field" id="video-url-field" style="display:none">
                        <label class="control-label">视频链接：</label>
                        <div class="controls">
                            <input type="text" id="video-url" name="video-url"
                                   <#if product??>value="${product.relateVirtualItemContent!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group category-field" id="article-url-field" style="display:none">
                        <label class="control-label">文章链接：</label>
                        <div class="controls">
                            <input type="text" id="article-url" name="article-url"
                                   <#if product??>value="${product.relateVirtualItemContent!''}"</#if>/>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <div style="color: red"> 一级标签和二级标签不可随意搭配，请按照 app 界面的对照关系谨慎勾选</div>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">一级标签：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls tagIdsContent">
                            <#if oneLevelTags?? >
                                <#list oneLevelTags as t >
                                    <label style="display: inline-block">
                                        <input name="tagIds" value="${t.id!}" type="checkbox"
                                               data-name="${t.tagName!}"/> ${t.tagName!}
                                    </label>
                                </#list>
                            </#if>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">二级标签：</label>
                        <div class="controls tagIdsContent">
                            <#if twoLevelTags?? >
                                <#list twoLevelTags as t >
                                    <label style="display: inline-block">
                                        <input name="tagIds" value="${t.id!}" type="checkbox"
                                               data-name="${t.tagName!}"/> ${t.tagName!}
                                    </label>
                                </#list>
                            </#if>
                        </div>
                        <input type="hidden" value="" id="tagIds" data-name=""/>
                    </div>

                <#--<div class="control-group">-->
                <#--<label class="control-label">产品图片：</label>-->
                <#--<div class="controls">-->
                <#--<input type="file" id="fileImages"  multiple="multiple"/>-->
                <#--</div>-->
                <#--</div>-->

                <#--<div class="control-group">-->
                <#--<label class="control-label">新学校可见：</label>-->
                <#--<div class="controls">-->
                <#--<input type="checkbox" id="nsVisible" name="nsVisible" <#if product?? && product.nsVisible>checked="checked"</#if>  />-->
                <#--</div>-->
                <#--</div>-->

                <#--<div class="control-group">-->
                <#--<label class="control-label">老学校可见：</label>-->
                <#--<div class="controls">-->
                <#--<input type="checkbox" id="osVisible" name="osVisible" <#if product?? && product.osVisible>checked="checked"</#if>  />-->
                <#--</div>-->
                <#--</div>-->


                <#--<div class="control-group">
                    <label class="control-label">是否返利：</label>
                    <div class="controls">
                        <input type="checkbox" id="rebated" name="rebated"
                               <#if product?? && product.rebated>checked="checked"</#if>/>
                    </div>
                </div>-->

                    <div class="control-group">
                        <label class="control-label">使用网址：</label>
                        <div class="controls">
                            <input type="text" <#if product??>value="${product.usedUrl!''}"</#if> name="usedUrl"
                                   id="usedUrl" class="input">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">产品描述：</label>
                        <div class="controls">
                            <textarea style="width: 800px; height: 150px;" id="description"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">备注：</label>
                        <div class="controls">
                            <input type="text" name="remarks" id="remarks" maxlength="100"
                                   <#if product??>value="${product.remarks!''}"</#if> class="input">
                        </div>
                    </div>
                </fieldset>
                <legend class="field-title">兑换设置</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">老师价格：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            原始积分：<input type="text" name="priceT" id="priceT"
                                        <#if product??>value="${product.priceT!''}"</#if> class="input"
                                        style="width: 50px;">
                            兑换积分：<input type="text" name="priceOldT" id="priceOldT"
                                        <#if product??>value="${product.priceOldT!''}"</#if> class="input"
                                        style="width: 50px;">
                            <span style="color: red;font-size: 12px;">(填写学豆价格，园丁豆在显示时自动换算)</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">托比装扮支付类型：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">

                            <#if product??>
                                <select name="spendType" id="spendType">
                                    <option value="0" <#if (product.spendType == 0 )>
                                                selected </#if>>学豆</option>
                                    <option value="1" <#if (product.spendType == 1 )>
                                                selected </#if>>碎片</option>
                                </select>
                                <#else>
                                 <select name="spendType" id="spendType">
                                     <option value="0" selected>学豆</option>
                                     <option value="1">碎片</option>
                                 </select>
                            </#if>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学生价格：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            原始积分：<input type="text" name="priceS" id="priceS"
                                        <#if product??>value="${product.priceS!''}"</#if> class="input"
                                        style="width: 50px;">
                            兑换积分：<input type="text" name="priceOldS" id="priceOldS"
                                        <#if product??>value="${product.priceOldS!''}"</#if> class="input"
                                        style="width: 50px;">
                            <span style="color: red;font-size: 12px;">(填写学豆价格，园丁豆在显示时自动换算)</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">进货价（元）：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <input type="text" name="buyingPrice" id="buyingPrice"
                                   <#if product??>value="${product.buyingPrice!''}"</#if> class="input"
                                   style="width: 50px;">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">老师等级：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <input type="text" name="teacherLevel" id="teacherLevel"
                                   <#if product??>value="${product.teacherLevel!''}"<#else>value="0"</#if> class="input"
                                   style="width: 50px;">
                            <span style="color: red;font-size: 12px;">(低于此等级的老师不能兑换)</span>
                        </div>
                    </div>
                <#--<div class="control-group">
                    <label class="control-label">校园大使等级：</label>
                    <div class="controls">
                        <input type="text" name="ambassadorLevel" id="ambassadorLevel"
                               <#if product??>value="${product.ambassadorLevel!''}"</#if> class="input"
                               style="width: 50px;">
                    </div>
                </div>-->

                    <div class="control-group">
                        <label class="control-label">售卖权限：</label>
                        <div class="controls">
                            <#if saleGroup?? >
                                <select id="saleGroup" name="saleGroup">
                                    <#list saleGroup as t >
                                        <option value="${t.name()!}"
                                                <#if t_index == 0 || (product?? && product.saleGroup =t.name())>selected="selected"</#if>>${t.getDescription()!}</option>
                                    </#list>
                                </select>
                            </#if>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">起兑件数：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <input type="text" name="minBuyNums" id="minBuyNums" maxlength="100"
                                   <#if product??>value="${product.minBuyNums!''}"<#else>value="1"</#if> class="input">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">是否允许重复兑换：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <select id="repeatExchanged" name="repeatExchanged">
                                <option value="true" <#if product?? && product.repeatExchanged ?? && product.repeatExchanged> selected</#if>>是</option>
                                <option value="false" <#if product?? && product.repeatExchanged ?? && !product.repeatExchanged> selected</#if>>否</option>
                            </select>
                        </div>
                    </div>

                </fieldset>
                <legend class="field-title">展示设置</legend>
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">是否上架：</label>
                        <div class="controls">
                            <input type="checkbox" id="onLined" name="onLined"
                                   <#if product?? && product.onlined>checked="checked"</#if>/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">展示端：</label>
                        <div class="controls">
                            <div id="display-terminal">
                                <label style="display: inline-block;">
                                    <input type="checkbox" value="PC"
                                           <#if (product??) && (product.displayTerminal!'')?contains("PC")>checked="checked"</#if>>
                                    PC
                                </label>
                                <label style="display: inline-block;">
                                    <input type="checkbox" value="Mobile"
                                           <#if (product??) && (product.displayTerminal!'')?contains("Mobile")>checked="checked"</#if>>
                                    移动
                                </label>
                            </div>
                            <input type="hidden" id="display-terminal-data" name="display-terminal-data" value=""/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">可见：</label>
                        <div class="controls">
                            <input type="checkbox" id="teacherVisible" name="teacherVisible"
                                   <#if product?? && product.teacherVisible>checked="checked"</#if>/>老师可见
                            <input type="checkbox" id="studentVisible" name="studentVisible"
                                   <#if product?? && product.studentVisible>checked="checked"</#if>/>学生可见
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学段：</label>
                        <div class="controls">
                            <input type="checkbox" id="primarySchoolVisible" name="primarySchoolVisible"
                                   <#if product?? && product.primarySchoolVisible>checked="checked"</#if>/>小学可见
                            <input type="checkbox" id="juniorSchoolVisible" name="juniorSchoolVisible"
                                   <#if product?? && product.juniorSchoolVisible>checked="checked"</#if>/>中学可见
                        </div>
                    </div>
                    <div class="control-group" name="grade-visible-field" style="display: none" >
                        <label class="control-label">年级：</label>
                        <div class="controls">
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="1"/> 1年级</span>
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="2"/> 2年级</span>
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="3"/> 3年级</span>
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="4"/> 4年级</span>
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="5"/> 5年级</span>
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="6"/> 6年级</span>
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="7"/> 7年级</span>
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="8"/> 8年级</span>
                            <span style="display: none"><input type="checkbox" name="grade" data-grade="9"/> 9年级</span>
                            <span style="color: red;font-size: 12px;">只针对学生有效</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学生排序值：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <input type="text" name="studentOrderValue" id="studentOrderValue"
                                   <#if product??>value="${product.studentOrderValue!''}"</#if> class="input"
                                   style="width: 50px;"><span style="color: red;font-size: 12px;">数值越大，排序越靠前</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">老师排序值：<span style="color: red;font-size: 20px;">*</span></label>
                        <div class="controls">
                            <input type="text" name="teacherOrderValue" id="teacherOrderValue"
                                   <#if product??>value="${product.teacherOrderValue!''}"</#if> class="input"
                                   style="width: 50px;"><span style="color: red;font-size: 12px;">数值越大，排序越靠前</span>
                        </div>
                    </div>
                </fieldset>
                <div class="control-group">
                    <div class="controls">
                        <input type="button" id="productMapperSubmit" value="提交" class="btn btn-large btn-primary">
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNumber").val(pageNumber);
        $("#frm").submit();
    }

    $(function () {
        var productId = 0;
        var productType = $("#productType");
        var productTypeContent = $("#productTypeContent");
        var categoryIds = $("select#categoryId");
        var tagIdsContent = $(".tagIdsContent");
        var tagIds = $("#tagIds");
        var productMapperSubmit = $("#productMapperSubmit");
        var singleItemMain = $("#singleItemMain");
        var fileImagesContent = [];
        var skuCContent = [];
        var relateVirtualItemId = "<#if product??>${product.relateVirtualItemId!''}</#if>";
        var relateVirtualItemContent = "<#if product??>${product.relateVirtualItemContent!''}</#if>";

        //奖品类型
        productType.on("change", function () {
            var $this = $(this);

            // 这个地方改成同步的，避免这边select的内容没初始化完成
            // 后面赋值的逻辑就先执行了
            $.ajax({
                type: 'POST',
                url: "getcategorys.vpage",
                data: {productType: $this.val()},
                async: false,
                success: function (data) {
                    // var html = "";
                    var $categorySelect = $("select#categoryId");
                    $categorySelect.html("");

                    $categorySelect.append("<option value=''>无</option>");
                    if (data.success) {
                        for (var i = 0, list = data.categories; i < list.length; i++) {
                            /* html += "<label style='display: inline-block;'><input type='checkbox' value='" + list[i].id + "' data-cname='"+ list[i].categoryName +"' /> " + list[i].categoryName + "</label>";*/
                            $categorySelect.append("<option value = '" + list[i].id + "' data-code = '" + list[i].categoryCode + "'>"
                                    + list[i].categoryName + "</option>")
                        }

                        // productTypeContent.html(html);
                        categoryIds.val("");
                    }
                }
            });

            if ($this.val() == "JPZX_SHIWU") {
                $("#head-wear-list").hide();
                $("#toby-list").hide();
                $("#video-url-field").hide();
                $("#article-url-field").hide();
                $("#isp-field").hide();
                $("#flow-packet-type").hide();
                $("#prompt-type").hide();
                $("#coupon-resource").hide();
                $("#prompt-sms").hide();
                $("#prompt-msg").hide();
            }

        }).change();

        //点击奖品类型
        $(document).on("change", "select#categoryId", function () {
            // 重置所有联动项的显示标志
            clearCategoryField();

            var $this = $(this);
            var categoryCode = $this.find("option:selected").data("code");
            // var tempArray = categoryIds.val() ? categoryIds.val().split(",") : [];
            // selectType($this, categoryIds, tempArray);

            // 如果是头饰的话，显示所有头饰列表
            if (categoryCode == 'HEAD_WEAR') {
                $("#head-wear-list").addClass("on");
                $("#expiry-field").addClass("on");

                // 如果未初始化，则初始化数据
                if ($("#head-wear-list").data("init") == "0") {
                    $.get("headwears.vpage", {}, function (data) {
                        if (data.success) {
                            var $select = $("#head-wear-list select");
                            $.each(data.headwears, function (index, headwear) {
                                $select.append("<option value='" + headwear.id + "'>" + headwear.name + "</option>")
                            });

                            $("#head-wear-list").data("init", "1");
                        }

                        if (relateVirtualItemId.trim() != "")
                            $select.val(relateVirtualItemId);
                        else
                        // 如果没有值则默认选中第一个做为值
                            relateVirtualItemId = $("option:first", $select).val();
                    });
                }

            } else if (categoryCode == "TOBY_WEAR") {
                $("#toby-list").addClass("on");
                $("#expiry-field").addClass("on");

                // 如果未初始化，则初始化数据
                if ($("#toby-list").data("init") == "0") {
                    $.get("tobyDressList.vpage", {}, function (data) {
                        if (data.success) {
                            var $select = $("#toby-list select");
                            $.each(data.tobyDressList, function (index, toby) {
                                $select.append("<option value='" + toby.id + "'>" + toby.name + "</option>")
                            });

                            $("#toby-list").data("init", "1");
                        }

                        if (relateVirtualItemId.trim() != "")
                            $select.val(relateVirtualItemId);
                        else
                        // 如果没有值则默认选中第一个做为值
                            relateVirtualItemId = $("option:first", $select).val();
                    });
                }
            } else if (categoryCode == "MINI_COURSE") {
                $("#video-url-field").addClass("on");
            } else if (categoryCode == "CHOICEST_ARTICLE") {
                $("#article-url-field").addClass('on');
            } else if (categoryCode == "FLOW_PACKET") {
                $("#isp-field").addClass("on")
                $("#flow-packet-type").addClass("on");

                // 初始化流量包
                if (relateVirtualItemContent != "")
                    $("#flow-packet-type select").val(relateVirtualItemContent);
                else
                    relateVirtualItemContent = $("#flow-packet-type select").val();
            } else if (categoryCode == 'COURSE_WARE' || categoryCode == 'COUPON') {
                $("#prompt-type").addClass("on");
                if (categoryCode == 'COUPON') {
                    $("#coupon-resource").addClass("on");
                }
                if ($("input#send-msg").prop("checked")) {
                    $("#prompt-msg").addClass("on");
                }

                if ($("input#send-sms").prop("checked")) {
                    $("#prompt-sms").addClass("on");
                }
            }

            controlCategoryField();
        });

        $("input[name=prompt-option]").click(function () {
            var $this = $(this);
            var target = $this.data("target");
            if ($this.prop('checked')) {
                $("#prompt-" + target).show();
            } else
                $("#prompt-" + target).hide();
        });

        function clearCategoryField() {
            $(".category-field").each(function (index, field) {
                $(field).removeClass("on");
            });
        }

        function controlCategoryField() {
            $(".category-field").each(function (index, field) {
                var $field = $(field);
                if ($field.hasClass("on"))
                    $field.show();
                else
                    $field.hide();
            })
        }

        $("#head-wear-list select").on("change", function () {
            relateVirtualItemId = $(this).val();
        });

        $("#toby-list select").on("change", function () {
            relateVirtualItemId = $(this).val();
        });

        $("#isp-field input[type=radio]").change(function () {
            relateVirtualItemId = $(this).val();
        });

        $("#flow-packet-type select").change(function () {
            relateVirtualItemContent = $(this).val();
        });

        /*$("input[name=studentVisible]").click(function () {
            if ($(this).prop("checked")) {
                $("div[name=grade-visible-field]").show();
            } else
                $("div[name=grade-visible-field]").hide();
        });*/

        $("input[name=primarySchoolVisible]").click(function () {
            var selectOpt = $(this).prop("checked");
            var juniorVisible = $("input[name=juniorSchoolVisible]").is(":checked");

            var hideIndex = 5;
            if(!selectOpt && !juniorVisible)
                hideIndex = 6;

            if(selectOpt)
                $("div[name=grade-visible-field]").show();

            $("input[name=grade]").each(function (index, field) {
                var grade = parseInt($(field).data("grade"));
                if (grade <= 6) {
                    var parent = $(field).parent();
                    // 勾选的时候才显示出来
                    if(selectOpt) {
                        parent.show();
                        $(field).prop("checked", selectOpt);
                    }
                    else if(grade <= hideIndex) {
                        parent.hide();
                        $(field).prop("checked", selectOpt);
                    }
                }
            });

            if(!selectOpt && !juniorVisible)
                $("div[name=grade-visible-field]").hide();
        });

        $("input[name=juniorSchoolVisible]").click(function () {
            var selectOpt = $(this).prop("checked");
            var primaryVisible = $("input[name=primarySchoolVisible]").is(":checked");

            var hideIndex = 7;
            if(!selectOpt && !primaryVisible)
                hideIndex = 6;

            if(selectOpt)
                $("div[name=grade-visible-field]").show();

            $("input[name=grade]").each(function (index, field) {
                var grade = parseInt($(field).data("grade"));
                if (grade >= 6) {
                    var parent = $(field).parent();
                    // 勾选的时候才显示出来
                    if(selectOpt) {
                        parent.show();
                        $(field).prop("checked", selectOpt);
                    }
                    else if(grade >= hideIndex) {
                        parent.hide();
                        $(field).prop("checked", selectOpt);
                    }
                }
            });

            if(!selectOpt && !primaryVisible)
                $("div[name=grade-visible-field]").hide();
        });

        //点击标签
        $(document).on("click", ".tagIdsContent input", function () {
            var $this = $(this);
            var tempArray = tagIds.val() ? tagIds.val().split(",") : [];
            var tempNameArray = tagIds.attr("data-name") ? tagIds.attr("data-name").split(",") : [];
            selectType($this, tagIds, tempArray);
            selectType($this, tagIds, tempNameArray, function ($id, data) {
                $id.attr("data-name", data.join());
            }, $this.attr("data-name"));
        });

        //添加单品属性
        var singleItemBox = 1;
        $(document).on("click", "#addRowSingle", function () {
            var newAddList = $("#newAddSingleItem");
            var _html = '<div class="controls singleItemBox" style="margin-top: 5px;">' +
                    '名称：<input type="text" name="vitality" value="" class="input nameCtn">' +
                    ' 库存：<input type="text" name="vitality" value="" class="input stockCtn" style="width: 80px;">' +
                    ' <input type="button" value="删除" class="btn thisDelete" data-val="' + (singleItemBox++) + '">' +
                    '</div>';
            newAddList.append(_html);
        });

        //产品描述
        var options = {
            filterMode: true,
            items: [
                'justifyleft', 'justifycenter',
                'justifyfull', 'insertorderedlist', 'insertunorderedlist',
                'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
                'italic', 'underline', 'removeformat', '|', 'image',
                'undo', 'redo', '|', 'preview', 'cut', 'copy', '|',
                'link', 'unlink', '|', 'source'
            ],
            uploadJson: 'uploaddescriptionimage.vpage?productId=${(product.id)!''}',
            fileManagerJson: 'uploaddescriptionimage.vpage',
            allowFileManager: true
        };

        var editor;
        KindEditor.ready(function (K) {
            editor = K.create('#description', options);
        });


        //删除单品属性
        $(document).on("click", "#singleItemMain .thisDelete", function () {
            var $this = $(this);

            singleItemBox--;
            skuCContent.splice($this.data("val"), 1);
            $this.closest(".singleItemBox").remove();
        });

//        //获取图片FileList
//        var inputElement = document.getElementById("fileImages");
//        inputElement.addEventListener("change", function(){
//            fileImagesContent = this.files;
//            console.info(this.files[0]);
//        }, false);

        //提交表单
        productMapperSubmit.on("click", function () {
            //获取单品属性列表
            skuCContent = [];
            singleItemMain.find(".singleItemBox").each(function (index) {
                var $this = $(this);

                //如果存在修改数组
                if ($.inArray(skuCContent[index], skuCContent) > -1) {
                    if (skuCContent[index].skuName != $this.find(".nameCtn").val() || skuCContent[index].skuName != $this.find(".stockCtn").val()) {
                        //如果都不为空才PUSH数据
                        if ($this.find(".nameCtn").val() && $this.find(".stockCtn").val()) {
                            skuCContent[index].skuId = $this.find(".idCtn").val();
                            skuCContent[index].skuName = $this.find(".nameCtn").val();
                            skuCContent[index].skuQuantity = $this.find(".stockCtn").val();
                        }
                    }
                } else {
                    //如果都不为空才PUSH数据
                    if ($this.find(".nameCtn").val() && $this.find(".stockCtn").val()) {
                        skuCContent.push({
                            skuId: $this.find(".idCtn").val(),
                            skuName: $this.find(".nameCtn").val(),
                            skuQuantity: $this.find(".stockCtn").val()
                        });
                    }
                }
            });

            editor.sync();

            var displayTerminals = $("#display-terminal input:checkbox:checked")
                    .map(function () {
                        if ($(this).is(":checked"))
                            return $(this).val();
                    })
                    .get()
                    .join(",");

            var gradeVisible = $("input[name=grade]")
                    .map(function () {
                        if ($(this).is(":checked"))
                            return $(this).data("grade");
                    })
                    .get()
                    .join(",");

            var videoUrl = $('input#video-url').val();
            if (videoUrl && videoUrl.trim() != '' && $("input#video-url").is(":visible")) {
                relateVirtualItemContent = videoUrl;
            }

            var articleUrl = $("input#article-url").val();
            if (articleUrl && articleUrl.trim() != '' && $("input#article-url").is(":visible")) {
                relateVirtualItemContent = articleUrl;
            }

            var productMapper = {
                priceT: parseInt($("#priceT").val()),
                priceOldT: parseInt($("#priceOldT").val()),
                priceS: parseInt($("#priceS").val()),
                priceOldS: parseInt($("#priceOldS").val()),
                teacherLevel: $("#teacherLevel").val(),
                ambassadorLevel: $("#ambassadorLevel").val(),
                productName: $("#productName").val(),
                tags: tagIds.attr("data-name"),
                saleGroup: $("#saleGroup").val(),
                teacherVisible: $("#teacherVisible").prop("checked"),
                studentVisible: $("#studentVisible").prop("checked"),
                primarySchoolVisible: $("#primarySchoolVisible").prop("checked"),
                juniorSchoolVisible: $("#juniorSchoolVisible").prop("checked"),
//                osVisible: $("#osVisible").prop("checked"),
//                nsVisible: $("#nsVisible").prop("checked"),
                categoryIds: categoryIds.val(),
                tagIds: tagIds.val(),
                productType: productType.val(),
//                images : fileImagesContent,
                skus: skuCContent,
                onlined: $("#onLined").prop("checked"),
                rebated: $("#rebated").prop("checked"),
                usedUrl: $("#usedUrl").val(),
                description: editor.html(),
                buyingPrice: $("#buyingPrice").val(),
                studentOrderValue: $("#studentOrderValue").val(),
                teacherOrderValue: $("#teacherOrderValue").val(),
                expiryDate: $("#expiry-date").val(),
                relateVirtualItemId: relateVirtualItemId,
                displayTerminal: displayTerminals,
                relateVirtualItemContent: relateVirtualItemContent,
                remarks: $("#remarks").val(),
                minBuyNums: $("#minBuyNums").val(),
                repeatExchanged: $("#repeatExchanged").val(),
                categoryCode: $("select#categoryId").find("option:selected").data("code"),
                needSendSms: $("input#send-sms").prop("checked"),
                needSendMsg: $("input#send-msg").prop("checked"),
                couponResource:$("#couponResource").val(),
                smsContent: $("#sms-content").val(),
                msgContent: $("#msg-content").val(),
                gradeVisible: gradeVisible,
                spendType: $("#spendType").val()
                <#if product??>, productId: '${(product.id)!''}'</#if>
            };

            if(!productMapper.productId && productId != 0)
                productMapper.productId = productId;

            if (productMapper.buyingPrice == undefined || productMapper.buyingPrice.trim() == '') {
                alert("请输入进货价");
                return false;
            }

            if (productMapper.productName == undefined || productMapper.productName.trim() == '') {
                alert("请输入奖品名称");
                return false;
            }

            if (productMapper.priceT == undefined || isNaN(productMapper.priceT)) {
                alert("请输入老师原价");
                return false;
            }

            if (productMapper.priceOldT == undefined || isNaN(productMapper.priceOldT)) {
                alert("请输入老师老学校价格");
                return false;
            }

            if (productMapper.priceS == undefined || isNaN(productMapper.priceS)) {
                alert("请输入学生原价");
                return false;
            }

            if (productMapper.priceOldS == undefined || isNaN(productMapper.priceOldS)) {
                alert("请输入学生老学校价格");
                return false;
            }

            if (productMapper.priceOldS < 10 || productMapper.priceOldT < 10 || productMapper.priceS < 10 || productMapper.priceT < 10) {
                alert("积分必须大于等于10!");
                return false;
            }

            if (productMapper.priceS < productMapper.priceOldS || productMapper.priceT < productMapper.priceOldT) {
                alert("原始积分不能小于兑换积分!");
                return false;
            }

            if (productMapper.teacherLevel == undefined || productMapper.teacherLevel.toString().trim() == '') {
                alert("请输入老师等级限制，默认请填0");
                return false;
            }

            if (productMapper.studentOrderValue == undefined || productMapper.studentOrderValue.toString().trim() == '') {
                alert("请输入学生排序值!");
                return false;
            }

            if (productMapper.teacherOrderValue == undefined || productMapper.teacherOrderValue.toString().trim() == '') {
                alert("请输入老师排序值!");
                return false;
            }

            if (productMapper.tagIds.length <= 0) {
                alert("请选择标签");
                return false;
            }

            if (productMapper.categoryIds.length <= 0) {
                alert("请选择目录");
                return false;
            }

            if(productMapper.categoryCode.toString().trim() == ''){
                alert("请选择二级分类!");
                return false;
            }

            if (productMapper.skus.length <= 0) {
                alert("请输入单品属性");
                return false;
            }

            var numberReg = /^\+?[1-9][0-9]*$/;
            if (!$("#expiry-field").is(":hidden") && !numberReg.test(productMapper.expiryDate)) {
                alert("有效期必须为大于零的正整数");
                return false;
            }

            if (productMapper.minBuyNums == undefined || productMapper.minBuyNums.trim() == '') {
                alert("起兑件数不能为空!");
                return false;
            }

            if (!numberReg.test(productMapper.minBuyNums)) {
                alert("起兑件数必须为大于零的正整数");
                return false;
            }
            if (!(productMapper.categoryCode.toString().trim() == 'COUPON')) {
                productMapper.couponResource = null;
            }
            appPostJson("addproduct.vpage", productMapper, function (data) {
                if (data.success) {
                    /* location.href = "productlist.vpage";*/
                    productId = data.productId;
                    alert("保存成功!");
                } else {
                    alert(data.info);
                }
            });
        });

        function appPostJson(url, data, callback, error, dataType) {
            dataType = dataType || "json";
            return $.ajax({
                type: 'post',
                url: url,
                data: JSON.stringify(data),
                success: callback,
                error: error,
                dataType: dataType,
                contentType: 'application/json;charset=UTF-8'
            });
        }

        function selectType($this, $id, data, callback, $value) {
            $value = $value ? $value : $this.val();

            if ($this.prop("checked") == true) {
                if ($.inArray($value, data) < 0) {
                    data.push($value);
                }
            } else {
                if ($.inArray($value, data) > -1) {
                    data.splice($.inArray($value, data), 1);
                }
            }

            if (callback) {
                $this.callback = callback($id, data);
            } else {
                $id.val(data.join());
            }
        }


        //编辑
        /*目录分类  编辑已选择的  */
        <#if categoryRefs??>
            <#list categoryRefs as c>
                setTimeout(function () {
                    /*$("#productTypeContent input").each(function (i) {
                        if ($(this).val() == ) {
                            $(this).trigger('click');
                        }


                    });*/
                    $("select#categoryId").val(${c.categoryId});
                    $("select#categoryId").trigger("change");
                }, 300);
            </#list>
        </#if>

        <#if sendSms?? && sendSms>
            setTimeout(function () {
                $("input#send-sms").trigger("click");
                $("textarea#sms-content").val("${smsTpl}");
            }, 300);
        </#if>

        <#if sendMsg?? && sendMsg>
            setTimeout(function () {
                $("input#send-msg").trigger("click");
                $("textarea#msg-content").val("${msgTpl}");
            }, 300);
        </#if>

        /*一、二 级标签*/
        <#if tagRefs??>
            <#list tagRefs as t>
                $("input[name='tagIds']").each(function () {
                    if ($(this).val() == ${t.tagId}) {
                        $(this).trigger('click');
                    }
                });
            </#list>
        </#if>

        <#if product??>
            /* 处理 ‘product.description’ 传过来的数据 把相对应的 ‘\n’ 处理成空字符串 */
            setTimeout(function () {
                editor.html('${(product.description)?replace('\n','')?replace("'" , "\\'")!''}');
            }, 600);
        </#if>

        <#if product?? && product.studentVisible>

            <#if product.primarySchoolVisible || product.juniorSchoolVisible>
                $("div[name=grade-visible-field]").show();
            </#if>

            var gradeVisible = "${product.gradeVisible!''}";
            var gradeOptions = gradeVisible.split(",")
                    .map(function (item) {
                        return parseInt(item);
                    });

            <#if product.primarySchoolVisible>
                $("input[name=grade]").each(function (index, field) {
                    var grade = parseInt($(field).data("grade"));
                    if (grade <= 6) {
                        $(field).parent().show();

                        if (gradeVisible == "" || $.inArray($(field).data("grade"), gradeOptions) >= 0) {
                            $(field).prop("checked", true);
                        }
                    }
                });
            </#if>

            <#if product.juniorSchoolVisible>
                $("input[name=grade]").each(function (index, field) {
                    var grade = parseInt($(field).data("grade"));
                    if (grade >= 6) {
                        $(field).parent().show();

                        if (gradeVisible == "" || $.inArray($(field).data("grade"), gradeOptions) >= 0) {
                            $(field).prop("checked", true);
                        }
                    }
                });
            </#if>


        </#if>
    });
</script>
</@layout_default.page>