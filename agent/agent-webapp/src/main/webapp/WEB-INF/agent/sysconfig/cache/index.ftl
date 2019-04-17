<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='缓存清除' page_num=6>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div class="row-fluid sortable ui-sortable">

    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 缓存清除</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="form-horizontal">

                <div class="control-group">
                    <label class="control-label" for="focusedInput">清除业绩缓存：</label>
                    <div class="controls">
                        <input type="text" class="input-small" id="performanceDay" name="performanceDay" value="${day!}"> &nbsp;&nbsp;&nbsp;&nbsp;
                        <button type="button" id="search_btn" class="btn btn-success" onclick="clearPerformanceCache()">清除</button>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">清除字典表缓存：</label>
                    <div class="controls">
                        <button type="button" id="search_btn" class="btn btn-success" onclick="clearDictSchoolCache()">清除</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">

    //时间控件
    $("#performanceDay").datepicker({
        dateFormat: 'yymmdd',  //日期格式，自己设置
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

    function clearPerformanceCache(){
        var day = $("#performanceDay").val();
        $.post("clear_performance_cache.vpage",{
            day: day
        }, function(data){
            if (!data.success) {
                alert(data.info);
            } else {
                alert("清除成功");
            }
        });
    }

    function clearDictSchoolCache(){
        $.post("clear_dict_school_cache.vpage", function(data){
            if (!data.success) {
                alert(data.info);
            } else {
                alert("清除成功");
            }
        });
    }

</script>

</@layout_default.page>
