/*作业详情*/
define(["jquery","$17",'logger',"audio","jbox"],function($,$17,logger){
    $(function(){
        var resetBut = $('#resetBut'),errorDetailBut = $('#errorDetailBut');
        var vip = resetBut.data('vip');
        $('input[name=sid]').val($17.getQuery('sid'));
        $('input[name=hid]').val($17.getQuery('hid'));

        resetBut.on('click', function () {
            var content = (vip) ? "您已经开通阿分提<br/>让孩子登录17作业进行学习吧" : "本次错题已加入阿分提错题工厂<br/>开通即可直接重练";
            var confirmButton = (vip) ? "知道了" : "查看详情";
            var confirm = new jBox('Confirm', {
                content: content,
                confirmButton: confirmButton,
                closeButton: false,
                cancelButton: '',
                confirm: function () {
                    if(!vip){
                        location.href = '/parent/product/info-afenti.vpage';
                    }
                },
                onOpen: function () {
                    $('.jBox-Confirm-button-cancel').hide();
                }
            });
            confirm.open();
        });

        //查看学习
        errorDetailBut.on('click',function(){
            var wrongList = errorDetailBut.data('wrong_list');
            $('form input[name=wrongList]').val(JSON.stringify(wrongList));
            $('form').submit();
        });

        //logger
        logger.log({
            module: 'homework',
            op: 'homework_pv_detail_' + homeworkType
        })
    });
});