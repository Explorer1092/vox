<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='编辑班级' page_num=26>

<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js" xmlns="http://www.w3.org/1999/html"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        更换产品&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <#--<form id="info_frm" name="info_frm" action="save.vpage" method="post">-->
                <div id="box">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">班名：</span></label>
                            <div class="controls">
                                <input type="hidden" id="clazzId" name="clazzId" v-bind:value="clazz.clazzId">
                                <input type="text" readonly="readonly" class="form-control input_txt"
                                       v-bind:value="clazz.clazzName"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">原产品：</span></label>
                            <div class="controls">
                                <input type="hidden" name="originProductId" v-bind:value="clazz.productId">
                                <input type="text" readonly="readonly"  class="form-control input_txt"
                                       v-bind:value="clazz.productName"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">目标产品：</label>
                            <div class="controls">
                                <select id="productId" v-model="productId" >
                                    <option value="">---请选择--</option>
                                    <option v-for="(item, index) in productList" :key="item.id" :value="item.value">{{ item.desc }}</option>
                                </select>
                                <template  v-for="(e, i) in productTypeList">
                                    <input v-if="e.selected" v-on:change="queryProduct" class="productCheckBox" name="productType" type="checkbox" checked v-bind:value="e.value">{{e.desc}}
                                    <input v-else v-on:change="queryProduct"  class="productCheckBox" name="productType" type="checkbox" v-bind:value="e.value">{{e.desc}}
                                </template>
                                <#--<input id="productId" type="hidden" name="productId" value="${clazz.productId!}">-->
                                <#--<select data-init='false'-->
                                        <#--class="multiple district_select" name="productId" id="productId">-->
                                    <#--<option value="">----请选择----</option>-->
                                    <#--<#if productOptionList?size gt 0>-->
                                        <#--<#list productOptionList as e >-->
                                            <#--<option value="${e.value!}"-->
                                                    <#--<#if e.selected>selected</#if>>${e.desc!}</option>-->
                                        <#--</#list>-->
                                    <#--</#if>-->
                                <#--</select>-->
                            </div>
                        </div>
                    </div>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a title="保存" href="javascript:void(0);" class="btn btn-primary" v-on:click="save">
                        <i class="icon-pencil icon-white"></i> 确定(OK)
                    </a>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a title="返回" href="javascript:window.history.back();" class="btn">
                        <i class="icon-share-alt"></i> 取消(Cancel)
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    var vm = new Vue({
        el: '#box',
        model: false,
        data: {
            clazzId:'',
            clazz:"",
//            originProductId:"",
            productId:"",
            productList:[],
            productTypeList:[]
        },
        methods: {
            queryProduct:function () {
                var _this = this;
                var productTypeArr = [];
                var ind = 0;
                $(".productCheckBox").each(function () {
                    if($(this).is(':checked')){
                        productTypeArr[ind] = $(this).val();
                        ind = ind + 1;
                    }
                    console.log($(this).is(':checked') + "-" + $(this).val())
                })
                if(productTypeArr.length == 0){
                    productTypeArr = [2,3]
                }
                console.log("productTypeArr",productTypeArr)
                console.log(productTypeArr.join(","))
                $.ajax({
                    url: "/chips/chips/clazz/productList.vpage",
                    type: "GET",
                    data: {
                        "productType": productTypeArr.join(",")
                    },
                    success: function (res) {
                        if (res.success) {
                            console.log(res.productList.length)
                            _this.productList = res.productList;
                            _this.productId = '';
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("获取产品失败");
                    }
                });
            },
            save:function () {
                var _this = this;
                var originProductId =  _this.clazz.productId;
                var clazzId =    _this.clazz.clazzId;
                var productId = _this.productId;
                console.log("originProductId", originProductId)
                console.log("clazzId", clazzId)
                console.log("productId", productId)
                if(productId === originProductId){
                    alert("要更换的产品不能和原产品相同")
                    return;
                }
                $.ajax({
                    url: "/chips/chips/clazz/updateChangeProduct.vpage",
                    type: "POST",
                    data: {
                        "originProductId":originProductId,
                        "clazzId":clazzId,
                        "productId":productId,
                    },
                    success: function (res) {
                        if (res.success) {
                           alert("更换产品成功")
                            window.history.back();
                            window.location.reload();
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("保存失败");
                    }
                });
            }

        },
        created: function () {
            var _this = this;
            $.get('${requestContext.webAppContextPath}/chips/chips/clazz/changeQuery.vpage', {
                clazzId:getUrlParam("clazzId"),
            }, function (res) {
                if(res.success) {
                    _this.clazz = res.pojo;
                    _this.clazzId = res.pojo.clazzId;
                    _this.productList = res.toProductList;
                    _this.productTypeList = res.productTypeList;
                }
            });
        }
    });

    function getUrlParam (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]); return null;
    }
</script>

</@layout_default.page>