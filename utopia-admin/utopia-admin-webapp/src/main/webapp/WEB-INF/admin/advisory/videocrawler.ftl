<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='音频抓取查询抓取配置' page_num=13>
<script src="${requestContext.webAppContextPath}/public/js/tablesorter/jquery.tablesorter.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/tablesorter.css" rel="stylesheet">
<div class="span9">
    <div class="control-group">
        <label class="control-label" for="productName">音频列表地址：</label>
        <div class="controls">
            <label for="title">
                <input type="text" name="url" id="list_url"
                       style="width: 60%" class="input">
            </label>
        </div>
    </div>
    <div class="control-group">
        <div class="controls">
            <input type="button" id="crawler" value="查询" class="btn btn-small btn-primary">
        </div>
    </div>
    <table id="videoList" class="table table-hover table-striped table-bordered tablesorter">
        <thead>
        <tr>
            <th style="width: 120px;">原音频id</th>
            <th style="width: auto;">音频标题</th>
            <th style="width: auto;">音频原地址</th>
            <th style="width: auto;">音频阿里云地址</th>
        </tr>
        </thead>
        <tbody id="data_table">

        </tbody>
    </table>
</div>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script>
    $(function () {
        var soundIds = [];
        var audio_info = [];
        $('#crawler').on('click', function () {
            $('#crawler').attr('disabled', "true");
            var url = $('#list_url').val();
            $.ajax({
                url: 'crawlerVideo.vpage',
                type: "GET",
                data: {"url": url},
                success: function (data) {
                    if (data.success) {
                        console.info(data.returnList);
                        soundIds = data.returnList;
                        console.info(soundIds);
                        if (soundIds) {
                            $(soundIds).each(function (i, n) {
                                $.ajax({
                                    url: 'videoInfo.vpage',
                                    type: "GET",
                                    data: {"sound_id": n},
                                    success: function (data) {
                                        if (data.success) {
                                            console.info(data.returnMap);
                                            video_info = data.returnMap;
                                            audio_info.push(data.returnMap);
                                            if (soundIds.length == audio_info.length) {
                                                audio_info.sort(sortid);
                                                $(audio_info).each(function (i, n) {
                                                    generateTableInfo(n);
                                                });
                                            }
                                        } else {
                                            console.info(data.info);
                                        }
                                    }
                                });
                            });
                        }
//                        $("#videoList").tablesorter();
                    } else {
                        console.info(data.info);
                    }
                }
            });
        });
//        $("#videoList").bind("DOMAttrModified", function () {
//
//        });

    });

    function generateTableInfo(video_info) {
        //动态创建一个tr行标签,并且转换成jQuery对象
        var $trTemp = $("<tr></tr>");
        //往行里面追加 td单元格
        $trTemp.append("<td>" + video_info.id + "</td>");
        $trTemp.append("<td>" + video_info.title + "</td>");
        $trTemp.append("<td>" + video_info.play_path + "</td>");
        if (video_info.status == 200) {
            $trTemp.append("<td>" + video_info.file_url + "</td>");
        } else {
            $trTemp.append("<td>" + "未抓取" + "</td>");
        }
        // $("#J_TbData").append($trTemp);
        $trTemp.appendTo("#data_table");
    }
    function sortid(a, b) {
        return a.id - b.id;
    }
</script>
</@layout_default.page>