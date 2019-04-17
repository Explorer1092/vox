<#import "../../researchstaffv3.ftl" as com>
<@com.page menuIndex=12 menuType="normal">
<#if currentUser.isResearchStaffForCounty()>
<div class="row_vox_right">
    <input type="button" value="园丁豆记录" id="goldRecordBtn">
    <a target="_blank" href="/reward/index.vpage">
        <i class="icon_rstaff icon_rstaff_7"></i> <strong class="text_orange">奖品中心</strong>
    </a>
</div>
</#if>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">大数据报告</a> <span class="divider">/</span></li>
    <li class="active">积分统计</li>
</ul>

<div class="paperStep" style="line-height: 2;text-align: center;">
    <strong>
        辖区认证老师人数：${(authenticateNum.teacherCount)!"暂无相关数据"}&nbsp;&nbsp;
        认证老师班级使用学生人数：${(authenticateNum.rstaffAuthCount)!"暂无相关数据"}
        [ 数据时间：${dataTime!"加载中......"} ]
    </strong>
</div>
<div id="content_all_box">
    <div class="article">
        <ul class="inline_vox">
            <#switch role>
                <#case "PROVINCE">
                    <li class="dayTimePop select_none">
                        <select id="school_provinces" next_level="school_citys" show_error="false" prev_level="school_provinces" style="width: 110px;" class="district_select int_vox">
                            <option value="${code!""}">${region!""}</option>
                        </select>
                    </li>
                <#case "CITY">
                    <li class="dayTimePop select_none">
                        <select id="school_citys" next_level="school_countys" show_error="false"  style="width: 105px;" prev_level="school_provinces" error_callback="get_region_error_callback" class="int_vox district_select" default_option='{key:"-1",value:"全部"}' success_callback="get_region_success_callback">
                            <#if role =="CITY">
                                <#list nameMap?keys as key>
                                    <option value="${key}">${nameMap[key]}</option>
                                </#list>
                            </#if>
                        </select>
                    </li>
                <#case "COUNTY">
                    <li class="dayTimePop select_none">
                        <select id="school_countys" prev_level="school_citys" style="width: 150px;" show_error="false" error_callback="get_region_error_callback" class="district_select int_vox" default_option='{key:"-1",value:"全部"}' success_callback="get_region_success_callback">
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
                <select id="school_name" class="int_vox" style="width: 180px;">
                    <option value="0">全部</option>
                    <#if role == "STREET">
                        <#list nameMap?keys as key>
                            <option value="${key}">${nameMap[key]}</option>
                        </#list>
                    </#if>
                </select>
                按学校名过滤
                <input id="school_input" class="int_vox" type="text" style="width: 80px;">
            </li>
            <li>
                <a id="query_but" href="javascript:void(0);" class="btn_vox btn_vox_warning">
                    <i class='icon_rstaff icon_rstaff_20'></i> 查询
                </a>
            </li>
            <li>
                <a id="query_by_time_but" href="javascript:void(0);" class="btn_vox btn_vox_primary">分时查询</a>
            </li>
            <#if currentUser.isResearchStaffForCounty()>
                <li>
                    <a id="query_by_school_but" href="javascript:void(0);" class="btn_vox btn_vox_primary">学校查询</a>
                </li>
            </#if>
            <li>
                <a id="download_by_query_but" href="javascript:void(0);" class="btn_vox btn_vox_primary">下载</a>
            </li>
        </ul>
    </div>
<#-- 用户汇总显示 -->
    <div id="content_box" style="margin-top: 14px;"></div>
</div>

<#-- 分时查询 -->
<div id="query_by_time_box"></div>
<#--学校查询显示-->
<div id="school_content_box"></div>

<script type="text/javascript">
    function get_region_error_callback( _this ){
        _this.closest("li.select_none").hide();
    }

    /** 成功操作 */
    function get_region_success_callback( _this ){
        _this.closest("li.select_none").show();
        var next_level = $("#"+ _this.attr("next_level") );
        if ( next_level.length > 0 ) {
            next_level.closest("li.select_none").hide();
        }
    }

    /** 获取学校 */
    function getSchoolList( _this ){
        if($17.isBlank(_this.val())){
            return false;
        }

        if ( _this && _this.length > 0 ) {
            $.getJSON( '/rstaff/getschool.vpage?schoolCode=' + _this.val(), function( data ){
                if ( data.success && data.total > 0 ) {
                    var html = '<option value="0">全部</option>';
                    $.each( data.rows, function(){
                        html += '<option value="'+this.key+'">'+this.value+'</option>';
                    });
                    $("#school_name").empty().html( html );
                    $('#school_name').filterByText($('#school_input'));
                }else {
                    $.prompt(data.info, {title : "提示", buttons : {"确定" : true}});
                }
            });
        }
        return false;
    }

    $(function(){


        $("#query_by_time_but").on("click", function(){
            $("#content_all_box").remove();

            $("#query_by_time_box").load("/rstaff/report/integralstat/detail.vpage", function(){
                $(".district_select").trigger("change");
            });
        });

        //学校统计查询
        $("#query_by_school_but").on("click",function(){
            $("#content_all_box").remove();
            $("#school_content_box").load("/rstaff/report/integralstat/schoolsummarychip.vpage", function(data){});
        });

        $("#download_by_query_but").on("click",function(){
            var _schoolcode = $("#school_name").val();
            if($17.isBlank(_schoolcode)){
                _schoolcode = 0;
            }
            var _areaCode = getQueryAreaCode();
            window.location  = '/rstaff/report/integralstat/downloadsummarychip.vpage?areaCode=' + _areaCode+'&schoolId=' + _schoolcode;
            return false;

        });

        function getQueryAreaCode(){
            var _province   = $("#school_provinces").val();
            var _city       = $("#school_citys").val();
            var _area       = $("#school_countys").val();
            var _areaCode   = _province || 0;

            if(_city && _city != "-1"){
                _areaCode = _city;
            }

            if(_area && _area != "-1"){
                _areaCode = _area;
            }
            return _areaCode;
        }

        /** 查询 */
        $("#query_but").on("click",function(){
            var _this       = $(this);
            var _schoolcode = $("#school_name").val();
            var _areaCode   = getQueryAreaCode();
            _this.removeClass("btn_vox_warning");
            if(_this.hasClass("waiting")){
                return false;
            }
            if($17.isBlank(_schoolcode)){
                _schoolcode = 0;
            }

            _this.addClass("waiting");
            $("#content_box").html('正在加载数据，请稍候......');
            $.get( '/rstaff/report/integralstat/summarychip/search.vpage?areaCode='+_areaCode+'&schoolId='+_schoolcode, function( data ){
                $("#content_box").empty();
                if(data){
                    $("#content_box").html(data);
                }else{
                    alert("网络连接失败，请重试。");
                }
                _this.addClass("btn_vox_warning");
                _this.removeClass("waiting");
            });
        });

        var role        = "${(role)!''}";
        var district    = null;

        switch(role){
            case "PROVINCE":
                district = $("#school_provinces");
                break;
            case "CITY":
                district = $("#school_citys");
                break;
            case "COUNTY":
                district = $("#school_countys");
                break;
        }

        App.districtSelect.init(district);


        // 园丁豆记录
        $("#goldRecordBtn").on("click",function(){
            $17.tongji("区教研员园丁豆记录点击");
            window.location.href = "/rstaff/report/integralstat/goldrecord.vpage";
        });
    });

    $(".district_select").on("change", function(){
        var _this = $(this);
        if(_this.val() == "-1"){
            var prev_level = $("#" + _this.attr("prev_level"));
            if(prev_level.length < 1){
                return;
            }
            getSchoolList(prev_level);
        }else{
            getSchoolList(_this);
        }
    });

    jQuery.fn.filterByText = function(textbox) {
        return this.each(function() {
            var select = this;
            var options = [];
            $(select).find('option').each(function() {
                options.push({value: $(this).val(), text: $(this).text()});
            });
            $(select).data('options', options);

            $(textbox).bind('change keyup', function() {
                var options = $(select).empty().data('options');
                var search = $.trim($(this).val());
                var regex = new RegExp(search,"gi");

                $.each(options, function(i) {
                    var option = options[i];
                    if(option.text.match(regex) !== null) {
                        $(select).append(
                                $('<option>').text(option.text).val(option.value)
                        );
                    }
                });
            });
        });
    };

</script>
</@com.page>