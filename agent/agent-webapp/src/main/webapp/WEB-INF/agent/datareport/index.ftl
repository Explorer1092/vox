<#import "../layout_default.ftl" as layout_default>
<#if type == "city">
    <#assign title = "城市表"/>
<#elseif type =="school">
    <#assign title = "学校表"/>
<#elseif type == "teacher">
    <#assign title = "老师表"/>
</#if>
<@layout_default.page page_title="${title!''}" page_num=10>
<div class="row-fluid sortable ui-sortable">

    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> ${title!''}</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="exportDataReport.vpage" class="btn btn-primary">导出${title!''}</a>
            </div>
        </div>
        <#assign isCountry = requestContext.getCurrentUser().isCountryManager()!false/>
        <#assign isRegion = requestContext.getCurrentUser().isRegionManager()!false/>
        <#assign isCityManager = requestContext.getCurrentUser().isCityManager()!false/>
        <div class="box-content ">
            <form id="data_report_form" action="/datareport/report/index.vpage" method="GET">
                <label class="control-label" style="float:left;margin-left: 16px;margin-top: 5px;margin-right: 15px;">学段</label>
                <select id="school_phase" name="phase" style="float:left;margin-left: 16px;margin-right: 2px;">
                    <option value="0">请选择</option>
                    <option value="1" <#if phase == 1>selected</#if>>小学</option>
                    <option value="2" <#if phase == 2>selected</#if>>中学</option>
                </select>
                <label class="control-label" style="float:left;margin-left: 16px;margin-top: 5px;margin-right: 15px;">观察粒度</label>

                <select id="grading" name="grading" class="can-selected" data-selected="${grading!0}" style="float:left;margin-left: 16px;margin-right: 2px;">
                    <option value="0">请选择</option>
                    <#if isCountry>
                        <option value="1" <#if grading == 1>selected</#if>>大区</option>
                        <option value="2" <#if grading == 2>selected</#if>>城市</option>
                        <option value="3" <#if grading == 3>selected</#if>>行政区</option>
                        <option value="4" <#if grading == 4>selected</#if>>专员</option>
                    </#if>

                    <#if isRegion>
                        <option value="2" <#if grading == 2>selected</#if>>城市</option>
                        <option value="3" <#if grading == 3>selected</#if>>行政区</option>
                        <option value="4" <#if grading == 4>selected</#if>>专员</option>
                    </#if>

                    <#if isCityManager>
                        <option value="3" <#if grading == 3>selected</#if>>行政区</option>
                        <option value="4" <#if grading == 4>selected</#if>>专员</option>
                    </#if>
                </select>
                <#if isCountry >
                    <label class="control-label" style="float:left;margin-left: 16px;margin-top: 5px;margin-right: 15px;">大区</label>
                    <select id="regionList" name="region" class="can-selected"
                            data-selected="${region!0}" style="float:left;margin-left: 16px;margin-right: 2px;">
                        <option value="0">请选择</option>
                        <option value="1">全部大区</option>
                    </select>
                </#if>

                <#if isCountry|| isRegion >
                    <label class="control-label" style="float:left;margin-left: 16px;margin-top: 5px;margin-right: 15px;">城市</label>
                    <select id="cityList" name="city" class="can-selected" data-selected="${city!0}" style="float:left;margin-left: 16px;margin-right: 2px;">
                        <option value="0">请选择</option>
                        <option value="1">全部城市</option>
                    </select>
                </#if>
                <#if isCountry || isRegion || isCityManager>
                    <label class="control-label" style="float:left;margin-left: 16px;margin-top: 5px;margin-right: 15px;">行政区</label>
                    <select id="countyList" name="county" class="can-selected"
                            data-selected="${county!0}" style="float:left;margin-left: 16px;margin-right: 2px;">
                        <option value="0">请选择</option>
                        <option value="1">全部行政区</option>
                    </select>
                    <label class="control-label" style="float:left;margin-left: 16px;margin-top: 5px;margin-right: 15px;">负责人</label>
                    <select id="bdList" name="bdId" class="can-selected" data-selected="${bdId!0}" style="float:left;margin-left: 16px;margin-right: 2px;">
                        <option value="0">请选择</option>
                        <option value="1">全部负责人</option>
                    </select>
                </#if>
                <#if type == "city">
                    <label class="control-label" for="startDate" style="float:left;margin-left: 16px;margin-top: 5px;margin-right: 15px;">开始日期</label>
                    <input type="text" class="input-small" id="startDate" name="startDate" style="float:left;margin-left: 16px;margin-right: 15px;" value="${startDate!''}">
                </#if>
                <label class="control-label" for="endDate" style="float:left;margin-left: 16px;margin-top: 5px;margin-right: 15px;">结束日期</label>
                <input type="text" class="input-small" id="endDate" name="endDate" style="float:left;margin-left: 16px;margin-right: 15px;" value="${endDate!''}">

                <input type="submit" style="float:left" value="搜索">
                <input name="type" type="hidden" value="${type!''}"/>
            </form>
            <#if type == "city">
                <#include "city.ftl">
            <#elseif type =="school">
                <#include "school.ftl">
            <#elseif type == "teacher">
                <#include "teacher.ftl">
            </#if>
        </div>
    </div>
</div>
<script type="application/javascript">
        <#if error??>
            alert("出错啦！ ${error!}");
        </#if>

    //获取大区数据
    var getRegionData = function () {
        $.post("searchRegion.vpage", {}, function (res) {
            if (res.success) {
                var grading = $("#grading").val();
                var sel_val = $("#regionList").val();
                $("#regionList").html("");
                var list = res.regionList;
                var htmlTemp;
                if(grading == 3 || grading ==4){
                    if (sel_val == 0) {
                        htmlTemp = "<option value='0' selected >请选择</option>";
                    } else {
                        htmlTemp = "<option value='0'>请选择</option>";
                    }
                }else {
                    if (sel_val == 0) {
                        htmlTemp = "<option value='0' selected >请选择</option>" + "<option value='1'  >全部大区</option>";
                    } else if (sel_val == 1) {
                        htmlTemp = "<option value='0'>请选择</option>" + "<option value='1' selected >全部大区</option>";
                    } else {
                        htmlTemp = "<option value='0'>请选择</option>" + "<option value='1'>全部大区</option>";
                    }
                }

                $.each(list, function (i, item) {
                    if (item.groupId == sel_val) {
                        htmlTemp += '<option value="' + item.groupId + '" selected >' + item.name + '</option>';
                    } else {
                        htmlTemp += '<option value="' + item.groupId + '" >' + item.name + '</option>';
                    }
                });
                $("#regionList").append(htmlTemp);
            } else {
                alert(res.info);
            }
        });
    };

    var getCity = function (regionCode) {
        var region = {};
        if (regionCode) {
            region = {region: regionCode}
        }
        $.post("searchCity.vpage", region, function (res) {
            if (res.success) {
                var grading = $("#grading").val();
                var sel_val = $("#cityList").val();
                $("#cityList").html("");
                var list = res.cityList;
                var htmlTemp;
                if(grading == 3 || grading ==4){
                    if (sel_val == 0) {
                        htmlTemp = "<option value='0' selected >请选择</option>";
                    } else {
                        htmlTemp = "<option value='0'>请选择</option>";
                    }
                }else{
                    if (sel_val == 0) {
                        htmlTemp = "<option value='0' selected >请选择</option>"+"<option value='1'  >全部城市</option>";
                    } else if(sel_val ==1){
                        htmlTemp = "<option value='0'  >请选择</option>"+"<option value='1' selected >全部城市</option>";
                    }else {
                        htmlTemp = "<option value='0'>请选择</option>"+"<option value='1'>全部城市</option>";
                    }
                }
                $.each(list, function (i, item) {
                    if (item.groupId == sel_val) {
                        htmlTemp += '<option value="' + item.groupId + '" selected >' + item.name + '</option>';
                    } else {
                        htmlTemp += '<option value="' + item.groupId + '" >' + item.name + '</option>';
                    }
                });
                $("#cityList").append(htmlTemp);
            } else {
                alert(res.info);
            }
        })
    };

    var getCounty = function (cityCode) {
        var city = {};
        if (cityCode) {
            city = {city: cityCode}
        }
        $.post("searchCounty.vpage", city, function (res) {
            if (res.success) {
                var sel_val = $("#countyList").val();
                $("#countyList").html("");
                var list = res.countyList;
                var htmlTemp;
                if (sel_val == 0) {
                    htmlTemp = "<option value='0' selected >请选择</option>"+"<option value='1'>全部行政区</option>";
                }else if(sel_val ==1){
                    htmlTemp = "<option value='0' >请选择</option>"+"<option value='1' selected>全部行政区</option>";
                } else {
                    htmlTemp = "<option value='0'>请选择</option>"+"<option value='1' >全部行政区</option>";
                }
                $.each(list, function (i, item) {
                    if (item.regionCode == sel_val) {
                        htmlTemp += '<option value="' + item.regionCode + '" selected >' + item.name + '</option>';
                    } else {
                        htmlTemp += '<option value="' + item.regionCode + '" >' + item.name + '</option>';
                    }
                });
                $("#countyList").append(htmlTemp);
            } else {
                alert(res.info);
            }
        })
    };

    var getBusinessDeveloper = function (cityCode) {
        var city = {};
        if (cityCode) {
            city = {city: cityCode}
        }
        $.post("searchCounty.vpage", city, function (res) {
            if (res.success) {
                var sel_val = $("#bdList").val();
                $("#bdList").html("");
                var list = res.countyList;
                var htmlTemp;
                if (sel_val == 0) {
                    htmlTemp = "<option value='0' selected >请选择</option>"+"<option value='1'>全部负责人</option>";
                }else if(sel_val == 1){
                    htmlTemp = "<option value='0'>请选择</option>"+"<option value='1' selected> 全部负责人</option>";
                } else {
                    htmlTemp = "<option value='0'>请选择</option>"+"<option value='1'>全部负责人</option>";
                }
                $.each(list, function (i, item) {
                    if (item.regionCode == sel_val) {
                        htmlTemp += '<option value="' + item.regionCode + '" selected >' + item.name + '</option>';
                    } else {
                        htmlTemp += '<option value="' + item.regionCode + '" >' + item.name + '</option>';
                    }
                });
                $("#bdList").append(htmlTemp);
            } else {
                alert(res.info);
            }
        })
    };
    $("#grading").change(function () {
        if ($("#grading").val() == 0) {
            $("#regionList").val(0);
            $("#regionList").attr("disabled", true);
            $("#cityList").val(0);
            $("#cityList").attr("disabled", true);
            $("#countyList").val(0);
            $("#countyList").attr("disabled", true);
            $("#bdList").val(0);
            $("#bdList").attr("disabled", true);
        }
        if ($("#grading").val() == 1) {
            $("#cityList").val(0);
            $("#regionList").attr("disabled", false);
            $("#countyList").val(0);
            $("#bdList").val(0);
            $("#cityList").attr("disabled", true);
            $("#countyList").attr("disabled", true);
            $("#bdList").attr("disabled", true);

        }
        if ($("#grading").val() == 2) {
            $("#countyList").val(0);
            $("#bdList").val(0);
            $("#regionList").attr("disabled", false);
            $("#cityList").attr("disabled", false);
            $("#countyList").attr("disabled", true);
            $("#bdList").attr("disabled", true);
        }
        if ($("#grading").val() == 3) {
            $("#bdList").val(0);
            $("#regionList").attr("disabled", false);
            $("#cityList").attr("disabled", false);
            $("#countyList").attr("disabled", false);
            $("#bdList").attr("disabled", true);

            $("#regionList>option[value=1]").remove();
            $("#cityList>option[value=1]").remove();

        }
        if ($("#grading").val() == 4) {
            $("#countyList").val(0);
            $("#regionList").attr("disabled", false);
            $("#cityList").attr("disabled", false);
            $("#countyList").attr("disabled", true);
            $("#bdList").attr("disabled", false);

            $("#regionList>option[value=1]").remove();
            $("#cityList>option[value=1]").remove();
        }
        <#if isCountry>
            getRegionData();
        </#if>

        <#if isRegion>
            getCity();
        </#if>

        <#if isCityManager>
            getCounty();
            getBusinessDeveloper();
        </#if>
    });
    $("#regionList").change(function () {
        var region = this.value;
        getCity(region);
    });

    $("#cityList").change(function () {
        var city = this.value;
        getCounty(city);
        getBusinessDeveloper(city);
    });

    $(function () {
        $.each($(".can-selected"), function (i, item) {
            $("#item").val($(item).data("selected"));
        });

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
                var endDate = $("#endDate").val();
                if (endDate != "" && selectedDate > endDate) {
                    alert("开始时间不能小于结束时间");
                    $("#endDate").val("");
                }
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
                var startDate = $("#startDate").val();
                if (startDate > selectedDate) {
                    alert("开始时间不能小于结束时间");
                    $("#endDate").val("");
                }
            }
        });
    });
</script>
</@layout_default.page>
