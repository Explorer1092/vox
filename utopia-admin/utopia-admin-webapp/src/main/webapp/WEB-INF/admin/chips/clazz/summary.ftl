<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='班级总览' page_num=26>

<style>
    .form-group {
        margin: 5px 0;
        display: inline-block;
    }

    .form-group .mylabel {
        width: 150px;
        text-align: right;
    }

    .table-responsive {
        min-height: .01%;
        overflow-x: auto
    }

    @media screen and (max-width: 767px) {
        .table-responsive {
            width: 100%;
            margin-bottom: 15px;
            overflow-y: hidden;
            -ms-overflow-style: -ms-autohiding-scrollbar;
            border: 1px solid #ddd
        }

        .table-responsive > .table {
            margin-bottom: 0
        }

        .table-responsive > .table > tbody > tr > td, .table-responsive > .table > tbody > tr > th, .table-responsive > .table > tfoot > tr > td, .table-responsive > .table > tfoot > tr > th, .table-responsive > .table > thead > tr > td, .table-responsive > .table > thead > tr > th {
            white-space: nowrap
        }

        .table-responsive > .table-bordered {
            border: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:first-child, .table-responsive > .table-bordered > tbody > tr > th:first-child, .table-responsive > .table-bordered > tfoot > tr > td:first-child, .table-responsive > .table-bordered > tfoot > tr > th:first-child, .table-responsive > .table-bordered > thead > tr > td:first-child, .table-responsive > .table-bordered > thead > tr > th:first-child {
            border-left: 0
        }

        .table-responsive > .table-bordered > tbody > tr > td:last-child, .table-responsive > .table-bordered > tbody > tr > th:last-child, .table-responsive > .table-bordered > tfoot > tr > td:last-child, .table-responsive > .table-bordered > tfoot > tr > th:last-child, .table-responsive > .table-bordered > thead > tr > td:last-child, .table-responsive > .table-bordered > thead > tr > th:last-child {
            border-right: 0
        }

        .table-responsive > .table-bordered > tbody > tr:last-child > td, .table-responsive > .table-bordered > tbody > tr:last-child > th, .table-responsive > .table-bordered > tfoot > tr:last-child > td, .table-responsive > .table-bordered > tfoot > tr:last-child > th {
            border-bottom: 0
        }
    }

    .table_box {
        max-height: 700px;
    }

    .table_box table tr td {
        white-space: nowrap;
    }

    .table_box table tbody tr td {
        white-space: nowrap;
    }
</style>

<div id="main_container" class="span9">
    <legend>班级总览
        <button type="button" id="create" class="btn btn-primary pull-right">新建(Create)</button>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form form-inline form-horizontal" action="/chips/chips/clazz/summary/list.vpage">
                    <div class="form-group">
                        <label for="" class="mylabel">班名(Class)：</label>
                        <input type="text" name="clazzName" class="form-control" placeholder="模糊搜索"
                               value=${clazzName!""}>
                    </div>
                    <div class="form-group">
                        <label for="" class="mylabel">班主任(Teacher)：</label>
                        <select id="clazzTeacher" data-init='false' name="clazzTeacher"
                                class="multiple district_select form-control">
                            <option value="">----请选择----</option>
                            <#if teacherOptionList?size gt 0>
                                <#list teacherOptionList as e >
                                    <option value="${e.value!}" <#if e.selected>selected</#if>>${e.desc!}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="" class="mylabel">产品(Product)：</label>

                        <#list productTypeList as e >
                            <input class="productCheckBox" name="productType" type="checkbox" <#if e.selected>checked</#if> value="${e.value!}">${e.desc!}
                        </#list>
                        <select id="product" data-init='false' name="product" class="multiple district_select">
                            <option value="">----请选择----</option>
                            <#if productOptionList?size gt 0>
                                <#list productOptionList as e >
                                    <option value="${e.value!}" <#if e.selected>selected</#if>>${e.desc!}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="filter" class="btn btn-info">筛选(Filter)</button>
                    </div>
                    <div class="form-group" style="text-align: center;width: 150px;">
                        <button type="button" id="export" class="btn btn-info">导出邮寄地址</button>
                    </div>
                </form>

            </div>
        </div>
    </div>
    <div id="data_table_journal">
        <div class="table-responsive table_box">
            <table class="table table-striped table-bordered">
                <tr>
                    <td style="text-align:center" colspan="8">班级列表</td>
                </tr>
                <tr>
                    <td>班名</br>Class</td>
                    <td>班主任</br>Teacher</td>
                    <td>用户上限</br>Limitation</td>
                    <td>产品</br>Product</td>
                    <td>课程</br>Book</td>
                    <td>类型<br />Type</td>
                    <td>建立时间</br>Built-Time</td>
                    <td>用户数</br>Count</td>
                    <td>操作</br>Operation</td>
                </tr>
                <#if clazzPojoList?? && clazzPojoList?size gt 0>
                    <#list clazzPojoList as e >
                        <tr>
                            <td style="width:150px;">${e.clazzName!}</td>
                            <td>${e.clazzTeacherName!}</td>
                            <td>${e.userLimitation!}</td>
                            <td style="width:150px;">${e.productName!}</td>
                            <td style="width:200px;">${e.bookName!}</td>
                            <td>${e.typeDesc!}</td>
                            <td>${e.createTime!}</td>
                            <td>${e.userCount!}</td>
                            <td>
                                <button type="button" class="btn btn-primary"
                                        onclick="enterClick(${e.clazzId!},'${e.productId!}')">进入
                                </button>
                                <button type="button" class="btn btn-primary" onclick="modifyClick(${e.clazzId!})">
                                    编辑
                                </button>
                                <button type="button" class="btn btn-primary"
                                        onclick="combineClick(${e.clazzId!},'${e.productId!}')">合并
                                </button>
                                <button type="button" class="btn btn-primary"
                                        onclick="enterChange(${e.clazzId!},'${e.productId!}')">更换产品
                                </button>
                            </td>
                        </tr>
                    </#list>
                <#else >
                    <tr>
                        <td colspan="13"><strong>暂无数据</strong></td>
                    </tr>
                </#if>
            </table>
        </div>
    </div>

    <script type="text/javascript">
        function enterClick(clazzId, productId) {
            window.location.href = "/chips/chips/clazz/manager/basicInfo.vpage?clazzId=" + clazzId + "&productId=" + productId;
        };
        function enterChange(clazzId, productId) {
            window.location.href = "/chips/chips/clazz/changeIndex.vpage?clazzId=" + clazzId;
        };

        function modifyClick(clazzId) {
            window.location.href = "/chips/chips/clazz/modify.vpage?clazzId=" + clazzId;
        };

        function combineClick(clazzId, productId) {
            window.location.href = "/chips/chips/clazz/combine.vpage?clazzId=" + clazzId + "&productId=" + productId;
        };

        $(function () {
            $("#create").on('click', function () {
                window.location.href = "/chips/chips/clazz/create.vpage";
            });

            $("#filter").on('click', function () {
                $("#frm").attr('action', "/chips/chips/clazz/summary/list.vpage");
                $("#frm").submit();
            });
            $("#export").on('click', function () {
                var productId = $("#product").val();
                if(productId == ""){
                    alert("请选择产品");
                    return;
                }
                $("#frm").attr('action', "/chips/chips/clazz/addrExport.vpage");
                $("#frm").submit();
            });
            $(".productCheckBox").on('change', function () {
                var productTypeArr = [];
                var ind = 0;
                $(".productCheckBox").each(function () {
                    if($(this).is(':checked')){
                        productTypeArr[ind] = $(this).val();
                        ind = ind + 1;
                    }
                    console.log($(this).is(':checked') + "-" + $(this).val())
                })
                console.log(productTypeArr)
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
                            var productObj = document.getElementById("product");
                            productObj.options.length=0
                            var option = new Option("----请选择----","");
                            productObj .options.add(option);
                            for(var j=0;j<res.productList.length;j++){
                                var opt = res.productList[j];
                                var option = new Option(opt.desc,opt.value);
                                productObj .options.add(option);
                            }
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        alert("获取产品失败");
                    }
                });
            });
        });

    </script>
</@layout_default.page>