$(function(){
    function HomeworkList(option){
        var self = this;
        self.hkLoading = ko.observable(false);
        self.homeworkList = ko.observableArray([]);
        self.loadHomeworkList = function(){
            self.hkLoading(true);

            var paramData = {
                subject : constantObj.subject
            },url = "/teacher/vacation/report/vacationhistory.vpage";
            $.get(url,paramData,function(data){
                if(data.success){
                    var _content = data.newVacationHomeworkHistoryList || [];
                    self.homeworkList(ko.mapping.fromJS(_content)());
                }else{
                    data.errorCode !== "200" && $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : url,
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : $uper.env
                    });
                }
                self.hkLoading(false);
            });
        };
        self.viewReport = function(){
            var kh = this;
            $17.voxLog({
                module : "m_Ri7qOfBC",
                op     : "view_click",
                s0     : kh.subject()
            });
            setTimeout(function(){
                location.href = '/teacher/vacation/report/clazzreport.vpage?packageId=' + kh.packageId();
            },200);
        };
        self.deleteHomework = function(){
            var vacationHomework = this;
            if(!vacationHomework.ableToDelete || vacationHomework.ableToDelete()){
                $.prompt("<p>确定删除吗？</p><p>若假期作业已开始，删除后学生的成绩和做题记录会被删除！</p>", {
                    title  : "删除作业",
                    focus  : 1,
                    buttons: { "取消": false, "确认删除": true },
                    submit : function(e, v){
                        if(v){
                            var url = "/teacher/vacation/delete.vpage",param = {
                                vacationPackageId : vacationHomework.packageId()
                            };
                            $.post(url,param,function(res){
                                if(res.success){
                                    $17.alert("删除成功",function(){
                                        window.location.reload();
                                    });
                                }else{
                                    $17.alert(res.info || "删除失败");
                                    res.errorCode !== "200" && $17.voxLog({
                                        module : "API_REQUEST_ERROR",
                                        op     : "API_STATE_ERROR",
                                        s0     : url,
                                        s1     : $.toJSON(res),
                                        s2     : $.toJSON(param),
                                        s3     : $uper.env
                                    });
                                }
                            });
                        }
                    }
                });
            }else{
                $17.alert("假期作业已经开始，无法删除");
            }
            $17.voxLog({
                module : "m_Ri7qOfBC",
                op     : "delete_click",
                s0     : vacationHomework.subject()
            });

        };
        self.run = function(){
            self.loadHomeworkList();
            $17.voxLog({
                module : "m_Ri7qOfBC",
                op     : "page_detail_report",
                s0 : constantObj.subject
            });
        };
    }

    var _homeworkList = new HomeworkList();
    _homeworkList.run();
    ko.applyBindings(_homeworkList,document.getElementById("vacationHomeworkList"));
});

