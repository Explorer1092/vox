(function(window,ko,clazReportObj){
    'use strict';

    var vacationReport = function(){
        this.clazzName = ko.observable("");
        this.totalStudentNum = ko.observable(0);
        this.finishedStudentNum = ko.observable(0);
        this.beginVacationHomeworkNum = ko.observable(0);
        this.vacationHomeworkStudentPanoramas = ko.observableArray([]);
        this.subject = ko.observable("");
        this.init();
    };

    vacationReport.prototype ={
        constructor:vacationReport,
        init : function(){
            var self = this;
            var param = $17.getQuery("packageId");
            self.subject(clazReportObj.subject);
            $17.voxLog({
                module: "m_Ri7qOfBC",
                op    : "class_page_detail_report",
                s0    : clazReportObj.subject
            });
            $.get("packagereport.vpage?packageId="+param,function(data){

                if(data.success){
                    self.finishedStudentNum(data.vacationHomeworkPackagePanorama.finishedStudentNum);
                    self.totalStudentNum(data.vacationHomeworkPackagePanorama.totalStudentNum);
                    self.beginVacationHomeworkNum(data.vacationHomeworkPackagePanorama.beginVacationHomeworkNum);
                    self.vacationHomeworkStudentPanoramas(data.vacationHomeworkPackagePanorama.vacationHomeworkStudentPanoramas);
                    //获取学生作业情况
                    self.clazzName(data.vacationHomeworkPackagePanorama.clazzName);
                }else{
                    $17.alert("班级学生作业获取失败,请稍后再试~");
                }
            })
        },
        viewPersonReport : function(element,self){
            var stuId,packageid;
            packageid = $17.getQuery("packageId");
            if(arguments[2].studentId){
                stuId = arguments[2].studentId;
            }
            $17.voxLog({
                module: "m_Ri7qOfBC",
                op    : "class_view_click",
                s0    : clazReportObj.subject
            });
            setTimeout(function(){
                location.href = '/teacher/vacation/report/studentweekdetail.vpage?packageId='+packageid+'&studentId='+stuId;
            },200);
        }
    };

    ko.applyBindings(new vacationReport(),document.getElementById("mcontent"));
})(window,ko,clazReportObj);