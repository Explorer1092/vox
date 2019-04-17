(function(window,ko,stuWeekDetail){
    'use strict';

    var studentweekDetail =function(){
        this.weekPlans = ko.observableArray([]);
        this.packageId = ko.observable("");
        this.clazzName = ko.observable("");
        this.studentName = ko.observable("");
        this.subject=ko.observable("");
        this.init();
    };

    studentweekDetail.prototype = {
        constructor : studentweekDetail,
        init : function(){
            var self = this;
            var packageId_param = $17.getQuery("packageId");
            var studentId_param = $17.getQuery("studentId");
            self.packageId(packageId_param);
            self.subject(stuWeekDetail.subject);
            $17.voxLog({
                module:"m_Ri7qOfBC",
                op : "page_stu_winter_vacation_list",
                s0 : stuWeekDetail.subject
            });
            $.get('studentpackagereport.vpage?packageId='+packageId_param+'&studentId='+studentId_param,function(data){
                if(data.success){
                    self.weekPlans(data.weekPlans);
                    self.clazzName(data.clazzName);
                    self.studentName(data.studentName);
                }
            })
        },
        viewDetail :function(element,self){
            var homeworkId =$(arguments[0]).data("num");
            $17.voxLog({
                module:"m_Ri7qOfBC",
                op : "stu_view_click",
                s0 : stuWeekDetail.subject
            });
            setTimeout(function(){
                location.href = '/teacher/vacation/report/studentdaydetail.vpage?homeworkId='+ homeworkId;
            },200);
        }
    };

    ko.applyBindings(new studentweekDetail(),document.getElementById("mainContent"));


})(window,ko,stuWeekDetail);
