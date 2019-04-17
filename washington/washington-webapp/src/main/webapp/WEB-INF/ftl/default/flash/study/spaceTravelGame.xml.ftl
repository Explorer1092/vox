<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
<#if lesson?exists>
<gameTime>${gameTime!''}</gameTime>
<lessonId>${lesson.id}</lessonId>
<lessonName>${lesson.ename}</lessonName>
<#list sentences as sent>
<sentence>
	<sentenceId>${sent.id}</sentenceId>
	<Data>
		<cn>${sent.cnText}</cn>
		<en>${sent.enText}</en>
		<metadata>${sent.metadata!''}</metadata>
		<waveFileLocator><@app.wave href="${sent.waveUri}" /></waveFileLocator>
		<slowWaveFileLocator><@app.wave href="${sent.slowWaveUri!''}" /></slowWaveFileLocator>
	</Data>	
</sentence>
</#list>
<#else>
<lessonId>null</lessonId>
</#if>
</ns2:Lesson>