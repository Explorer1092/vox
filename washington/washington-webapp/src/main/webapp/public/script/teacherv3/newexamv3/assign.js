$(function(){

    var subject = $17.getQuery("subject");

    var levelClazzs = $17.comblock.levelClazz;

    var bookUnits = $17.comblock.bookUnits;

    var paperList = $17.comblock.paperList;

    var paperInfo = $17.comblock.paperInfo;

    // 更新教材提醒，换教材弹窗，
    new Vue({
        el : "#assignmockexam",
        data : {
            subject : subject,
            clazzList : [],
            currentDate : constantObj.currentDate,
            startDateTime : constantObj.startDateTime,
            endDateTime : constantObj.endDateTime,
            clazzBook : null,  //后台返回的课本单元信息

            focusLevel : 0,
            focusClazzs : [],   //选择的班级
            book : null,  //使用的课本单元 {bookId,unitId,bookName,unitName}
            focusUnit : {}, //选择后的单元
            unitTestPaperInfos : [],
            focusPaperType : -1,
            focusPaperTypeName : "",
            paperMap : {},
            paperInfo : null  //选择的试卷
        },
        components : {
            "level-clazzs" : levelClazzs,
            "book-units" : bookUnits,
            "paper-list" : paperList,
            'paper-info' : paperInfo
        },
        methods : {
            getClazzList : function(){
                var vm = this;
                $.get("/teacher/unit/test/clazzlist.vpage",{
                    subject : vm.subject
                }).done(function(res){
                    if(res.success){
                        vm.clazzList = res.clazzList || [];
                    }else{
                        vm.clazzList = [];
                    }
                }).fail(function(){
                    vm.clazzList = [];
                });
            },
            levelClickCb : function(clazzs,level){
                var vm = this;
                vm.focusClazzs = clazzs || [];
                vm.focusLevel = level;
                vm.fetchDefaultBook();
            },
            clazzClickCb : function(clazzs,level){
                var vm = this;
                vm.focusClazzs = clazzs || [];
            },
            tabClick : function(paperTypeObj){
                var vm = this;
                if(paperTypeObj){
                    vm.focusPaperType = paperTypeObj.paperTypeId;
                    vm.focusPaperTypeName = paperTypeObj.paperType;
                }else{
                    vm.focusPaperType = -1;
                    vm.focusPaperTypeName = "";
                }
            },
            autoUpdateBook : function(clazzBook,exchangeBookCb,noExchangeBookCb){
                var vm = this;
                $.prompt(template("T:自动更新新学期教材", {
                    bookName       : clazzBook.bookName,
                    remindBookName : clazzBook.remindBook.name,
                    remindBookPress: clazzBook.viewContent,
                    color          : clazzBook.color
                }), {
                    title   : "系统提示",
                    focus   : 1,
                    buttons : { "暂不更换": false, "更换教材": true },
                    position: { width: 500 },
                    submit  : function(e, v){
                        if(v){
                            $17.voxLog({
                                module: "m_H1VyyebB",
                                op : "systemPrompt_changetNewBook_popup_change_click",
                                s0 : vm.subject
                            });
                            typeof exchangeBookCb === "function" && exchangeBookCb();
                        }else{
                            $17.voxLog({
                                module: "m_H1VyyebB",
                                op : "systemPrompt_changetNewBook_popup_nonChange_click",
                                s0 : vm.subject
                            });
                            typeof noExchangeBookCb === "function" && noExchangeBookCb();
                        }
                    },
                    loaded : function(){
                        $17.voxLog({
                            module: "m_H1VyyebB",
                            op : "systemPrompt_changetNewBook_popup_show",
                            s0 : vm.subject
                        });
                    }
                });
            },
            saveChangeBook : function(bookId){
                var vm = this;
                var clazzGroupIds = [];
                vm.focusClazzs.forEach(function(clazzObj,index){
                    clazzGroupIds.push(clazzObj.clazzId + "_" + clazzObj.groupId);
                });
                var paramData = {
                    clazzs  : clazzGroupIds.join(","),
                    bookId  : bookId,
                    subject : vm.subject
                };
                if($17.isBlank(bookId)){
                    return false;
                }
                $.post("/teacher/new/homework/changebook.vpage", paramData, function(data){
                    $17.alert(data.info,function(){
                        if(data.success){
                            vm.fetchDefaultBook();
                        }else{
                            $17.voxLog({
                                module : "API_REQUEST_ERROR",
                                op     : "API_STATE_ERROR",
                                s0     : "/teacher/new/homework/changebook.vpage",
                                s1     : $.toJSON(data),
                                s2     : $.toJSON(paramData),
                                s3     : $uper.env
                            });
                        }
                    });
                });
            },
            fetchDefaultBook : function(){
                var vm = this;
                var clazzGroupIds = [];
                vm.focusClazzs.forEach(function(clazzObj,index){
                    clazzGroupIds.push(clazzObj.clazzId + "_" + clazzObj.groupId);
                });
                $.get("/teacher/new/homework/clazz/book.vpage",{
                    clazzs  : clazzGroupIds.join(","),  //clazzId_groupId
                    subject : vm.subject
                }).done(function(res){
                    if(res.success){
                        var clazzBook = res.clazzBook || {};

                        //自动更新新学期教材
                        if(clazzBook.remindBookFlag && !$17.isBlank(clazzBook.remindBook) && $17.getQuery("step") !== 'showtip'){
                            vm.autoUpdateBook(clazzBook,function(){
                                // 保存课本
                                vm.saveChangeBook(clazzBook.remindBook.id);
                            },function(){
                                $.get("/teacher/book/remindbook.vpage", {
                                    clazzLevel : vm.focusLevel,
                                    subject    : vm.subject
                                }, function(data){
                                    if(!data.success){
                                        $17.voxLog({
                                            module : "API_REQUEST_ERROR",
                                            op : "API_STATE_ERROR",
                                            s0 : "/teacher/book/remindbook.vpage",
                                            s1 : $.toJSON(data),
                                            s2 : $.toJSON({
                                                clazzLevel : vm.focusLevel,
                                                subject    : vm.subject
                                            }),
                                            s3 : $uper.env
                                        });
                                    }
                                });
                            });
                        }else{
                            vm.fetchUnitList(clazzBook);
                        }
                    }else{

                    }
                }).fail(function(){

                });
            },
            fetchUnitList : function(clazzBook){
                var vm = this;
                $.get("/teacher/unit/test/unitlist.vpage",{
                    bookId : clazzBook.bookId
                }).done(function(res){
                    if(res.success){
                        var book = res.book;
                        var moduleList = book.moduleList || [];
                        var unitObj;
                        if(moduleList.length > 0){

                        }else{
                            unitObj = book.unitList.filter(function(unitObj){
                                return !!unitObj.defaultUnit;
                            })[0];
                        }

                        if(unitObj){
                            vm.book = Object.assign({},vm.book,{
                                bookId : book.bookId,
                                bookName : book.bookName,
                                unitId : unitObj.unitId,
                                unitName : unitObj.cname,
                                unitList : book.unitList || [],
                                moduleList : book.moduleList || []
                            });
                        }else{
                            vm.book = null;
                        }
                    }else{
                        vm.book = null;
                    }
                }).fail(function(){
                    vm.book = null;
                });
            },
            exchangeBook : function(){
                var vm = this;
                var clazzGroupIds = [];
                $17.info("exchangebook..");
                vm.focusClazzs.forEach(function(clazzObj,index){
                    clazzGroupIds.push(clazzObj.clazzId + "_" + clazzObj.groupId);
                });
                $17.comblock.exchangeBook({
                    clazzGroupIds : clazzGroupIds,
                    bookName : vm.book.bookName, //默认选择的课本
                    subject : vm.subject,
                    isSaveBookInfo : false
                },function(res){
                    $17.alert(res.info,function(){},function(){
                        vm.fetchUnitList(res);
                    });
                });
            },
            exchangeUnit : function(unitObj){
                var vm = this;
                $17.info(unitObj);
                vm.focusUnit = unitObj;
                vm.fetchPaperContent();
            },
            fetchPaperContent : function(){
                var vm = this,focusUnit = vm.focusUnit || {};
                $.get("/teacher/unit/test/paperlist.vpage",{
                    unitId : focusUnit.unitId,
                    subject : vm.subject
                }).done(function(res){
                    var paperTypeInfos = res.unitTestPaperInfos || [];
                    var focusPaperType = paperTypeInfos[0].paperTypeId,paperMap = {};
                    paperTypeInfos.forEach(function(paperTypeObj){
                        paperMap[paperTypeObj.paperTypeId] = paperTypeObj.papers || [];
                    });
                    vm.unitTestPaperInfos = res.unitTestPaperInfos || [];
                    vm.paperMap = paperMap;
                    vm.tabClick(vm.unitTestPaperInfos[0]);
                }).fail(function(){
                    vm.unitTestPaperInfos = [];
                });
            },
            paperClickCb : function(paperInfo){
                var vm = this;
                if(paperInfo){
                    $.get("/teacher/unit/test/preview.vpage",{
                        paperId : paperInfo.paperId,
                        subject : vm.subject
                    }).done(function(res){
                        if(res.success){
                            vm.paperInfo = res;

                            var clazzGroupIds = [];
                            vm.focusClazzs.forEach(function(clazzObj,index){
                                clazzGroupIds.push(clazzObj.clazzId + "_" + clazzObj.groupId);
                            });
                            $17.voxLog({
                                module : "m_atxqsPemeA",
                                op : "unittest_paperdetail_load",
                                s0 : clazzGroupIds.join(","),
                                s1 : vm.focusPaperTypeName,
                                s2 : paperInfo.paperId
                            })

                        }else{
                            vm.paperInfo = null;
                        }
                    }).fail(function(){
                        vm.paperInfo = null;
                    });
                }else{
                    vm.paperInfo = null;
                }
            },
            goAssignCb : function(){
                var vm = this;
                var clazzGroupIds = [],groupIds = [],clazzNames = [],paperType = vm.focusPaperTypeName;
                vm.focusClazzs.forEach(function(clazzObj,index){
                    groupIds.push(clazzObj.groupId);
                    clazzNames.push(vm.focusLevel + "年级" + clazzObj.clazzName);
                    clazzGroupIds.push(clazzObj.clazzId + "_" + clazzObj.groupId);
                });
                var paperInfo = vm.paperInfo,paperId = paperInfo.paperId;
                $17.voxLog({
                    module : "m_atxqsPemeA",
                    op : "unittest_arrange_btn_click",
                    s0 : clazzGroupIds,
                    s1 : paperType,
                    s2 : paperId
                });

                $17.comblock.confirmInfo({
                    clazzNames : clazzNames,  //一年级1班,一年级2班
                    paperInfo : {
                        paperId : paperId,
                        paperName : paperInfo.paperName,
                        questionCount : paperInfo.questionCount,
                        minutes : paperInfo.minutes,
                        examTime : paperInfo.paperTime
                    },
                    paperType : paperType,
                    assignDateTimeStr : vm.currentDate,  //布置时间--当天时间 yyyy-MM-dd HH:mm:ss
                    startDateTimeStr : vm.startDateTime,   //开始时间 yyyy-mm-dd hh:mm:ss
                    endDateTimeStr : vm.endDateTime   //结束时间 yyyy-mm-dd hh:mm:ss
                },function(res){
                    $17.voxLog({
                        module : "m_atxqsPemeA",
                        op : "unittest_comfirminfo_arrange_click",
                        s0 : clazzGroupIds.join(","),
                        s1 : paperType,
                        s2 : paperId
                    });
                    $.post("/teacher/unit/test/assign.vpage",{
                        subject : vm.subject,
                        examData : JSON.stringify({
                            paperIds : paperId,
                            groupIds : groupIds.join(","),
                            startTime : res.startDateTimeStr,
                            endTime : res.endDateTimeStr
                        })
                    }).done(function (res) {
                        if(res.success){
                            $17.alert("布置成功",function(){
                                location.href = "/teacher/newexam/list.vpage?subject=" + vm.subject;
                            });
                        }else{
                            $17.alert(res.info);
                        }
                    }).fail(function(){
                        $17.alert("网络错误，请重试");
                    });
                },function(){

                });
            }
        },
        created : function(){
            this.getClazzList();

            $17.voxLog({
                module : "m_atxqsPemeA",
                op : "unittest_paperlist_load"
            });

        }
    });
});