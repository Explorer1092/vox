var setTopBar = {
    show:true,
    rightText:userName,
    rightTextColor:"ff7d5a",
    needCallBack:true
};
var topBarCallBack = function () {
    window.location.href = url;
};
setTopBarFn(setTopBar,topBarCallBack);
$(document).on('click','.res-autInfor',function(){
    var schoolId = $(this).attr("schoolId");
    try{openSecond( '/mobile/analysis/long_time_no_use_teachers.vpage?schoolId=' + schoolId);}catch(e){alert(e)}

});