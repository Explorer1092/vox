<#import "../module.ftl" as com>
<@com.page t=1 s=5>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">组 卷</a> <span class="divider">/</span></li>
    <li class="active">试卷列表</li>
</ul>
<#if userBookList?size gt 0>
    <ul class="inline_vox">
        <li>
            <select class="int_vox" id="book_press_box">
                <option value="0">所有版本教材</option>
                <#list bookPress as p>
                    <option>${p}</option>
                </#list>
            </select>
        </li>
        <li>
            <select class="int_vox" id="great_box">
                <option value="0">所有年级</option>
            </select>
        </li>
        <li>
            <select class="int_vox" id="book_type_box" style="width: 100px;">
                <option value="0">所有册别</option>
            </select>
        </li>
        <li>
            <select class="int_vox" id="paper_category" style="width: 100px;">
                <option value="">所有类型</option>
                <#list paperCategoryList as paperCategory>
                  <option value="${paperCategory.name()}">${paperCategory.description}</option>
                </#list>
            </select>
        </li>
        <li>
            <#if bookPress?size gt 0>
                <a id="query_but" href="javascript:void (0)" class="btn_vox btn_vox_warning">筛选</a>
            <#else>
                <a href="javascript:void (0)" class="btn_vox" style="cursor: default;" title="暂时没有教材，不能查询">筛选</a>
            </#if>
        </li>
    </ul>
    <div id="exam_message_info_box" class="fill_vox_20">
        <#-- 试卷详细信息 -->
    </div>

    <script type="text/javascript">
        function createPageList(index){
            $.post('/rstaff/testpaper/listchip.vpage?currentPage=1', function(data){
                $("#exam_message_info_box").html(data);
            });
        }

        $(function(){
            createPageList(1);

            /** 所有版本教材 */
            $("#book_press_box").on("change", function(){
                var _this = $(this);
                var press = _this.children("option:selected").val();

                if(press == 0){
                    $("#great_box option:gt(0)").remove();
                    $("#great_box option").text("所有年级");
                    $("#book_type_box option").first().text("所有册别");
                    $("#paper_category option").first().text("所有类型");
                    return false;
                }

                $("#book_press_box").removeClass("alert_vox_error");

                $.post("/rstaff/book/findClassLevelByPress.vpage", { press: press }, function(data){
                    var html = "<option value='0'>所有年级</option>";
                    $.each(data.value,function(i){
                        html += "<option value='" + data.value[i].level + "'> " + data.value[i].description + " </option>";
                    });
                    $("#great_box").html(html);
                });
            });

            /** 所有年级 */
            $("#great_box").on("change", function(){
                var _this       = $(this);
                var level       = _this.val();
                var description = $("#book_press_box").children("option:selected").val();

                $.post("/rstaff/book/findTermByPressAndClazzLevel.vpage", { press: description, clazzLevel: level }, function(data){
                    var html = "<option value='0'>所有册别</option>";

                    $.each(data.value, function(i){
                        html += "<option value='" + data.value[i].key + "'> " + data.value[i].value + " </option>";
                    });
                    $("#book_type_box").html(html);
                });
            });

            /** 筛选 */
            $("#query_but").on("click", function(){
                var _error_tip_press    = $("#book_press_box");
                var _press              = _error_tip_press.children("option:selected").val();
                var _level              = $("#great_box").children("option:selected").val();
                var _bookType           = $("#book_type_box").children("option:selected").val();
                var _paperCategory      = $("#paper_category").children("option:selected").val();

                _error_tip_press.removeClass("alert_vox_error");

                var _p = _press     == 0 ? "" : _press;
                var _l = _level     == 0 ? "" : _level;
                var _b = _bookType  == 0 ? "" : _bookType;
                var _d = {
                    pressName     : _p,
                    clazzLevel    : _l,
                    termType      : _b,
                    paperCategory : _paperCategory
                };

                $.post("/rstaff/testpaper/listchip.vpage?currentPage=1", _d, function(data){
                    $("#exam_message_info_box").html(data);
                });
            });
        });
    </script>
<#else>
    <div style="text-align: center" class="edge_vox_tb">
        <strong>尚未创建任何试卷，立刻为全区<a href="/rstaff/testpaper/index.vpage">［创建同步试卷］</a>。</strong>
    </div>
</#if>
</@com.page>