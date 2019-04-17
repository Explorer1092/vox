$(function(){

    var vuePage = $17.pagination.vuePages;

    var vueLoading = $17.vueLoading;

    var newexamListVue = new Vue({
        el : "#newexamList",
        data : {
            subject : constantObj.subject,
            levelClazzList : [],
            groupIds : [],
            pageNo : 1,
            totalPage : 0,
            hkLoading : true,
            contentList : []
        },
        components : {
            "vue-page" : vuePage,
            "vue-loading" : vueLoading
        },
        methods : {
            getClazzList : function(){
                var vm = this;
                $.get("/teacher/unit/test/clazzlist.vpage",{
                    subject : vm.subject
                }).done(function(res){
                    if(res.success){
                        vm.levelClazzList = res.clazzList || [];
                        vm.fetchList();
                    }else{
                        vm.clazzList = [];
                    }
                }).fail(function(){
                    vm.clazzList = [];
                });
            },
            clazzClick : function(clazzObj,level){
                var vm = this;
                var groupIdIndex = vm.groupIds.indexOf(clazzObj.groupId);
                if(groupIdIndex === -1){
                    vm.groupIds.push(clazzObj.groupId);
                }else{
                    vm.groupIds.splice(groupIdIndex,1);
                }
                vm.fetchList();
            },
            fetchList : function(){
                var vm = this;
                vm.hkLoading = true;

                var groupIds = vm.groupIds;
                if(groupIds.length  === 0){
                    vm.levelClazzList.forEach(function(leveObj,index){
                        var levelGroupIds = leveObj.clazzs.map(function(clazzObj){
                            return clazzObj.groupId;
                        });
                        groupIds = groupIds.concat(levelGroupIds);
                    });
                }
                $.get("/teacher/unit/test/report/exam/list.vpage",{
                    groupIds : groupIds.join(","),
                    subject : vm.subject,
                    currentPage : vm.pageNo
                }).done(function(res){
                    if(res.success){
                        vm.hkLoading = false;
                        var pageContent = res.pageable || {};
                        vm.contentList = pageContent.content || [];
                        vm.totalPage = pageContent.totalPages || 0;

                        $17.voxLog({
                            module : "m_Odd245xH",
                            op : "unittest_report_list_load",
                            s0 : vm.subject
                        });

                    }else{
                        vm.contentList = [];
                    }
                }).fail(function(){
                    vm.totalPage = 0;
                    vm.contentList = [];
                    vm.hkLoading = true;
                });
            },
            pageClickCb : function(pageNo){
                var vm = this;
                vm.pageNo = pageNo;
                vm.fetchList();
            },
            deleteHomework : function(content){
                var vm = this;
                $.prompt("确定删除此次检测吗", {
                    title  : "删除检测",
                    focus  : 1,
                    buttons: { "取消": false, "确认删除": true },
                    submit : function(e, v){
                        e.preventDefault();
                        if(v){
                            $.post("/teacher/unit/test/delete.vpage",{
                                newExamId : content.newExamId
                            },function(res){
                                if(res.success){
                                    $17.alert("删除成功",function(e,v){
                                        e.preventDefault();
                                        vm.pageClickCb(1);
                                    });
                                }else{
                                    $17.alert(res.info || "删除失败");
                                    $17.voxLog({
                                        module : "API_REQUEST_ERROR",
                                        op     : "API_STATE_ERROR",
                                        s0     : "/teacher/new/homework/disablenewhomework.vpage",
                                        s1     : $.toJSON(res),
                                        s2     : $.toJSON({homeworkId : content.newExamId}),
                                        s3     : $uper.env
                                    });
                                }
                            });
                        }
                        $.prompt.close();
                    }
                });
                $17.voxLog({
                    module : "m_Odd245xH",
                    op : "unittest_report_list_del_test_click",
                    s0 : vm.subject,
                    s2 : content.newExamId
                });
            },
            adjustHomework : function(content){
                var vm = this;
                if(vm.ing) return false;
                vm.ing = true;
                $.get("/teacher/unit/test/adjust/index.vpage",{
                    newExamId : content.newExamId
                }).done(function(res){
                    if(res.success){
                        mathAdjustTimeFn(content.newExamId,res);
                    }else{
                        $17.alert("获取数据超时，请刷新页面重试");
                        $17.voxLog({
                            module : "API_REQUEST_ERROR",
                            op     : "API_STATE_ERROR",
                            s0     : "/teacher/unit/test/adjust/index.vpage",
                            s1     : $.toJSON(res),
                            s2     : $.toJSON({newExamId : content.newExamId}),
                            s3     : $uper.env
                        });
                    }
                    vm.ing = false;
                }).fail(function(){
                    vm.ing = false;
                });

                $17.voxLog({
                    module : "m_Odd245xH",
                    op : "unittest_report_adjust_time_click",
                    s0 : vm.subject,
                    s1 : content.newExamId
                });
            },
            viewReport : function(content){
                var vm =  this;
                $17.voxLog({
                    module : "m_Odd245xH",
                    op : "unittest_report_detailview_click",
                    s0 : vm.subject,
                    s1 : content.newExamId
                });
                setTimeout(function(){
                    location.href = '/teacher/newexam/report/detail.vpage?clazzId=' + content.clazzId + '&newExamId=' + content.newExamId;
                },200);
            },
            timestampToTime: function(timestamp){
                return $17.dateToString(timestamp,"MM-dd hh:mm");
            }
        },
        created : function(){
            this.getClazzList();
        }
    });

    /*调整班级考试时间*/
    function mathAdjustTimeFn(newExamId,option){
        var defaultOption = {
            clazz : "",
            currentDate : "",
            nowEndTime : "",
            practices : {},
            startDateTime : "",  //yyyy-MM-dd
            endDateTime : ""  //yyyy-MM-dd HH:mm:ss
        };
        var newOption = {},option = option || {};
        for(var key in defaultOption){
            if(defaultOption.hasOwnProperty(key)){
                var tempValue = option[key] || "";
                if($.isPlainObject(tempValue)){
                    tempValue = $.extend(true,{},tempValue);
                }else if($.isArray(tempValue)){
                    tempValue = [].concat(tempValue);
                }
                newOption[key] = tempValue;
            }
        }

        var adjustTimeHtml = template("T:ADJUSTTIME_POPUP", {});

        var mathAdjustState = {
            state0 : {
                name    : 'mathTime',
                comment : '数学调整时间',
                html    : adjustTimeHtml,
                title   : '调整检测',
                position: { width : 760},
                buttons : {"取消" : false , "确定" : true },
                focus   : 1,
                submit  : function(e,v,m,f){
                    if(v){
                        adjustTime.saveAdjust();
                        return false;
                    }else{
                        adjustTime.$destroy();
                        $.prompt.close();
                    }
                    return false;
                }
            },
            state1 : {
                name    : 'success',
                comment : '调整成功',
                html    : '<div class="jqicontent" style="line-height: 30px;"> 调整检测时间成功 <br /></div>',
                title   : '调整检测',
                buttons : {"确定" : true },
                position: { width : 450},
                focus   : 1,
                submit:function(e,v,m,f){
                    newexamListVue.pageClickCb(newexamListVue.pageNo);
                }
            },
            state2 : {
                name    : 'messageTip',
                comment : '调整检测',
                html    : '<div class="jqicontent" style="line-height: 30px;"><span id="messageTip"></span><br/></div>',
                title   : '调整检测',
                buttons : {"确定" : true },
                position: { width : 450},
                focus   : 1,
                submit:function(e,v,m,f){
                    e.preventDefault();
                    $.prompt.goToState('mathTime');
                }
            }

        };
        var adjustTime;
        var dateTime17Picker = $17.comblock.dateTimePicker;
        $.prompt(mathAdjustState,{
            loaded : function(event){
                adjustTime = new Vue({
                    el : "#saveMathDialog",
                    data : $.extend(true,newOption,{
                        timeStr : newOption.endDateTime  //选择的结束时间
                    }),
                    computed : {
                        startDateTimeStr : function(){
                            return this.startDateTime + " " + this.nowEndTime + ":59";
                        }
                    },
                    components : {
                        'datetime-17picker' : dateTime17Picker
                    },
                    methods : {
                        changeDateTime : function(timeStr){
                            this.timeStr = timeStr || this.endDateTime;
                        },
                        saveAdjust : function(){
                            var endDateTm = this.timeStr;
                            if($17.isBlank(endDateTm)){
                                $("#messageTip").text("结束时间不能为空，请刷新重试");
                                $.prompt.goToState('messageTip',true);
                            }else{
                                var paramData = {
                                    newExamId : newExamId,
                                    endDate    : endDateTm
                                };
                                $.post("/teacher/unit/test/adjust.vpage", paramData, function(data){
                                    if(data.success){
                                        $.prompt.goToState('success');
                                    }else{
                                        $("#messageTip").text(data.info || "失败，请刷新重试");
                                        $.prompt.goToState('messageTip',true);
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
                        }
                    },
                    created : function(){
                        $17.info("adjust time created");
                    },
                    mounted : function(){
                        $17.info("adjust time mounted");
                        var vm = this;

                    },
                    beforeDestroy : function(){
                        $17.info("adjust time beforeDestroy");
                    }
                });


                // $.prompt.goToState();


            },
            close : function(){
                adjustTime.$destroy();
            }
        });

    }


});