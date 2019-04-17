$(function(){
   "use strict";

   function Dictation(){
       var self = this;
       self.ctLoading = ko.observable(true);
       self.title = ko.observable(null);
       self.totalCount = ko.observable(0);  //当前包下所有的单词数（每个模块下的单词数总和）
       self.totalMin = ko.observable(0);  //当前包的总时间
       self.lessonList = ko.observableArray([]); //当前包的模块列表
       self.lessonIds = ko.observableArray([]); //选中的lessonId集合
       self.questionIds = ko.observableArray([]); //选中的题目集合
       self.previewLessonBtns = ko.observableArray([]); //lesson下的预览按钮是否显示
       self.option = {};
       self.carts = null;
       self.clazzGroupIdsStr = "";
   }

   var isInitUFO = false;
   Dictation.prototype = {
       constructor : Dictation,
       initialise : function(config){
           this.option = $.extend(true,{},config);
           this.carts = config.carts || null;
           if(!isInitUFO){
               isInitUFO = true;
               var str = ["<span class=\"name\">" + config.tabTypeName +"</span>" +
               "<span class=\"count\" data-count=\"0\">0</span>" +
               "<span class=\"icon\"><i class=\"J_delete h-set-icon-delete h-set-icon-deleteGrey\"></i></span>"].join("");
               $(".J_UFOInfo p[type='" + config.tabType + "']").html(str);
           }
       },
       run : function(param){
           var self = this,option = self.option,paramData = {
               bookId   : option.bookId,
               unitId   : option.unitId,
               sections : "", //[].toString()
               type     : option.tabType,
               subject  : option.subject,
               objectiveConfigId : option.objectiveConfigId
           };
           self.clazzGroupIdsStr = param.clazzGroupIdsStr || "";
           self.ctLoading(true);
           self.typeSelectPopup(function(){
               //获取购题车中的题目ID
               var selectQuestionIds = [];
               var previewLessonBtns = [];
               var cartQuestions = constantObj._homeworkContent.practices[option.tabType].questions || [];
               for(var z = 0,zLen = cartQuestions.length; z < zLen; z++){
                   selectQuestionIds.push(cartQuestions[z].questionId);
                   if(previewLessonBtns.indexOf(cartQuestions[z].lessonId) === -1){
                       previewLessonBtns.push(cartQuestions[z].lessonId);
                   }
               }
               self.questionIds(selectQuestionIds);
               self.previewLessonBtns(previewLessonBtns);
               self.lessonIds([]);
               $.get("/teacher/new/homework/objective/content.vpage",paramData,function(data){
                   if(data.success){
                       var content = data.content || [];
                       var questionCount = 0,totalSeconds = 0;
                       var selectLessonIds = [];
                       for(var m = 0,mLen = content.length; m < mLen; m++){
                           var questions = $.isArray(content[m].questions) ? content[m].questions : [];
                           questionCount += questions.length;
                           //lesson下题目是否全选
                           var selectAllQuestion = true;
                           for(var j = 0,jLen = questions.length; j < jLen; j++){
                               totalSeconds += questions[j]["seconds"] || 0;
                               selectAllQuestion = selectAllQuestion && (selectQuestionIds.indexOf(questions[j]["id"]) !== -1);
                           }
                           selectAllQuestion && (selectLessonIds.push(content[m].lessonId));
                       }
                       self.totalCount(questionCount);
                       self.totalMin(Math.ceil(totalSeconds/60));
                       self.lessonList(content);
                       self.lessonIds(selectLessonIds);

                       $17.voxLog({
                           module : "m_H1VyyebB",
                           op : "pc_WD_word_selection_page_load",
                           s0 : self.clazzGroupIdsStr,
                           s1 : !!constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] ? "纸质拍照听写" : "线上键盘听写"
                       });
                   }else{
                       $17.voxLog({
                           module : "API_REQUEST_ERROR",
                           op     : "API_STATE_ERROR",
                           s0     : "/teacher/new/homework/content.vpage",
                           s1     : $.toJSON(data),
                           s2     : $.toJSON(paramData),
                           s3     : option.env
                       });
                   }
                   self.ctLoading(false);
               });
           });
       },
       typeSelectPopup : function(callback){
           var self = this,option = self.option;
           callback = (typeof callback === "function" ? callback : function(){});
           var questions = constantObj._homeworkContent.practices[option.tabType]["questions"];
           if($.isArray(questions) && questions.length > 0){
               callback.call(self);
               return false;
           }
           var submitWays = [{
               id : "OCR_DICTATION",
               name : "纸质拍照听写",
               ocrDictation : true,
               checked : true  //默认选中
           },{
               id : "ONLINE_DICTATION",
               name : "线上键盘听写",
               ocrDictation : false,
               checked : false
           }];
           var viewModel = {
               submitWays : submitWays,
               selectWay : ko.observable(submitWays[0]),
               switchWay : function(that){
                   that.selectWay(this);
               }
           };
           $.prompt(template("t:SUBMIT_WAY_SELECT_POPUP",{}), {
               title   : "预 览",
               buttons : {"取消": false,"确认": true},
               position: { width: 500 },
               loaded : function(){
                   $17.voxLog({
                      module : "m_H1VyyebB",
                      op : "pc_WD_dictation_type_selection_popover_load",
                      s0 : self.clazzGroupIdsStr
                   });
                   ko.applyBindings(viewModel, document.getElementById('jqistate_state0'));
               },
               submit  : function(e,v,m,f){
                    e.preventDefault();
                    $.prompt.close(true);
                   var selectWay = viewModel.selectWay();
                    $17.voxLog({
                        module : "m_H1VyyebB",
                        op : v ? "pc_WD_dictation_type_selection_popover_ok-button_click" : "pc_WD_dictation_type_selection_popover_cancel_button_click",
                        s0 : self.clazzGroupIdsStr,
                        s1 : v ? selectWay.name : ""
                    });
               },
               close   : function(v,m,f){
                   var selectWay = viewModel.selectWay();
                   //更新标题及购物车字段
                   self.title(selectWay.name);
                   constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] = selectWay["ocrDictation"];
                   callback.call(self);
               }
           });
       },
       addOrRemovePackage : function(){
           //全部lesson选入或移除
           var self = this,option = self.option,selectLessonIds = self.lessonIds();
           var lessonList = self.lessonList();
           if(selectLessonIds.length >= lessonList.length){
                //移除操作
               for(var t = 0,tLen = lessonList.length; t < tLen; t++){
                   self.removeLesson.call(lessonList[t],self);
               }
               $17.voxLog({
                   module : "m_H1VyyebB",
                   op : "pc_WD_word_selection_page_all_in/all_out_button_click",
                   s0 : self.clazzGroupIdsStr,
                   s1 : !!constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] ? "纸质拍照听写" : "线上键盘听写",
                   s2 : "移除"
               });
           }else{
               //未选中的筛选出来
               var unSelectLessons = [];
               for(var m = 0,mLen = lessonList.length; m < mLen; m++){
                   selectLessonIds.indexOf(lessonList[m].lessonId) === -1 && (unSelectLessons.push(lessonList[m]));
               }
               //添加操作
               for(var k = 0,kLen = unSelectLessons.length; k < kLen; k++){
                   self.addLesson.call(unSelectLessons[k],self);
               }

               $17.voxLog({
                   module : "m_H1VyyebB",
                   op : "pc_WD_word_selection_page_all_in/all_out_button_click",
                   s0 : self.clazzGroupIdsStr,
                   s1 : !!constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] ? "纸质拍照听写" : "线上键盘听写",
                   s2 : "选入"
               });
           }
       },
       addLesson : function(self){
           var lessonObj = this,option = self.option,selectQuestionIds = self.questionIds();
           var questions = lessonObj.questions || [];
           //找出未添加的题目添加
           var unCheckedQuestions = [];
           for(var t = 0,tLen = questions.length; t < tLen; t++){
               selectQuestionIds.indexOf(questions[t].id) === -1 && (unCheckedQuestions.push(questions[t]));
           }
           for(var m = 0, mLen = unCheckedQuestions.length; m < mLen; m++){
               self.addOrRemoveQuestion.call(unCheckedQuestions[m],lessonObj,self);
           }

           $17.voxLog({
               module : "m_H1VyyebB",
               op : "pc_WD_word_selection_page_select/removet_button_click",
               s0 : self.clazzGroupIdsStr,
               s1 : !!constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] ? "纸质拍照听写" : "线上键盘听写",
               s2 : "选入"
           });
       },
       removeLesson : function(self){
           var lessonObj = this,option = self.option,selectQuestionIds = self.questionIds();
           //找出添加的题目移除
           var questions = lessonObj.questions || [];
           var deleteQuestions = [];
           for(var t = 0,tLen = questions.length; t < tLen; t++){
               selectQuestionIds.indexOf(questions[t].id) !== -1 && (deleteQuestions.push(questions[t]));
           }
           for(var m = 0, mLen = deleteQuestions.length; m < mLen; m++){
               self.addOrRemoveQuestion.call(deleteQuestions[m],lessonObj,self);
           }
           $17.voxLog({
               module : "m_H1VyyebB",
               op : "pc_WD_word_selection_page_select/removet_button_click",
               s0 : self.clazzGroupIdsStr,
               s1 : !!constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] ? "纸质拍照听写" : "线上键盘听写",
               s2 : "移除"
           });
       },
       _resetLessonStatus : function(lessonObj){
            var self = this,selectQuestionIds = self.questionIds();
            var lessonId = lessonObj.lessonId;
            var lessonIdsKo = self.lessonIds;
            var lessonIdIndex = lessonIdsKo.indexOf(lessonId);

            var questions = lessonObj.questions || [];
            //lesson下的题是否全选中
            var selectQuestonsAll = true;
            for(var t = 0,tLen = questions.length; t < tLen; t++){
                selectQuestonsAll = selectQuestonsAll && (selectQuestionIds.indexOf(questions[t].id) !== -1);
                if(!selectQuestonsAll){
                    break;
                }
            }
            if(!selectQuestonsAll && lessonIdIndex > -1){
                lessonIdsKo.splice(lessonIdIndex,1);
            }else if(selectQuestonsAll && lessonIdIndex === -1){
                lessonIdsKo.push(lessonId);
            }
       },
       addOrRemoveQuestion : function(lessonObj,self,single){
           //single参数值single时表示通过点击单个单词触发的此方法
           var questionObj = this,option = self.option;
           var questionIdsKo = self.questionIds;
           var previewLessonBtnsKo = self.previewLessonBtns;
           var questionIndex = questionIdsKo.indexOf(questionObj.id);
           if(questionIndex === -1){
               constantObj._homeworkContent.practices[option.tabType].questions.push({
                   questionId    : questionObj.id,
                   seconds       : questionObj.seconds,
                   submitWay     : questionObj.submitWay,
                   book          : questionObj.book || null,
                   objectiveId   : option.objectiveTabType,
                   lessonId      : lessonObj.lessonId
               });
               constantObj._reviewQuestions[option.tabType].push($.extend(true,{},questionObj,{
                   lessonId : lessonObj.lessonId,
                   lessonName : lessonObj.lessonName
               }));
               self.updateUfoExam(questionObj.seconds,constantObj._homeworkContent.practices[option.tabType].questions.length);
               questionIdsKo.push(questionObj.id);
               previewLessonBtnsKo.indexOf(lessonObj.lessonId) === -1 && previewLessonBtnsKo.push(lessonObj.lessonId);
               single === "single" && ($17.voxLog({
                   module : "m_H1VyyebB",
                   op : "pc_WD_word_selection_page_check_single_words_click",
                   s0 : self.clazzGroupIdsStr,
                   s1 : !!constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] ? "纸质拍照听写" : "线上键盘听写",
                   s2 : "选入"
               }));
           }else{
               var _tempObj = self._getSpecialBoxInfo(questionObj.id);
               var _questionIndex = _tempObj.questionIndex;
               if(_questionIndex !== -1){
                   constantObj._homeworkContent.practices[option.tabType].questions.splice(_questionIndex,1);

                   $.each(constantObj._reviewQuestions[option.tabType],function(i){
                       if(this.id === questionObj.id){
                           constantObj._reviewQuestions[option.tabType].splice(i,1);
                           return false;
                       }
                   });
                   self.updateUfoExam(0 - questionObj.seconds,constantObj._homeworkContent.practices[option.tabType].questions.length);
                   questionIdsKo.splice(questionIndex,1);

                   //查看lesson下是否还有题
                   var hasQuestionUnderLesson = false;
                   $.each(constantObj._reviewQuestions[option.tabType],function(i){
                       if(this.lessonId == lessonObj.lessonId){
                           hasQuestionUnderLesson = true;
                           return false;
                       }
                   });
                   var previewLessonIndex = previewLessonBtnsKo.indexOf(lessonObj.lessonId);
                   if(hasQuestionUnderLesson && previewLessonIndex === -1){
                       previewLessonBtnsKo.push(lessonObj.lessonId);
                   }else if(!hasQuestionUnderLesson && previewLessonIndex !== -1){
                       previewLessonBtnsKo.splice(previewLessonIndex,1);
                   }

               }else{
                   $17.info("已经移除过这道题了");
               }

               single === "single" && ($17.voxLog({
                   module : "m_H1VyyebB",
                   op : "pc_WD_word_selection_page_check_single_words_click",
                   s0 : self.clazzGroupIdsStr,
                   s1 : !!constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] ? "纸质拍照听写" : "线上键盘听写",
                   s2 : "移除"
               }));
           }
           self._resetLessonStatus(lessonObj);
       },
       updateUfoExam : function(sec,questionCnt){
           var self = this,option = self.option;
           constantObj._moduleSeconds[option.tabType] = constantObj._moduleSeconds[option.tabType] + sec;
           self.carts
           && typeof self.carts["recalculate"] === 'function'
           && self.carts.recalculate(option.tabType,questionCnt);
       },
       _getSpecialBoxInfo : function(questionId){
           //指定题ID在购题车下标，没有返回-1
           var self = this,option = self.option;
           var _questions = constantObj._homeworkContent.practices[option.tabType].questions;
           var _questionIndex = -1;
           var cnt = 0;
           for(var m = 0,mLen = _questions.length; m < mLen; m++){
               cnt++;
               if(_questions[m].questionId === questionId && _questionIndex === -1){
                   _questionIndex = m;
               }
           }
           return {
               selectCount : cnt,
               questionIndex : _questionIndex
           };
       },
       previewWords : function(self){
           var lessonObj = this,option = self.option;
           var lessonId = lessonObj.lessonId;
           var questions = [];
           $.each(constantObj._reviewQuestions[option.tabType],function(i){
               if(this.lessonId == lessonId){
                   questions.push(this);
               }
           });

           $.prompt(template("t:SELECT_QUESTION_PREVIEW",{}),{
               title   : "预 览",
               buttons : {"确认": true},
               position: { width: 500 },
               loaded : function(){
                   ko.applyBindings({
                       questions : questions
                   }, document.getElementById('jqistate_state0'));
               },
               submit  : function(e,v,m,f){
                   e.preventDefault();
                   $.prompt.close(true);
               },
               close   : function(){}
           });

           $17.voxLog({
               module : "m_H1VyyebB",
               op : "pc_WD_word_selection_page_preview_button_click",
               s0 : self.clazzGroupIdsStr,
               s1 : !!constantObj._homeworkContent.practices[option.tabType]["ocrDictation"] ? "纸质拍照听写" : "线上键盘听写"
           });
       }
   };

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        getDictation : function(){
            return new Dictation();
        }
    });
});