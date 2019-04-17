/**
 * @author xinqiang.wang
 * @description "发布管理"
 * @createDate 2016/11/9
 */

define(["jquery",'$17',"prompt", "datetimepicker"], function ($,$17) {

    function getQuery(item) {
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    var isSID = $("#isSID");
    var submitBtn = $("#submitBtn");
    var accountId = getQuery("accountId");




    submitBtn.on('click', function () {
        var articleList = [];
        $('#listBox').find('tr').each(function () {
            var that = $(this);
            var materialId = that.find('select.articleUrl').find('option:selected').val();
            var articleUrl = that.find('select.articleUrl').find('option:selected').data('url');
            var title = that.find('input.articleTitle').val();
            var imgUrl = that.find('div.imgShowBox img').attr('src');
            if (!$17.isBlank(articleUrl) || !$17.isBlank(title) || !$17.isBlank(imgUrl)) {
                articleList.push({
                    articleUrl: articleUrl,
                    title: title,
                    imgUrl: imgUrl,
                    publishDateTime: that.data('pdt'),
                    status: that.data('status'),
                    bundleId: that.data('bundle_id'),
                    id: that.data('id'),
                    materialId: materialId
                });
            }
        });

        if(articleList.length == 0){
            alert('请编辑发布内容');
            return false;
        }

        for(var i = 0; i < articleList.length; i++){
            if($17.isBlank(articleList[i].articleUrl) || $17.isBlank(articleList[i].title) || $17.isBlank(articleList[i].imgUrl)){
                alert('第'+(i+1)+'行被必须完整填写！');
                return false;
            }
        }

        var json = {
            accountId: accountId,
            articleList: articleList,
            sendJpush: false
        };
        $.ajax({
            type: 'post',
            url: "/basic/officialaccount/savearticle.vpage",
            data: JSON.stringify(json),
            success: function (res) {
                if (res.success) {
                    alert("已全部保存成功！");
                    location.href = '/basic/officialaccount/articlelist.vpage';
                } else {
                    alert(res.info);
                }
            },
            contentType: 'application/json;charset=UTF-8'
        });
    });

    $(".fileUploadBtn").change(function () {
        var $this = $(this);
        var ext = $this.val().split('.').pop().toLowerCase();
        var height = $this.siblings('div.imgShowBox').data('height');
        var width = $this.siblings('div.imgShowBox').data('width');

        if ($this.val() != '') {
            if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                return false;
            }

            var formData = new FormData();
            formData.append('file', $this[0].files[0]);
            formData.append('height', height);
            formData.append('width', width);


            var fileSize = ($this[0].files[0].size / 1024 / 1012).toFixed(4); //MB
            if (fileSize >= 2) {
                alert("图片过大，重新选择。");
                return false;
            }
            $.ajax({
                url: '/basic/officialaccount/uploadarticleimg.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        var img_html = '<img src="' + data.fileName + '" style="width: 100px; height: 70px;">';
                        $this.siblings('div.imgShowBox').html(img_html);
                    } else {
                        alert(data.info);
                    }
                }
            });


        }
    });


});