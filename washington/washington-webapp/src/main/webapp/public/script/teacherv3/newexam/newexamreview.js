;(function ($17,ko) {
    var newexamReview = function () {

        this.checkedGroups   = [];
        this.paperId         = ko.observable("");
        this.paperName       = ko.observable("");
        this.questionCount   = ko.observable("");
        this.durationMinutes = ko.observable(0);
        this.questions       = ko.observableArray([]);
        this.currentQuestionCount  = ko.observable(3);

        this.initExamCore();
    };

    newexamReview.prototype = {
        constructor : newexamReview,

        init : function (param) {
            var self = this;
            this.checkedGroups = param.checkedGroups;
            this.paperId(param.paperId);
            this.paperName(param.paperName);
            this.questionCount(param.questionCount);
            this.durationMinutes(param.durationMinutes);
            this.currentQuestionCount(3);

            $.get("/teacher/newexam/preview.vpage",{
                paperId : self.paperId()
            },function (res) {
                if(res.success){

                    self.questions(res.questions || []);
                }else{
                    $17.alert("试卷数据获取失败，请稍后再试～");
                }
            });
        },

        backToArrange : function () {

            $("#newexamreview").hide();
            $("#arrangenewexam").show();
        },

        saveNewExam : function(){
            var self        = this,
                _groupNames = "",
                _groupIds   = "";
            $.each(self.checkedGroups,function(){
                _groupNames += this.groupName + ",";
                _groupIds   += this.groupId + ",";
            });

            $.prompt(template("t:confirm",{}),{
                title : "布置专项测试",
                buttons : {},
                position: { width : 760},
                loaded : function(){

                    var node = document.getElementById("saveMathDialog");
                    ko.cleanNode(node);
                    var confirmModule = $17.homeworkv3.getConfirmModule();
                    confirmModule.initialise({
                        paperId         : self.paperId(),
                        groupNames      : _groupNames.slice(0,-1),
                        groupIds        : _groupIds.slice(0,-1),
                        durationMinutes : self.durationMinutes(),
                        startDateTime   : constantObj.currentDateTime,
                        endDateTime     : constantObj.endDate
                    });
                    ko.applyBindings(confirmModule, node);
                }
            });
        },
        loadQuestionContent : function (data,index) {
            var $mathExamImg = $("#subjective_" + data.id + index);
            $mathExamImg.empty();
            $("<div></div>").attr("id","examImg_" + data.id + index).appendTo($mathExamImg);
            var node = document.getElementById("examImg_" + data.id + index);
            vox.exam.render(node, 'normal', {
                ids       : [data.id],
                imgDomain : constantObj.imgDomain,
                env       : constantObj.env,
                domain    : constantObj.domain
            });

            return null;
        },
        showMoreQuestion: function () {

            this.currentQuestionCount(this.currentQuestionCount()+3);
        },
        initExamCore : function(){//初始化加载应试
            try{
                vox.exam.create(function(data){
                    if(data.success){
                        //成功
                    }else{
                        $17.voxLog({
                            module: 'vox_exam_create',
                            op:'create_error'
                        });
                    }
                },false,{
                    imgDomain : constantObj.imgDomain,
                    env       : constantObj.env,
                    domain    : constantObj.domain
                });
            }catch(exception){
                $17.voxLog({
                    module: 'vox_exam_create',
                    op: 'examCoreJs_error',
                    errMsg: exception.message,
                    userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
                });
            }
        }
    };

    var obj = new newexamReview();
    ko.applyBindings(obj, document.getElementById('newexamreview'));

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        newexamReview : obj.init.bind(obj)
    });

}($17,ko));


(function($17,ko){

    var h = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'];
    var m = [
        '00', '01', '02', '03', '04', '05', '06', '07', '08', '09',
        '10', '11', '12', '13', '14', '15', '16', '17', '18', '19',
        '20', '21', '22', '23', '24', '25', '26', '27', '28', '29',
        '30', '31', '32', '33', '34', '35', '36', '37', '38', '39',
        '40', '41', '42', '43', '44', '45', '46', '47', '48', '49',
        '50', '51', '52', '53', '54', '55', '56', '57', '58', '59'
    ];

    var ConfirmModule = function(){
        var self = this;

        self.groupNames      = ko.observable("");
        self.groupIds        = ko.observable("");
        self.paperId         = ko.observable("");
        self.durationMinutes = ko.observable("");
        self.hourAndMin      = constantObj.endTime;

        //开始
        self.startDateTime   = ko.observable("");  //yyyy-MM-dd 00:00:00
        self.startDate       = ko.pureComputed(function(){
            return self.startDateTime().substring(0,10);
        });
        self.startHourSelect = ko.observableArray(h);
        self.startFocusHour  = ko.observable(self.hourAndMin.split(":")[0]);
        self.startMinSelect  = ko.observableArray(m);
        self.startFocusMin   = ko.observable(self.hourAndMin.split(":")[1]);
        self.startFocusHour.subscribe(function(newValue){
            self.startDateTime(self.startDate() + " " + newValue + ":" + self.startFocusMin() + ":00");
        },self);
        self.startFocusMin.subscribe(function(newValue){
            self.startDateTime(self.startDate() + " " + self.startFocusHour() + ":" + newValue + ":00");
        },self);

        //结束
        self.endDateTime    = ko.observable("");
        self.endDate        = ko.pureComputed(function(){
            return self.endDateTime().substring(0,10);
        });
        self.endHourSelect = ko.observableArray(h);
        self.endFocusHour  = ko.observable(self.hourAndMin.split(":")[0]);
        self.endMinSelect  = ko.observableArray(m);
        self.endFocusMin   = ko.observable(self.hourAndMin.split(":")[1]);
        self.endFocusHour.subscribe(function(newValue){
            self.endDateTime(self.endDate() + " " + newValue + ":" + self.endFocusMin() + ":00");
        },self);
        self.endFocusMin.subscribe(function(newValue){
            self.endDateTime(self.endDate() + " " + self.endFocusHour() + ":" + newValue + ":00");
        },self);
    };
    ConfirmModule.prototype = {
        constructor       : ConfirmModule,

        splitDateTime     : function(dateTime){
            return dateTime.split(/:|-|\s/g);
        },
        getTimeArray      : function(array, index){
            return $.grep(array, function (val, key) {
                return val >= index;
            });
        },
        saveNewExam : function(){
            var self = this;

            var startDateTime    = new Date(this.startDateTime()).getTime(),
                endDateTime      = new Date(this.endDateTime()).getTime(),
                durationMinutes  = this.durationMinutes() * 60 * 1000,
                monthLater       = new Date(constantObj.currentDateTime.slice(0,10)).getTime() + 30*24*60*60*1000;
            if(startDateTime > endDateTime){
                $17.alert("测试结束时间应晚于测试开始时间");
                return false;
            };
            if(endDateTime > monthLater){
                $17.alert("截止时间应设置在30天之内，请重新设置～");
                return false;
            };
            if((endDateTime-startDateTime) < durationMinutes*2 ){
                $17.alert("考试最小时间为" + this.durationMinutes()*2 + "分钟,请重新设定～");
                return false;
            };

            if((endDateTime-startDateTime) >= durationMinutes*2 && (endDateTime-startDateTime) < durationMinutes*4){
                $.prompt("<div style='text-align: center;'>考试时长建议设置为" + this.durationMinutes()*4 + "分钟,给孩子更多的时间完成测试</div>",{
                    buttons : { "坚持我的设置" : true , "重新设置" : false },
                    position: { width : 500},
                    submit : function(e,v,m,f){
                        if(v){
                            self.postnewExam();
                        }
                    }
                });
            }else{
                self.postnewExam();
            }
        },

        postnewExam : function(){
            var self = this;
            $.post("/teacher/newexam/assign.vpage",{
                examData : JSON.stringify({
                    paperId         : self.paperId(),
                    groupIds        : self.groupIds(),
                    startTime       : self.startDateTime(),
                    endTime         : self.endDateTime(),
                    durationMinutes : self.durationMinutes()
                })
            },function(res){
                if(res.success){

                    $.prompt(template("t:confirmSuccess",{}),{
                        title : "布置专项测试",
                        buttons : { "确认" : true },
                        position: { width : 760},
                        submit : function(){

                            window.location.href = "/teacher/newexam/independent/report.vpage?subject=" + constantObj.subject;
                        }
                    });
                }else{
                    $17.alert(res.info || "考试发布失败，请稍后再试～");
                }
            });
        },

        initialise        : function(option){
            var self = this,
                _startDateTime = option.startDateTime.substring(0,10),
                _endDateTime = option.endDateTime;

            self.paperId(option.paperId);
            self.groupIds(option.groupIds);
            self.groupNames(option.groupNames);
            self.durationMinutes(option.durationMinutes);
            self.startDateTime(_startDateTime + " "+ self.hourAndMin + ":00");
            self.endDateTime(_endDateTime + " "+ self.hourAndMin + ":00");

            $("#startDateInput").datepicker({
                dateFormat      : 'yy-mm-dd',
                defaultDate     : _startDateTime,
                numberOfMonths  : 1,
                minDate         : _startDateTime,
                maxDate         : null,
                onSelect        : function(selectedDate){
                    self.startDateTime(selectedDate + " 00:00:00");
                    self.startFocusHour("00");
                    self.startFocusMin("00");
                }
            });
            $("#endDateInput").datepicker({
                dateFormat      : 'yy-mm-dd',
                defaultDate     : _endDateTime,
                numberOfMonths  : 1,
                minDate         : _startDateTime,
                maxDate         : null,
                onSelect        : function(selectedDate){
                    self.endDateTime(selectedDate + " " + self.endFocusHour() + ":" + self.endFocusMin() + ":00");
                }
            });
        }
    };
    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getConfirmModule: function(){
            return new ConfirmModule();
        }
    });
}($17,ko));