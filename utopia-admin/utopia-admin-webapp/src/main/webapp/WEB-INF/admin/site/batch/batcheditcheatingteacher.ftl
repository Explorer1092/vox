<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='Web manage' page_num=4>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="span9">

    <@h.head/>

    <fieldset>
        <legend>批量编辑作弊老师</legend>

        <form method="post" action="/site/batch/batcheditcheatingteacher.vpage">
            <ul class="inline">
                <li>
                    <label>输入需要编辑的老师ID：<textarea name="teacherIds" cols="45" rows="10"
                                                 placeholder="请在这里输入用户ID，一行一条"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>描述：<textarea name="desc" cols="45" rows="10" placeholder="输入描述"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>操作：<select name="editType">
                        <option value="Delete">删除作弊老师</option>
                        <option value="Save">添加作弊老师</option>
                        <option value="GoldDelete">导入园丁豆已清零老师</option>
                        <option value="AuthDelete">导入认证已取消老师</option>
                    </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    创建时间：
                    <input id="startDate" type="text" class="input-small" placeholder="开始时间" name="startDate"
                           value="${startDate!}">~
                    <input id="endDate" type="text" class="input-small" placeholder="结束时间" name="endDate"
                           value="${endDate!}">
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <input class="btn" type="submit" value="提交"/>
                    <input id="exportBlack" class="btn" type="button" value="导出作弊历史"/>
                    <input id="exportWhite" class="btn" type="button" value="导出待洗白名单"/>
                    <input id="exportGoldWait" class="btn" type="button" value="导出园丁豆待清零名单"/>
                    <input id="exportAuthWait" class="btn" type="button" value="导出认证待取消名单"/>
                    <input id="exportAll" class="btn" type="button" value="导出全部作弊老师（尽量少用）"/>

                </li>
            </ul>
        </form>
        <div>
            <label>统计：</label>
            <table class="table table-bordered">
                <tr>
                    <td>失败：</td>
                    <td><#if failedList??>${failedList?size}</#if>个</td>
                </tr>
            </table>
            <label>失败记录：</label>
            <table class="table table-bordered">
                <#if failedList??>
                    <#list failedList as l>
                        <tr>
                            <td>${l}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </fieldset>
</div>
<script type="text/javascript">

    Date.prototype.format = function (format) {
        var o = {
            "M+": this.getMonth() + 1, //month
            "d+": this.getDate(), //day
            "h+": this.getHours(), //hour
            "m+": this.getMinutes(), //minute
            "s+": this.getSeconds(), //second
            "q+": Math.floor((this.getMonth() + 3) / 3), //quarter
            "S": this.getMilliseconds() //millisecond
        }

        if (/(y+)/.test(format)) {
            format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        }

        for (var k in o) {
            if (new RegExp("(" + k + ")").test(format)) {
                format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
            }
        }
        return format;
    }
    $(function () {
        $("#startDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $("#endDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $("#exportBlack").on("click", function () {
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if (startDate == undefined || startDate.length == 0) {
                alert("请选择开始时间");
                return false;
            }
            if (endDate == undefined || endDate.length == 0) {
                alert("请选择结束时间");
                return false;
            }
            window.location.href = "exportcheating.vpage?startDate=" + startDate + "&endDate=" + endDate + "&type=BLACK";
        });
        $("#exportWhite").on("click", function () {
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if (startDate == undefined || startDate.length == 0) {
                alert("请选择开始时间");
                return false;
            }
            if (endDate == undefined || endDate.length == 0) {
                alert("请选择结束时间");
                return false;
            }
            window.location.href = "exportcheating.vpage?startDate=" + startDate + "&endDate=" + endDate + "&type=WHITE";
        });

        $("#exportGoldWait").on("click", function () {
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if (startDate == undefined || startDate.length == 0) {
                alert("请选择开始时间");
                return false;
            }
            if (endDate == undefined || endDate.length == 0) {
                alert("请选择结束时间");
                return false;
            }
            window.location.href = "exportcheating.vpage?startDate=" + startDate + "&endDate=" + endDate + "&type=GOLD_WAIT";
        });

        $("#exportAuthWait").on("click", function () {
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if (startDate == undefined || startDate.length == 0) {
                alert("请选择开始时间");
                return false;
            }
            if (endDate == undefined || endDate.length == 0) {
                alert("请选择结束时间");
                return false;
            }
            window.location.href = "exportcheating.vpage?startDate=" + startDate + "&endDate=" + endDate + "&type=AUTH_WAIT";
        });

        $("#exportAll").on("click", function () {
            window.location.href = "exportallcheatingteacher.vpage";
        });
    });
</script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>