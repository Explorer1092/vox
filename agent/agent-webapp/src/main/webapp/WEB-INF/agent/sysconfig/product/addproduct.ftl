<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加商品' page_num=6>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<style type="text/css">
    img{vertical-align: top;}
    .deleteBtn{margin-left: 15px;cursor: pointer;}
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 添加/编辑商品</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <#if error??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>出错啦！ ${error!}</strong>
            </div>
        </#if>
        <div class="box-content">
            <form id="add_product_form" class="form-horizontal" method="post" action="addproduct.vpage" enctype="multipart/form-data" >
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">名称</label>
                        <div class="controls">
                            <input maxlength="20" id="productName" name="productName" class="input-xlarge focused postDate" type="text"
                                   value="<#if product??>${product.productName!}</#if>"
                                <#if !requestContext.getCurrentUser().isCountryManager() && !requestContext.getCurrentUser().isAdmin()>readonly="true" </#if>
                                    >
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">描述</label>
                        <div class="controls">
                            <textarea id="productDesc" name="productDesc" style="width: 270px;resize:none" rows="5"
                                      class="postDate"
                                      <#if !requestContext.getCurrentUser().isCountryManager() && !requestContext.getCurrentUser().isAdmin()>readonly="true" </#if>
                                     maxlength="180"><#if product??>${product.productDesc!}</#if></textarea>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="focusedInput">图片1</label>
                        <div class="controls">
                            <div id="pp1">
                            <#if product??&&product.productImg1??&&product.productImg1!=''>
                                <img src="${product.productImg1!}"
                                                   width="80px" height="80px"/><span class="deleteBtn">删除</span></#if>
                            </div>
                            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                                <input id="pf1" type="file"></#if>
                            <input name="img1" type="hidden" class="postDate"
                                   value="<#if product??&&product.productImg1??>${product.productImg1!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">图片2</label>
                        <div class="controls">
                            <div id="pp2">
                            <#if product??&&product.productImg2??&&product.productImg2!=''>
                                <img src="${product.productImg2!}"
                                                   width="80px" height="80px"/><span class="deleteBtn">删除</span></#if>
                            </div>
                            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                                <input id="pf2" type="file"></#if>
                            <input name="img2" type="hidden" class="postDate"
                                   value="<#if product??&&product.productImg2??>${product.productImg2!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">图片3</label>
                        <div class="controls">
                            <div id="pp3">
                            <#if product??&&product.productImg3??&&product.productImg3!=''>
                                <img src="${product.productImg3!}"
                                                   width="80px" height="80px"/><span class="deleteBtn">删除</span></#if>
                            </div>
                            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                                <input id="pf3" type="file"></#if>
                            <input name="img3" type="hidden" class="postDate"
                                   value="<#if product??&&product.productImg3??>${product.productImg3!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">图片4</label>
                        <div class="controls">
                            <div id="pp4">
                            <#if product??&&product.productImg4??&&product.productImg4!=''>
                                <img src="${product.productImg4!}"
                                                   width="80px" height="80px"/><span class="deleteBtn">删除</span></#if>
                            </div>
                            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                                <input id="pf4" type="file"></#if>
                            <input name="img4" type="hidden" class="postDate"
                                   value="<#if product??&&product.productImg4??>${product.productImg4!}</#if>">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">价格</label>
                        <div class="controls">
                            <input id="price" name="price" class="input-xlarge focused postDate" type="text"
                                   value="<#if product??>${product.price?string("##0.##")}</#if>"
                                   <#if !requestContext.getCurrentUser().isCountryManager() && !requestContext.getCurrentUser().isAdmin()>readonly="true" </#if>
                                    >
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="focusedInput">库存</label>
                        <div class="controls">
                            <input id="inventoryQuantity" maxlength="10" name="inventoryQuantity" class="input-xlarge focused "
                                   type="text"
                                   value="<#if product?? && product.inventoryQuantity??>${product.inventoryQuantity?string(",##0")}</#if>"
                                   readonly="true"
                            />
                            <select id="inventoryOpt" name="inventoryOpt" class="postDate">
                                <option value="1" >增加库存</option>
                                <option value="-1" >减少库存</option>
                            </select>
                            <input id="quantity" name="quantity" class="input-xlarge focused postDate" type="text"
                                   value=""/>
                            变更原因：
                            <input id="quantityChangeDesc" name="quantityChangeDesc"
                                   class="input-xlarge focused postDate" type="text" value="" maxlength="100"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">可见业务</label>
                        <div class="controls">
                            <input type="checkbox" class="primarySchoolVisible" <#if product?? && product.primarySchoolVisible?? && product.primarySchoolVisible==true>checked</#if>><span>小学可见</span>
                            <input type="checkbox" class="juniorSchoolVisible" <#if product?? && product.juniorSchoolVisible?? && product.juniorSchoolVisible==true>checked</#if>><span>中学可见</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">可见角色</label>
                        <div class="controls">
                            <input type="checkbox" value="8" id="businessDeveloper" name="businessDeveloper" /><span>专员</span>
                            <input type="checkbox" value="4" id="cityManager" name="cityManager"/><span>市经理</span>
                            <input type="checkbox" value="2" id="countryMannager" name="countryMannager"/><span>全国总监</span>
                        </div>
                    </div>
                    <input type="hidden" name="roleVisibleAuthority" value="<#if product??>${product.roleVisibleAuthority!}</#if>" id="roleVisibleAuthority">
                    <input type="hidden" name="id" value="<#if product??>${product.id!}</#if>" id="id">
                    <div class="form-actions">
                        <button id="add_product_btn" type="button" class="btn btn-primary">保存</button>
                        <a class="btn" href="index.vpage"> 取消 </a>
                    </div>
                </fieldset>
            </form>
        </div>
    </div><!--/span-->
</div>
<input type="hidden" id="productId" name="productId" class="postDate" value="${productId!}">
<script type="text/javascript">
    $(function() {
        roleVisibleAuthority();
        $('#add_product_btn').live('click', function () {
            var post = {};
                $(".postDate").each(function (index, element) {
                    var name = $(element).attr("name");
                    post[name] = $(element).val();
                });
                post.primarySchoolVisible = $('.primarySchoolVisible').prop('checked');
                post.juniorSchoolVisible = $('.juniorSchoolVisible').prop('checked');
                post.roleVisibleAuthority = 0;
                if ($('#countryMannager').prop("checked")) {
                    post.roleVisibleAuthority = post.roleVisibleAuthority | $('#countryMannager').val();
                }
                if ($('#cityManager').prop("checked")) {
                    post.roleVisibleAuthority = post.roleVisibleAuthority | $('#cityManager').val();
                }
                if ($('#businessDeveloper').prop("checked")) {
                    post.roleVisibleAuthority = post.roleVisibleAuthority | $('#businessDeveloper').val();
                }
                if (!checkAddProduct(post.productName, post.price, post.quantity, post.quantityChangeDesc)) {
                    return false;
                }
                if(!post.primarySchoolVisible&&!post.juniorSchoolVisible){
                    layer.alert("请选择中小学是否可见!");
                    return false;
                }
                $.post("/sysconfig/product/addproduct.vpage", post, function (res) {
                    if (res.success) {
                        history.back();
                    } else {
                        layer.alert(res.info);
                    }
                });
            });

            upLoadProductPicture("pf1", "pp1", "img1");
            upLoadProductPicture("pf2", "pp2", "img2");
            upLoadProductPicture("pf3", "pp3", "img3");
            upLoadProductPicture("pf4", "pp4", "img4");

            $('body,html').on('click','.deleteBtn',function () {
                var $this = $(this);
                layer.confirm("确定删除此商品图片?",{
                    btn: ['确认','取消'] //按钮
                },function () {
                    layer.close(layer.index);
                    $this.parent().empty().next('.uploader')
                            .find('input').val('').end()
                            .find('.filename').html('No file selected').end().end()
                            .siblings('.postDate').val('');
                });
            });
        });

        function roleVisibleAuthority() {
            var roleVisibleAuthority = $('#roleVisibleAuthority').val();
            if ((roleVisibleAuthority & 2) > 0) {
                $("input[type=checkbox][name='countryMannager']").prop('checked', "checked").parent('span').addClass('checked');
            }
            if ((roleVisibleAuthority & 4) > 0) {
                $("input[type=checkbox][name='cityManager']").prop('checked', "checked").parent('span').addClass('checked');
            }
            if ((roleVisibleAuthority & 8) > 0) {
                $("input[type=checkbox][name='businessDeveloper']").prop('checked', true).parent('span').addClass('checked');
            }

        }
        function checkAddProduct(productName, price, quantity, quantityChangeDesc) {
            if (productName === '') {
                layer.alert("请输入名称!");
                return false;
            }
            if (price === '' || !$.isNumeric(price)) {
                layer.alert("价格必须输入且必须为数字类型!");
                return false;
            }

            if (quantity !== "" && quantityChangeDesc === "") {
                layer.alert("请填写库存变更原因！");
                return false;
            }
            return true;
        }

        function upLoadProductPicture(id, show, name) {
            $(document).on("change", "#" + id, function () {

                if ($("#" + id).val() != '') {
                    var formData = new FormData();
                    var file = $('#' + id)[0].files[0];
                    formData.append('file', file);
                    formData.append('file_size', file.size);
                    formData.append('file_type', file.type);
                    $.ajax({
                        url: '/file/upload.vpage',
                        type: 'POST',
                        data: formData,
                        processData: false,
                        contentType: false,
                        success: function (data) {
                            if (data.success) {
                                $("#" + show).html("");
                                $("#" + show).append("<img src='" + data.fileUrl + "?x-oss-process=image/resize,w_100,h_100/auto-orient,1'/><span class='deleteBtn'>删除</span>");
                                $("input[name='" + name + "']:first").val(data.fileUrl);
                                layer.alert("上传成功");
                            } else {
                                layer.alert("上传失败");
                            }
                        }
                    });
                }
            });
        }
</script>

</@layout_default.page>