<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
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
        font-size: 20px;
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
    <legend><font color="#00bfff">学习币商城</font>/商品编辑</legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>商品ID:</label>
                    <div class="controls">
                        <input type="text" id="commodityId" disabled="disabled" value="${commodity.id!''}">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>商品名称:</label>
                    <div class="controls">
                        <input type="text" id="commodityName" value="${commodity.name!''}">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>商品分类:</label>
                    <div class="controls">
                        <select id="category">
                            <#if categoryMap?has_content>
                                <#list categoryMap? keys as key>
                                    <option value="${key}" <#if commodity.category?? && commodity.category=="${key}">selected="selected"</#if>>${categoryMap[key]}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>展示栏目:</label>
                    <div class="controls">
                        <select id="column">
                            <#if columnMap?has_content>
                                <#list columnMap? keys as key>
                                    <option value="${key}" <#if commodity.column?? && commodity.column=="${key}">selected="selected"</#if>>${columnMap[key]}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>商品图片</label>
                    <div class="controls">
                        <label id="imgNote" style="color: red"></label>
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <div class="uploadBox">
                            <table>
                                <tr>
                                    <td>
                                        <button class="deleteImg" data-pic_index="0">删除</button>
                                    </td>
                                    <td>
                                        <button class="deleteImg" data-pic_index="1">删除</button>
                                    </td>
                                    <td>
                                        <button class="deleteImg" data-pic_index="2">删除</button>
                                    </td>
                                    <td>
                                        <button class="deleteImg" data-pic_index="3">删除</button>
                                    </td>
                                </tr>
                             <#assign imgUrls=imageMap!{}>
                             <#assign imgNames=imageNameMap!{}>
                                <tr>
                                    <td>
                                        <div id="addBox0" class="addBox regularBox" data-pic_index="0">
                                    <#assign imgUrl=imgUrls['img1']!''>
                                    <#assign imgName=imgNames['img1']!''>
                                            <span class="imgShowBox">
                                    <#if imgUrl!="">
                                        <img src="${imgUrl}" data-file_name="${imgName}">
                                    <#else >
                                        <i class="addIcon">首页展示</i>
                                    </#if>
                                            </span>
                                            <input class="fileUpBtn" id="fileUp0" type="file" accept="image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                        </div>
                                    </td>
                                    <td>
                                        <div id="addBox1" class="addBox regularBox" data-pic_index="1">
                                <#assign imgUrl=imgUrls['img2']!''>
                                <#assign imgName=imgNames['img2']!''>
                                            <span class="imgShowBox">
                                 <#if imgUrl!="">
                                        <img src="${imgUrl}"  data-file_name="${imgName}">
                                 <#else >
                                        <i class="addIcon">商品轮播</i>
                                 </#if>
                                            </span>
                                            <input class="fileUpBtn"  id="fileUp1" type="file" accept="image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                        </div>
                                    </td>
                                    <td>
                                        <div id="addBox2" class="addBox regularBox" data-pic_index="2">
                                <#assign imgUrl=imgUrls['img3']!''>
                                <#assign imgName=imgNames['img3']!''>
                                            <span class="imgShowBox">
                                 <#if imgUrl!="">
                                        <img src="${imgUrl}" data-file_name="${imgName}">
                                 <#else >
                                        <i class="addIcon">商品轮播</i>
                                 </#if>
                                            </span>
                                            <input class="fileUpBtn" id="fileUp2" type="file" accept="image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                        </div>
                                    </td>
                                    <td>
                                        <div id="addBox3" class="addBox regularBox" data-pic_index="3">
                                <#assign imgUrl=imgUrls['img4']!''>
                                <#assign imgName=imgNames['img4']!''>
                                            <span class="imgShowBox">
                                 <#if imgUrl!="">
                                        <img src="${imgUrl}" data-file_name="${imgName}">
                                 <#else >
                                        <i class="addIcon">商品轮播</i>
                                 </#if>
                                            </span>
                                            <input class="fileUpBtn" id="fileUp3" type="file" accept="image/jpeg, image/png, image/jpg"  style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">商品状态:</label>
                    <div class="controls">
                        <input type="radio" name="status" value="yes" <#if commodity.onSale?? && commodity.onSale>checked="checked"</#if>>上架
                        <input type="radio" name="status" value="no" <#if !commodity.onSale?? || !commodity.onSale>checked="checked"</#if>>下架
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>库存数量:</label>
                    <div class="controls">
                        <input type="text" id="stock" value="${commodity.stock!0}">件
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">采购价:</label>
                    <div class="controls">
                        <input type="text" id="purchase" placeholder="必填，输入0-999999的数字" value="${commodity.purchase!0}">元
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="textNote">公司内部采购价</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>配送费:</label>
                    <div class="controls">
                        <input type="text" id="dispatchPrice" placeholder="必填，输入0-999999的数字" value="${commodity.dispatchPrice!0}">元
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>用户专享:</label>
                    <div class="controls">
                        <input type="radio" name="userType" value="all" <#if userType == 'all'>checked="checked"</#if>>所有用户
                        <input type="radio" name="userType" value="MONITOR" <#if userType == 'MONITOR'>checked="checked"</#if>>KOL
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>KOL价格:</label>
                    <div class="controls">
                        原价学习币:<input type="text" id="monitorCoin" value="${commodity.monitorCoin!0}" style="width: 50px;">
                        优惠学习币:<input type="text" id="monitorCoinS" value="${commodity.monitorCoinS!0}" style="width: 50px;">
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="textNote">学习币兑换率=1：100，即：1元=100学习币</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label"><span style="color: red;font-size: 20px;">*</span>普通价格:</label>
                    <div class="controls">
                        原价学习币:<input type="text" id="ordinaryCoin" value=${commodity.ordinaryCoin!0} style="width: 50px;">
                        优惠学习币:<input type="text" id="ordinaryCoinS" value=${commodity.ordinaryCoinS!0} style="width: 50px;">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">是否允许重复兑换:</label>
                    <div class="controls">
                        <input type="radio" name="allowRepeat" value="yes" <#if commodity.allowRepeat?? && commodity.allowRepeat>checked="checked"</#if>>是
                        <input type="radio" name="allowRepeat" value="no" <#if !commodity.allowRepeat?? || !commodity.allowRepeat>checked="checked"</#if>>否
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">是否推荐至首页banner</label>
                    <div class="controls">
                        <input type="radio" name="recommendFlag" value="true" <#if commodity.recommendFlag?? && commodity.recommendFlag>checked="checked"</#if>>是
                        <input type="radio" name="recommendFlag" value="false" <#if !commodity.recommendFlag?? || !commodity.recommendFlag>checked="checked"</#if> >否
                    </div>
                </div>
                <div class="control-group recommendDiv">
                    <label class="control-label">自定义推荐时间:</label>
                    <div class="controls">
                        <input id="startDate" value="${startDate!''}" name="startDate" class="input-medium" type="text" autocomplete="off">
                        <span style="font-size: 20px">~</span>
                        <input id="endDate" value="${endDate!''}" name="endDate" class="input-medium" type="text" autocomplete="off">
                    </div>
                </div>
                <div class="control-group recommendDiv">
                    <div class="controls">
                        <#assign weekDayList = commodity.weekDayList![]>
                        <input type="text" id="weekDayListSize" value="${weekDayList?size}" style="display: none">
                        <input type="checkbox" name="weekDay" value="1" <#if weekDayList?seq_contains(1)>checked="checked"</#if>>周一&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="2" <#if weekDayList?seq_contains(2)>checked="checked"</#if>>周二&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="3" <#if weekDayList?seq_contains(3)>checked="checked"</#if>>周三&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="4" <#if weekDayList?seq_contains(4)>checked="checked"</#if>>周四&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="5" <#if weekDayList?seq_contains(5)>checked="checked"</#if>>周五&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="6" <#if weekDayList?seq_contains(6)>checked="checked"</#if>>周六&nbsp;&nbsp;
                        <input type="checkbox" name="weekDay" value="0" <#if weekDayList?seq_contains(0)>checked="checked"</#if>>周日
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
                            <span class="imgShowBox">
                                <#assign rImgMap = recommendImgMap!{}>
                                <#assign rImgName = rImgMap['imageName']!''>
                                <#assign rImgUrl = rImgMap['imageUrl']!''>
                                <#if rImgName!="">
                                    <img src="${rImgUrl}" data-file_name="${rImgName}">
                                <#else >
                                 <i class="addIcon">首页banner</i>
                                </#if>
                            </span>
                                <input class="fileUpBtn" id="fileUp" type="file"
                                       accept="image/jpeg, image/png, image/jpg"
                                       style="width: 100%;display:block; height: 100px; top:-100px; position: relative; opacity: 0;">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="control-group recommendDiv">
                    <label class="control-label">banner推荐权重值:</label>
                    <div class="controls">
                        <input type="text" id="recommendOrder" value="${commodity.recommendOrder!0}">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">商品展示权重值:</label>
                    <div class="controls">
                        <input type="text" id="order" value="${commodity.order!0}">
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="textNote">权重越大，商品展示越靠前</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">商品描述:</label>
                    <div class="controls">
                        <label style="color: red">建议字体使用：微软雅黑，595959色号；图片宽度为690px
                        </label>
                    </div>
                </div>
                <div>
                    <div class="controls">
                        <script id="description" type="text/plain"></script>
                    </div>
                    <span id="desc" hidden="hidden">${commodity.description!''}</span>
                </div>
                <div class="control-group">
                    <label class="control-label">创建人:</label>
                    <div class="controls">
                        <input type="text" id="author" disabled="disabled" value="${commodity.author!''}">
                    </div>
                </div>
                <div class="control-group">
                    <div class="controls">
                        <input type="button" id="saveBtn" value="提交" class="btn btn-large btn-primary">
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
        $("#startDate").datetimepicker({
            showSecond: true,
            autoclose: true,
            minuteStep: 1,
            format: 'yyyy-mm-dd hh:ii'
        });
        $("#endDate").datetimepicker({
            showSecond: true,
            autoclose: true,
            minuteStep: 1,
            format: 'yyyy-mm-dd hh:ii'
        });

        //商品描述
        var ue = UE.getEditor('description', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 0,
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

        var column = $("#column option:selected").val();
        if (column == 'RECOMMEND') {
            $("#imgNote").text("最多4张，首页展示1张，商品轮播3张；格式为PNG,JPG；首页尺寸为470*240px；商品轮播图尺寸为750*370px，均小于6M");
        } else {
            $("#imgNote").text("最多4张，首页展示1张，商品轮播3张；格式为PNG；首页尺寸为260*260px，背景图透明，主体四周留20px安全区；商品轮播图尺寸为750*370px，均小于6M");
        }

        if (column == 'MONITOR') {
            //展示栏目为KOL时，用户类型默认KOL，并且不可改变
            $("input[name='userType'][value='MONITOR']").attr("checked", true);
            $("input[name='userType']").attr("disabled", true);
        } else {
            $("input[name='userType']").attr("disabled", false);
        }

        $("#column").on('blur', function () {
            if ($("#column option:selected").val() == 'RECOMMEND') {
                $("#imgNote").text("最多4张，首页展示1张，商品轮播3张；格式为PNG,JPG；首页尺寸为470*240px；商品轮播图尺寸为750*370px，均小于6M");
            } else {
                $("#imgNote").text("最多4张，首页展示1张，商品轮播3张；格式为PNG；首页尺寸为260*260px，背景图透明，主体四周留20px安全区；商品轮播图尺寸为750*370px，均小于6M");
            }
            if ($("#column option:selected").val() == 'MONITOR') {
                $("input[name='userType'][value='MONITOR']").attr("checked", true);
                $("input[name='userType']").attr("disabled", true);
            } else {
                $("input[name='userType']").attr("disabled", false);
            }
        });

        if ($("input[name='userType']:checked").val() == 'MONITOR') {
            $("#ordinaryCoin").attr("disabled", true);
            $("#ordinaryCoinS").attr("disabled", true);
        }

        $("input[name='userType']").on('blur', function () {
            if ($("input[name='userType']:checked").val() == 'MONITOR') {
                $("#ordinaryCoin").attr("disabled", true);
                $("#ordinaryCoinS").attr("disabled", true);
            } else {
                $("#ordinaryCoin").attr("disabled", false);
                $("#ordinaryCoinS").attr("disabled", false);
            }
        });

        $(".deleteImg").on('click', function () {
            var index = $(this).data("pic_index");
            var defaultHtml = "<i class=\"addIcon\">首页展示</i>";
            if (index != 0) {
                defaultHtml = "<i class=\"addIcon\">商品轮播</i>"
            }
            $("#addBox" + index).find('span.imgShowBox').html(defaultHtml);
            $("#fileUp" + index).val('');
            return false;
        });

        if ($("#weekDayListSize").val() == 0) {
            $("input[name='weekDay']").each(function () {
                $(this).attr("checked", true);
            });
        }

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

        $("#saveBtn").on('click', function () {
            var monitorCoin = parseInt($("#monitorCoin").val());
            var monitorCoinS = parseInt($("#monitorCoinS").val());
            var ordinaryCoin = parseInt($("#ordinaryCoin").val());
            var ordinaryCoinS = parseInt($("#ordinaryCoinS").val());

            var coinNumReg = /^([1-9]\d{0,}|0)$/;
            if (!coinNumReg.test($("#monitorCoin").val()) || !coinNumReg.test($("#monitorCoinS").val())) {
                alert("学习币必须是大于等于0的整数");
                return;
            }

            if ($("#column option:selected").val() != 'MONITOR' && $("input[name='userType']:checked").val() != 'MONITOR') {
                if (!coinNumReg.test($("#ordinaryCoin").val()) || !coinNumReg.test($("#ordinaryCoinS").val())) {
                    alert("学习币必须是大于等于0的整数");
                    return;
                }
                if (monitorCoin < monitorCoinS) {
                    alert("班长原价必须大于等于优惠价");
                    return;
                }

                if (ordinaryCoin < ordinaryCoinS) {
                    alert("普通原价必须大于等于优惠价");
                    return;
                }
                if (monitorCoinS > ordinaryCoinS) {
                    alert("班长优惠价必须小于等于普通优惠价");
                    return;
                }

            }

            if ($("#column option:selected").val() == 'MONITOR' || $("input[name='userType']:checked").val() == 'MONITOR') {
                if (ordinaryCoin > 0 || ordinaryCoinS > 0) {
                    alert("KOL专享不用设置普通价格");
                    return;
                }
            }

            if ($("#column option:selected").val() == 'MONITOR' && $("input[name='userType']:checked").val() != 'MONITOR') {
                alert("展示栏目为KOL，用户专享也必须是KOL");
                return;
            }

            var imgStr=[];
            $(".regularBox img").each(function () {
               imgStr.push($(this).data('file_name'));
            });
            var description = ue.getContent();
            description = description.replace(/\n/g, "");
            description = description.replace(/>\s+?</g, "><");

            var startDate = "";
            var endDate = "";
            var weekDays = [];
            var recommendImg = "";
            var recommendOrder = 0;
            var recommendFlag = $('input[name="recommendFlag"]:checked').val();
            if (recommendFlag == "true") {
                startDate = $("#startDate").val();
                endDate = $("#endDate").val();
                recommendImg = $(".recommendBox img").data('file_name');
                recommendOrder = $("#recommendOrder").val();
                $("input[name='weekDay']:checked").each(function () {
                   weekDays.push($(this).val());
                });
            }
            var postData = {
                id: $("#commodityId").val(),
                name: $("#commodityName").val(),
                category: $("#category").val(),
                imgStr: imgStr.toString(),
                status: $("input[name='status']:checked").val(),
                stock: $("#stock").val(),
                purchase: $("#purchase").val(),
                dispatchPrice: $("#dispatchPrice").val(),
                userType: $("input[name='userType']:checked").val(),
                monitorCoin: $("#monitorCoin").val(),
                monitorCoinS: $("#monitorCoinS").val(),
                ordinaryCoin: $("#ordinaryCoin").val(),
                ordinaryCoinS: $("#ordinaryCoinS").val(),
                allowRepeat: $("input[name='allowRepeat']:checked").val(),
                column: $("#column").val(),
                order: $("#order").val(),
                description: description,
                recommendFlag: recommendFlag,
                startDate: startDate,
                endDate: endDate,
                weekDays: weekDays.toString(),
                recommendImg: recommendImg,
                recommendOrder: recommendOrder
            };

            if (postData.name.length > 20) {
                alert("名字长度不超过20字符");
                return;
            }

            var numberReg = /^\d{1,6}(\.\d{1,2}){0,1}$/;
            if (!numberReg.test(postData.purchase)) {
                alert("采购价0-999999,最多两位小数");
                return;
            }

            if (!numberReg.test(postData.dispatchPrice)) {
                alert("配送费0-999999,最多两位小数");
                return;
            }

            $.post("addCommodity.vpage", postData, function (data) {
                if (data.success) {
                    alert("保存商品成功");
                    window.location.href = "list.vpage";
                } else {
                    alert(data.info);
                }
            })
        });

        $(".fileUpBtn").change(function () {
            pic_index = $(this).closest('div.addBox').data("pic_index");
            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val() != '') {
                if ($.inArray(ext, ['png', 'jpg', 'jpeg']) == -1) {
                    alert("仅支持以下格式的图片【'png','jpg','jpeg'】");
                    return false;
                }

                var formData = new FormData();
                formData.append('file', $this[0].files[0]);
                var fileSize = ($this[0].files[0].size / 1024 / 1024).toFixed(4); //MB
                if (fileSize >= 6) {
                    alert("图片过大，重新选择。");
                    return false;
                }
                $.ajax({
                    url: 'uploadImg.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var img_html = '<img src="' + data.url + '" data-file_name="' + data.fileName + '">';
                            $("#addBox" + pic_index).find('span.imgShowBox').html(img_html);
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        });
    });

</script>

</@layout_default.page>