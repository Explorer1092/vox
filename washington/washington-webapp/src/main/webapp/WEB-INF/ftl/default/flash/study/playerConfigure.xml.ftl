<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
<#list configs as cfg>
	<${cfg.configKey}>${cfg.configValue}</${cfg.configKey}>
</#list>
</ns2:Lesson>