var setTopBar = {
    show: true,
    rightText:"新建" ,
    rightTextColor: "ff7d5a",
    needCallBack: true
};
var topBarCallBack =  function(){
    $('.js-newlyBuild').click();
};
setTopBarFn(setTopBar, topBarCallBack);