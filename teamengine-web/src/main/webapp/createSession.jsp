<%@ page language="java" session="false"
	import="java.util.*,com.occamlab.te.index.*,com.occamlab.te.web.*"%><%!
	Config Conf = null;
	List<String> organizationList = null;
	Map<String, List<String>> standardMap = null;
	Map<String, List<String>> versionMap = null;
	Map<String, List<String>> revisionMap = null;
//	Map<String, SuiteEntry> suites = null;
	Map<String, List<ProfileEntry>> profiles = null;
	
	public void jspInit() {
		try {
			Conf = new Config();
			organizationList = Conf.getOrganizationList();
			standardMap = Conf.getStandardMap();
			versionMap = Conf.getVersionMap();
			revisionMap = Conf.getRevisionMap();
//			suites = Conf.getSuites();
			profiles = Conf.getProfiles();
		} catch (Exception e) {	
			e.printStackTrace(System.out);
		}
	}%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Compliance Testing</title>
    <script src="https://code.jquery.com/jquery-1.9.1.min.js"></script>
<script>
    var profiles_key = null;
	function fillOrganization(){ 
		 // this function is used to fill the category list on load
		<% 
		//Fill Organization
		for(int i = 0; i < organizationList.size(); i++){ 
		%>
		addOption(document.standardsForm.Organization, "<%=organizationList.get(i)%>", "<%=organizationList.get(i)%>", "");
		<%}//for loop%>
        
        $("#Organization").val($("#Organization option:first").val());
        
		// When default organization is selected then corresponding standards will appear in dropdownlist
		SelectStandard();
		sortStandards();
	}

	function SelectStandard(){
		// ON selection of organization this function will work
		
		removeAllOptions(document.standardsForm.Standard);
		addOption(document.standardsForm.Standard, "", "Standard", "");
		<%
		for(int i=0;i<organizationList.size();i++){
		%>
			if(document.standardsForm.Organization.value == '<%=organizationList.get(i)%>'){
			<% 
			// get standard
				List<String> standardList = standardMap.get(organizationList.get(i)); 
				for(int j=0; j < standardList.size(); j++){
					
					// Get version
					List<String> versionList = versionMap.get(organizationList.get(i) + "_" + standardList.get(j)); 
					for(int k=0; k < versionList.size(); k++){
						
						// Get Revision 
						List<String> revisionList = revisionMap.get(organizationList.get(i) + "_" + standardList.get(j) + "_" + versionList.get(k)); 
						    for(int l=0; l < revisionList.size(); l++){
						    	
			%>
			
			var std_ver_rev_value="<%=organizationList.get(i)%>" + "_" + "<%=standardList.get(j)%>" + "_" +"<%=versionList.get(k)%>" + "_" + "<%=revisionList.get(l)%>";
			var std_ver_rev_key="<%=standardList.get(j)%>" + " - " +"<%=versionList.get(k)%>" + "  " + "[ <%=revisionList.get(l)%> ]";
				addOption(document.standardsForm.Standard,std_ver_rev_value, std_ver_rev_key);
			
			<%
						    }//loop l
					}//loop k
				}//loop j
			%>
			}//organization	
		<%}//loop i%>
	}//function

	$(document).ready(function() {
		$( "#Organization" ).change(function() {
	  
			sortStandards();
	        //e.preventDefault();
		});
	});
	
	function sortStandards(){
		
		$("#Standard").html($('#Standard option').sort(function(x, y) {
            return $(x).val() < $(y).val() ? -1 : 1;
        }));
        $("#Standard").get(0).selectedIndex = 0;
	}
	
	
	function SelectVersion(){
		// ON selection of organization this function will work
		
		removeAllOptions(document.standardsForm.Version);
		addOption(document.standardsForm.Version, "", "Version", "");
		<%
		for(int i=0;i<organizationList.size();i++){
		%>
			if(document.standardsForm.Organization.value == '<%=organizationList.get(i)%>'){			
                        <%	
				List<String> standardList = standardMap.get(organizationList.get(i)); 
				for(int j=0;j<standardList.size();j++){
			%>
				if(document.standardsForm.Standard.value == '<%=standardList.get(j)%>'){
				<% 
					List<String> versionList = versionMap.get(organizationList.get(i) + "_" + standardList.get(j)); 
					for(int k=0; k < versionList.size(); k++){
				%>
					addOption(document.standardsForm.Version,"<%=versionList.get(k)%>", "<%=versionList.get(k)%>");
					<%}//loop k%>
				}//standard
				<%}//loop j%>
			}//organization
		<%}//loop i%>
	}//function
	function SelectTest(){
		// ON selection of organization this function will work
		
		removeAllOptions(document.standardsForm.Test);
		addOption(document.standardsForm.Test, "", "Test", "");
		<%
		for(int i=0; i < organizationList.size(); i++){
		%>
			if(document.standardsForm.Organization.value == '<%=organizationList.get(i)%>'){			
			<%	
				List<String> standardList = standardMap.get(organizationList.get(i)); 
				for(int j=0; j < standardList.size(); j++){
				%>
					if(document.standardsForm.Standard.value == '<%=standardList.get(j)%>'){
					<% 
						List<String> versionList = versionMap.get(organizationList.get(i) + "_" + standardList.get(j)); 
					    for(int k=0; k < versionList.size(); k++){
					%>
						if(document.standardsForm.Version.value == '<%=versionList.get(k)%>'){
						<% 
							List<String> revisionList = revisionMap.get(organizationList.get(i) + "_" + standardList.get(j) + "_" + versionList.get(k)); 
						    for(int l=0; l < revisionList.size(); l++){
						%>
								addOption(document.standardsForm.Test,"<%=revisionList.get(l)%>", "<%=revisionList.get(l)%>");
							<%}//loop l%>
						}//version	
						<%}//loop k%>
					}//standard
				<%}//loop j%>
			}//organization
		<%}//loop i%>
	}//funciton
	
	function SelectProfile() {
        var profile_div;
	    var i = 0;
		if (profiles_key != null) {
            profile_div = document.getElementById(profiles_key);
            if (profile_div != null) {
				profile_div.style.display = "none";
				var inputs = profile_div.getElementsByTagName("input");
				for (i = 0; i < inputs.length; i++) {
					inputs[i].checked = false;
				}
			}
		}
		profiles_key = "Profiles";
	    profiles_key += "_" + document.standardsForm.Standard.value;
	    /* 
	    profiles_key += "_" + document.standardsForm.Organization.value;
	    profiles_key += "_" + document.standardsForm.Standard.value;
	    profiles_key += "_" + document.standardsForm.Version.value;
	    profiles_key += "_" + document.standardsForm.Test.value;
	    */
	    
        profile_div = document.getElementById(profiles_key);
        if (profile_div != null) {
			profile_div.style.display = "block";
		}
	}


	function removeAllOptions(selectbox)
	{
		var i;
		for(i=selectbox.options.length-1;i>=0;i--)
		{
			//selectbox.options.remove(i);
			selectbox.remove(i);
		}
	}
	
	function addOption(selectbox, value, text)
	{
		var optn = document.createElement("OPTION");
		optn.text = text;
		optn.value = value;
	
		selectbox.options.add(optn);
	}
	
	
	function viewInformation(){
	    var sourceId = document.standardsForm.Standard.value + "-" + document.standardsForm.Version.value;
	    if(sourceId != "-"){
			alert("No Documentation Available for: "+sourceId)
<%--
			<%
			Set suiteLinks = suiteMap.keySet();         // The set of keys in the map.
		    Iterator suiteLinksIter = suiteLinks.iterator();
		    while (suiteLinksIter.hasNext()) {
		       Object sourceId = suiteLinksIter.next();  	 // Get the next key.
		       Suite suite = (Suite)suiteMap.get(sourceId);  // Get the value for that key.
			%>
				if(sourceId == '<%=sourceId%>'){
				<%	if(suite.getLink() != null){
					%>
						window.location = '<%=suite.getLink()%>'
					<%	
					}//not null
					else {
					%>
						alert("No Documentation Available for: "+sourceId)
					<%	
					}//null	
				%>
				}     
	    	<%}//loop iterator%>
--%>
	    }
	    else{
	    	alert("Plase select from available standards");
	    }
	}
	
	function submitform() {
		var form = document.standardsForm;
		if (form.Organization.value == "") {
	    	alert("Please select from available Organization");
			document.getElementById("Organization").focus();
			return false;
	    } else if (form.Standard.value == "") {
	    	alert("Please select from available standards");
			document.getElementById("Standard").focus();
			return false;
	    } else {
	      //  var sourceId = form.Organization.value + "_" + form.Standard.value + "_" + form.Version.value + "_" + form.Test.value;
	        var sourceId = form.Standard.value;
	        
			document.forms["standardsForm"].elements["sources"].value = sourceId;
			document.standardsForm.submit();
	    }
	}
				
</script>
</head>
<body onload="fillOrganization()" >
<%@ include file="header.jsp"%>
<form name="standardsForm" action="test.jsp" method="post" >

<h4> Select a test suite: </h4>

<table  width="60%" >
	<!-- <tr>
		<th width="15%">Organization</th>
		<th width="15%">Specification</th>
	<th width="15%">Version</th>
		<th width="15%">Revision</th> 
	</tr> -->
	<tr>
		<td style="width:20%;">Organization</td>
		<td style="width:100%;">
			<select  id="Organization" name="Organization" onChange="SelectStandard();" style="width:60%;">
			<!--  <option value="">Organization</option> -->
			</select>
		</td>
	</tr>
	<tr>
	    <td style="width:20%;">Specification</td>	
		<td style="width:100%;">
			<select id="Standard" name="Standard" onChange="SelectProfile();" style="width:60%;" >
			<!-- <option value="">Specification</option> -->
			</select>
		</td>
		
		<!-- 
		<td>
			<select id="Version" name="Version" onChange="SelectTest();" >
			<option value="">Version</option>
			</select>
		</td>
		<td>
			<select id="Test" name="Test" onChange="SelectProfile();" disabled="true" >
			<option value="">Revision</option>
			</select>
		</td>
		-->
	</tr>

	<tr>
		<td style="width:20%;">Description (Optional):</td>
		<td style="width:100%;"> 
			<input type="text" name="description" id="description" style="width:60%;" />
		</td>
	</tr>
	
</table>


<%
	for (int i=0; i < organizationList.size(); i++) {
	    String org = organizationList.get(i);
		List<String> standardList = standardMap.get(org); 
		for (int j=0; j < standardList.size(); j++) {
		    String std = standardList.get(j);
			List<String> versionList = versionMap.get(org + "_" + std); 
		    for (int k=0; k < versionList.size(); k++) {
		        String ver = versionList.get(k);
				List<String> revisionList = revisionMap.get(org + "_" + std + "_" + ver); 
				for (int l=0; l < revisionList.size(); l++) {
				    String rev = revisionList.get(l);
				    String key = org + "_" + std + "_" + ver + "_" + rev;
%>
<div id="Profiles_<%=key%>" style="display:none">
		<%
		List<ProfileEntry> profileList = profiles.get(key);
 		if(profileList.size() != 0){ 
		%>
			<br />
			Select Profile(s): <br/>
<%
		}
					
					for (int m=0; m < profileList.size(); m++) {
					    ProfileEntry profile = profileList.get(m);
%>
<input type="checkbox" name="profile_<%=m%>" value="<%=profile.getId()%>"/><%=profile.getTitle()%><br/>
<%
					}
%>
</div>
<%
				}
		    }
		}
	}
%>
<br/>
<input type="submit" value="Start a new test session" onclick="return submitform();" /> 
<br/>
<input type="hidden" name="mode" value="test" />
<input type="hidden" id="sources" name="sources" />
<input type="hidden" id="suite" name="suite" />
<p></p>
</form>
<%@ include file="footer.jsp"%>
</body>
</html>
