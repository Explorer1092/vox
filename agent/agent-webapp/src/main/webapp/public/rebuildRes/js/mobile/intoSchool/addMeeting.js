$(document).ready(function () {

    $.get("get_can_join_meeting_data.vpage",function (res) {
        if(res){
            if(res.length > 0 ){
                for(var i = 0; i< res.length ; i++){
                    res[i].workTime = new Date(res[i].workTime).Format('yyyy-MM-dd hh:mm:ss')
                }
            }
            $(".main_body").html(template("main_body",{res:res}));
        }
    });

});