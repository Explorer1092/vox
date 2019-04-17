/**
 * Common utilities for market; eg. number, string, array, dater, activtaor.
 * @author Jia HuanYin
 * @since 2015/11/23
 */

function validNumber(value) {
    return !isNaN(value) && parseInt(value) > 0;
}

function nullString(value) {
    return value == undefined || value == null;
}

function emptyString(value) {
    return nullString(value) || value === "";
}

function blankString(value) {
    return $.trim(value) === "";
}

function getQuery(item){
    var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
    return svalue ? decodeURIComponent(svalue[1]) : '';
}

function notBlankString(value) {
    return !blankString(value);
}

function blankStringOrZero(value) {
    return $.trim(value) === "" || $.trim(value) ==="0";
}

function emptyArray(source) {
    return source == null || source.length < 1;
}

function stringArray(source) {
    if (emptyArray(source)) {
        return "";
    }
    var values = "";
    for (var i in source) {
        values += source[i] + ",";
    }
    return values;
}

function stringLength(str){
    if (str == null) return 0;
    if (typeof str != "string"){
        str += "";
    }
    return str.length;
}

//验证是否手机号
function isMobile(value){
    value = value + "";
    //严格判定
    var _reg = /^0{0,1}(13[4-9]|15[7-9]|15[0-2]|18[7-8])[0-9]{8}$/;
    //简单判定
    var reg = /^1[0-9]{10}$/;
    if(!value || value.length != 11 || !reg.test(value)){
        return false;
    }
    return true;
}

var imenu = {};
imenu.show = function () {
    $("#iMenu").show("fast");
};
imenu.cancel = function () {
    $("#iMenu").hide("fast");
};

var dater = {};
dater.render = function () {
    $(".date").datepicker({
        dateFormat: "yy-mm-dd",
        monthNames: ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
        monthNamesShort: ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: true,
        changeYear: true
    });
};
dater.parse = function (format, value) {
    if (blankString(format) || blankString(value)) {
        return null;
    }
    try {
        return $.datepicker.parseDate(format, value);
    } catch (e) {
    }
    return null;
};

var activtaor = {};
activtaor.bind = function () {
    $(".activtaor").click(function () {
        activtaor.active(this);
    });
};
activtaor.active = function (node) {
    $(".activtaor").removeClass("active");
    var value = $(node).addClass("active").attr("active-value");
    $(".activity").hide();
    $(".activity[active-value=" + value + "]").show();
};

var viewer = {};
viewer.view = function (id) {
    $("#view-" + id).toggle("fast");
};

var performance = {};
performance.render = function () {
    var schoolLevelType = $.cookie("SCHOOL_LEVEL_TYPE");
    var schoolLevel = $.cookie("SCHOOL_LEVEL");
    if (schoolLevelType != null && schoolLevelType == "MIDDLE" || schoolLevel != null && schoolLevel == "MIDDLE") {
        $(".stu-sl").hide();
    }
};
Date.prototype.Format = function (fmt) { //author: meizz
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
};
var getParamFromUrl = function(url,name) {
    var reg = new RegExp("[&?#]" + name + "=([^&?#]+)", "i");
    var r = url.match(reg);
    if (r != null) return unescape(r[1]); return null;
};
var getUrlParam = function(name) {
    return this.getParamFromUrl(window.location.href,name);
};
//获取本月剩余天数
var getRestOfMonthDay = function (date){
    var year = date.getFullYear();
    var month = date.getMonth()+1;
    var d = new Date(year, month, 0);
    var day = d.getDate(); //本月天数
    return day - date.getDate(); //本月剩余天数
};