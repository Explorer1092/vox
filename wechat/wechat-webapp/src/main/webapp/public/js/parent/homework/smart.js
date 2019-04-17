define(["jquery", "knockout", 'userpopup', '$17','menu'],function($, knockout, userpopup, $17){
    //依赖初始化
    userpopup.selectStudent("smart");

    var smartModel = function(){
        var self = this;
        self.smartData = knockout.observableArray([]);
        self.currentDate = nowDate;
        self.firstDate = knockout.observable();
        self.studentId = knockout.observable(0);
        self.isToday = knockout.observable(false);
        self.isGraduate = knockout.observable(false);

        //贡献班级学豆
        self.giveCoinBtn = function(){
            location.href = '/parent/integral/order.vpage?sid='+self.studentId;
        }

    };

    var viewModel = new smartModel([]);
    knockout.applyBindings(viewModel);


    return{
        loadMessageById: function (sid) {
            viewModel.studentId = sid;
            //获取作业信息
            $17.ajax({
                url: '/parent/homework/loadsmart.vpage',
                showLoading: true,
                data: {
                    studentId: sid
                },
                success: function (data) {
                    if(data.smart.length > 0){
                        viewModel.firstDate(data.smart[0].date);
                    }
                    viewModel.smartData(data.smart);
                    viewModel.isToday(viewModel.currentDate == viewModel.firstDate());
                    viewModel.isGraduate(data.isGraduate);
                },
                error: function (data) {

                }
            });

        }
    };

});