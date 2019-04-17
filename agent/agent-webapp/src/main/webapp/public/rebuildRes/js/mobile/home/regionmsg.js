/**
 * 首页
 * */

var setTopBar = {
    show:true,
    rightText:'保存',
    rightTextColor:"ff7d5a",
    needCallBack:true
};
var topBarCallBack = function () {
    var msg = $("#regMsg").val();
    if(msg.length != 0 ){
        $.post("save_region_message.vpage",{groupId:groupid,message:msg},function(res){
            if(res.success){
                AT.alert("保存成功");
                location.href = "/mobile/performance/index.vpage";
            }else{
                AT.alert(res.info);
            }
        });
    }else{
        AT.alert("请填写大区寄语后提交");
    }
};
setTopBarFn(setTopBar,topBarCallBack);