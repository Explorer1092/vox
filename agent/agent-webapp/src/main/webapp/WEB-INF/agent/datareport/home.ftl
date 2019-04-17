<#import "../layout_default.ftl" as layout_default>
<#if type == "city">
    <#assign title = "城市表"/>
<#elseif type =="school">
    <#assign title = "学校表"/>
<#elseif type == "teacher">
    <#assign title = "老师表"/>
</#if>
<@layout_default.page page_title="${title!''}" page_num=10>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">数据查询中，请稍后……</p>
</div>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> ${title!''}</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a href="javascript:void(0);" class="btn btn-primary" id="searchBtn">搜索</a>
                <a href="javascript:void(0);" class="btn btn-primary" id="exportBtn" target="_blank">导出Excel</a>
            </div>
        </div>
        <#assign isCountry = requestContext.getCurrentUser().isCountryManager()!false/>
        <#assign isRegion = requestContext.getCurrentUser().isRegionManager()!false/>
        <#assign isCityManager = requestContext.getCurrentUser().isCityManager()!false/>
        <div class="box-content ">
            <style>
                select {width: 120px;position: relative;z-index: 3} /*label会遮住select的点击区域*/
            </style>
            <form id="data_report_form" <#--action="exportDataReport.vpage"--> method="GET" class="form-horizontal">
                <div class="row-fluid">
                    <div class="span3">
                        <div class="control-group">
                            <label class="control-label">学段</label>
                            <div class="controls">
                                <select id="school_phase" name="phase">
                                    <option value="0">请选择</option>
                                    <#if phase?? && phase?size gt 0>
                                        <#list phase as p >
                                            <option value="${p.value!}">${p.name}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="span3">
                        <div class="control-group">
                            <label class="control-label">观察粒度</label>
                            <div class="controls">
                                <select id="grading" name="grading" class="can-selected">
                                    <option value="0">请选择</option>
                                    <#if grading?? && grading?size gt 0>
                                        <#list grading as g >
                                            <option value="${g.value!}">${g.name}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="span3">
                        <div class="control-group">
                            <#if isCountry >
                                <label class="control-label">大区</label>
                                <div class="controls">
                                    <select id="regionList" name="region" class="can-selected">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </#if>
                        </div>
                    </div>
                </div>
                <div class="row-fluid">
                    <div class="span3">
                        <#if isCountry|| isRegion >
                            <div class="control-group">
                                <label class="control-label">城市</label>
                                <div class="controls">
                                    <select id="cityList" name="city" class="can-selected">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </#if>
                    </div>
                    <#if isCountry || isRegion || isCityManager>
                        <div class="span3">
                            <div class="control-group">
                                <label class="control-label">地区</label>
                                <div class="controls">
                                    <select id="countyList" name="county" class="can-selected">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="span3">
                            <div class="control-group">
                                <label class="control-label">负责人</label>
                                <div class="controls">
                                    <select id="bdList" name="bdId" class="can-selected">
                                        <option value="0">请选择</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </#if>
                </div>

                <div class="row-fluid">
                    <#if type == "city">
                        <div class="span3">
                            <div class="control-group">
                                <label class="control-label">开始日期</label>
                                <div class="controls">
                                    <input type="text" class="input-small focused" style="width: 111px;position: relative;z-index: 3" id="startDate"
                                           name="startDate" value="${startDate!''}">
                                </div>
                            </div>
                        </div>
                    </#if>
                    <div class="span3">
                        <div class="control-group">
                            <label class="control-label"><#if type == "city">结束</#if>日期</label>
                            <div class="controls">
                                <input type="text" class="input-small focused" style="width: 111px;position: relative;z-index: 3" id="endDate"
                                       name="endDate" value="${startDate!''}">
                            </div>
                        </div>
                    </div>
                    <#if type == "city">
                        <div class="span3">
                            <div class="control-group">
                                <label for="" class="control-label">数据展示</label>
                                <div class="controls">
                                    <select name="data_show" id="data_show">
                                        <option value="0">请选择</option>
                                        <option value="1">每日数据</option>
                                        <option value="2">汇总数据</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </#if>
                </div>

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
<script>
    var dataType = "${type!''}";
    $(function () {
        //大区联动
        //获取大区数据
        var getRegionData = function () {
            $.post("searchRegion.vpage", {}, function (res) {
                if (res.success) {
                    var grading = $("#grading").val();
                    var sel_val = $("#regionList").val();
                    $("#regionList").html("");
                    var list = res.regionList;
                    var htmlTemp;
                    if (dataType != "city") {
                        if (sel_val == 0) {
                            htmlTemp = "<option value='0' selected >请选择</option>";
                        } else {
                            htmlTemp = "<option value='0'>请选择</option>";
                        }
                    } else {
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
                    if (dataType != "city") {
                        if (sel_val == 0) {
                            htmlTemp = "<option value='0' selected >请选择</option>";
                        } else {
                            htmlTemp = "<option value='0'>请选择</option>";
                        }
                    } else {
                        if (sel_val == 0) {
                            htmlTemp = "<option value='0' selected >请选择</option>" + "<option value='1'  >全部城市</option>";
                        } else if (sel_val == 1) {
                            htmlTemp = "<option value='0'  >请选择</option>" + "<option value='1' selected >全部城市</option>";
                        } else {
                            htmlTemp = "<option value='0'>请选择</option>" + "<option value='1'>全部城市</option>";
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
                    if (dataType == "teacher") {
                        if (sel_val == 0) {
                            htmlTemp = "<option value='0' selected >请选择</option>";
                        } else {
                            htmlTemp = "<option value='0'>请选择</option>";
                        }
                    } else {
                        if (sel_val == 0) {
                            htmlTemp = "<option value='0' selected >请选择</option>" + "<option value='1'>全部地区</option>";
                        } else if (sel_val == 1) {
                            htmlTemp = "<option value='0' >请选择</option>" + "<option value='1' selected>全部地区</option>";
                        } else {
                            htmlTemp = "<option value='0'>请选择</option>" + "<option value='1' >全部地区</option>";
                        }
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
            $.post("searchBusinessDeveloper.vpage", city, function (res) {
                if (res.success) {
                    var sel_val = $("#bdList").val();
                    $("#bdList").html("");
                    var list = res.bdList;
                    var htmlTemp;
                    if (dataType == "teacher") {
                        if (sel_val == 0) {
                            htmlTemp = "<option value='0' selected >请选择</option>";
                        } else {
                            htmlTemp = "<option value='0'>请选择</option>";
                        }
                    } else {
                        if (sel_val == 0) {
                            htmlTemp = "<option value='0' selected >请选择</option>" + "<option value='1'>全部负责人</option>";
                        } else if (sel_val == 1) {
                            htmlTemp = "<option value='0'>请选择</option>" + "<option value='1' selected> 全部负责人</option>";
                        } else {
                            htmlTemp = "<option value='0'>请选择</option>" + "<option value='1'>全部负责人</option>";
                        }
                    }
                    $.each(list, function (i, item) {
                        if (item.userId == sel_val) {
                            htmlTemp += '<option value="' + item.userId + '" selected >' + item.name + '</option>';
                        } else {
                            htmlTemp += '<option value="' + item.userId + '" >' + item.name + '</option>';
                        }
                    });
                    $("#bdList").append(htmlTemp);
                } else {
                    alert(res.info);
                }
            })
        };

        var initOtherItem = function () {
            var listIds = ["regionList", "cityList", "countyList", "bdList"];
            $.each(listIds, function (i, item) {
                $("#" + item).html("");
                $("#" + item).append('<option value="0">请选择</option>');
                $("#" + item).val(0);
            })
        };

        var forbiddenItems = function () {
            var listIds = ["regionList", "cityList", "countyList", "bdList"];
            $.each(listIds, function (i, item) {
                $("#" + item).attr("disabled", true);
                $("#" + item).val(0);
            })
        };

        $("#grading").change(function () {

            //粒度改变,清除之下
            initOtherItem();

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
            if ($("#grading").val() == 5) {
                //禁止其他联动条件
                forbiddenItems();
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

        var initTableStyle = function () {
            var width = 80;
            $("#DataTables_Table_0").find("th").css({
                "width": width + "px"
            });
            var colNum = $("#DataTables_Table_0").find("th").length,
                    totalLength = width * colNum + "px";

            $("#DataTables_Table_0").css("width", totalLength);

            $("#DataTables_Table_0_wrapper").css({
                "width": "100%",
                "overflow-x": "auto"
            })
        };
        initTableStyle();

        var searchFlag = true;

        // 搜索
        $(document).on("click","#searchBtn",function(){
            if(searchFlag){
                $("#loadingDiv").show();
                searchFlag = false;
                var formElement = document.getElementById("data_report_form");
                var postData = new FormData(formElement);
                $.ajax({
                    url: "search_data_report.vpage",
                    type: "POST",
                    data: postData,
                    processData: false,  // 告诉jQuery不要去处理发送的数据
                    contentType: false,
                    success: function (res) {
                        $("#loadingDiv").hide();
                        searchFlag = true;
                        if (res.success) {
                            var table = $('#DataTables_Table_0').dataTable();
                            table.fnClearTable(); //清除表格的数据
                            $('#DataTables_Table_0').dataTable().fnAddData(res.data); //添加添加新数据
                            if (res.rowCount && res.rowCount > 500) {
                                alert("总共找到" + res.rowCount + "条数据,最多展示500条数据");
                            }
                        } else {
                            alert(res.info);
                        }
                    },
                    error: function (e) {
                        console.log(e);
                        $("#loadingDiv").hide();
                        searchFlag = true;
                    }
                });
            } else {
                alert("等待搜索结果,请不要频繁操作");
                searchFlag = true;
            }
        });

        // 下载
        $(document).on("click", "#exportBtn", function () {
            $("#data_report_form").attr({
                "action": "exportDataReport.vpage",
                "method": "GET"
            });
            var formElement = document.getElementById("data_report_form");
            formElement.submit();
        });

        //时间控件
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
