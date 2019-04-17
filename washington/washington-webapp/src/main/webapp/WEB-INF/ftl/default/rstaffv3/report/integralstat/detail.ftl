<div class="article">
    <ul class="inline_vox">
    <#switch role>
        <#case "PROVINCE">
            <li class="dayTimePop select_none">
                <select id="school_provinces" next_level="school_citys" prev_level="school_provinces" style="width: 110px;" class="district_select int_vox" show_error="false">
                    <option value="${code!""}">${region!""}</option>
                </select>
            </li>
        <#case "CITY">
            <li class="dayTimePop select_none">
                <select id="school_citys" next_level="school_countys" style="width: 105px;" prev_level="school_provinces" class="int_vox district_select" default_option='{key:"-1",value:"全部"}' show_error="false" error_callback="get_region_error_callback" success_callback="get_region_success_callback">
                    <#if role =="CITY">
                        <#list nameMap?keys as key>
                            <option value="${key}">${nameMap[key]}</option>
                        </#list>
                    </#if>
                </select>
            </li>
        <#case "COUNTY">
            <li class="dayTimePop select_none">
                <select id="school_countys" prev_level="school_citys" style="width: 150px;" class="district_select int_vox" default_option='{key:"-1",value:"全部"}' show_error="false" error_callback="get_region_error_callback" success_callback="get_region_success_callback">
                    <#if role =="COUNTY">
                        <#list nameMap?keys as key>
                            <option value="${key}">${nameMap[key]}</option>
                        </#list>
                    </#if>
                </select>
            </li>
            <#break>
        <#default>
    </#switch>
        <li class="dayTimePop select_none">
            <select id="school_name" class="int_vox" style=" width: 180px;">
                <#if role == "STREET">
                    <#list nameMap?keys as key>
                        <option value="${key}">${nameMap[key]}</option>
                    </#list>
                </#if>
            </select>
        </li>
        <li class="dayTimePop">
            <input id="startDate" style="width: 110px;" type="text" placeholder="起始日期" class="int_vox" readonly="readonly"/>
            <label for="startDate"><i class='icon_rstaff icon_rstaff_6'></i></label>
        </li>
        <li class="dayTimePop">
            <input id="endDate" style="width: 110px;" type="text" placeholder="结束日期" class="int_vox" readonly="readonly"/>
            <label for ="endDate"><i class='icon_rstaff icon_rstaff_6'></i></label></i>
        </li>
        <li>
            <a id="query_but" href="javascript:void(0);" class="btn_vox btn_vox_warning">
                <i class='icon_rstaff icon_rstaff_20'></i> 查询
            </a>
        </li>
        <li>
            <a href="/rstaff/report/integralstat/summary.vpage" class="btn_vox btn_vox_primary rstaff_add_books_but">返回</a>
        </li>
    </ul>
    <table class="table_vox table_vox_bordered table_vox_striped edge_vox_bot">
        <thead>
        <tr>
            <th colspan="2">新增人数</th>
        </tr>
        <tr>
            <td>认证老师新增数</td>
            <td>认证学生新增数</td>
        </tr>
        </thead>
        <tbody id="detail_content_query_by_time_box">
        <tr>
            <td>请选择查询</td>
            <td>请选择查询</td>
        </tr>
        </tbody>


    </table>
</div>
<script type="text/html" id="list:分时查询列表">
    <tr>
        <td>
            <%if(infoList != null && infoList.teacherCount > 0){%>
            <%=infoList.teacherCount%>
            <%}else{%>
            0
            <%}%>
        </td>
        <td>
            <%if(infoList != null && infoList.authStudentCount > 0){%>
            <%=infoList.authStudentCount%>
            <%}else{%>
            0
            <%}%>
        </td>
    </tr>
</script>

<script>
    $(function(){

        /** 开始时间 */
        $("#startDate").datepicker({
            dateFormat: 'yy-mm-dd',
            defaultDate: "",
            numberOfMonths: 1,
            maxDate: "-1D",
            onSelect: function (selectedDate) {
                $('#endDate').datepicker('option', 'minDate', selectedDate);
                $("#startDate").removeClass("alert_vox_error");
            }
        });

        /** 结束时间 */
        $('#endDate').datepicker({
            dateFormat: 'yy-mm-dd',
            defaultDate: "",
            numberOfMonths: 1,
            maxDate: "-1D",
            onSelect:function(){
                $("#endDate").removeClass("alert_vox_error");
            }
        });

        /** 查询 */
        $("#query_but").on("click",function(){
            var _this = $(this);
            var _province = $("#school_provinces").val();
            var _city = $("#school_citys").val();
            var _area = $("#school_countys").val();
            var _schoolCode = $("#school_name").val();
            var _areaCode = 0;
            var _beginTime = $("#startDate").val();
            var _endTime = $("#endDate").val();
            if($17.isBlank(_beginTime)){
                $("#startDate").addClass("alert_vox_error");
                return false;
            }
            if($17.isBlank(_endTime)){
                $("#endDate").addClass("alert_vox_error");
                return false;
            }
            if(_province){
                _areaCode = _province;
            }
            if(_city && _city != "-1"){
                _areaCode = _city;
            }
            if(_area && _area != "-1"){
                _areaCode = _area;
            }
            _this.removeClass("btn_vox_warning");
            if(_this.hasClass("waiting")){return false;}
            _this.addClass("waiting");
            $("#detail_content_query_by_time_box").html("<tr><td colspan='2' class='text_gray_6'>数据正在查询，请耐心等待....</td></tr>");
            $.get( '/rstaff/report/integralstat/detailchip.vpage?areaCode=' + _areaCode + '&schoolId=' + _schoolCode + '&beginTime=' + _beginTime + '&endTime=' + _endTime, function( data ){
                if(data.success){
                    var html = template("list:分时查询列表",{
                        infoList : data.value
                    });
                    $("#detail_content_query_by_time_box").html(html);
                }else{
                    alert("查询失败，请重试。");
                }
                _this.addClass("btn_vox_warning");
                _this.removeClass("waiting");
            });

        });
    });

    $(".district_select").on("change", function(){
        var _this = $(this);
        var regionCode = _this.val();
        if ( regionCode == "-1" ) {
            var prev_level = $("#" + _this.attr("prev_level") );
            if ( prev_level.length < 1 ) {
                return;
            }
            getSchoolList( prev_level );
        } else {
            getSchoolList( _this );
        }
    });
</script>