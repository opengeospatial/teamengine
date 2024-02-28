<?xml version="1.0" encoding="UTF-8"?>
<!--
  #%L
  TEAM Engine - Shared Resources
  %%
  Copyright (C) 2006 - 2024 Open Geospatial Consortium
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  -->

<xsl:transform
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:txsl="http://www.w3.org/1999/XSL/Transform/target"
  xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:te="java:com.occamlab.te.TECore"
  xmlns:saxon="http://saxon.sf.net/"
  version="2.0">

	<xsl:strip-space elements="*"/>
	<xsl:output indent="yes"/>
	<xsl:namespace-alias stylesheet-prefix="txsl" result-prefix="xsl"/>

	<xsl:template match="ctl:get-session-info">	
		<ctl:SessionInfo>
			<ctl:SessionId><txsl:value-of select="te:getSessionId()"/></ctl:SessionId>
			<ctl:SessionDir><txsl:value-of select="te:getSessionDir()"/></ctl:SessionDir>
		</ctl:SessionInfo>
	</xsl:template>
	
</xsl:transform>

