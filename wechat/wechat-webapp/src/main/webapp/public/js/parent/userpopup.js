define(['jquery','jbox'],function($){
    var studentsListBox = $("#studentsListBox");
    $('#popup_children').jBox('Tooltip', {
        trigger: 'click',
        content: studentsListBox,
        position: {
            x: 'center',
            y: 'bottom'
        },
        closeOnClick: 'box'
    });


    function setCookie(cookieName, cookieValue, nDays) {
        var today = new Date();
        var expire = new Date();
        if (nDays == null || nDays == 0) nDays = 1;
        expire.setTime(today.getTime() + 1000 * 60 * 60); //默认一小时
        document.cookie = cookieName + "=" + escape(cookieValue)  + ("; path=/")+ ";expires=" + expire.toGMTString();
    }

    function getCookie(name) {
        var pattern = RegExp(name + "=.[^;]*");
        var matched = document.cookie.match(pattern);
        if (matched) {
            var cookie = matched[0].split('=');
            return cookie[1]
        }
        return ''
    }

    var studentIds = [],student = studentsListBox.data('student');
    $('.select_student_but').each(function(){
        studentIds.push($(this).data('student_id'))
    });

    return {
        //jsName  根据依赖关系，调用。 并且被依赖的js中要提供loadMessageById方法。
        selectStudent : function(jsName){
            //选择孩子
            $(document).on('click', '.select_student_but', function () {
                var $this = $(this);
                var student_index= $this.data('student_index');
                var studentMes = student[student_index];
                $("#studentName_b").text(studentMes.name || studentMes.id);
                $("#studentImg_b").attr('src', $this.data('img'));
                setCookie('ssid',studentMes.id,0);
                require(jsName).loadMessageById(studentMes.id);
            });

            setTimeout(function(){
                var cId = getCookie('ssid') * 1;
                if(cId == '' || $.inArray(cId,studentIds) == -1){
                    studentsListBox.find("ul a:eq(0)").click();
                }else{
                    studentsListBox.find("ul a[data-student_id="+cId+"]").click();
                }
            },10);
        }
    };
});