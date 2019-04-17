;(function(){

    var newexamReport = function(){

        this.webLoading     = ko.observable(true);
        this.subject        = constantObj.subject;
        this.clazzList      = ko.observableArray([]);
        this.currentGroupId = ko.observable("");
        this.examList       = ko.observableArray([]);
        this.examListCache  = {};
        this.termPage       = null;
        this.currentPage    = 1;

        this.initClazz();

        $17.voxLog({
            module : "m_jr0GAJRd",
            op     : "page_exam_report_list"
        });
    };
    newexamReport.prototype = {

        constructor : newexamReport,
        
        initClazz : function () {
            var self = this;

            $.get("/teacher/newexam/report/independent/clazzlist.vpage",{

                subject : self.subject
            },function (res) {

                if(res.success && res.clazzList && res.clazzList.length > 0){

                    self.clazzList(res.clazzList);
                    self.currentGroupId(res.clazzList[0].groupId);
                    self.getIndependentList();
                }else{

                    $17.alert(res.info || "班级数据获取失败，请稍后再试～");
                };
            });
        },

        changeClazz : function (self) {

            self.currentPage = 1;
            self.currentGroupId(this.groupId);
            self.getIndependentList();
        },

        getIndependentList : function () {
            var self = this;

            if(self.examListCache[self.currentGroupId()] && self.currentPage == 1){

                self.examList(self.examListCache[self.currentGroupId()].content || []);
                self.termPage.setPage(self.currentPage, self.examListCache[self.currentGroupId()].totalPages);
                self.webLoading(false);

                return false;
            };

            $.post("/teacher/newexam/report/independent/list.vpage",{
                groupId     : self.currentGroupId(),
                currentPage : self.currentPage-1
            },function (res) {

                if(res.success && res.pageable){

                    self.examList(res.pageable.content || []);
                    if(self.currentPage == 1){
                        self.examListCache[self.currentGroupId()] = res.pageable;
                    };

                    if(!self.termPage){
                        self.termPage  = $17.pagination.initPages({
                            totalPage   : res.pageable.totalPages,
                            currentPage : self.currentPage,
                            pageSize    : 10,
                            pageClickCb : function(pageNo){
                                self.currentPage = pageNo;
                                self.getIndependentList();
                            }
                        });
                    }else{
                        self.termPage.setPage(self.currentPage, res.pageable.totalPages)
                    }

                    self.webLoading(false);
                }else{

                    $17.alert(res.info || "数据获取失败，请稍后再试～");
                }
            });
        },
        deleteExam : function (self) {
            var that = this;

            $.prompt("<div style='text-align: center;'>此操作将会删除已发布至学生端的测试，确认删除？</div>",{
                title    : "系统提示",
                buttons  : { "删除测试": true },
                position : { width: 500 },
                submit   : function(){

                    $.post("/teacher/newexam/delete.vpage",{
                        subject   : self.subject,
                        newExamId : that.newExamId
                    },function (res) {
                        if(res.success){

                            window.location.reload();
                        }else{

                            $17.alert(res.info || "删除失败，请稍后再试～");
                        }
                    });
                }
            });
        },
        detailReview : function () {

            window.location.href = "/teacher/newexam/report/independent/detail.vpage?newExamId=" + this.newExamId;
            $17.voxLog({
                module : "m_jr0GAJRd",
                op     : "page_exam_report_detail"
            });
        }
    }

    ko.applyBindings(new newexamReport(), document.getElementById('independentReport'));
}());