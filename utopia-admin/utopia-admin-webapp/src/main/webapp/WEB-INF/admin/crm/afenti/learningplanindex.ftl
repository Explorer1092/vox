<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<div class="span9">
    <div>
        <fieldset>
            <legend>学生<a href="../student/studenthomepage.vpage?studentId=${userId!}">${userId!}</a>阿分题做题记录
            </legend>
        </fieldset>
        <form method="post" action="?" class="form-horizontal">
            <ul class="inline form_datetime">
                <li>
                    <label for="userId">
                        用户ID
                        <input name="userId" type="text" value="${userId!}" style="width: 100px;"/>
                    </label>
                </li>
                <li>
                    <label for="startDate">
                        起始时间
                        <input name="startDate" id="startDate" type="text" placeholder="格式：2014-11-21"/>
                    </label>
                </li>
                <li>
                    <label for="endDate">
                        截止时间
                        <input name="endDate" id="endDate" type="text" placeholder="格式：2014-11-21"/>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-success">查询</button>
                </li>
            </ul>
        </form>
    </div>
    <div>
        <fieldset><legend>查询结果</legend></fieldset>
        <table class="table table-striped table-bordered do_table" style="font-size: 14px; display:none;">
            <thead>
            <tr>
                <th>用户ID</th>
                <th>学科</th>
                <th>课本名称</th>
                <th>课本ID</th>
                <th>单元ID</th>
                <th>关卡序号</th>
                <th>题目ID</th>
                <th>题目重复次数</th>
                <th>正确次数</th>
                <th>错误次数</th>
                <th>创建时间</th>
                <th>更新时间</th>
                <th>超纲</th>
            </tr>
            </thead>
            <#if datas?has_content>
                <#list datas as data>
                    <tr>
                        <td nowrap>
                        ${data.userId!}
                        </td>
                        <td nowrap>
                        ${data.subject!}
                        </td>
                        <td nowrap>
                        ${data.bookName!}
                        </td>
                        <td nowrap>
                        ${data.newBookId!}
                        </td>
                        <td nowrap>
                        ${data.newUnitId!}
                        </td>
                        <td nowrap>
                        ${data.rank!}
                        </td>
                        <td nowrap class="doExamId">
                            <a href="http://www.17zuoye.com/container/viewpaper.vpage?qid=${data.examId!}" target="_blank">${data.examId!}</a>
                        </td>
                        <td nowrap>--</td>
                        <td nowrap>
                        ${data.rightNum!}
                        </td>
                        <td nowrap>
                        ${data.errorNum!}
                        </td>
                        <td nowrap>
                        ${data.createtime!}
                        </td>
                        <td nowrap>
                        ${data.updatetime!}
                        </td>
                        <td nowrap>
                            <a href="http://10.0.1.193:8080/${data.newUnitId!}/${(data.examId)?replace("(\\d)\\D.*","$1","r")!}" target="_blank">检查</a>
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script type="text/javascript">

    $(function(){
        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            minDate         : -30,
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        // init
        $('#startDate').val('${startDate!''}');
        $('#endDate').val('${endDate!''}');
        $('input[name="userId"]').val('${userId!}');

        // 计算重复次数
		(function(){
			var $doExamIds = $(".doExamId"),
				has_compute_ids =  [],
				index = -1,
				do_filter_dom = function($filters_dom){
                    var $filter_next_doms = $filters_dom.next(),
                        count = $filter_next_doms.length;

                    $filter_next_doms.each(function(index, dom){
                        $(dom).text(count--);
                    });
				};

			while(index < ($doExamIds.length - 1) ){
				++index;

				var examId = $doExamIds.eq(index).text().trim();

				if(has_compute_ids.indexOf(examId) === -1){
					do_filter_dom($doExamIds.filter(':contains("'+ examId +'")'));
				}

			}

			$('.do_table').show();

		})();
    });
</script>
</@layout_default.page>
