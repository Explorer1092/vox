/**
    * Created by xinqiang.wang on 2016/7/15.
    */


define(["$17"],function($17){
    function EnglishExamModule(){
        var self = this;
        self._unitSetBooks = function(unitId,questionId,operate,$this){
            if($17.isBlank(operate) || (operate != 'add' && operate != 'remove')){
                return false;
            }
            if($17.isBlank(unitId) || $17.isBlank(questionId)){
                return false;
            }
            var _bookExams = homeworkConstant._homeworkContent.books.EXAM;
            var unitIndex = -1;
            for(var k = 0,kLen = _bookExams.length; k < kLen; k++){
                if(_bookExams[k].unitId == unitId){
                    unitIndex = k;
                    break;
                }
            }
            if(unitIndex != -1){
                var _includeQuestions = homeworkConstant._homeworkContent.books.EXAM[unitIndex].includeQuestions;
                if(operate == 'add'){
                    homeworkConstant._homeworkContent.books.EXAM[unitIndex].includeQuestions.push(questionId);
                }else{
                    var _qIndex = $.inArray(questionId,_includeQuestions);
                    if(_qIndex != -1){
                        _includeQuestions.splice(_qIndex,1);
                    }
                    if(_includeQuestions.length == 0){
                        homeworkConstant._homeworkContent.books.EXAM.splice(unitIndex,1);
                    }
                }
            }else{
                if(operate == 'add') {
                    var _bookObj = {
                        bookId: $this.book.bookId() || null,
                        unitId: $this.book.unitId() || null,
                        includeQuestions : []
                    };
                    _bookObj.includeQuestions.push(questionId);
                    homeworkConstant._homeworkContent.books.EXAM.push(_bookObj);
                }
            }
            return true;

        };


    }
    return EnglishExamModule;

});