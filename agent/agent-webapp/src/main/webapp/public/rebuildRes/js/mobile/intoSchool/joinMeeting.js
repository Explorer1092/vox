
var schoolRecordId = "${schoolRecordId!}";
var AT = new agentTool();



var setTopBar = {
    show: true,
    rightText:"提交" ,
    rightTextColor: "ff7d5a",
    needCallBack: true
};
var checkData = function () {
    var flag = true;
    // if($("#photoUrl").val() == "" || $("#meetingNote").val() == ""){
    //     AT.alert("请填完整后再提交");
    //     flag = false;
    //     return false;
    // }
    return flag;
};
var submitBtn = true;

var pushAjaxFn = function (postData) {
    if(checkData()){
        postData.url = $("#photoUrl").val();
        postData.meetingNote = $("#meetingNote").val();
        postData.longitude = meetingLongitude;
        postData.latitude = meetingLatitude;
        $.post("saveJoinMeetingRecord.vpage",postData,function (res) {
            if(res.success){
                AT.alert("提交成功");
                setTimeout("disMissViewCallBack()",1500);
            }else{
                submitBtn = true;
                alert(res.info)
            }
        })
    }
};
var topBarCallBack =  function(){
    if(submitBtn){
        submitBtn = false;
        pushAjaxFn(postData);
    }
};
setTopBarFn(setTopBar, topBarCallBack);
$(".js-submitVisBtn").click(function () {
    pushAjaxFn(postData);
})
$.get("meeting_detail.vpage?recordId="+getUrlParam('id'),function (res) {
    if(res.success){
        if(res.data){
            res.data.workTime = new Date(res.data.workTime).Format("yyyy-MM-dd");
        }
        $(".main_body").html(template("main_body",{res:res.data}));
    }
});