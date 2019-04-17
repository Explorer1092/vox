var setTopBar = {
    show: true,
    rightText:"保存" ,
    rightTextColor: "ff7d5a",
    needCallBack: true
};
var topBarCallBack =  function(){
    $(".subBtn").click();
};
setTopBarFn(setTopBar, topBarCallBack);