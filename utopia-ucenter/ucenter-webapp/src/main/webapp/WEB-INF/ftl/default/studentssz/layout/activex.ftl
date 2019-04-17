<script id="t:17zuoye_pc" type="text/html">
    <div class="voicePlugin">
        <div class="vtop">
            如已安装，请双击桌面上的一起作业快捷方式<i class="plugicon"></i>。
        </div>
        <div class="vbot">
            <span><b>第<i>1</i>步：</b>请点击下载</span>
        </div>
        <div class="btnre"><a href="<@app.client_setup_url />" target="_blank" class="downloadPlugtogether w-btn-dic w-btn-green-well">下载一起作业电脑版</a></div>
        <div class="vbot">
            <span><b>第<i>2</i>步：</b>下载后需要安装才能使用，查看<a href="/help/installplug.vpage" id="howtoInstall" target="_blank">如何安装？</a></span>
        </div>
    </div>
</script>
<script type="text/javascript">
function AC_InstallActiveX() {
    $.prompt(template("t:17zuoye_pc", {}), {
        title   : "下载并安装《一起作业电脑版》",
        buttons : {}
    });
}
</script>