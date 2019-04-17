<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">

<syllId>${syllId!''}</syllId>
<htmlCode>${htmlCode!''}</htmlCode>
<description>${description!''}</description>
<description2>${description2!''}</description2>
<#list phonemeExampleWordSentenceList as sentence>
<sentence>
	<sentenceId>${sentence.id}</sentenceId>
	<Data>
		<cn>${sentence.cnText}</cn>
		<en>${sentence.enText}</en>
		<metadata>${sentence.metadata}</metadata>
		<waveFileLocator><@app.wave href="${sentence.waveUri}" /></waveFileLocator>
		<slowWaveFileLocator><@app.wave href="${sentence.slowWaveUri}" /></slowWaveFileLocator>
	</Data>	
</sentence>
</#list>
</ns2:Lesson>