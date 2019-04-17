<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='薯条英语课程管理' page_num=26>
<script type="text/javascript"
        src="${requestContext.webAppContextPath}/public/js/kalendae/kalendae.standalone.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/kalendae/kalendae.css" rel="stylesheet">

<style>
    .kalendae .k-days span.k-out-of-month {
        color: #ddd !important;
        background: transparent !important;
        border: none !important;
    }
</style>

<div id="main_container" class="span9">
    <legend>${coursePojo.productName!}
    </legend>
    <div id="data_table_journal">
        <div class="table-responsive table_box">
            <form id="info_frm" name="info_frm" action="save.vpage" method="post">
                开始日期：<input id="beginDate" name="beginDate" class="auto-kal"
                            data-kal="mode:'single',format:'YYYY-MM-DD'" readonly
                            value="${coursePojo.beginDate!}">
                结束日期：<input name="endDate" class="auto-kal" data-kal="mode:'single',format:'YYYY-MM-DD'" readonly
                            value="${coursePojo.endDate!}">
                <input type="hidden" name="bookList" value="${bookList!}">
                <br>
                <br>
                <#assign flag = 0>
                <#if coursePojo.editPojoList?? && coursePojo.editPojoList?size gt 0>
                    <#list coursePojo.editPojoList as e >
                        <#assign flag = flag + 1>
                        <div class="table-responsive table_box" style="position: relative;">
                            <table>
                                <tr>
                                    <td>初始化日期规则:</td>
                                    <td>周六不上课:<input name="skip_saturday_${e.bookId!}" type="checkbox" name="名称"/></td>
                                    <td>周日不上课:<input name="skip_sunday_${e.bookId!}" type="checkbox" name="名称"/></td>
                                    <td>
                                        <#if flag != 1>
                                            起始日期:<input name="init_date_${e.bookId!}" class="auto-kal"
                                                        style="width: 80px" data-kal="mode:'single',format:'YYYY-MM-DD'" readonly>
                                        </#if>
                                    </td>
                                    <td><input type="button" value="初始化日期" class="but"></td>
                                    <input type="hidden" value="${e.bookId!}">
                                    <input type="hidden" value="${e.unitNum!}">
                                </tr>
                                <tr>
                                    <td>${e.bookName!}:</td>
                                    <td>单元数量:${e.unitNum!}</td>
                                    <td>选中数量:<span class="num btn btn-default"></span></td>
                                </tr>
                            </table>
                            <textarea name="dateList_${e.bookId!}" id="dateList_${e.bookId!}"
                                      style="width: 60%;height: 200px;">${e.dateList!}</textarea>
                            <input type="text" class="auto-kal auto_kal"
                                   data-kal="mode:'multiple',months: 4,format:'YYYY-MM-DD'"
                                   style="position: absolute;top:30px;left:60px; height:200px;width:90%;opacity: 0">
                        </div>
                    </#list>
                </#if>
                <br>
                <input type="hidden" name="productId" value="${coursePojo.productId!}">
                <div style="margin-left: 50%">
                    <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
                        <i class="icon-pencil icon-white"></i>确定
                    </a>
                    <a title="返回" href="javascript:window.history.back();" class="btn">
                        <i class="icon-share-alt"></i>返回
                    </a>
                </div>
            </form>
        </div>
    </div>
    <script type="text/javascript">
        $(function () {
            $('#save_info').on('click', function () {
                // 保存商品信息
                $('#info_frm').ajaxSubmit({
                    type: 'post',
                    url: 'save.vpage',
                    success: function (data) {
                        if (data.success) {
                            alert("保存成功");
                            window.history.back();
                            window.location.reload();
                        } else {
                            alert("保存失败:" + data.info);
                        }
                    },
                    error: function (msg) {
                        alert("保存失败！");
                    }
                });
            })

            $(".auto_kal").change(function (ev) {
                $(this).prev().val(ev.currentTarget.value);
//                $(this).next(".num").html(ev.currentTarget.value.split(',').length)
                $(this).prev().prev().children(":eq(0)").children(":eq(1)").children(":eq(2)").children(".num").html(ev.currentTarget.value.split(',').length)
            });

            var textareaArr = $('textarea');
            var inputArr = $('.auto_kal');
            var numArr = $('.num');
            for (var i = 0; i < textareaArr.length; i++) {
                inputArr.eq(i).val(textareaArr.eq(i).val());
                if (textareaArr.eq(i).val() == "") {
                    numArr.eq(i).html(0);
                } else {
                    numArr.eq(i).html(textareaArr.eq(i).val().split(",").length);
                }
            }

            $(".but").click(function () {
                var butArr = $(".but");
                console.log(butArr)
                var initDate;
                if (this == butArr[0]) {
                    initDate = $("#beginDate").val();
                } else {
                    initDate = $(this).parent().prev().children().val();
                }
                if (initDate == "") {
                    alert("请输入初始化起始日期")
                }
                var bookId = $(this).parent().next().val();
                var unitNum = $(this).parent().next().next().val();
//                var initDate = $(this).parent().prev().children().val();
                var skipSun = $(this).parent().prev().prev().children().is(':checked');
                var skipSat = $(this).parent().prev().prev().prev().children().is(':checked');
                $.ajax({
                    url: "/chips/chips/coursemanager/initdate.vpage",
                    type: "POST",
                    data: {
                        "initDate": initDate,
                        "skipSun": skipSun,
                        "skipSat": skipSat,
                        "unitNum": unitNum,
                        "bookId": bookId
                    },
                    success: function (data) {
                        if (data.success) {
                            $("#" + data.id).val(data.dateList);
                            console.log($("#" + data.id).next())
                            console.log($("#" + data.id).next().next(".num"))
                            var ll = data.dateList;
                            $("#" + data.id).next().next(".num").html(ll.split(',').length);
                            var textareaArr = $('textarea');
                            var inputArr = $('.auto_kal');
                            var numArr = $('.num');
                            for (var i = 0; i < textareaArr.length; i++) {
                                inputArr.eq(i).val(textareaArr.eq(i).val());
                                if (textareaArr.eq(i).val() == "") {
                                    numArr.eq(i).html(0);
                                } else {
                                    numArr.eq(i).html(textareaArr.eq(i).val().split(",").length);
                                }
                            }
                        }
                    },
                    error: function (e) {
                        alert("error")
                    }
                });
            })
        });
    </script>
</@layout_default.page>