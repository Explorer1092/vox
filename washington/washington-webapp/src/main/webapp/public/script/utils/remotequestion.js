/***
 * 缓存应试题目在前端,
 * 依赖jquery
 */
(function () {

    function getQuestionByIds(questionIds,callback){
        callback = $.isFunction(callback) ? callback : function(){};
        if(typeof questionIds === "string"){
            questionIds = [questionIds];
        }
        if(!$.isArray(questionIds) || questionIds.length === 0){
            callback({
                success : false,
                info    : "题目ID参数错误"
            });
            return false;
        }
        $.get("/exam/flash/load/question/byids.vpage",{
            data:JSON.stringify({ids: questionIds,containsAnswer:false})
        }).done(function(res){
            var result = res.success ? res.result : {};
            callback({
                success     : res.success,
                questionMap : result
            });
        }).fail(function(e){
            callback({
                success : false,
                info    : e.message
            });
        });
    }

    /*$.get("/exam/flash/load/newquestion/byids.vpage",{
            data:JSON.stringify({ids: questionIds,containsAnswer:false})
        }).done(function(res){
            var result = res.success ? res.result : [];
            callback({
                success : res.success,
                questions : result
            });
        }).fail(function(e){
            callback({
                success : false,
                info    : e.message
            });
        });*/

    $17.QuestionDB = $17.QuestionDB || {};
    $17.extend($17.QuestionDB, {
        getQuestionByIds : getQuestionByIds
    });
}());
