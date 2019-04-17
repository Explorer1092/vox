<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='商品详情页' page_num=9>

<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<div class="span9">
    <legend>
        <strong>商品详情页</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="publishForm" class="well form-horizontal" method="post" action="save.vpage">
                <input type="hidden" value="${id!''}" name="id"/>
                <fieldset>
                    <legend>商品详情页配置</legend>
                    <div class="control-group">
                        <label class="control-label">产品类型：（必选）</label>
                        <div class="controls">
                            <select id="productType" name="productType" class="option" data-value="${productType!''}">
                                    <#list productTypes as type>
                                        <option value="${type.name()!}">${type.name()!}</option>
                                    </#list>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">产品：（可选）</label>
                        <div class="controls">
                            <select id="productId" name="productId" class="option" data-value="${productId!''}">

                            </select>
                        </div>
                    </div>

                    <legend>基本信息</legend>

                    <div class="control-group">
                        <label class="control-label">商品名称（必填）</label>
                        <div class="controls">
                            <input type="text" name="productName" id="productName" value="${productName!''}"/>
                        </div>
                    </div>


                    <div class="control-group">
                        <label class="control-label">显示类别：（必选）</label>
                        <div class="controls">
                            <select id="viewType" name="viewType" class="option" data-value="${viewType!'1'}">
                                <option value="1">头图</option>
                                <option value="2">ICON</option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">学科：（必填）</label>
                        <div class="controls">
                            <input type="text" name="subject" id="subject" value="${subject!''}"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">副标题：（必填）</label>
                        <div class="controls">
                            <input type="text" name="subhead" id="subhead" value="${subhead!''}"/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">购买用户：（必选）</label>
                        <div class="controls">
                            <select id="userType" name="userType" class="option" data-value="${userType!''}">
                                    <option value="STUDENT">孩子</option>
                                    <option value="PARENT">家长</option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">售卖方式：（必选）</label>
                        <div class="controls">
                            <select id="salesType" name="salesType" class="option" data-value="${salesType!''}">
                                <option value="TIME_BASED">时间</option>
                                <option value="ITEM_BASED">项目</option>
                            </select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">ICON地址：（必填）</label>
                        <div class="controls">
                            <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                            <input class="fileUpBtn" id="iconUrlFile" data-name="icon" type="file"
                                   accept="image/gif, image/jpeg, image/png, image/jpg"
                                   style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                            <input type="hidden" name="iconUrl" value="${iconUrl!''}" id="iconUrl"/>
                            <img id="iconUrlIMG" width="250" src="${iconUrl!}" data-file_name="">
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="headImage">上传头图：<br/>（头图模式必填）</label>
                        <div class="control-group">
                            <div class="controls">
                                <i class="addIcon">上传图片（小于3M，无尺寸限制）</i>
                                <input class="fileUpBtn" id="headImageFile" data-name="head" type="file"
                                       accept="image/gif, image/jpeg, image/png, image/jpg"
                                       style="width: 100%; display:block; height: 20px; top:-20px; position: relative; opacity: 0;">
                                <input type="hidden" name="headImage" value="${headImage!''}" id="headImage"/>
                                <img id="headImageIMG" width="250" src="${headImage!}" data-file_name="">
                            </div>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">目标用户：</label>
                        <#assign keys=grades?keys/>
                        <div class="controls">
                            <select id="startClazzLevel" name="startClazzLevel" data-value="${startClazzLevel!'1'}" class="option" style="width: 60px;">
                                <#list keys as key>
                                    <option value="${key}">${grades[key]!''}</option>
                                </#list>
                            </select> 年级
                            &nbsp;&nbsp;--&nbsp;&nbsp;
                            <select id="endClazzLevel" name="endClazzLevel" data-value="${endClazzLevel!'6'}" class="option" style="width: 60px;">
                                <#list keys as key>
                                    <option value="${key}">${grades[key]!''}</option>
                                </#list>
                            </select> 年级
                        </div>
                    </div>

                    <legend data-type="package">下属单品</legend>
                    <div class="control-group">
                        <table class="table table-bordered">
                            <thead>
                            <tr>
                                <th>类别</th>
                                <th>商品</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody  id="packageId" data-wrapper="packageContainer">
                            <#if subProducts?exists>
                            <#list subProducts as item>
                            <tr data-id="${item.itemId!''}" data-value="${item.productId!''}">
                                <td>${item.productType!''}</td>
                                <td>${item.productName!''}</td>
                                <td>
                                    <button data-id="${item.itemId!''}" class="btn btn-default btn-small">删除</button>
                                </td>
                            </tr>
                            </#list>
                            </#if>
                            </tbody>
                        </table>
                    </div>
                    <div class="controls">
                        <input type="hidden" name="packageProductIds" value=""/>
                        <input type="button" id="packageChoice" value="设置"
                               class="btn btn-small btn-info">
                    </div>

                    <legend data-type="recommend">推荐配置</legend>
                    <div class="control-group">
                        <table class="table table-bordered" >
                            <thead>
                            <tr>
                                <th>类别</th>
                                <th>商品</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody id="recommendId" data-wrapper="recommendContainer">
                            <#if recommendProducts?exists>
                            <#list recommendProducts as item>
                            <tr data-id="${item.itemId!''}" data-value="${item.productId!''}">
                                <td>${item.productType!''}</td>
                                <td>${item.productName!''}</td>
                                <td>
                                    <button data-id="${item.itemId!''}" class="btn btn-default btn-small">删除</button>
                                </td>
                            </tr>
                            </#list>
                            </#if>
                            </tbody>
                        </table>
                    </div>
                    <div class="controls">
                        <input type="hidden" name="recommendProductIds" value=""/>
                        <input type="button" id="recommendChoice" value="设置" class="btn btn-small btn-info">
                    </div>

                    <legend>商品详情</legend>
                    <div class="control-group">
                        <label class="control-label">商品详情（必填）：</label>
                        <div class="controls">
                            <textarea style="display: none;" class="form-control span8"
                                      placeholder="商品详情"
                                      name="intro" rows="3">${intro!''}</textarea>

                            <script id="container" name="container" type="text/plain"></script>
                        </div>
                    </div>

                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="saveBtn" value="保存" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

<!-- 模态框（Modal） -->
<div class="modal fade" id="recommendProductModal" tabindex="-1" role="dialog"
     aria-labelledby="choiceProductModal"
     aria-hidden="true" style="width: 800px;">
    <div class="modal-dialog">
        <div class=" modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="optionShardModalLabel">
                    推荐商品
                </h4>
            </div>
            <div class="modal-body">
                <div class="control-group">
                    <div class="controls">
                        类型：
                        <select id="recommendProductType" name="recommendProductType" class="option">
                            <#list productTypes as type>
                                <option value="${type.name()!}">${type.name()!}</option>
                            </#list>
                        </select>
                        商品：
                        <select id="recommendProductId" name="recommendProductId" class="option">
                        </select>
                        <button id="recommendSelect" class="btn btn-small btn-primary">选择</button>
                    </div>
                </div>
                <table class="table table-bordered">
                    <thead>
                    <tr>
                        <th>类别</th>
                        <th>商品</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody data-wrapper="recommendContainer">
                    <#if recommendProducts?exists>
                            <#list recommendProducts as item>
                            <tr data-id="${item.itemId!''}" data-value="${item.productId!''}">
                                <td>${item.productType!''}</td>
                                <td>${item.productName!''}</td>
                                <td>
                                    <button data-id="${item.itemId!''}" class="btn btn-default btn-small">删除</button>
                                </td>
                            </tr>
                            </#list>
                    </#if>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>


<!-- 模态框（Modal） -->
<div class="modal fade" id="packageProductModal" tabindex="-1" role="dialog"
     aria-labelledby="choiceProductModal"
     aria-hidden="true" style="width: 800px;">
    <div class="modal-dialog">
        <div class=" modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title">
                    子商品
                </h4>
            </div>
            <div class="modal-body">
                <div class="control-group">
                    <div class="controls">
                        类型：
                        <select id="packageProductType" name="packageProductType" class="option">
                            <#list productTypes as type>
                                <option value="${type.name()!}">${type.name()!}</option>
                            </#list>
                        </select>
                        商品：
                        <select id="packageProductId" name="packageProductId" class="option">
                        </select>
                        <button id="packageSelect" class="btn btn-small btn-primary">选择</button>
                    </div>
                </div>
                <table class="table table-bordered">
                    <thead>
                    <tr>
                        <th>类别</th>
                        <th>商品</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody data-wrapper="packageContainer">
                    <#if subProducts?exists>
                            <#list subProducts as item>
                            <tr data-id="${item.itemId!''}" data-value="${item.productId!''}">
                                <td>${item.productType!''}</td>
                                <td>${item.productName!''}</td>
                                <td>
                                    <button data-id="${item.itemId!''}" class="btn btn-default btn-small">删除</button>
                                </td>
                            </tr>
                            </#list>
                    </#if>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

    <script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>
    <script language="javascript" type="application/javascript">

        var choiceType = "";

        // 加载商品
        var loadProduct = function (current, container, value) {
            var productType = current.val();
            var productUrl = "productlist.vpage?productType=" + productType;
            $.get(productUrl, function (data, status) {
                var products = data.products;
                var length = products.length;
                container.html("");
                if (length == 0) {
                    return;
                }
                for (var index = 0; index < length; index++) {
                    var current = products[index];
                    container.append($("<option>").val(current.id).text(current.name));
                }
                if(value != undefined && value != ''){
                    container.val(value);
                }
            });
        }

        // 图片校验
        var uploadValidate = function (object, target, targetText, width, height) {
            var file = object.prop('files')[0];
            var image = new Image();
            image.onload = function () {
                if (width != -1 && height != -1) {
                    if (image.width != width || image.height != height) {
                        alert("图片尺寸不正确");
                        return;
                    }
                }
                upload(object, target, targetText);
            }
            var _URL = window.URL || window.webkitURL;
            image.src = _URL.createObjectURL(file);
        };

        var loadDefaultValue = function (object) {
            if(object.attr("data-value") != '' ){
                object.val(object.attr("data-value"));
            }
        }

        // 上传图片
        var upload = function (object, targetVal, targetTxt) {
            if (object.val() != '') {

                var formData = new FormData();
                formData.append('inputFile', object[0].files[0]);
                $.ajax({
                    url: 'upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            var path = data.path;
                            targetTxt.attr('src', path);
                            targetVal.val(path);
                            alert("上传成功");
                        } else {
                            alert("上传失败");
                        }
                    }
                });
            }
        };

        var packageProduct = function (exists) {
            if (exists) {
                $("[data-type='package']").show();
            } else {
                $("#singles-wrapper").html("");
                $("[data-type='package']").hide();
            }
        };

        $(document).ready(function () {
            $("#productType").change(function () {
                loadProduct($("#productType"), $("#productId"));
            });

            $("#recommendProductType").change(function () {
                loadProduct($("#recommendProductType"), $("#recommendProductId"));
            });

            $("#packageProductType").change(function () {
                loadProduct($("#packageProductType"), $("#packageProductId"));
            });


            loadDefaultValue($("#salesType"));
            loadDefaultValue($("#userType"));


            var type = $("#productType").attr("data-value");
            if(type != undefined && type != ''){
                $("#productType").val(type);
            }

            loadProduct($("#productType"), $("#productId"), $("#productId").attr("data-value"));
            loadProduct($("#recommendProductType"), $("#recommendProductId"), "");
            loadProduct($("#packageProductType"), $("#packageProductId"), "");

            $("#headImageFile").change(function () {
                uploadValidate($(this), $("#headImage"), $("#headImageIMG"), -1, -1);
            });

            $("#iconUrlFile").change(function () {
                uploadValidate($(this), $("#iconUrl"), $("#iconUrlIMG"), -1, -1);
            });

            var ue = UE.getEditor('container', {
                serverUrl: "/opmanager/lordaeron/ueditorcontroller.vpage",
                zIndex: 1040,
                fontsize: [16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
                toolbars: [[
                    'fullscreen', 'source', '|', 'undo', 'redo', '|',
                    'bold', 'italic', 'underline', 'fontborder', 'strikethrough', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'formatmatch', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                    'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                    'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                    'directionalityltr', 'directionalityrtl', 'indent', '|',
                    'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                    'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|', 'insertvideo',
                    'simpleupload', 'pagebreak', 'template', 'background', '|',
                    '|', 'preview'
                ]]
            });

            ue.ready(function () {
                ue.setContent($("[name='intro']").val());
            });

            $("#endGrade").val("6");

            $("#recommendChoice").click(function () {
                $("#recommendProductModal").modal("show");
            });

            $("#recommendSelect").click(function () {
                var id = $("#recommendProductType").val() + "@" + $("#recommendProductId").val();
                var str =
                        "                   <tr data-id=\"" + id + "\" data-value=\""+$("#recommendProductId").val() +"\">\n" +
                        "                        <td>\n" + $("#recommendProductType").val() + "</td>\n" +
                        "                        <td>\n" + $("#recommendProductId").find("option:selected").text() + "</td>\n" +
                        "                        <td>\n" +
                        "                            <button data-id=\"" + id + "\" class=\"btn btn-default btn-small\">删除</button>\n" +
                        "                        </td>\n" +
                        "                    </tr>";

                $("[data-wrapper='recommendContainer']").append(str).find("button").click(function () {
                    var attr = $(this).attr("data-id");
                    $("tr[data-id='" + attr + "']").remove();
                });
            });

            $("#packageChoice").click(function () {
                $("#packageProductModal").modal("show");
            });

            $("#packageSelect").click(function () {
                var id = $("#packageProductType").val() + "@" + $("#packageProductId").val();
                var str =
                        "                   <tr data-id=\"" + id + "\" data-value=\"" + $("#packageProductId").val() + "\">\n" +
                        "                        <td>\n" + $("#packageProductType").val() + "</td>\n" +
                        "                        <td>\n" + $("#packageProductId").find("option:selected").text() + "</td>\n" +
                        "                        <td>\n" +
                        "                            <button data-id=\"" + id + "\" class=\"btn btn-default btn-small\">删除</button>\n" +
                        "                        </td>\n" +
                        "                    </tr>";

                $("[data-wrapper='packageContainer']").append(str).find("button").click(function () {
                    var attr = $(this).attr("data-id");
                    $("tr[data-id='" + attr + "']").remove();
                });
            });

            $("#startClazzLevel").val($("#startClazzLevel").attr("data-value"));
            $("#endClazzLevel").val($("#endClazzLevel").attr("data-value"));

            $("#viewType").val($("#viewType").attr("data-value"));

            $("[data-wrapper='packageContainer']").find("button").click(function () {
                var attr = $(this).attr("data-id");
                $("tr[data-id='" + attr + "']").remove();
            });
            $("[data-wrapper='recommendContainer']").find("button").click(function () {
                var attr = $(this).attr("data-id");
                $("tr[data-id='" + attr + "']").remove();
            });

            $("#saveBtn").click(function () {
                $("[name='intro']").val(ue.getContent());
                var trs = $("#packageId tr");
                var length = trs.length;
                var str = "";
                var value;
                var i;
                for (i = 0; i < length; i++){
                    value = trs.eq(i).attr("data-id");
                    str +=  "," + value;
                }
                if(str != ""){
                    str = str.substring(1);
                }
                $("[name='packageProductIds']").val(str);

                str = "";
                trs = $("#recommendId tr");
                length = trs.length;
                for (i = 0; i < length; i++){
                    value = trs.eq(i).attr("data-id");
                    str +=  "," + value;
                }
                if(str != ""){
                    str = str.substring(1);
                }
                $("[name='recommendProductIds']").val(str);

                $(":hidden[name='packageId']").val();
                $("#publishForm").ajaxSubmit({
                    url:"/opmanager/lordaeron/save.vpage",
                    type:"POST",
                    dateType:"json",
                    beforeSubmit:function(){
                        if($("#subject").val() === ""){
                            alert("请输入学科");
                            return false;
                        }

                        if($("#subhead").val() === ""){
                            alert("请输入副标题");
                            return false;
                        }

                        if($("#iconUrl").val() === ""){
                            alert("请上传ICON");
                            return false;
                        }

                        if($("#viewType").val() === "1" && $("#headImage").val() === ""){
                            alert("请上传头图");
                            return false;
                        }

                        if($("[name='intro']").val() === ""){
                            alert("请输入商品详情");
                            return false;
                        }

                        return true;
                    },
                    success:function (data) {
                        if(data.success){
                            document.location.href="/opmanager/lordaeron/list.vpage";
                        }else {
                            alert("服务器异常");
                        }
                    },
                    clearForm:false,
                    resetForm:false
                });
            });


        });
    </script>
</@layout_default.page>