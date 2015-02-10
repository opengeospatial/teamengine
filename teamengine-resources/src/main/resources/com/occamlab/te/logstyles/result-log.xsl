<xsl:transform
  xmlns:viewlog="viewlog"
  xmlns:te="java:com.occamlab.te.TECore"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:encoder="java:java.net.URLEncoder"
  xmlns:file="java:java.io.File"
  xmlns:ctl="http://www.occamlab.com/ctl"
  exclude-result-prefixes="viewlog encoder file te ctl"
  version="2.0">
  <xsl:template name="Client-Result">
    <xsl:param name="continue">-1</xsl:param>
    <xsl:param name="bestPractice">0</xsl:param>
    <xsl:param name="pass">1</xsl:param>
    <xsl:param name="notTested">2</xsl:param>
    <xsl:param name="skipped">3</xsl:param>
    <xsl:param name="warning">4</xsl:param>
    <xsl:param name="inheritedFailure">5</xsl:param>
    <xsl:param name="fail">6</xsl:param>
    <xsl:variable name="coverage-results">
      <service-requests>
        <xsl:for-each select="collection(concat($logdir,'/',$sessionDir,'?select=WMS-*.xml'))">
          <xsl:copy-of select="doc(document-uri(.))"/>
        </xsl:for-each>
      </service-requests>
    </xsl:variable>
    <xsl:variable name="address" select="$TESTNAME"/>
    <!-- Displayed the all test name -->
    <fieldset style="background:#ffffff; border: 0px">
      <div id="client"></div>
      <p id="show"  style="margin-left:60px; margin-top: 0px;margin-bottom: 0px;"/>
    </fieldset>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"> &#160; </script>
    <!-- Change the image on click show and hide button -->
    <script language="javascript">  
      <xsl:comment><![CDATA[ 
		function toggleDiv(divID) {
		    var url = location.href;
		    var divid = $(divID).attr("id");
		    var value = divid.split("__");
		    var path = $(divID).attr("src");
		    var result_url = url.split("teamengine");
		    var expend = "images/plus.png";
		    var merge = "images/minus.png";
		    var other = result_url[0] + "teamengine/images/abc.png";
		    if (path.indexOf('minus') > -1) {
			$("#" + divid).attr("src", expend);
		    } else if (path.indexOf('plus') > -1) {
			$("#" + divid).attr("src", merge);
		    } else {
			$("#" + divid).attr("src", other);
		    }
		    $("#" + value[0] + "_" + value[1]).toggle();
		}
]]> </xsl:comment>
    </script>
    <!-- getSession and user directory -->
    <script type="text/javascript">
      function getSession() {
      var sessionID = '<xsl:value-of select="$sessionDir"/>';
      return sessionID;
      }

      function getUser() {
      var userName = '<xsl:value-of select="$logdir"/>';
      return userName.split("users/")[1];
      }
    </script>   
    <!-- Displayed the result on viewSessionLog screen dynamically -->
    <script type="text/javascript">   
      <xsl:comment><![CDATA[ 
            $(document).ready(function() {
                var url = location.href;
                var result_url = url.split("teamengine");
                var c_name_value = getUser();
                var c_sessionID_value = getSession();
                var urlpath = "";
                var success = "images/pass.png";
                var error = "images/fail.png";
                var warning = "images/warning.png";
                urlpath = result_url[0] + "teamengine/restResult/suiteResult?userID=" + c_name_value + "&sessionID=" + c_sessionID_value;
                $.ajax({
                    type: "GET",
                    url: urlpath,
                    success: function(data) {
                        var jsonData = JSON.parse(data);
                        var text = "";
                        if (jsonData.Result !== undefined) {
                            for (var index = 0; index < 18; index++) {
                                var reqno=jsonData.Result[index].Name.split(" ")[1];
                                var id=jsonData.Result[index].id+"_result";
                                var img=jsonData.Result[index].id+"_img";
                                var leftmarginbefore=(+jsonData.Result[index].ParentID)*20;
                                if(index==0){
                                 $('#client').append($('<p style="margin-left:'+leftmarginbefore+'px; margin-bottom:0px; margin-top:0px;"><img src="images/minus.png" name="image1" id="'+jsonData.Result[index].node_id+'" onclick="toggleDiv(this);"><img src="' + warning + '" id="'+img+'" ></img><b>'+jsonData.Result[index].Name+'</b>'+"("+'<a href='+"viewClientTestLog.jsp?test="+c_sessionID_value+"&testNo="+index+'>'+"View Details"+'</a>'+")"+'</p><div id="'+id+'"/>'));
                                }else{
                                 var nodeid=jsonData.Result[index].ParentID+"_result";
                                 $('#'+nodeid).append($('<p style="margin-left:'+leftmarginbefore+'px; margin-bottom:0px; margin-top:0px;"><img src="images/minus.png" name="image1" id="'+jsonData.Result[index].node_id+'" onclick="toggleDiv(this);"><img src="' + warning + '" id="'+img+'" ></img><b>'+jsonData.Result[index].Name+'</b>'+"("+'<a href='+"viewClientTestLog.jsp?test="+c_sessionID_value+"&testNo="+index+'>'+"View Details"+'</a>'+")"+'</p><div id="'+id+'"/>'));
                                }
                            }
                        }
                        if (jsonData.Result !== undefined) {
                            for (var index = 18; index < jsonData.Result.length; index++) {
                                var reqno=jsonData.Result[index].Name.split(" ")[1];
                                var id=jsonData.Result[index].ParentID+"_result";
                                var leftmarginafter=((+jsonData.Result[index].indent)+1)*20;
                              if(jsonData.Result[index].Name.indexOf('Passed')>-1){
                                $('#' + id).append($('<p style="margin-left:'+leftmarginafter+'px; margin-bottom:0px; margin-top:0px;"><img src="' + success + '" ></img>' + jsonData.Result[index].Name +"("+'<a href='+"viewClientTestLog.jsp?test="+c_sessionID_value+"&reqNo="+reqno+"&result=P"+'>'+"View Details"+'</a>'+")"+ '</p>'));
                              }else{
                                $('#' + id).append($('<p style="margin-left:'+leftmarginafter+'px; margin-bottom:0px; margin-top:0px;"><img src="' + error + '" ></img>' + jsonData.Result[index].Name +"("+'<a href='+"viewClientTestLog.jsp?test="+c_sessionID_value+"&reqNo="+reqno+"&result=F"+'>'+"View Details"+'</a>'+")"+ '</p>'));
                              }
                          }
                        }
                        if (($('#2_result').text().indexOf('Request') == -1) || ($('#2_result').text().indexOf('Failed') != -1)) {
                            $('#2_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#2_img').attr("src", success);
                        }
                        if ($('#4_result').text().indexOf('Request') == -1) {
                            $('#4_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#4_img').attr("src", success);
                        }
                        if ($('#5_result').text().indexOf('Request') == -1) {
                            $('#5_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#5_img').attr("src", success);
                        }
                        if ($('#6_result').text().indexOf('Request') == -1) {
                            $('#6_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#6_img').attr("src", success);
                        }
                        if ($('#7_result').text().indexOf('Request') == -1) {
                            $('#7_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#7_img').attr("src", success);
                        }
                        if ($('#8_result').text().indexOf('Request') == -1) {
                            $('#8_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#8_img').attr("src", success);
                        }
                        if ($('#9_result').text().indexOf('Request') == -1) {
                            $('#9_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#9_img').attr("src", success);
                        }
                        if ($('#10_result').text().indexOf('Request') == -1) {
                            $('#10_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#10_img').attr("src", success);
                        }
                        if ($('#11_result').text().indexOf('Request') == -1) {
                            $('#11_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#11_img').attr("src", success);
                        }
                        if ($('#12_result').text().indexOf('Request') == -1) {
                            $('#12_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#12_img').attr("src", success);
                        }
                        if ($('#13_result').text().indexOf('Request') == -1) {
                            $('#13_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#13_img').attr("src", success);
                        }
                        if ($('#14_result').text().indexOf('Request') == -1) {
                            $('#14_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#14_img').attr("src", success);
                        }
                        if ($('#15_result').text().indexOf('Request') == -1) {
                            $('#15_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#15_img').attr("src", success);
                        }
                        if ($('#16_result').text().indexOf('Request') == -1) {
                            $('#16_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#16_img').attr("src", success);
                        }
                        if ($('#17_result').text().indexOf('Request') == -1) {
                            $('#17_img').attr("src", error);
                            $('#3_img').attr("src", error);
                            $('#1_img').attr("src", error);
                        } else {
                            $('#17_img').attr("src", success);
                        }
                        if ($('#3_img').attr("src").indexOf('warning') > -1) {
                            $('#3_img').attr("src", success);
                        }
                        if (($('#18_result').text().indexOf('Request') == -1) || ($('#18_result').text().indexOf('Failed') != -1)) {
                            $('#18_img').attr("src", error);
                        } else {
                            $('#18_img').attr("src", success);
                        }
                        if ($('#1_img').attr("src").indexOf('warning') > -1) {
                            $('#1_img').attr("src", success);
                        }
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        $('#show').text("");
                    },
                    dataType: "text"
                });
            });   
            ]]> </xsl:comment>
    </script>
  
    <xsl:if test="test">
      <!-- Count the run test which passed, failed or skipped  -->
      <table id="summary" border="0" cellpadding="4">
        <tr>
          <th align="left" colspan="8" 
              style="font-family: sans-serif; color: #000099; background:#ccffff">Summary of results</th>
        </tr>
        <tr>
          <td>
            <img src="images/bestPractice.png" /> Best Practice</td>
          <td>
            <img src="images/pass.png" /> Passed</td>
          <td>
            <img src="images/continue.png" /> Continue</td>
          <td>
            <img src="images/notTested.png" /> Not Tested</td>
          <td>
            <img src="images/warn.png" /> Warning</td>
          <td>
            <img src="images/skipped.png" /> Skipped</td>
          <td>
            <img src="images/fail.png" /> Failed</td>
          <td>
            <img src="images/inheritedFailure.png" /> Failed (Inherited)</td>
        </tr>
        <tr>
          <td id="nBestPractice" align="center" bgcolor="#00FF00">
            <xsl:value-of select="count(//test[@result=$bestPractice and @complete='yes'])"/>
          </td>
          <td id="nPass" align="center" bgcolor="#00FF00">
            <xsl:if test="count($coverage-results//param) > 0">
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml'))))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value)+1)"/>
              </xsl:if>
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml')))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value)+2)"/>
              </xsl:if>
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml'))))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value)+1)"/>
              </xsl:if>
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml')))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value)+2)"/>
              </xsl:if>
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml')))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value)+2)"/>
              </xsl:if>
              <xsl:if test="(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml')))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml'))))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value))"/>
              </xsl:if>
              <xsl:if test="(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml'))))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value)+1)"/>
              </xsl:if>
              <xsl:if test="(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml')))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value)+1)"/>
              </xsl:if>
              <xsl:if test="(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml')))">
                <xsl:value-of select="16-(count($coverage-results//param[@name='layers']/value)+2)"/>
              </xsl:if>        
            </xsl:if>       
          </td>
          <td id="nContinue" align="center" bgcolor="#FFFF00">
            <xsl:value-of select="count(//test[@result=$continue or @complete='no'])"/>
          </td>
          <td id="nNotTested" align="center" bgcolor="#FFFF00">
            <xsl:value-of select="0"/>
          </td>
          <td id="nWarn" align="center" bgcolor="#FFFF00">
            <xsl:value-of select="count(//test[@result=$warning and @complete='yes'])"/>
          </td>
          <td id="nSkipped" align="center" bgcolor="#FFFF00">
            <xsl:value-of select="count(//test[@result=$skipped and @complete='yes'])"/>
          </td>
          <td id="nFail" align="center" bgcolor="#FF0000">
            <xsl:if test="count($coverage-results//param) > 0">
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml'))))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value)+1)"/>
              </xsl:if>
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml')))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value)+2)"/>
              </xsl:if>
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml'))))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value)+1)"/>
              </xsl:if>
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml')))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value)+2)"/>
              </xsl:if>
              <xsl:if test="(not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml')))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value)+2)"/>
              </xsl:if>
              <xsl:if test="(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml')))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml'))))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value))"/>
              </xsl:if>
              <xsl:if test="(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml'))))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value)+1)"/>
              </xsl:if>
              <xsl:if test="(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (not(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml')))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml')))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value)+1)"/>
              </xsl:if>
              <xsl:if test="(doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesPass.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetFeatureInfoFail.xml'))) and (doc-available(concat($logdir,'/',$sessionDir,'/test_data/WMS1-GetCapabilitiesFail.xml')))">
                <xsl:value-of select="(count($coverage-results//param[@name='layers']/value)+2)"/>
              </xsl:if>   
            </xsl:if>
          </td>
          <td id="nInheritedFail" align="center" bgcolor="#FF0000">
            <xsl:value-of select="0"/>
          </td> 
        </tr>
      </table>
    </xsl:if>
  </xsl:template>
</xsl:transform>
