
(function () {

    var $modal = document.createElement('div');
    $modal.id = 'preivew_modal';
    $modal.style = 'position: fixed;top: 50%;left: 50%;transform: translate(-50%, -50%);width:320px;height: 640px;background: #fff;border: 15px solid #abb3b7;border-radius: 10px;overflow: auto;display:none;'
    
    var closeElement = document.createElement('div');
    closeElement.id = 'closeElement';
    closeElement.innerText = 'x';
    closeElement.style = 'position: absolute;top: 0px;right: 0px;display: inline-block;background: #afaeae;font-size: 20px;width: 25px;height: 25px;text-align: center;border-bottom-left-radius: 50%;cursor: pointer;';
    $modal.append(closeElement);

    closeElement.addEventListener('click', function(){
        document.getElementById('preivew_modal').style.display = 'none';
    });

    var $contentWrapper = document.createElement('div');
    $contentWrapper.id = 'content-wrapper';
    $contentWrapper.style = 'width: 100%;height: 100%;overflow: auto;padding: 0 10px;box-sizing: border-box;'
    $modal.append($contentWrapper);

    document.body.append($modal);

    var a = {
        exec: function (editor) {
            var modal = document.getElementById('preivew_modal');
            modal.style.display = 'block';
            var title = '', $title = document.getElementById('ck-title');
            if($title) {
                title ='<div style="text-align: center;font-size: 20px;margin: 20px 0;">' + document.getElementById('ck-title').value + '</div>'
            }
            
            document.getElementById('content-wrapper').innerHTML = title + editor.getData();
        }
    },
    b = '_preview';
    CKEDITOR.plugins.add(b, {
        init: function (editor) {
            editor.addCommand(b, a);
            editor.ui.addButton('_preview', {
                label: '预览',  //鼠标悬停在插件上时显示的名字
                icon: './preview-icon.png',   //自定义图标的路径
                command: b
            });
        }
    });
})();