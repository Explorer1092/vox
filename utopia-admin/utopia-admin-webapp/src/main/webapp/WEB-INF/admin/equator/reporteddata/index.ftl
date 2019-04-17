<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='学生操作记录' page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<style>
select { width: auto }
.beautiful-pre {margin: 0; padding: 0; border: none; line-height: 14px; font-size: 12px; background: transparent}
</style>
<div class="span9">
    <span>
      <form class="form-horizontal" action="/equator/student/history/index.vpage" method="get" id="historyQueryForm">
            学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus" placeholder="请输入学生Id"/>
            时间：<input type="text" id="time" name="time" value="${time!''}" readonly>
            模块类型:<select id="moduleType" name="moduleType">
                        <#if moduleTypes??>
                            <#list moduleTypes?keys as key>
                                <option value="${key}"
                                        <#if moduleType?? && moduleType == key>selected</#if>>${moduleTypes[key]!''}
                                </option>
                            </#list>
                        </#if>
      </select>
            <button id="submit_query" style="margin-top: -12px;" class="btn btn-info">查询</button>
     </form>
    </span>

    <div>
        <table class="table table-hover table-striped table-bordered" style="font-size: 12px">
            <thead>
            <tr>
                <th style="width: 10%">模块类型</th>
                <th style="width: 30%">行为描述</th>
                <th style="width: 15%">额外信息</th>
                <th style="width: 10%">创建时间</th>
                <th style="width: 10%">环境</th>
                <th style="width: 10%">操作人员</th>
            </tr>
            </thead>
            <tbody id="tbody">
                <#if historyInfoList ?? >
                    <#list historyInfoList as info>
                       <tr>
                           <td>${info.moduleType!''}</td>
                           <td><pre class="beautiful-pre"></pre><span class="extInfo" style="display: none">${info.behaviorDesc!''}</span></td>
                           <td><pre class="beautiful-pre"></pre><span class="extInfo" style="display: none">${info.extInfo!''}</span></td>
                           <td>${info.createTime!''}</td>
                           <td>
                               <#if info.mode??>
                                   <#if info.mode == "UNIT_TEST">
                                      单元测试环境
                                   <#elseif info.mode == "DEVELOPMENT">
                                      开发环境
                                   <#elseif info.mode == "TEST">
                                      测试环境
                                   <#elseif info.mode == "STAGING">
                                      预发布环境
                                   <#elseif info.mode == "PRODUCTION">
                                      生产环境
                                   <#else>
                                      未知
                                   </#if>
                               </#if>
                           </td>
                           <td>${info.operator!''}</td>
                       </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script>
    $(function () {
        $.fn.datetimepicker.dates['zh-CN'] = {
            days: ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
            daysShort: ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
            daysMin: ["日", "一", "二", "三", "四", "五", "六", "日"],
            months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            monthsShort: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            today: "今日",
            suffix: [],
            meridiem: []
        };
        $('#time').datetimepicker({
            language: 'zh-CN',
            weekStart: 1,
            autoclose: 1,
            minView: 2,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1,
            format: "yyyy-mm-dd",
            endDate: new Date()
        });


        $('.extInfo').each(function () {
            var desc = $(this).text().trim();
            if (isJson(desc)) {
                // 解析成json对象
                const jsonObject = JSON.parse(desc);
                // json对象可能是一个对象数组
                if (jsonObject instanceof Array) {
                    for (var index = 0; index < jsonObject.length; index++) {
                        // 数组的元素是不是一个json对象
                        var arrayItemStr = JSON.stringify(jsonObject[index]);
                        if (isJson(arrayItemStr)) {
                            // 数组元素是一个json对象，解析对象
                            jsonObjectParse(jsonObject[index]);
                        }
                    }
                }
                // 就是一个对象
                else {
                    jsonObjectParse(jsonObject);
                }
                desc = JSON.stringify(jsonObject, null, 2);
            }
            $(this).parent().find('pre').text(desc);
        });

        $("#submit_query").on("click", function () {
            $("#historyQueryForm").submit();
        });
    });

    function isJson(str) {
        try {
            const obj = JSON.parse(str);
            if (typeof obj === 'object' && obj) {
                return true
            }
        } catch (e) {
            return false
        }
    }

    function jsonObjectParse(jsonObject) {
        for (var attr in jsonObject) {
            if (isJson(jsonObject[attr])) {
                jsonObject[attr] = JSON.parse(jsonObject[attr]);
            }
        }
    }
</script>
</@layout_default.page>