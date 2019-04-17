
define(["common"], function (dispatchEvent) {

    $(document).on("click",function () {
        $('.feedbackList-pop').hide();
    });
    $(document).on('click', '.tab_row', function () {
        $(this).css('background', '#f1f2f5').siblings().css('background', '#fff');
        $(this).find('a').css('color', '#fb7e5f').parent().siblings().find('a').css('color', '#898c91');
    });
    $("#isfake").on("click", function () {
        if (isFakedTeacher) {
            AT.alert("该老师已作判假处理，无需重复操作！");
        }
        else {
            /*location.href = "/mobile/teacher_fake/fake_teacher.vpage?teacherId=" + teacherId;*/
            $('.show_now').hide();
            $('.fakeTeacherDom').show();
        }
    });
    $(document).ready(function () {
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_dmyGAMr5", //打点流程模块名
            op : "o_EPrqQ2dG" ,//打点事件名
            userId:userId,
            s0 : teacherId
        });
        reloadCallBack();
        var setTopBar = {
            show:true,
            rightTextColor:"ff7d5a",
            rightImage:window.location.protocol+ "//" + window.location.host + "/public/rebuildRes/css/mobile/feedback/images/add_new.png",
            needCallBack:true
        };
        var topBarCallBack = function () {
            if(!isFakedTeacher){
                $('.feedbackList-pop').toggle();
            }
        };
        setTopBarFn(setTopBar,topBarCallBack);
    });
    $(document).on('click', '.chooseFakeReason', function () {
        $(this).addClass('active').siblings().removeClass("active");
        if($(this).data("info") == "其他"){
            $('.otherFakeReason').removeAttr("disabled");
        }
    });
    $(document).on('click', '.closeFake', function () {
        $('.fakeTeacherDom').hide();
    });

    $(document).on('click','.fakeTeacherSubmit', function () {
        if($('.chooseFakeReason.active').data("info") == "其他"){
            var fakeNote = $('.otherFakeReason').val();
        }else{
            var fakeNote = $('.chooseFakeReason.active').data("info");
        }
        var fakerData = {
            fakeNote : fakeNote,
            teacherId : teacherId,
            is17ActiveTeacher: is17ActiveTeacher,
            isKLXTeacher : isKLXTeacher
        };
        if (is17ActiveTeacher && !isKLXTeacher) {//一起作业老师展示
            if (window.confirm("近期，该老师为活跃老师，请再次确认是否举报！")) {
                $.post('/mobile/teacher_fake/fake_teacher.vpage', fakerData, function(res) {
                    if (res.success) {
                        AT.alert("提交成功");
                        setTimeout("window.location.reload()",1500);
                    } else {
                        alert(res.info);
                    }
                });
            }
        } else {
            $.post('/mobile/teacher_fake/fake_teacher.vpage', fakerData, function(res) {
                if (res.success) {
                    AT.alert("提交成功");
                    setTimeout("window.location.reload()",1500);
                } else {
                    alert(res.info);
                }
            });
        }
    });
    var rab_teacher = true;
    $(".tab_teacher").on("click", function () {
        var index = $(this).index();
        $(this).addClass("_active").siblings().removeClass("_active");
        if (index == 1) {
            if(rab_teacher){
                $.post("work_view.vpage", {teacherId: teacherId}, function (res) {
                    if (res.success) {
                        rab_teacher = false;
                        $('.teacher_information').html(template("teacher_information", {res: res}));
                    }
                });
            }
        }
        $('.teacher_detail').eq(index).show().siblings().hide();
        teacherIndex = $('.teacher_detail').eq(index).find(".teacher_data.active").data("index");
        rendermyChart();
    });
    $('.js-showHand').on('click', function () {
        $('.feedbackList-pop').toggle();
    });

    $("#applyService").on("click", function () {
        if (isFakedTeacher) {
            AT.alert("该老师已作判假处理，无需重复操作！");
        }
        else {
            openSecond("/mobile/task/change_school_page.vpage?teacherId=" + teacherId);
        }
    });
    $("#testLibrarian").on("click",function(){
        checkSchool("/mobile/resource/teacher/change_school_quiz_bank_administrator_view.vpage?teacherId=" + teacherId + "&schoolId=" + schoolId, "testLibrarian");
    });
    $("#headMan").on("click",function(){
        checkSchool("/mobile/resource/teacher/change_klx_subject_leader_view.vpage?teacherId=" + teacherId + "&schoolId=" + schoolId, "headMan");
    });
    function checkSchool(url, type) {
        $.post("check_school.vpage", {teacherId: teacherId, type: type}, function (res) {
            if (res.success) {
                openSecond(url);
            } else {
                $('.feedbackList-pop').toggle();
                AT.alert(res.info);
            }
        })
    }

//产品反馈
    $("#retroAction").on("click", function () {
        openSecond("/mobile/feedback/view/feedbackinfo.vpage?teacherId=" + teacherId);
    });

//记录老师职务
    $("#recordTeacherJob").on("click", function () {
        openSecond("/mobile/tag/teacher_tag_page.vpage?teacherId=" + teacherId);
    });

//重置密码
    $("#resetpwd").on("click", function () {
        $('.feedbackList-pop').toggle();
        if (confirm("是否确认重置" + realName + "的密码？")) {
            $.ajax({
                type: "post",
                url: "/mobile/workbench/resetteacherpassword.vpage",
                data: {
                    teacherId: teacherId
                },
                success: function (data) {
                    if (data.success) {
                        AT.alert("重置密码成功");
                    } else {
                        AT.alert(data.info);
                    }
                }
            });
        }
    });

   column();
    var dataList;
    function column() {
        $.post('data_view.vpage', {teacherId: teacherId}, function (res) {
            if (res.success) {
                dataList = res.dataList;
                rendermyChart();
            } else {
                alert(res.info)
            }
        });

    }
    $(document).on("click",".unClick",function(){
        $('.feedbackList-pop').toggle();
        AT.alert("暂无可申请的班级，无法开通包班");
    });
    $('.teacher_data').on("click",function(){
        teacherIndex = $(this).data("index");
        $(this).addClass("active").siblings().removeClass("active");
        rendermyChart();
    });
    var teacherIndex = 1;
    var dataName;
    var dataMonth = [],
        dataParameter = [];
    function rendermyChart(){
        var parentContainer =  $('.tab_teacher._active').data("info");
        if(parentContainer != 3){
            if(teacherType == 1){
                dataName = '本月布置作业套数';
                if (teacherIndex == 2) {
                    dataName = '月活';
                }
                for(var i = 0;i < dataList.length;i++){
                    dataMonth.push(dataList[i].month.toString().substring(4).replace(/\b(0+)/gi,"") +'月');
                    if(teacherIndex == 1){
                        dataParameter.push(dataList[i].tmHwSc);
                    }else if (teacherIndex == 2) {
                        dataParameter.push(dataList[i].finCsHwGte3AuStuCount);
                    }
                }
            }else if(teacherType == 2){
                dataName = '扫描试卷数';
                if (teacherIndex == 2) {
                    dataName = '普通扫描(≥1次)学生数';
                }else if (teacherIndex == 3) {
                    dataName = '布置作业数';
                }else if (teacherIndex == 4) {
                    dataName = '月活';
                }

                for(var i=0;i< dataList.length;i++){
                    dataMonth.push(dataList[i].month.toString().substring(4).replace(/\b(0+)/gi,"")+'月');
                    if(teacherIndex == 1){
                        dataParameter.push(dataList[i].tmScanTpCount);
                    }else if (teacherIndex == 2) {
                        dataParameter.push(dataList[i].tmFinCsTpGte1StuCount);
                    }else if (teacherIndex == 3) {
                        dataParameter.push(dataList[i].tmHwSc);
                    }else if (teacherIndex == 4) {
                        dataParameter.push(dataList[i].finCsHwGte3AuStuCount);
                    }
                }
            }


                // var myChart = echarts.init(document.getElementById("container"));
                var myChart = echarts.init(document.getElementById("container"+parentContainer));
                var option = {
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        }
                    },
                    title: {
                        show: false
                    },
                    legend: {
                        data: dataName,
                        x: 'center',
                        y: 'bottom',
                        show: true,
                        itemWidth: 12,
                        itemHeight: 12
                    },
                    grid: {
                        y: 35,
                        x: 45
                    },
                    xAxis: [
                        {
                            type: 'category',
                            data: dataMonth
                        }
                    ],
                    yAxis: [
                        {
                            type: 'value',
                            splitNumber: 4
                        }
                    ],
                    /*dataZoom: [
                        {
                            type: 'inside',
                            start: 80,
                            end: 100,
                            zoomLock: true
                        }
                    ],*/
                    series: [
                        {
                            name: dataName,
                            type: 'bar',
                            barWidth: 5,
                            data: dataParameter,
                            itemStyle: {
                                normal: {color: '#ff7d5a'}
                            }
                        }
                    ]
                };
                // myChart.setOption(option);
                myChart.setOption(option);
            dataMonth = [];
                dataParameter = [];
            $("#container"+parentContainer).show();
        }
    }
});
