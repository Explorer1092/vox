<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <p>在此输入长地址:</p>
    <div>
        <textarea id="txt_longurl" name="txt_longurl" style="width:450px;height:150px;"
                  placeholder="输入一个长地址"></textarea>
        <input id="btn_submit" class="btn" type="button" value="生成"/>
    </div>
    <p>短地址:</p>
    <pre id="surl"></pre>
</div>
<script type="text/javascript">
    $(function () {
        $('#btn_submit').on('click', function () {
            var longUrl = $('#txt_longurl').val();

            if (longUrl.length == 0) {
                alert('请输入长地址');
                return;
            }

            $.post('create.vpage', {url: longUrl}, function (data) {
                if (data.success) {
                    $('#surl').html(data.surl);
                } else {
                    alert(data.info);
                }
            });
        });
    });
</script>

</@layout_default.page>
