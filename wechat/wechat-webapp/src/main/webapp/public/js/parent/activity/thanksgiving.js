define(['jquery','knockout','$17','jbox','komapping'],function($,ko,$17){
    var detailViewModel = function(){
        var self = this;
        self.studentList = ko.observableArray([]); //学生列表
        self.currentStudent = ko.observable(); //当前已选择的学生
        self.currentDayTarget = ko.observable('暂无'); //学生今天的目标
        self.thanksDetail = ko.observableArray([]); //感谢详情列表

        self.thanksParentCount = ko.observable(0); //感谢老师家长人数
        self.integralParentCount = ko.observable(0); //贡献学豆的人数
        self.integralCount = ko.observable(0); //贡献的总学豆数

        //初始化学生列表
        self.studentListInit = function(){
            $.get('/parent/activity/getStudentList.vpage',function(data){
                if(data.success){
                    if(data.students.length > 0){
                        self.studentList(ko.mapping.fromJS(data.students)());
                    }else{
                        location.href = '/parent/ucenter/bindchild.vpage';
                    }
                }else{
                    $17.jqmHintBox('孩子列表获取失败');
                }
            });
        }();

        //学生切换
        self.currentStudent.subscribe(function() {
            self.getTargetDetail();
        });

        //根据当前学生获取列表
        self.getTargetDetail = function(){
            $17.ajax({
                url : '/parent/activity/getthankstarget.vpage',
                data : {
                    studentId : self.currentStudent().id()
                },
                showLoading : true,
                success : function(data){
                    if(data.success){
                        self.thanksParentCount(data.parentCount);
                        self.integralParentCount(data.parentIntegralCount);
                        self.integralCount(data.integralCount);
                        if(data.target){
                            self.currentDayTarget(data.target);
                        }else{
                            self.currentDayTarget('暂无');
                        }
                        self.thanksDetail(data.rankInfo);
                    }
                },
                error : function(){
                    $17.jqmHintBox('数据加载失败');
                }
            });
        };

        //感谢老师
        self.thanksTeacherBtn = function(){
            $17.loadingStart();
            $.post('/parent/activity/thanksteacher.vpage',{studentId : self.currentStudent().id(), source : 'parentWechat'},function(data){
                if(data.success){
                    self.thanksParentCount(self.thanksParentCount() + 1);
                    $17.jqmHintBox('感谢成功');
                }else{
                    $17.jqmHintBox(data.info);
                }
                $17.loadingEnd();
            }).fail(function(){
                $17.jqmHintBox('感谢失败');
                $17.loadingEnd();
            });
        }
    };
    ko.applyBindings(new detailViewModel());
});