<div id="menuBoxContainer" class="menuBoxContainer">
	<div class="menuBox" data-bind="foreach:catalogs">
		<div class="menuItem" data-bind="template:{name : 'items', data : $data}"></div>
	</div>
</div>
<script id='items' type='text/html'>
	<div data-bind="foreach:$data">
		<a data-bind="attr:{href:href}"><div data-bind="text:title">2</div></a>
	</div>
</script>