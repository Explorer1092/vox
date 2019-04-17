define(["jquery", "knockout", 'userpopup', '$17','menu'], function ($, knockout, userpopup, $17) {

    //依赖初始化
    userpopup.selectStudent("wrongQuestionList");

    var HomeworkModel = function(homeworks) {
        var self = this;
        self.englishHomeworks = knockout.observableArray(homeworks);
        self.mathHomeworks = knockout.observableArray(homeworks);
        self.hasEnglishTeacher = knockout.observable(true);
        self.hasMathTeacher = knockout.observable(true);
        self.available = knockout.observable('');
        self.currentStudentId = knockout.observable(0);
        self.isGraduate = knockout.observable(false);
        //学科标签切换
        self.focusTab = knockout.observable('english');
        self.getHomework = function(){
            if(self.focusTab() == 'english'){
                return self.englishHomeworks();
            }else if (self.focusTab() == 'math'){
                return self.mathHomeworks();
            }
        };
        self.changeTab = function(subject){
            self.focusTab(subject);
        };

        //错题重做
        self.doAgainBtn = function(isVip){
            var content = (isVip == 'true') ? "您已经开通阿分提<br/>让孩子登录17作业进行学习吧" : "本次错题已加入阿分提错题工厂<br/>开通即可直接重练";
            var confirmButton = (isVip) ? "知道了" : "查看详情";
            var confirm = new jBox('Confirm', {
                content: content,
                confirmButton: confirmButton,
                closeButton: false,
                cancelButton: '',
                confirm: function () {
                    if(!isVip){
                        location.href = '/ucenter/product/info-afenti.vpage';
                    }
                },
                onOpen: function () {
                    $('.jBox-Confirm-button-cancel').hide();
                }
            });
            confirm.open();
        };

        //查看题目详情
        self.showErrorDetail = function(index){
            document.forms[index].submit();
        }
    };

    var viewModel = new HomeworkModel([]);
    knockout.applyBindings(viewModel);

    return {
        loadMessageById: function (sid) {
            //初始化数据
            viewModel.englishHomeworks({});
            viewModel.mathHomeworks({});
            viewModel.available();
            viewModel.currentStudentId(sid);

            //获取错题考点信息
            $17.ajax({
                url : '/parent/homework/wrongquestionlist.vpage',
                data : {
                    sid : sid,
                    subject : 'ENGLISH'
                },
                success : function(data){
                    viewModel.englishHomeworks(data.ENGLISH);
                    // 数学暂时不支持#18649
                    viewModel.mathHomeworks(data.MATH);
                    viewModel.available(data.available);

                    if (!data.MATH) {
                        viewModel.hasMathTeacher(false);
                    } else {
                        viewModel.hasMathTeacher(true);
                    }

                    if (!data.ENGLISH) {
                        viewModel.hasEnglishTeacher(false);
                    } else {
                        viewModel.hasEnglishTeacher(true);
                    }

                    viewModel.isGraduate(data.isGraduate);
                },
                error : function(){

                }
            });
        }
    };
});
