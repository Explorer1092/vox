<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<#-- 使用已改过的 kindeditor-all.js-->
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    /*.uploadBox{ height: 100px;}*/
    .uploadBox .addBox {
        cursor: pointer;
        width: 170px;
        height: 124px;
        border: 1px solid #ccc;
        text-align: center;
        color: #ccc;
        float: left;
        margin-right: 20px;
    }

    .uploadBox .addBox .addIcon {
        vertical-align: middle;
        display: inline-block;
        font-size: 80px;
        line-height: 95px;
    }

    .uploadBox img {
        width: 500px;
        height: 124px;
    }

    .textNote {
        color: grey;
    }
</style>

<div id="main_container" class="span9">
    <legend><font color="#00bfff">学习币商城</font>/商品详情</legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>商品ID:</label>
                    <div class="controls">
                        <input type="text" value="${commodity.id!''}" disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>商品名称:</label>
                    <div class="controls">
                        <input type="text" value="${commodity.name!''}" disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>商品分类:</label>
                    <div class="controls">
                        <input type="text" value="${category!''}" disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>展示栏目:</label>
                    <div class="controls">
                        <input type="text" value="${column!''}" disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>商品图片</label>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <div class="uploadBox">
                            <#if commodity.images?has_content>
                                <#list commodity.images as imgUrl>
                                <div id="addBox0" class="addBox" data-pic_index="0">
                                    <img src="${imgUrl}">
                                </div>
                                </#list>
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">商品状态:</label>
                    <div class="controls">
                        <input type="text" value=<#if commodity.onSale?? && commodity.onSale>上架<#else >下架</#if> disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>库存数量:</label>
                    <div class="controls">
                        <input type="text" value="${commodity.stock!0}" disabled="disabled">件
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">采购价:</label>
                    <div class="controls">
                        <input type="text" value="${commodity.purchase!0}" disabled="disabled">元
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="textNote">公司内部采购价</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>配送费:</label>
                    <div class="controls">
                        <input type="text" value="${commodity.dispatchPrice!0}" disabled="disabled">元
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>用户专享:</label>
                    <div class="controls">
                        <input type="radio" name="userType" value="all" <#if userType == 'all'>checked="checked"</#if> disabled="disabled">所有用户
                        <input type="radio" name="userType" value="MONITOR" <#if userType == 'MONITOR'>checked="checked"</#if> disabled="disabled">KOL
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>KOL价格:</label>
                    <div class="controls">
                        原价学习币:<input type="text" id="monitorCoin" value="${commodity.monitorCoin!0}" style="width: 50px;" disabled="disabled">
                        优惠学习币:<input type="text" id="monitorCoinS" value="${commodity.monitorCoinS!0}" style="width: 50px;" disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>普通价格:</label>
                    <div class="controls">
                        原价学习币:<input type="text" id="ordinaryCoin" value=${commodity.ordinaryCoin!0} style="width: 50px;" disabled="disabled">
                        优惠学习币:<input type="text" id="ordinaryCoinS" value=${commodity.ordinaryCoinS!0} style="width: 50px;" disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">是否允许重复兑换:</label>
                    <div class="controls">
                        <input type="text" value=<#if commodity.allowRepeat?? && commodity.allowRepeat>是<#else >否</#if> disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">是否推荐至首页banner</label>
                    <div class="controls">
                        <input type="radio" name="recommendFlag" value="true" <#if commodity.recommendFlag?? && commodity.recommendFlag>checked="checked"</#if> disabled="disabled">是
                        <input type="radio" name="recommendFlag" value="false" <#if !commodity.recommendFlag?? || !commodity.recommendFlag>checked="checked"</#if> disabled="disabled" >否
                    </div>
                </div>
                <div class="control-group recommendDiv">
                    <label class="control-label">自定义推荐时间:</label>
                    <div class="controls">
                        <input id="startDate" value="${startDate!''}" class="input-medium" type="text" disabled="disabled">
                        <span style="font-size: 20px">~</span>
                        <input id="endDate" value="${endDate!''}" class="input-medium" type="text" disabled="disabled">
                    </div>
                </div>
                <div class="control-group recommendDiv">
                    <div class="controls">
                        <#assign weekDayList = commodity.weekDayList![]>
                        <input type="text" id="weekDayListSize" value="${weekDayList?size}" style="display: none">
                        <input type="checkbox" name="weekDay" value="1" <#if weekDayList?seq_contains(1)>checked="checked"</#if> disabled="disabled">周一&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="2" <#if weekDayList?seq_contains(2)>checked="checked"</#if> disabled="disabled">周二&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="3" <#if weekDayList?seq_contains(3)>checked="checked"</#if> disabled="disabled">周三&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="4" <#if weekDayList?seq_contains(4)>checked="checked"</#if> disabled="disabled">周四&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="5" <#if weekDayList?seq_contains(5)>checked="checked"</#if> disabled="disabled">周五&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="6" <#if weekDayList?seq_contains(6)>checked="checked"</#if> disabled="disabled">周六&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="0" <#if weekDayList?seq_contains(0)>checked="checked"</#if> disabled="disabled">周日
                    </div>
                </div>
                <div class="control-group recommendDiv">
                    <label class="control-label">首页banner图:</label>
                    <div class="controls">
                        <label id="imgNote" style="color: red">1张，格式为PNG、JPG，470*240px，小于6M</label>
                    </div>
                </div>
                <div class="control-group recommendDiv">
                    <div class="controls">
                        <div class="uploadBox">
                            <div id="addBox4" class="addBox recommendBox" data-pic_index="4">
                                <img src="${commodity.recommendImage!''}">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="control-group recommendDiv">
                    <label class="control-label">banner推荐权重值:</label>
                    <div class="controls">
                        <input type="text" id="recommendOrder" value="${commodity.recommendOrder!0}" disabled="disabled">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">商品展示权重值:</label>
                    <div class="controls">
                        <input type="text" id="order" value="${commodity.order!0}" disabled="disabled">
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="textNote">权重越大，商品展示越靠前</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">商品描述:</label>
                    <div class="controls">
                        <script id="description" type="text/plain"></script>
                    </div>
                    <span id="desc" hidden="hidden">${commodity.description!''}</span>
                </div>
                <div class="control-group">
                    <label class="control-label">创建人:</label>
                    <div class="controls">
                        <input type="text" id="author" value="${commodity.author}" disabled="disabled">
                    </div>
                </div>

            </form>
        </div>
    </div>
</div>

<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>

<script type="text/javascript">
    $(function () {
        //商品描述
        var ue = UE.getEditor('description', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 1040,
            fontsize: [16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });
        ue.ready(function () {
            ue.setContent($("#desc").html());
        });
        $("input[name='userType'][value='${userType}']").prop("checked", true);

        var recommendFlag = $('input[name="recommendFlag"]:checked').val();
        if (recommendFlag == "true") {
            $(".recommendDiv").show();
        } else {
            $(".recommendDiv").hide();
        }

        $('input[name="recommendFlag"]').on('click', function () {
            if ($(this).val() == "true") {
                $(".recommendDiv").show();
            }  else {
                $(".recommendDiv").hide();
            }
        });
    });

</script>

</@layout_default.page>