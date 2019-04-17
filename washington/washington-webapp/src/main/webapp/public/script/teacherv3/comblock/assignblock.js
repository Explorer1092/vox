/* 基于vue的业务布置模块*/

/* levelclazz.js */
(function(){
    /*
    * 班级样例： {"success":true,"clazzList":[{"clazzLevel":4,"clazzLevelName":"四年级","canBeAssigned":false,"clazzs":[{"clazzId":33878894,"clazzName":"1班","fullName":"四年级1班","clazzLevel":4,"groupId":20056,"hasAssignedExams":true},{"clazzId":17931970,"clazzName":"20班","fullName":"四年级20班","clazzLevel":4,"groupId":10891,"hasAssignedExams":true}]},{"clazzLevel":5,"clazzLevelName":"五年级","canBeAssigned":true,"clazzs":[{"clazzId":33878916,"clazzName":"3班","fullName":"五年级3班","clazzLevel":5,"groupId":100507,"hasAssignedExams":false}]}]}
    * */
    var levelClazz = {
        template : template("T:LEVEL_CLAZZS_TPL",{}),
        data : function(){
            var levelList = [];
            var levelClazzMap = {};
            var focusLevel = 0;
            var groupIds = [];
            var vm = this;
            this.clazzList.forEach(function(levelObj,index,array){
                var clazzLevel = levelObj["clazzLevel"];
                levelList.push({
                    clazzLevel : clazzLevel,
                    name : clazzLevel + "年级"
                });
                var clazzs = levelObj.clazzs || [];
                if(index === 0){
                    focusLevel = clazzLevel;
                    groupIds = vm.fetchGroupIds(clazzs);
                }
                levelClazzMap[clazzLevel] = clazzs;
            });
            vm.$emit("level-click",[].concat(levelClazzMap[focusLevel]),focusLevel);
            return {
                levelList : levelList,
                levelClazzMap : levelClazzMap,
                focusLevel : focusLevel,
                groupIds : groupIds
            };
        },
        computed : {
            isAllChecked : function(){
                var vm = this;
                //是否全选
                return this.levelClazzList.every(function(clazzObj){
                     return  vm.groupIds.indexOf(clazzObj.groupId) !== -1;
                });
            },
            levelClazzList : function(){
                return this.focusLevel > 0 ? (this.levelClazzMap[this.focusLevel] || []) : [];
            }
        },
        props : {
            clazzList : {
                type : Array,
                default : []
            }
        },
        methods : {
            fetchGroupIds : function(clazzs){
                var groupIds = [];
                clazzs.forEach(function(clazzObj){
                    groupIds.push(clazzObj["groupId"]);
                });
                return groupIds;
            },
            fetchClazzContainGroupIds : function(groupIds){
                var vm = this;
                return vm.levelClazzList.filter(function(clazzObj){
                    return groupIds.indexOf(clazzObj["groupId"]) !== -1;
                });
            },
            levelClick : function(levelObj){
                var vm = this;
                vm.focusLevel = levelObj.clazzLevel;
                vm.groupIds = vm.fetchGroupIds(this.levelClazzList);
                vm.$emit("level-click",[].concat(this.levelClazzList),levelObj.clazzLevel);
            },
            singleClazzAddOrCancel : function(clazzObj){
                var vm = this;
                var groupId = clazzObj["groupId"];
                var zIndex = vm.groupIds.indexOf(groupId);
                if(zIndex === -1){
                    vm.groupIds.push(groupId);
                }else{
                    vm.groupIds.splice(zIndex,1);
                }

                vm.$emit("clazz-click",vm.fetchClazzContainGroupIds(this.groupIds),vm.focusLevel);
            },
            chooseOrCancelAll : function(){
                this.groupIds = this.isAllChecked ? [] : this.fetchGroupIds(this.levelClazzList);

                vm.$emit("clazz-click",vm.fetchClazzContainGroupIds(this.groupIds),vm.focusLevel);
            }
        }
    };

    $17.comblock = $17.comblock || {};
    $17.extend($17.comblock, {
        levelClazz   : levelClazz
    });
}());

/*book.js*/
(function(){
    // 展示课本和单元列表相关信息
    var bookUnits = {
        template: template("T:BOOK_UNIT",{}),
        data : function(){
            $17.info("unitId" + this.unitId);
            this.$emit("exchange-unit",{
                unitId : this.unitId,
                cname : this.unitName
            });
            return {
                focusUnitId : this.unitId,
                focusUnitName : this.unitName
            };
        },
        watch : {
            unitId : function(newValue,oldValue){
                this.focusUnitId = newValue;
                this.$emit("exchange-unit",{
                    unitId : this.unitId,
                    cname : this.unitName
                });
            },
            unitName : function(newValue,oldValue){
                this.focusUnitName = newValue;
            }
        },
        computed : {
            unitObj : function () {
                this.focusUnitId = this.unitId;
                this.focusUnitName = this.unitName;

                this.$emit("exchange-unit",{
                    unitId : this.unitId,
                    cname : this.unitName
                });
            }
        },
        props : {
            bookId : {
                type : String,
                default : ""
            },
            bookName : {
                type : String,
                default : ""
            },
            unitId : {
                type : String,
                default : ""
            },
            unitName : {
                type : String,
                default : ""
            },
            unitList : {
                type : Array,
                default : function(){
                    return [];
                }
            },
            moduleList : {
                type : Array,
                default : function(){
                    return [];
                }
            }
        },
        methods : {
            changeBook : function(){
                var vm = this;
                vm.$emit("exchange-book");
            },
            changeUnit : function(unitObj){
                var vm = this;
                vm.focusUnitId = unitObj.unitId;
                vm.focusUnitName = unitObj.cname;
                vm.$emit("exchange-unit",{
                    unitId : unitObj.unitId,
                    cname : unitObj.cname
                });
            }
        }

    };

    $17.comblock = $17.comblock || {};
    $17.extend($17.comblock, {
        bookUnits   : bookUnits
    });

}());

/* exchange-book.js */
(function(){
    //换课本模块
    var defaultOption = {
        level : 1,
        term : 2,
        clazzGroupIds:[],
        bookName : "", //默认选择的课本
        subject : null,
        isSaveBookInfo : true,  //是否保存课本信息
        bookListUrl : "/teacher/new/homework/sortbook.vpage"  //获取课本列表地址
    };
    function exchangeBook(option,closeChangeBook){
        var bookHtml = template("t:换课本",{});
        var newOption = $.extend(true,{},defaultOption,option);
        var bookVue;
        closeChangeBook = typeof closeChangeBook === "function" ? closeChangeBook : function(){};
        $.prompt(bookHtml,{
            title : "换课本",
            buttons : {},
            position: { width : 690},
            loaded : function(){
                bookVue = new Vue({
                    el : "#bookListV5",
                    data : $.extend(true,{},newOption,{
                        termList : [{term : 1,name:"上册"},{term:2,name:"下册"}],
                        levelList : [{
                            level : 1,
                            name : "一年级"
                        },{
                            level : 2,
                            name : "二年级"
                        },{
                            level : 3,
                            name : "三年级"
                        },{
                            level : 4,
                            name : "四年级"
                        },{
                            level : 5,
                            name : "五年级"
                        },{
                            level : 6,
                            name : "六年级"
                        }],
                        selectBookId : "",
                        selectBookName : "",
                        searchText : "",
                        bookList : [],
                        levelTermBookMap : {},  //key : level_term
                        noFilterRes : false,
                        submiting : false  //按钮点击后，置一个状态锁
                    }),
                    methods : {
                        changeBookTermClick : function(termObj){
                            if(!this.isLoadData){
                                this.term = termObj.term;
                                this.fetchBookList();
                            }
                        },
                        changeBookLevelClick : function(levelObj){
                            if(!this.isLoadData){
                                this.level = levelObj.level;
                                this.fetchBookList();
                            }
                        },
                        noBookFeedBack : function(){

                        },
                        saveChangeBook : function(){
                            var vm = this;
                            if(!vm.selectBookId){
                                return false;
                            }
                            if(vm.submiting){
                                return false;
                            }
                            vm.submiting = true;
                            if(vm.isSaveBookInfo){
                                $17.voxLog({
                                    module: "m_H1VyyebB",
                                    op : "popup_changeBook_confirm_click",
                                    s0 : vm.subject
                                });
                                $.post("/teacher/new/homework/changebook.vpage", {
                                    clazzs  : vm.clazzGroupIds.join(","),
                                    bookId  : vm.selectBookId,
                                    subject : vm.subject
                                }).done(function(data){
                                    vm.submiting = false;
                                    $.prompt.close();
                                    closeChangeBook($.extend(true,data,{
                                        bookId : vm.selectBookId,
                                        bookName : vm.selectBookName
                                    }));
                                }).fail(function(){
                                    vm.submiting = false;
                                });
                            }else{
                                $.prompt.close(true);
                                closeChangeBook({
                                    success : true,
                                    info : "更新成功",
                                    bookId : vm.selectBookId,
                                    bookName : vm.selectBookName
                                });
                            }
                        },
                        filterBook : function(){
                            var vm = this,count=0;
                            var searchText = vm.searchText.trim();
                            vm.isSaveBookInfo && $17.voxLog({
                                module: "m_H1VyyebB",
                                op : "popup_changeBook_search_click",
                                s0 : self.subject
                            });
                            $.each(vm.bookList,function(index,bookObj){
                                if(searchText && bookObj.name.toLowerCase().indexOf(searchText) === -1){
                                    count += 1;
                                    bookObj.isShow = false;
                                }else{
                                    bookObj.isShow = true;
                                }
                            });
                            vm.noFilterRes = (vm.bookList.length == count);
                        },
                        bookClick : function(book){
                            var vm = this;
                            vm.selectBookId  = book.id;
                            vm.selectBookName  = book.name;
                            vm.isSaveBookInfo && $17.voxLog({
                                module: "m_H1VyyebB",
                                op : "popup_changeBook_bookName_click",
                                s0 : self.subject
                            });
                        },
                        fetchBookList : function(){
                            var vm = this;
                            var _level = vm.level,_term = vm.term;
                            var _levelTerm = _level + "_" + _term;
                            vm.searchText = "";
                            vm.noFilterRes = false;
                            if(!vm.levelTermBookMap.hasOwnProperty(_levelTerm)){
                                vm.isLoadData = true;
                                var paramData = {
                                    level: _level,
                                    term : _term,
                                    subject : vm.subject
                                };
                                $.get(vm.bookListUrl, paramData).done(function(data){
                                    vm.isLoadData = false;
                                    if(data.success){
                                        $.each(data.rows,function(){
                                            this.isShow = true;
                                        });
                                        vm.levelTermBookMap[_levelTerm] = data.rows;
                                        vm.bookList = data.rows;
                                    }else{
                                        vm.levelTermBookMap[_levelTerm] = [];
                                        vm.bookList = [];
                                        $17.voxLog({
                                            module : "API_REQUEST_ERROR",
                                            op     : "API_STATE_ERROR",
                                            s0     : "/teacher/new/homework/sortbook.vpage",
                                            s1     : $.toJSON(data),
                                            s2     : $.toJSON(paramData),
                                            s3     : $uper.env
                                        });
                                    }
                                }).fail(function(){
                                    vm.isLoadData = false;
                                });
                            }else{
                                vm.bookList = vm.levelTermBookMap[_levelTerm];
                            }
                        }
                    },
                    beforeCreate : function(){
                        $17.info("bookListV5 beforeCreate");
                    },
                    created : function(){
                        $17.info("bookListV5 created");
                        this.fetchBookList();
                    },
                    beforeMount : function(){
                        $17.info("bookListV5 beforeMount");
                    },
                    mounted : function(){
                        $17.info("bookListV5 mounted");
                    },
                    beforeDestroy : function(){
                        $17.info("bookListV5 beforeDestroy");
                    },
                    destroyed : function(){
                        $17.info("bookListV5 destroyed");
                    }
                });
            },
            close : function(){
                bookVue && bookVue.$destroy();
            }
        });
    }

    $17.comblock = $17.comblock || {};
    $17.extend($17.comblock, {
        exchangeBook   : exchangeBook
    });

}());


/* paperlist.js 试卷列表模块*/

(function(){
    var paperList = {
        template : template("T:PAPER_LIST",{}),
        data : function(){
            var papers = this.papers || [];
            var paperInfo = papers.length > 0 ? papers[0] : null;
            this.$emit("paper-click",paperInfo ? $.extend(true,{},paperInfo) : null);
            return {
                paperId : paperInfo ? paperInfo.paperId : ""
            };
        },
        watch : {
            papers : function(newPapers,oldPapers){
                var papers = newPapers || [];
                var paperInfo = papers.length > 0 ? papers[0] : null;
                this.$emit("paper-click",paperInfo ? $.extend(true,{},paperInfo) : null);
                this.paperId = paperInfo ? paperInfo.paperId : "";
            }
        },
        props : {
            papers : {
                type : Array,
                default : function(){
                    return [];
                }
            }
        },
        methods : {
            paperClick : function(paperInfo){
                this.paperId = paperInfo.paperId;
                this.$emit("paper-click",$.extend(true,{},paperInfo));
            }
        },
        created : function(){

        }
    };
    $17.comblock = $17.comblock || {};
    $17.extend($17.comblock, {
        paperList   : paperList
    });
}());

/* paperinfo.js 试卷详情模块,依赖freya模块*/
(function(){
    var freyaElem = null;    //题目窗口元素
    var paperInfo = {
        template : template("T:PAPER_INFO",{}),
        data : function(){
            return {
                questionEntityMap : {} //题目实体映射
            };
        },
        watch: {
            modules : function(newValue, oldValue){
                this.fetchQuestions();
            }
        },
        props : {
            paperId : {
                type : String,
                default : ""
            },
            paperName : {
                type : String,
                default : ""
            },
            questionCount : {
                type : Number,
                default : 0
            },
            minutes : {
                type : Number,
                default : 0
            },
            modules : {
                type : Array,
                default : function(){
                    return []
                }
            },
            totalScore : {
                type : Number,
                default : 0
            },
            description : {
                type : String,
                default : ""
            }
        },
        computed : {
            subQuestionNumberMap : function(){
                var subQuestionNumberMap = {};
                this.modules.forEach(function(moduleObj,mIndex) {
                    var subQuestionsLen = moduleObj.subQuestions.length;
                    moduleObj.subQuestions.forEach(function (squestion, index) {
                        var questionId = squestion.qid;
                        subQuestionNumberMap[questionId + "_" + squestion.subIndex] = $.extend(true, squestion, {
                            mePartTitle: $17.Arabia_To_SimplifiedChinese(mIndex + 1) + '、' + moduleObj.moduleName,
                            moduleNumber: (mIndex + 1),
                            moduleFirstSubQuestion: index === 0,
                            moduleLastSubQuestion: (index === subQuestionsLen - 1)
                        });
                    });
                });
                return subQuestionNumberMap;
            },
            qids : function(){
                var qids = [];
                this.modules.forEach(function(moduleObj){
                    moduleObj.subQuestions.forEach(function(squestion,index){
                        if (squestion.subIndex === 0) {
                            qids.push(squestion.qid);
                        }
                    });
                });
                // $17.info("qids:" + JSON.stringify(qids));
                return qids;
            }
        },
        methods : {
            goAssign : function(){
                this.$emit("go-assign");
            },
            updateContentMainHeight : function(){
                document.getElementById("J_paperContent").style.height =  freyaElem.offsetHeight + 150 + 'px';
            },
            loadQuestionContent: function () {
                var vm = this;
                var questionIds = vm.qids;
                var questionEntityMap = vm.questionEntityMap;
                var questionObjects = [];
                for(var m = 0; m < questionIds.length; m++){
                    questionObjects.push(questionEntityMap[questionIds[m]]);
                }

                var moduleElem,questionListContainerElem;
                var testData = {
                    container: "#J_paperContent",
                    renderOptions: {
                        showAnswerAnalysis : true,
                        state: 1,
                        active : false,
                        showAudioOperation : true
                    },
                    questions: questionObjects,
                    onFreyaUpdated : function(freya){
                        freya.contentContainer.style.overflow = "visible";
                        freya.questionContainer.style.width = "100%";
                        freyaElem = freya.contentContainer;
                    },
                    onQuestionRender : function(target,question){
                        var qid = question.id;
                        target.container.style = "margin-left:20px;";
                        target.questionContainer.style="margin-top:10px;";
                        target.description.style = "margin-top:20px;";
                        var paperSubQuestion = vm.subQuestionNumberMap[qid + "_" + 0];

                        //题目容器
                        var questionContainerElem = document.createElement("div");
                        questionContainerElem.style.cssText = "position:relative;";
                        questionContainerElem.appendChild(target.container);

                        if(paperSubQuestion.moduleFirstSubQuestion){
                            //模块标题
                            var moduleTitleElem = document.createElement("h5");
                            moduleTitleElem.innerHTML = paperSubQuestion.mePartTitle;
                            moduleTitleElem.className = "sub-title";
                            //模块题目列表容器
                            questionListContainerElem = document.createElement("div");
                            questionListContainerElem.appendChild(questionContainerElem);

                            moduleElem = document.createElement("div");
                            moduleElem.className = "subject-type";
                            moduleElem.appendChild(moduleTitleElem);
                            moduleElem.appendChild(questionListContainerElem);
                            return moduleElem;
                        }else{
                            questionListContainerElem.appendChild(questionContainerElem);
                        }
                    },
                    onSubQuestionRender : function(target,subQuestion,question){
                        //复合题回调
                        var subIndex = question.content.subContents.indexOf(subQuestion);
                        if(subIndex === -1){
                            return false;
                        }
                        var qid = question.id;
                        var targetTemp = vm.createSubQuestionNumber(target,qid,subIndex);

                        if(questionIds[questionIds.length - 1] === qid && ((question.content.subContents.length - 1) === subIndex)){
                            $17.info("最后一道题");
                            //最后一道题
                            setTimeout(function(){
                                vm.updateContentMainHeight();
                                moduleElem = null;
                                questionListContainerElem = null;
                            },1000);
                        }
                        return targetTemp;
                    }
                };
                Freya.render(testData);
                return "";
            },
            createSubQuestionNumber : function(target,qid,subIndex){
                var self = this;
                var paperSubQuestion = self.subQuestionNumberMap[qid + "_" + subIndex];
                //创建题号html
                var iElement = document.createElement("i");
                iElement.innerText = paperSubQuestion.index;
                var className = iElement.className;
                iElement.className = className ? className + " " + "question-number" : "question-number";
                var fragment = document.createDocumentFragment();
                var subQuestionElem = target.subQuestion;
                subQuestionElem.id = "anchor_id_" + paperSubQuestion.index;
                subQuestionElem.style.cssText = "display:flex;align-items: flex-start;margin-top:10px;";
                subQuestionElem.insertBefore(iElement,subQuestionElem.children[0]);
                fragment.appendChild(subQuestionElem);
                fragment.appendChild(self.getAnswerAreaTarget(target,qid,subIndex));
                return fragment;
            },
            getAnswerAreaTarget : function(target,qid,subIndex){
                var self = this;
                var paperSubQuestion = self.subQuestionNumberMap[qid + "_" + subIndex];
                var answerAreaHtml = template("TPL_DESCRIPTION", {
                    standardScore : paperSubQuestion.standardScore
                });
                var answerAreaDivElement = document.createElement("div");
                answerAreaDivElement.innerHTML = answerAreaHtml;
                answerAreaDivElement.children[0].appendChild(target.answer.container);
                var fragment = document.createDocumentFragment();
                fragment.appendChild(answerAreaDivElement);
                return fragment;
            },
            fetchQuestions : function(){
                var vm = this;
                $.get("/exam/flash/load/newquestion/byids.vpage",{
                    data : JSON.stringify({
                        ids : vm.qids,
                        containsAnswer : true
                    })
                }).done(function(res){
                    if(res && res.success){
                        var questionCollect = res.result || [];
                        var questionEntityMap = {};
                        for(var m = 0,mLen = questionCollect.length; m < mLen; m++){
                            var questionObj = questionCollect[m];
                            questionEntityMap[questionObj.id] = questionObj;
                        }
                        vm.questionEntityMap = questionEntityMap;
                        vm.loadQuestionContent();
                    }else{
                        $17.alert("获取题目失败");
                    }
                }).fail(function(){

                });
            }
        },
        created : function(){
            this.fetchQuestions();
        },
        mounted: function mounted() {

        },
        beforeDestroy : function(){
            freyaElem = null;
        }
    };


    $17.comblock = $17.comblock || {};
    $17.extend($17.comblock, {
        paperInfo   : paperInfo
    });
}());

/* confirm.js 确认模块*/
(function(){

    function splitDateTime(dateTime){
        return dateTime.split(/:|-|\s/g);
    }

    function getTimeArray(array, index){
        return $.grep(array, function (val, key) {
            return val >= index;
        });
    }

    var defaultOption = {
        clazzNames : [],  //一年级1班,一年级2班
        paperInfo : {
            paperId : "",
            paperName : "",
            questionCount : 0,
            minutes : 0,
            examTime : 0  //作答限时(分钟)
        },
        paperType : "单元检测",
        assignDateTimeStr : "",  //布置时间--当前时间
        startDateTimeStr : "",   //开始时间 yyyy-mm-dd hh:mm:ss
        endDateTimeStr : ""   //结束时间 yyyy-mm-dd hh:mm:ss
    };

    var h = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'];
    var m = [
        '00', '01', '02', '03', '04', '05', '06', '07', '08', '09',
        '10', '11', '12', '13', '14', '15', '16', '17', '18', '19',
        '20', '21', '22', '23', '24', '25', '26', '27', '28', '29',
        '30', '31', '32', '33', '34', '35', '36', '37', '38', '39',
        '40', '41', '42', '43', '44', '45', '46', '47', '48', '49',
        '50', '51', '52', '53', '54', '55', '56', '57', '58', '59'
    ];

    function confirmInfo(option,submitFn,closeFn){
        var confirmHtml = template("T:CONFIRM",{});
        var newOption = $.extend(true,{},defaultOption,option);
        var confirmVue;
        submitFn = typeof submitFn === "function" ? submitFn : function(){};
        closeFn = typeof closeFn === "function" ? closeFn : function(){};

        var assignDateArr = splitDateTime(newOption.assignDateTimeStr);
        var startTimeArr = splitDateTime(newOption.startDateTimeStr);
        var endTimeArr = splitDateTime(newOption.endDateTimeStr);
        var paramData = $.extend(true,{},newOption,{
            startHourSelect : getTimeArray(h,startTimeArr[3]),
            startMinSelect : getTimeArray(m,startTimeArr[4]),
            startDateInput : newOption.startDateTimeStr.substring(0,10),
            startHour : startTimeArr[3],
            startMin : startTimeArr[4],
            endHourSelect : h,
            endMinSelect : m,
            endDateInput : newOption.endDateTimeStr.substring(0,10),
            endHour : endTimeArr[3],
            endMin : endTimeArr[4]
        });
        $.prompt(confirmHtml,{
            title : "布置单元检测",
            buttons : {},
            position: { width : 690},
            loaded : function(){
                confirmVue = new Vue({
                    el : "#saveMathDialog",
                    data : paramData,
                    watch : {
                        startHour : function(newValue,oldValue){
                            var vm = this;
                            var startDayDiff = $17.DateDiff(vm.startDateInput,assignDateArr.slice(0,3).join("-"),"d");
                            var defaultHour = "00";
                            var defaultMin = "00";
                            if(startDayDiff === 0){
                                defaultHour = startTimeArr[3];
                                if(assignDateArr[3] === newValue){
                                    defaultMin = startTimeArr[4];
                                }
                            }
                            vm.startHourSelect = getTimeArray(h,defaultHour);
                            vm.startMinSelect = getTimeArray(m,defaultMin);
                        }
                    },
                    methods : {
                        saveHomework : function(){
                            var vm = this;
                            submitFn({
                                startDateTimeStr : vm.startDateInput + " " + vm.startHour + ":" + vm.startMin + ":00",
                                endDateTimeStr : vm.endDateInput + " " + vm.endHour + ":" + vm.endMin + ":59"
                            });
                        }
                    },
                    beforeCreate : function(){
                        $17.info("saveMathDialog beforeCreate");
                    },
                    created : function(){
                        $17.info("saveMathDialog created");
                    },
                    beforeMount : function(){
                        $17.info("saveMathDialog beforeMount");
                    },
                    mounted : function(){
                        $17.info("saveMathDialog mounted");
                        var vm = this;
                        this.$nextTick(function(){
                            var _minDate = vm.assignDateTimeStr.substring(0,10);
                            $("#startDateInput").datepicker({
                                dateFormat      : 'yy-mm-dd',
                                defaultDate     : vm.startDateInput,
                                numberOfMonths  : 1,
                                minDate         : _minDate,
                                maxDate         : null,
                                onSelect        : function(selectedDate){
                                    var startDayDiff = $17.DateDiff(selectedDate,_minDate,"d");
                                    var defaultHour = "00";
                                    var defaultMin = "00";
                                    if(startDayDiff === 0){
                                        defaultHour = startTimeArr[3];
                                        defaultMin = startTimeArr[4];
                                    }
                                    vm.startHourSelect = getTimeArray(h,defaultHour);
                                    vm.startMinSelect = getTimeArray(m,defaultMin);
                                    vm.startDateInput = selectedDate;

                                    $("#endDateInput").datepicker( "option", { minDate: selectedDate } );
                                    var dayDiff = $17.DateDiff(selectedDate,vm.endDateInput,"d");
                                    if(dayDiff <= 0){
                                        var endDateTime = $17.Date(selectedDate + " 23:59:59").getTime();
                                        endDateTime += 86400000;
                                        vm.endDateInput = $17.DateUtils("%Y-%M-%d", 0, "d",new Date(endDateTime));
                                        vm.endHour = "23";
                                        vm.endMin = "59";
                                    }
                                }
                            });

                            $("#endDateInput").datepicker({
                                dateFormat      : 'yy-mm-dd',
                                defaultDate     : vm.endDateInput,
                                numberOfMonths  : 1,
                                minDate         : _minDate,
                                maxDate         : null,
                                onSelect        : function(selectedDate){
                                    vm.endDateInput = selectedDate;
                                }
                            });
                        });
                    },
                    beforeDestroy : function(){
                        $17.info("saveMathDialog beforeDestroy");
                    },
                    destroyed : function(){
                        $17.info("saveMathDialog destroyed");
                    }
                });
            },
            close : function(){
                confirmVue && confirmVue.$destroy();
            }
        });

    }


    $17.comblock = $17.comblock || {};
    $17.extend($17.comblock, {
        confirmInfo   : confirmInfo
    });
}());




