define(['jquery','knockout','$17','jbox','komapping'],function($,ko,$17){
    var detailViewModel = function(){
        var self = this;
        self.studentList = ko.observableArray([]);
        self.currentStudent = ko.observableArray([]);

        self.sendFlowerParentCount = ko.observable('--'); //已送花的家长
        self.sendIntegralParentCount = ko.observable('--'); //给班级赠送学豆的家长数
        self.clazzIntegralCount = ko.observable('--');  //班级总学豆数
        self.rewardList = ko.observableArray([]);

        self.canSendFlower = ko.observable(true); //是否可以送花
        self.studentSocks = ko.observable('--');
        self.studentIntegral = ko.observable('--');

        //获取孩子列表
        self.getStudent = function(){
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
            if(!$17.isBlank(self.currentStudent())){
                self.getDetail();
                $17.tongji("家长微信感恩节","活动页","学生切换");
            }
        });

        //给老师送花
        self.sendFlowersToTeacherBtn = function(){
            if(self.canSendFlower()){
                $17.loadingStart();
                $.post("/parent/activity/christmasSendFlower.vpage",{studentId : self.currentStudent().id(),source : 'parentWechat'},function(data){
                    if(data.success){
                        $17.jqmHintBox("送花成功");
                        // self.sendFlowerParentCount(self.sendFlowerParentCount() + 1);
                        self.canSendFlower(false);
                    }else{
                        $17.jqmHintBox(data.info);
                    }
                    $17.loadingEnd();
                }).fail(function(){
                    $17.jqmHintBox("数据提交失败");
                    $17.loadingEnd();
                });
            }
            $17.tongji("家长微信感恩节","活动页","给老师送花");
        };

        //补充圣诞袜
        self.sendChristmasSocksBtn = function(){
            setTimeout(function(){
                $17.loadingStart();
                location.href = '/parent/integral/order.vpage?sid='+self.currentStudent().id();
            },200);
            $17.tongji("家长微信感恩节","活动页","补充圣诞袜");
        };

        //根据孩子获取奖励详情列表
        self.getDetail = function(){
            $17.loadingStart();
            $.post("/parent/activity/christmas.vpage",{studentId : self.currentStudent().id()},function(data){
                if(data.success){
                    self.rewardList(data.students);
                    self.sendFlowerParentCount(data.sfpc);
                    self.sendIntegralParentCount(data.cipc);
                    self.clazzIntegralCount(data.cic);
                    self.canSendFlower(data.canSend);
                    self.studentSocks(data.ssc);
                    self.studentIntegral(data.sic);
                }else{
                    $17.jqmHintBox(data.info);
                }
                $17.loadingEnd();
            }).fail(function(){
                $17.jqmHintBox("数据提交失败");
                $17.loadingEnd();
            });
        };

    };
    ko.applyBindings(new detailViewModel());
});