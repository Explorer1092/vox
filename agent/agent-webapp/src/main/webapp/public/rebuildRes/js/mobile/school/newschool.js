$(document).ready(function () {
    reloadCallBack();
});
var setTopBar = {
    show: true,
    rightText:"开通" ,
    rightTextColor: "ff7d5a",
    needCallBack: true
};
var topBarCallBack =  function(){
    $('.js-submit').click();
};
setTopBarFn(setTopBar, topBarCallBack);