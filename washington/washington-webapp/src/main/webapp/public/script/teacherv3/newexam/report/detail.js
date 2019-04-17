(function(){
    function PreviewPic(){
        var self = this;
        self.questionId = ko.observable("");
        self.users      = ko.observableArray([]);
        self.focusIndex = ko.observable(0);
        self.focusUser  = ko.pureComputed(function(){
            return self.users()[self.focusIndex()];
        });
        self.loadImgUrl = function(imgUrl,imgLen){
            var imgFormat = "@410w_410h";
            if(imgLen == 3){
                imgFormat = "@270w_270h";
            }
            var newImgUrl = "";
            if(imgUrl.indexOf("upflie-img") != -1){
                newImgUrl = imgUrl;
            }else{
                newImgUrl = imgUrl + imgFormat;
            }
            return newImgUrl;
        }
    }
    PreviewPic.prototype = {
        constructor : PreviewPic,
        close       : function(){
            var self = this;
            self.users([]);
            self.questionId("");
            self.focusIndex(0);
        },
        prevOrNext  : function(step){
            var self = this;
            var _newIndex = self.focusIndex() + step;
            if(_newIndex < 0 || _newIndex >= self.users().length){
                return false;
            }
            self.focusIndex(_newIndex);
        },
        rotationImg : function(index,element){
            var imgUrl = this; //this --> imgurl
            var $element = $(element);
            //旋转图片
            var rotate270 = ["@270w_270h_0r","@270w_270h_90r","@270w_270h_180r","@270w_270h_270r"];
            var rotate410 = ["@410w_410h_0r","@410w_410h_90r","@410w_410h_180r","@410w_410h_270r"];
            // var _rotate = self.rotate() || 0;
            var _rotate = +$element.attr("rotate") || 0;
            var new_ratate = (_rotate + 1) % 4;
            if(imgUrl.indexOf("upflie-img") != -1){
                return false;
            }
            var newImgSrc = imgUrl.indexOf("@") != -1 ? (imgUrl.substring(0,imgUrl.indexOf("@"))) : imgUrl;
            if($element.parents("li").height() == 270){
                $element.parent("div").find("img").attr("src",newImgSrc + rotate270[new_ratate]);
            }else{
                $element.parent("div").find("img").attr("src",newImgSrc + rotate410[new_ratate]);
            }
            $element.attr("rotate",new_ratate);
        },
        setOptions  : function(options){
            var self = this;
            for(var i = 0,iLen = options.users.length; i < iLen; i++){
                //初始旋转角度为0
                options.users[i]['rotate'] = 0;
            }
            self.focusIndex(options.focusIndex);
            self.questionId(options.questionId);
            self.users(ko.mapping.fromJS(options.users)());

            if(typeof options.close == "function"){
                self.close = options.close;
            }
        }
    };

    function ReportDetail(customOpts){
        var self = this,opts = {
            clazzId   : null,
            newExamId : null,
            initExamSuccess : false,
            viewPic   : null,
            subject   : null
        };
        //导入用户参数值
        if ($.isPlainObject(customOpts)) {
            for (var key in opts) {
                if(opts.hasOwnProperty(key)){
                    customOpts.hasOwnProperty(key) && (opts[key] = customOpts[key])
                }
            }
        }
        self.clazzId         = opts.clazzId;
        self.newExamId       = opts.newExamId;
        self.subject         = ko.observable(opts.subject || "ENGLISH");
        self.schoolName      = ko.observable("");
        self.title           = ko.observable("");
        self.clazzName       = ko.observable("");
        self.joinCount       = ko.observable(0);
        self.submitCount     = ko.observable(0);
        self.fullScore       = ko.observable(0);
        self.examNeedCorrect = ko.observable(false);
        self.allowCorrect    = ko.observable(false);
        self.initExamSuccess = opts.initExamSuccess || false;
        self.correctStopAt   = ko.observable(null);
        self.examStopAt      = ko.observable(null);
        self.focusTab        = ko.observable("paper");
        self.prLoading       = ko.observable(true); //试卷列表加载
        self.paperReports    = ko.observableArray([]);
        self.partNames       = ko.observableArray([]);
        self.studentList     = ko.observableArray([]);
        self.resultMap       = {};
        self.viewPicObj      = opts.viewPicObj;
    }

    ReportDetail.prototype = {
        constructor     : ReportDetail,
        displayMode     : function(question,bindingContext){

            if(!question.questionNeedCorrect() && question.showType() == 0){
                return "T:OnlyUserTextAnswer";
            }else if(!question.questionNeedCorrect() && question.showType() == 1){
                return "T:UserAnswerWithPic";
            }else if(question.questionNeedCorrect() && question.showType() == 1){
                return "T:UserSubjectiveAnswerWithPic";
            }else if(question.questionNeedCorrect() && question.showType() == 2){
                if(question.isNewOral && question.isNewOral()){
                    return "T:NewSubjectiveUserAnswerWithAudio";
                }else{
                    return "T:OldUserSubjectiveAnswerWithAudio";
                }
            }else{
                return "T:NotMatchTemplate";
            }
        },
        setHead         : function(schoolName,clazzName,examName,joinCount,submitCount,correctStopAt,examNeedCorrect,allowCorrect,examStopAt,fullScore){
            var self = this;
            self.schoolName(schoolName);
            self.clazzName(clazzName);
            self.title(examName);
            self.joinCount(joinCount);
            self.submitCount(submitCount);
            self.correctStopAt(correctStopAt);
            self.examStopAt(examStopAt);
            self.examNeedCorrect(examNeedCorrect || false);
            self.allowCorrect(allowCorrect || false);
            self.fullScore(fullScore || 0);
        },
        loadStudentInfo : function(){
            var self = this;
            $.post("/teacher/newexam/report/detail/users.vpage",{
                clazzId   : self.clazzId,
                newExamId : self.newExamId
            },function(data){
                self.resultMap["student"] = (+self.resultMap["student"] || 0) + 1;
                if(data.success){
                    var users = data.users || [];
                    self.studentList(users);
                    self.partNames(data.parts);
                }
                self.prLoading(false);
            });
        },
        loadPaperReport : function(){
            var self = this;
            $.post("/teacher/newexam/report/detail/clazz.vpage",{
                clazzId   : self.clazzId,
                newExamId : self.newExamId
            },function(data){
                self.resultMap["paper"] = (+self.resultMap["paper"] || 0) + 1;
                if(data.success){
                    var partReportList = data.partQuestionInfos || [];
                    self.paperReports(ko.mapping.fromJS(partReportList)());
                    self.setHead(data.schoolName,data.clazzName,data.examName,data.joinCount,data.submitCount,data.correctStopAt,data.examNeedCorrect,data.allowCorrect,data.examStopAt,data.fullScore);
                }
                self.prLoading(false);
            });
        },
        validateUserInput : function(inputVal,initScore,maxScore){
            var reg = new RegExp("^[0-9]+(.[0-9]{1,2})?$"),userInput = +inputVal || 0;
            maxScore = +maxScore || 0;
            var maxScoreScale100 = Math.floor(maxScore * 100)
                ,inputValScale100 = Math.floor(userInput * 100)
                ,initScoreScale100 = Math.floor(initScore * 100);
            return (reg.test(inputVal) && (inputValScale100 <= maxScoreScale100) && inputValScale100 != initScoreScale100);
        },
        correctEvent    : function(element,qid,qscore,grandfather,self,subQIndex){
            // grandfather --> question object
            var that = this; //this->user object
            var $element = $(element);
            var initScore = +$element.attr("data-score") || 0;
            var inputVal = $.trim($element.val());
            if(!self.validateUserInput(inputVal,initScore,qscore)){
                $element.val(initScore);
                return false;
            }

            if($element.isFreezing()){
                return false;
            }
            var userScoreMap = {},currentUserId = that.userId(),paramData = {
                questionId   : qid,
                newExamId    : self.newExamId,
                userScoreMap : userScoreMap
            },isNewOral = (grandfather.isNewOral && grandfather.isNewOral());
            userScoreMap[that.userId()] = inputVal;
            isNewOral && (paramData["subId"] = subQIndex);
            $element.freezing();
            App.postJSON("/teacher/newexam/correct.vpage",paramData,function(data){
                $element.thaw();
                var $pResult = $element.closest("p").siblings("p.resultInfo");
                if(data.success){
                    $pResult.removeClass("txt-red").addClass("txt-green").text("修改成功").show();
                    that.score(inputVal);
                    var userCount = 0,totalScore = 0;
                    if(grandfather){
                        var _users,getUserCount = function(_users){
                            var internalTotalScore = 0;
                            for(var z = 0, zLen = _users.length; z < zLen; z++ ){
                                internalTotalScore += (+_users[z].score() || 0);
                            }
                            return internalTotalScore;
                        };
                        if(isNewOral){
                            var newOralAnswerList = grandfather.newOralAnswerList() || [];
                            _users = [];
                            for(var m = 0,mLen = newOralAnswerList.length; m < mLen; m++){
                                var subUsers = newOralAnswerList[m];
                                for(var j = 0,jLen = subUsers.length; j < jLen; j++){
                                    if(_users.indexOf(subUsers[j].userId()) == -1){
                                        _users.push(subUsers[j].userId());
                                    }
                                }
                                totalScore += getUserCount(subUsers);
                            }
                            userCount += _users.length;
                        }else{
                            var _answerList = grandfather.errorAnswerList();
                            for(var i = 0, iLen = _answerList.length; i < iLen; i++){
                                _users = _answerList[i].users() || [];
                                userCount += _users.length;
                                totalScore = getUserCount(_users);
                            }
                        }
                        if(userCount > 0){
                            grandfather.avgScore((totalScore/userCount).toFixed(2));
                        }
                    }
                }else{
                    $pResult.removeClass("txt-green").addClass("txt-red").text(data.info || '修改失败').show();
                }
                setTimeout(function(){
                    $pResult.hide();
                },2000);
            });
        },
        playAudio       : function(element,qid,rootObj){
            var that = this; //this -> user object
            var showFiles = that.showFiles() || [];
            if(showFiles.length > 0){

                if($(element).hasClass("pause")){
                    $(element).removeClass("pause");
                    $("#jquery_jplayer_1").jPlayer("clearMedia");
                }else{
                    var playIndex = 0;
                    $("#jquery_jplayer_1").jPlayer("destroy");
                    setTimeout(function(){
                        $("#jquery_jplayer_1").jPlayer({
                            ready: function (event) {
                                rootObj.playSpecialAudio(showFiles[playIndex]);
                            },
                            error : function(event){
                                playIndex = rootObj.playNextAudio(playIndex,showFiles);
                            },
                            ended : function(event){
                                playIndex = rootObj.playNextAudio(playIndex,showFiles);
                            },
                            volume: 0.8,
                            solution: "flash, html",
                            swfPath: "/public/plugin/jPlayer",
                            supplied: "mp3"
                        });
                    },200);
                    $(".voicePlayer").removeClass("pause");
                    $(element).addClass("pause");
                }
            }
        },
        playNextAudio    : function(playIndex,audioArr){
            if(playIndex >= audioArr.length - 1){
                $(".voicePlayer").removeClass("pause");
                $(this).jPlayer("destroy");
            }else{
                playIndex++;
                this.playSpecialAudio(audioArr[playIndex]);
            }
            return playIndex;
        },
        playSpecialAudio : function(url){
            if(url){
                $("#jquery_jplayer_1").jPlayer("setMedia", {
                    mp3: url
                }).jPlayer("play");
            }
        },
        concatUserNames : function(users){
            var _users = users || [];
            var names = [];
            for(var i = 0,iLen = _users.length; i < iLen; i++){
                names.push(_users[i].userName);
            }
            return names.toString();
        },
        loadExamImg     : function(parentIndex,index,qid){
            var self = this;
            if(!$17.isBlank(qid) && self.initExamSuccess){
                var $mathExamImg = $("#newExamImg" + parentIndex + "-" + index);
                $mathExamImg.empty();
                $("<div></div>").attr("id","examImg-" +  + parentIndex + "-" + index).appendTo($mathExamImg);
                vox.exam.createPreview({
                    dom : "examImg-" +  + parentIndex + "-" + index,
                    ids : [qid]
                });
            }else{
                $("#newExamImg" +  + parentIndex + "-" + index).html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
            }
            return "";
        },
        previewImg      : function(index,qid,parent,self){
            var that = this,viewPicObj = self.viewPicObj; //this --> user object
            viewPicObj && typeof viewPicObj.setOptions === 'function'
            && viewPicObj.setOptions({
                questionId : qid,
                users      : ko.mapping.toJS(parent.users()),
                focusIndex : index
            });
        },
        changeTab       : function(tab){
            var self = this;
            self.focusTab(tab);
            var accessCount = self.resultMap[tab];
            if(!accessCount){
                self.prLoading(true);
                //首次访问
                self.run();
            }
        },
        run             : function(){
            var self = this;

            switch (self.focusTab()){
                case "paper" :
                    self.loadPaperReport();
                    break;
                case "student":
                    self.loadStudentInfo();
                    break;
                default :
                    $17.info(self.focusTab() + "not found");
                    break;
            }
        },
        init            : function(){
            var self = this;
            self.run();
            return self;
        }
    };

    $17.newexam = $17.newexam || {};
    $17.extend($17.newexam, {
        getPreviewPic          : function(){
            return new PreviewPic();
        },
        getNewExamReport          : function(options){
            return new ReportDetail(options);
        }
    });

}());