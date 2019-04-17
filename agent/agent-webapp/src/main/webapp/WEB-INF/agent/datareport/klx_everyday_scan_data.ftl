<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="数据下载" page_num=10>
    <#assign isBd = requestContext.getCurrentUser().isBusinessDeveloper()!false/>
    <#assign isCountry = requestContext.getCurrentUser().isCountryManager()!false/>
    <#assign isRegion = requestContext.getCurrentUser().isRegionManager()!false/>
    <#assign isCityManager = requestContext.getCurrentUser().isCityManager()!false/>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 快乐学学校每日扫描量</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <style>
            select {width: 120px;position: relative;z-index: 3} /*label会遮住select的点击区域*/
        </style>
        <div class="box-content ">
            <form method="GET" id="school_data_report_form" class="form-horizontal">
                <ul class="row-fluid">
                    <li class="span3" style="<#if isCountry >width:15%<#else>width:20%</#if>">
                        <div class="control-group">
                            <label class="control-label" style="width:90px">选择日期</label>
                            <div class="controls" style="margin-left:100px">
                                <input type="text" class="input-small focused"
                                       style="width: 111px;position: relative;z-index: 3" id="schoolDate"
                                       name="startDate" value="${date!}">
                            </div>
                        </div>
                    </li>
                    <#if isCountry >
                        <li class="span3" style="width:15%;margin-left:5%">
                            <div class="control-group">
                                <label class="control-label" style="width:90px">选择大区:</label>
                                <div class="controls" style="margin-left:100px">
                                    <select id="schoolRegionList" name="region" class="can-selected post-item">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </li>
                    </#if>
                    <#if isCountry|| isRegion >
                        <li class="span3" style="width:20%;margin-left:5%">
                            <div class="control-group">
                                <label class="control-label" style="width:90px">选择城市:</label>
                                <div class="controls" style="margin-left:100px">
                                    <select id="schoolCityList" name="city" class="can-selected post-item">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </li>
                    </#if>
                    <li class="span6" style="width:20%;margin-left:10%">
                        <a href="javascript:void(0);" class="btn btn-primary" id="schoolExportBtn"
                           target="_blank">下载</a>
                    </li>
                    <input type="hidden" value="school" name="type">
                </ul>
            </form>
        </div>
    </div>
</div>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>快乐学老师每日扫描量</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <style>
            select {width: 120px;position: relative;z-index: 3} /*label会遮住select的点击区域*/
        </style>
        <div class="box-content ">
            <form method="GET" id="teacher_data_report_form" class="form-horizontal">
                <ul class="row-fluid">
                    <li class="span3" style="<#if isCountry|| isCityManager >width:15%<#else>width:20%</#if>">
                        <div class="control-group">
                            <label class="control-label" style="width:90px">选择日期</label>
                            <div class="controls" style="margin-left:100px">
                                <input type="text" class="input-small focused"
                                       style="width: 111px;position: relative;z-index: 3" id="teacherDate"
                                       name="startDate" value="${date!}">
                            </div>
                        </div>
                    </li>
                    <#if isCountry >
                        <li class="span3" style="width:15%;margin-left:5%">
                            <div class="control-group">
                                <label class="control-label" style="width:90px">选择大区:</label>
                                <div class="controls" style="margin-left:100px">
                                    <select id="teacherRegionList" name="region" class="can-selected">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </li>
                    </#if>
                    <#if isCountry|| isRegion >
                        <li class="span3" style="width:15%;margin-left:5%">
                            <div class="control-group">
                                <label class="control-label" style="width:90px">选择城市:</label>
                                <div class="controls" style="margin-left:100px">
                                    <select id="teacherCityList" name="city" class="can-selected">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </li>
                    </#if>
                    <#if isCountry|| isRegion || isCityManager>
                        <li class="span3" style="width:20%;margin-left:5%">
                            <div class="control-group">
                                <label class="control-label" style="width:90px">选择专员:</label>
                                <div class="controls" style="margin-left:100px">
                                    <select id="teacherBdList" name="bdId" class="can-selected">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </li>
                    </#if>
                    <li class="span6" style="width:15%;margin-left:5%">
                        <a href="javascript:void(0);" class="btn btn-primary" id="teacherExportBtn" target="_blank">下载</a>
                    </li>
                </ul>
                <input type="hidden" value="teacher" name="type">
            </form>
        </div>
    </div>
</div>

<script>
    //时间控件
    $("#schoolDate").datepicker({
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

    $("#teacherDate").datepicker({
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

    $(function () {
        <#if isCountry>
            getRegionData("teacherRegionList");
            getRegionData("schoolRegionList");
        </#if>

        <#if isRegion>
            getCity("", "teacherCityList");
            getCity("", "schoolCityList");
        </#if>

        <#if isCityManager>
            getBusinessDeveloper("", "teacherBdList");
        </#if>
    });

    function getRegionData(region) {
        $.post("searchRegion.vpage", {}, function (res) {
            if (res.success) {
                var list = res.regionList;
                var htmlTemp;
                var sel_val = $("#" + region).val();
                $("#" + region).html("");
                if (sel_val == 0) {
                    htmlTemp = "<option value='0' selected >请选择</option>";
                } else {
                    htmlTemp = "<option value='0'>请选择</option>";
                }
                $.each(list, function (i, item) {
                    if (item.groupId == sel_val) {
                        htmlTemp += '<option value="' + item.groupId + '" selected >' + item.name + '</option>';
                    } else {
                        htmlTemp += '<option value="' + item.groupId + '" >' + item.name + '</option>';
                    }
                });
                $("#" + region).append(htmlTemp);
            } else {
                alert(res.info);
            }
        })
    }

    function getCity(regionCode, city) {
        var region = {};
        if (regionCode) {
            region = {region: regionCode}
        }
        $.post("searchCity.vpage", region, function (res) {
            if (res.success) {
                var sel_val = $("#" + city).val();
                $("#" + city).html("");
                var list = res.cityList;
                var htmlTemp;
                if (sel_val == 0) {
                    htmlTemp = "<option value='0' selected >请选择</option>";
                } else {
                    htmlTemp = "<option value='0'>请选择</option>";
                }
                $.each(list, function (i, item) {
                    if (item.groupId == sel_val) {
                        htmlTemp += '<option value="' + item.groupId + '" selected >' + item.name + '</option>';
                    } else {
                        htmlTemp += '<option value="' + item.groupId + '" >' + item.name + '</option>';
                    }
                });
                $("#" + city).append(htmlTemp);
            } else {
                alert(res.info);
            }
        })
    }

    function getBusinessDeveloper(cityCode, bd) {
        var city = {};
        if (cityCode) {
            city = {city: cityCode}
        }
        $.post("searchBusinessDeveloper.vpage", city, function (res) {
            if (res.success) {
                var sel_val = $("#" + bd).val();
                $("#" + bd).html("");
                var list = res.bdList;
                var htmlTemp;
                if (sel_val == 0) {
                    htmlTemp = "<option value='0' selected >请选择</option>";
                } else {
                    htmlTemp = "<option value='0'>请选择</option>";
                }
                $.each(list, function (i, item) {
                    if (item.userId == sel_val) {
                        htmlTemp += '<option value="' + item.userId + '" selected >' + item.name + '</option>';
                    } else {
                        htmlTemp += '<option value="' + item.userId + '" >' + item.name + '</option>';
                    }
                });
                $("#" + bd).append(htmlTemp);
            } else {
                alert(res.info);
            }
        })
    }
    $("#teacherRegionList").change(function () {
        var region = this.value;
        getCity(region, "teacherCityList");
    });

    $("#schoolRegionList").change(function () {
        var region = this.value;
        getCity(region, "schoolCityList");
    });

    $("#teacherCityList").change(function () {
        var city = this.value;
        getBusinessDeveloper(city, "teacherBdList");
    });

    // 下载
    $(document).on("click", "#schoolExportBtn", function () {
        if(checkSchool()){
            if($('#schoolDate').val()!=''){
                $("#school_data_report_form").attr({
                    "action": "exportDataReport.vpage",
                    "method": "GET"
                });
                var formElement = document.getElementById("school_data_report_form");
                formElement.submit();
            }else{
                alert('请选择日期')
            }
        }
    });

    $(document).on("click", "#teacherExportBtn", function () {
        if(checkTeacher()){
            if($('#teacherDate').val()!='') {
                $("#teacher_data_report_form").attr({
                    "action": "exportDataReport.vpage",
                    "method": "GET"
                });
                var formElement = document.getElementById("teacher_data_report_form");
                formElement.submit();
            }else{
                alert('请选择日期')
            }
        }
    });
    //检测是否选中
    var checkSchool = function () {
        var flag = true;
        <#if isCountry || isCityManager>
            if(flag){
                if($("#schoolRegionList option:selected").val() == '0'){
                    alert('请选择大区');
                    return false;
                }else if($("#schoolCityList option:selected").val() == '0'){
                    alert('请选择城市');
                    return false;
                }
            }
        </#if>
        return flag;
    };
    var checkTeacher = function () {
        var flag = true;
        <#if isCountry || isCityManager>
            if(flag){
                if($("#teacherRegionList option:selected").val() == '0'){
                    alert('请选择大区');
                    return false;
                }else if($("#teacherCityList option:selected").val() == '0'){
                    alert('请选择城市');
                    return false;
                }else if($("#teacherBdList option:selected").val() == '0'){
                    alert('请选择专员');
                    return false;
                }
            }
        </#if>
        return flag;
    };
</script>
</@layout_default.page>