$(function(){
    function HomeworkList(option){
        var self = this;
        option = option || {};
        self.todayEndDateTime = option.currentDayEndDate;
        self.ing       = false;
        self.hkLoading = ko.observable(false);
        self.levelClazzList = ko.observableArray([]);
        self.groupIdCart = [];
        self.allGroupIds = [];  //所有组ID
        self.clazzClick = function(){
            var that = this; //clazz object
            if(self.hkLoading()){
                return false;
            }
            that.checked(!that.checked());
            var _groupId = that.clazzGroupId();
            if(that.checked()){
                //选中
                self.groupIdCart.push(_groupId);
            }else{
                var _index = $.inArray(_groupId,self.groupIdCart);
                if(_index != -1){
                    self.groupIdCart.splice(_index,1);
                }
            }
            self.currentPage(1);
            self.loadHomeworkList();

            $17.voxLog({
                module: "m_Odd245xH",
                op : "page_report_class_select_click",
                s0 : constantObj.subject
            });
        };

        self.currentPage = ko.observable(1);
        self.homeworkList = ko.observableArray([]);
        self.totalPage  = ko.observable(1);
        self.isFirstPage = ko.pureComputed(function(){
            return self.currentPage() == 1;
        });
        self.isLastPage = ko.pureComputed(function(){
            return self.currentPage() == self.totalPage();
        });
        self.page_click = function(hk,index){
            if(self.hkLoading()){
                return false;
            }
            if(index < 1 || index > self.totalPage()){
                return false;
            }
            self.currentPage(index);
            self.loadHomeworkList();
        };

        self.processRate = function(fenzi,fenmu){
            fenzi = +fenzi || 0;
            fenmu = +fenmu || 0;
            if(fenmu == 0) return '0%';
            return Math.ceil(fenzi/fenmu * 100) + "%";
        };
        self.subject = option.subject;
        self.loadHomeworkList = function(){
            if(self.groupIdCart.length == 0 && self.allGroupIds.length == 0){
                return false;
            }
            self.hkLoading(true);
            var groupIds = [];
            if(self.groupIdCart.length == 0){
                groupIds = self.allGroupIds;
            }else{
                groupIds = self.groupIdCart;
            }
            var paramData = {
                groupIds    : groupIds.join(","),
                currentPage : self.currentPage(),
                subject     : constantObj.subject
            };
            $.get("/teacher/new/homework/report/homeworklist.vpage",paramData,function(data){
                self.hkLoading(false);
                if(data.success){
                    var _result = data.page || {};
                    var _content = _result.content || [];
                    self.totalPage(_result.totalPages || 1);
                    self.homeworkList(ko.mapping.fromJS(_content)());
                }else{
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/report/homeworklist.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON(paramData),
                        s3     : $uper.env
                    });
                }
            });
        };
        self.checkHomework = function(){
            var kh = this;
            if(!kh.showCheck()){
                // $17.info("还没有到检查时间");
                return false;
            }
            if(self.ing) return false;
            self.ing = true;

            $17.voxLog({
                module: "m_Odd245xH",
                op : "page_report_checkup_click",
                s0 : constantObj.subject
            });

            var __check = function(){

                $.get("/teacher/new/homework/check.vpage?homeworkId=" + kh.homeworkId(), function(data){
                    if(data.success){
                        location.href = "/teacher/new/homework/report/detail.vpage?from=check&homeworkId=" + kh.homeworkId() ;

                        //$17.homeworkv3.teacherAppPopup("检查成功","m_Odd245xH","popup_report_checkup_success_show",forwardHwReport,constantObj.subject);
                    }else{
                        var _info = $17.isBlank(data.info) ? '检查作业失败！' : data.info;
                        $17.alert(_info);
                        $17.voxLog({
                            module : "API_REQUEST_ERROR",
                            op     : "API_STATE_ERROR",
                            s0     : "/teacher/new/homework/check.vpage?homeworkId=" + kh.homeworkId(),
                            s1     : _info,
                            s3     : $uper.env
                        });
                    }

                    self.ing = false;
                });
            };
            var unFinishedCount = kh.userCount() - kh.finishedCount();
            if(!kh.terminated() && unFinishedCount != 0){
                $.prompt("本次作业尚未到期，可能有学生还未完成作业，您确认要提前检查作业吗？", {
                     title  : "系统提示",
                     focus  : 1,
                     buttons: { "取消": false, "确定": true },
                     submit : function(e, v){
                         if(v){
                             __check();
                         }else{
                            self.ing = false;
                         }
                     },
                     close : function(){
                        self.ing = false;
                     },
                     loaded : function(){
                         $17.voxLog({
                             module: "m_Odd245xH",
                             op : "popup_report_systemPrompt_homework_notdue_show",
                             s0 : constantObj.subject
                         });
                     }
                });
            }else{
                __check();
            }
        };
        self.viewReport = function(){
            var kh = this;
            $17.voxLog({
                module: "m_Odd245xH",
                op : "page_report_details_click",
                s0 : constantObj.subject,
                s1 : kh.homeworkType(),
                s2 : kh.homeworkId()
            });
            setTimeout(function(){
                location.href = '/teacher/new/homework/report/detail.vpage?from=view&homeworkId=' + kh.homeworkId();
            },200);
        };
        self.adjustHomework = function(){
            var kh = this;
            if(kh.checked()){
                $17.info("已经检查");
                return false;
            }
            if(self.ing) return false;
            self.ing = true;

            if($17.isBlank(self.todayEndDateTime)){
                $17.alert("当前时间获取错误，请刷新页面重试");
                return false;
            }

            var homeworkId = kh.homeworkId();
            $.getJSON("/teacher/new/homework/adjust/index.vpage",{homeworkId : homeworkId},function(data){
                if(!$17.isBlank(data.success) && data.success){
                    mathAdjustTimeFn(homeworkId,data,self.todayEndDateTime);
                }else{
                    $17.alert("获取数据超时，请刷新页面重试");
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/teacher/new/homework/adjust/index.vpage",
                        s1     : $.toJSON(data),
                        s2     : $.toJSON({homeworkId : homeworkId}),
                        s3     : $uper.env
                    });
                }
            });
            self.ing = false;

            $17.voxLog({
                module: "m_Odd245xH",
                op : "page_report_homework_adjust_click",
                s0 : constantObj.subject
            });
            return false;
        };

        self.deleteHomework = function(){
            var self = this;
            $17.voxLog({
                module: "m_Odd245xH",
                op : "page_report_homework_delete_click",
                s0 : constantObj.subject
            });
            $.prompt("确定删除此次作业吗", {
                title  : "删除作业",
                focus  : 1,
                buttons: { "取消": false, "确认删除": true },
                submit : function(e, v){
                    if(v){
                        $17.voxLog({
                            module: "m_Odd245xH",
                            op : "popup_report_homework_delete_confirm_click",
                            s0 : constantObj.subject,
                            s1 : self.homeworkId()
                        });
                        $.post("/teacher/new/homework/disablenewhomework.vpage",{
                            homeworkId:self.homeworkId()
                        },function(res){
                            if(res.success){
                                $17.alert("删除成功",function(){
                                    window.location.reload();
                                });
                            }else{
                                $17.alert(res.info || "删除失败");
                                $17.voxLog({
                                    module : "API_REQUEST_ERROR",
                                    op     : "API_STATE_ERROR",
                                    s0     : "/teacher/new/homework/disablenewhomework.vpage",
                                    s1     : $.toJSON(res),
                                    s2     : $.toJSON({homeworkId : self.homeworkId()}),
                                    s3     : $uper.env
                                });
                            }
                        });
                    }
                }
            });
        };
        self.run = function(){
            self.loadHomeworkList();
        };
        self.init  = function(){
            var levelClazzList = option.levelClazzList || [];
            for(var z = 0,zLen = levelClazzList.length; z < zLen;z++){
                for(var k = 0,kLen = levelClazzList[z].length; k < kLen; k++){
                    levelClazzList[z][k]["checked"] = false;
                    self.allGroupIds.push(levelClazzList[z][k].clazzGroupId);
                }
            }
            self.levelClazzList(ko.mapping.fromJS(levelClazzList)());
        };
        self.init();
    }

    var _homeworkList = new HomeworkList(constantObj);
    _homeworkList.run();
    ko.applyBindings(_homeworkList,document.getElementById("homeworkList"));


    function mathAdjustTimeFn(homeworkId,data,todayEndDateTime){
        var h = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'];
        var m = [
            '00', '01', '02', '03', '04', '05', '06', '07', '08', '09',
            '10', '11', '12', '13', '14', '15', '16', '17', '18', '19',
            '20', '21', '22', '23', '24', '25', '26', '27', '28', '29',
            '30', '31', '32', '33', '34', '35', '36', '37', '38', '39',
            '40', '41', '42', '43', '44', '45', '46', '47', '48', '49',
            '50', '51', '52', '53', '54', '55', '56', '57', '58', '59'
        ];
        var splitDateTime = function(dateTime){
             return dateTime.split(/:|-|\s/g);
        };
        var getTimeArray = function (array,index) {
            return $.grep(array, function (val, key) {
                return val >= index;
            });
        };
        var displayWeekName = function(weekDay){
            return ["(星期日)","(星期一)","(星期二)","(星期三)","(星期四)","(星期五)","(星期六)"][weekDay];
        };
        var displayDateAndWeek = function(dateTime){
            var timeArr = dateTime.split(/:|-|\s/g);
            var specDate = new Date(timeArr[0], timeArr[1] - 1, timeArr[2], timeArr[3], timeArr[4], timeArr[5]);
            return dateTime + displayWeekName(specDate.getDay());
        };
        var startDateTime = data.currentDate;
        var nowEndTime = data.nowEndTime;
        var resetEndDateTime = function(endDateTime){
            var _startDate = startDateTime;
            var _endDate = endDateTime.substring(0,10);
            var defaultHour = "00";
            var defaultMin = "00";
            if(_startDate === _endDate){
                var sdtArr = splitDateTime(nowEndTime);
                defaultHour = sdtArr[0];
                defaultMin = sdtArr[1];
            }
            var _hArr = getTimeArray(h,defaultHour);
            var _mArr = getTimeArray(m,defaultMin);
            $17.setSelect("#endHour", _hArr, _hArr, _hArr[_hArr.length - 1]);
            $17.setSelect("#endMin", _mArr, _mArr, _mArr[_mArr.length - 1]);
        };
        var setEndDateTime = function(endDateTime){
            $("#endDateTime").val(endDateTime);
            $("#endDateInput").val(endDateTime.substring(0,10));
            $("#endDateId").text(displayDateAndWeek(endDateTime));
        };

        var eventConfig = {
            ".endDateLable -> click"     : function(){
                $17.voxLog({
                    module: "m_Odd245xH",
                    op : "popup_report_homework_adjust_finishtime_click",
                    s0 : constantObj.subject
                });
                $(this).addClass("w-radio-current");
                $(this).siblings("label").removeClass("w-radio-current");
                var day = (+$(this).attr("data-relativeday")) || 0;
                var $endDateTime = $("#endDateTime");
                switch (day){
                    case 0:
                    case 1:
                    case 2:
                        var timeArray = todayEndDateTime.split(/:|-|\s/g);
                        var _specDate = new Date(timeArray[0], timeArray[1] - 1, timeArray[2], timeArray[3], timeArray[4], timeArray[5]);
                        var endDateTime = $17.DateUtils("%Y-%M-%d 23:59:59",day,"d",_specDate);
                        $("#endDateInput").val(endDateTime.substring(0,10));
                        $endDateTime.val(endDateTime);
                        $("#endDateId").text(displayDateAndWeek(endDateTime));
                        resetEndDateTime($endDateTime.val());
                        break;
                    default:
                        break;
                }
            }
        };

        var mathAdjustState = {
            state0 : {
                name    : 'mathTime',
                comment : '数学调整时间',
                html    : template("t:mathAdjustTime", {data : data, minute : Math.ceil(data.finishTime/60)}),
                title   : '调整作业',
                position: { width : 760},
                buttons : {"取消" : false , "确定" : true },
                focus   : 1,
                submit  : function(e,v,m,f){
                    if(v){
                        var endDateTm = $("#endDateTime").val();
                        if($17.isBlank(endDateTm)){
                            $("#messageTip").text("结束时间不能为空，请刷新重试");
                            $.prompt.goToState('missdate');
                        }else{
                            $17.voxLog({
                                module: "m_Odd245xH",
                                op : "popup_report_homework_adjust_confirm_click",
                                s0 : constantObj.subject,
                                s1 : endDateTm,
                                s2 : homeworkId
                            });
                            var paramData = {
                                homeworkId : homeworkId,
                                endDate    : endDateTm
                            };
                            $.post("/teacher/new/homework/adjust.vpage", paramData, function(data){
                                if(data.success){
                                    $.prompt.goToState('success');
                                }else{
                                    $("#messageTip").text(data.info || "失败，请刷新重试");
                                    $.prompt.goToState('messageTip');
                                    $17.voxLog({
                                        module : "API_REQUEST_ERROR",
                                        op     : "API_STATE_ERROR",
                                        s0     : "/teacher/new/homework/adjust.vpage",
                                        s1     : $.toJSON(data),
                                        s2     : $.toJSON(paramData),
                                        s3     : $uper.env
                                    });
                                }
                            });
                        }
                        return false;
                    }else{
                        $.prompt.close();
                    }
                    return false;
                }
            },
            state1 : {
                name    : 'success',
                comment : '调整成功',
                html    : '<div class="jqicontent" style="line-height: 30px;"> 调整作业时间成功 <br /></div>',
                title   : '调整作业',
                buttons : {"确定" : true },
                position: { width : 450},
                focus   : 1
            },
            state4 : {
                name    : 'messageTip',
                comment : '调整作业',
                html    : '<div class="jqicontent" style="line-height: 30px;"><span id="messageTip"></span><br/></div>',
                title   : '调整作业',
                buttons : {"确定" : true },
                position: { width : 450},
                focus   : 1
            }

        };
        $.prompt(mathAdjustState,{
            loaded : function(event){
                $17.delegate(eventConfig);
                if($17.isBlank(todayEndDateTime)){
                    $("#messageTip").text("当前时间获取错误，请刷新页面重试");
                    $.prompt.goToState('messageTip');
                }else{
                    var _startDate = data.startDateTime.substring(0, 10);
                    var _endDate = data.endDateTime.substring(0, 10);
                    $("#startDateId").val(displayDateAndWeek(data.startDateTime));
                    $("#endDateId").text(displayDateAndWeek(data.endDateTime));
                    $("#endDateInput").val(_endDate).datepicker({
                        dateFormat      : 'yy-mm-dd',
                        defaultDate     : _endDate,
                        numberOfMonths  : 1,
                        minDate         : _startDate,
                        maxDate         : null,
                        onSelect        : function(selectedDate){
                            var selectedEndDateTime = selectedDate + " 23:59:59";
                            $("#endDateTime").val(selectedEndDateTime);
                            $("#endDateId").text(displayDateAndWeek(selectedEndDateTime));
                            resetEndDateTime(selectedEndDateTime);
                        }
                    });
                    resetEndDateTime(data.endDateTime);

                    function generateEndTime(endDate,hour,min){
                        hour = +hour || 0;
                        hour = (hour < 0 || hour > 23) ? 23 : hour;
                        min = +min || 0;
                        min = (min < 0 || min > 59) ? 59 : min;
                        return endDate + " " + (hour < 10 ? "0" + hour : hour) + ":" + (min < 10 ? "0" + min : min) + ":59";
                    }
                    $("#endHour").on("change",function(){
                        var $this = $(this);
                        var endDate = $("#endDateInput").val();
                        var $custom = $("label[data-relativeday='-1']");
                        $custom.addClass("w-radio-current");
                        $custom.siblings("label").removeClass("w-radio-current");
                        setEndDateTime(generateEndTime(endDate, $this.val(), $("#endMin").val()));
                    });

                    $("#endMin").on("change",function(){
                        var $this = $(this);
                        var endDate = $("#endDateInput").val();
                        var $custom = $("label[data-relativeday='-1']");
                        $custom.addClass("w-radio-current");
                        $custom.siblings("label").removeClass("w-radio-current");
                        setEndDateTime(generateEndTime(endDate, $("#endHour").val(), $this.val()));
                    });
                }
            },
            close : function(){
                _homeworkList.run();
            }
        });

    }

});