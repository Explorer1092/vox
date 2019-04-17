define(["$17", "knockout", "logger", "komapping", "weuijs"], function ($17, ko, logger) {
    "use strict";
    function offlineLog(logJson) {
        logJson = $.extend(true,{
            app    : "teacher",
            module : "m_2M8WOHc5"
        },logJson);
        logger.log(logJson);
    }
    function OfflineHomeworkShare(params){
        var self = this,opts = $.isPlainObject(params) ? params : {};
        self.from = ko.observable(opts.from || "");
    }

    OfflineHomeworkShare.prototype = {
        constructor : OfflineHomeworkShare,
        downloadApp : function(){
            var self = this;
            offlineLog({
                op : "o_VojymWox"
            });
            setTimeout(function(){
                location.href = "//wx.17zuoye.com/download/17teacherapp?cid=300124";
            },200);
        },
        fowardReport: function () {
            var self = this;
            offlineLog({
                op : "o_dZHDAlyV"
            });
            setTimeout(function(){
                location.href = "/teacher/homework/report/history.vpage";
            },200);
        }
    };
    $.showLoading();
    var offlineHomework = new OfflineHomeworkShare($17.getQuery("from")),
        nodeList = document.getElementsByClassName("offlineHomeworkShare");
    for(var t = 0,tLen = nodeList.length; t < tLen; t++){
        ko.applyBindings(offlineHomework,nodeList[t]);
    }
    offlineLog({
        op : "o_UucHG8GR"
    });
    $.hideLoading();
});