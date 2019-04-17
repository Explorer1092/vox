define(['jquery', 'logger', '$17', 'jbox'], function ($, logger, $17) {

    $(document).on('click', 'p.star span', function () {
        if (question.hasCommented) {
            return false;
        }
        $(this).prevAll().addClass('star-active');
        $(this).addClass('star-active');
        $(this).nextAll().removeClass('star-active')
    });
    $(document).on('click', '.comm-btn', function () {
        var qid = question.id;
        var detail = question.detail;
        var stars = $('.total p span.star-active').length;
        if (stars <= 0) {
            $17.jqmHintBox('请给整体评分选择星星');
            return false;
        }
        var speed = $('.speed p span.star-active').length;
        if (speed <= 0) {
            $17.jqmHintBox('请给答题速度选择星星');
            return false;
        }
        var quality = $('.quality p span.star-active').length;
        if (quality <= 0) {
            $17.jqmHintBox('请给答题质量选择星星');
            return false;
        }
        var content = $('#tt-comment')[0].value;
        if (!content) {
            $17.jqmHintBox('请输入评论');
            return false;
        }
        var data = {qid: qid, detail: question.detail, content: content, stars: stars, quality: quality, speed: speed};
        $.post('/parent/onlineqa/docomment.vpage', data, function (resp) {
            logger.log({
                module:'onlineqa',
                op:'onlineqa_'+productType+'_docomment'
            })
            location.reload();
        });
    });
    logger.log({
        module:'onlineqa',
        op:'onlineqa_'+productType+'comment_detail_pv'
    })
});