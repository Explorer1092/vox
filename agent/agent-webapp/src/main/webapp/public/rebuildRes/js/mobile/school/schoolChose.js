
define(["dispatchEvent"],function (dispatchEvent) {
    var AT = new agentTool();
    var setTopBar = {
        show:true,
        rightText:'',
        rightTextColor:"ff7d5a",
        needCallBack:true
    };
    setTopBarFn(setTopBar);
    var eventOption = {
      base:[
          {
             selector:".js-search",
              eventType:"click",
              callBack : function(){
                  var schoolId = $("#schoolSearchInput").val().trim();
                  if(schoolId != ''){
                      chooseSchool(schoolId);
                  }else{
                      AT.alert('请输入学校Id')
                  }
              }
           },
          {
              selector:".js-schoolItem",
              eventType:"click",
              callBack:function(){
                  var schoolId = $(this).data().sid;
                  saveSchool(schoolId);
              }
          }
      ]
    };
    new dispatchEvent(eventOption);
    //搜索学校列表
   var chooseSchool = function(schoolId){
        $.post("/mobile/resource/school/search.vpage",{schoolKey:schoolId, scene:2},function (res) {
            if(res.success){
                $("#schoolContainer").html("");
                if(res.schoolList.length>0){
                    var data={
                        data:res.schoolList.map(function(obj){
                            var tmp=obj;
                            return tmp;
                        })
                    };
                    $("#schoolContainer").html(template("schoolListTemp",data));
                }
            }else{
                AT.alert(res.info);
            }
        })
    };
    var saveSchool = function(schoolId){
       $.post("save_appraisal_school.vpage",{schoolId:schoolId},function(res){
           if(res.success){
               window.history.back();
           }else{
               AT.alert(res.info);
           }
       })
    }
});