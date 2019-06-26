<style>
.dropbtn {
  padding: 16px;
  font-size: 16px;
  border: none;
  background-color: white;
}

.dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-content {
  display: none;
  position: fixed;
  background-color: #f1f1f1;
  min-width: 165px;
  box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
  z-index: 1;
}

.dropdown-content a {
  color: black;
  padding: 12px 16px;
  text-decoration: none;
  display: block;
}

.dropdown:hover .dropdown-content {display: block;}

.no-close .ui-dialog-titlebar-close { display: none }
div.ui-dialog { position:fixed; } 
</style>
<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="https://code.jquery.com/jquery-1.12.4.js"></script>
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>

<%
String r_username = (String) request.getRemoteUser();
String u_fname = (String) request.getAttribute("u_firstName");
String u_lname = (String) request.getAttribute("u_lastName");
String u_email = (String) request.getAttribute("u_email");
String u_organization = (String) request.getAttribute("u_organization");
%>

<script>
var status = '<%=request.getAttribute("updateUserPopup")%>';
var r_username = '<%=r_username%>';

$(function () {
    var dialog, form,

        emailRegex = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/,
        fname = $("#FirstName"),
        lname = $("#LastName"),
        email = $("#Email"),
        org = $("#Organization"),
        allFields = $([]).add(fname).add(lname).add(email).add(org),
        tips = $(".validateTips");

    function updateTips(t) {
        tips.text(t).addClass("ui-state-highlight");
        setTimeout(function () {
            tips.removeClass("ui-state-highlight", 1500);
        }, 500);
    }

    function checkLength(o, n, min, max) {
        if (o.val().length > max || o.val().length < min) {
            o.addClass("ui-state-error");
            updateTips("Length of " + n + " must be between " + min +
                " and " + max + ".");
            return false;
        } else {
            return true;
        }
    }

    function checkRegexp(o, regexp, n) {
        if (!(regexp.test(o.val()))) {
            o.addClass("ui-state-error");
            updateTips(n);
            return false;
        } else {
            return true;
        }
    }

    function validateForm() {
        var valid = true;
        allFields.removeClass("ui-state-error");

        valid = valid && checkLength(fname, "FirstName", 3, 16);
        valid = valid && checkLength(lname, "LastName", 3, 16);
        valid = valid && checkLength(org, "Organization", 3, 16);
        valid = valid && checkRegexp(email, emailRegex, "Not a valid email");
        
        if(valid){
        	updateUserDetails();
        }
    }
    
    function updateUserDetails() {
        $.ajax({
            type: "post",
            url:  "updateUserDetails",
            data: "firstName=" + fname + "&lastName=" + lname + "&email=" + email + "&organization=" + org,
            success: function(msg){      
            	$( "#dialog-form" ).dialog( "close" );
            	setTimeout(function () {
            		if(msg.includes("success")){
                		alert("Details updated successfully!");
                	}
                }, 500);            	
            }
        });
    }

    dialog = $("#dialog-form").dialog({
    	dialogClass: "no-close",
		closeOnEscape: false,
        autoOpen: false,
        height: 400,
        width: 450,
        modal: true,
        buttons: {
            "Update": validateForm
        }
    });

    form = dialog.find("form").on("submit", function (event) {
        event.preventDefault();
        validateForm();
        updateUserDetails();
    });

    if (status == 'true') {
        //Open dialog on page load.
        dialog.dialog("open");
    }
});	
</script>
<div style="position: static">
	<div
		style="position: static; background-color: black; width: 100%; height: 100px; overflow: hidden">
		<!-- Image derived from "Dinky the Steam Engine - main drive wheel", Steve Karg, http://www.burningwell.org -->
		<img style="position: absolute" src="images/banner.jpg" alt="TEAM Engine Banner" />
		<div style="position: absolute;">
      	<div style="margin-bottom: 0.75em;"><img src="site/logo.png"/></div>
      	<%@include file="site/title.html" %>
		</div>
		<%
		    String user = request.getRemoteUser();
                    Cookie c_userName=new Cookie("User", user);
                    response.addCookie(c_userName);
		    if (user != null && user.length() > 0) {
		        out.println("\t\t<div style=\"position: absolute; right:20px; top:25px; background-color: white; border-style: inset\">");
		        out.println("<div class=\"dropdown\">");
				out.println("   <div class=\"dropbtn\"> User: " + user + " &#9660;</div>");
				out.println("	<div class=\"dropdown-content\">");
				out.println("		<a href=\"changePassword.jsp\">Change Password</a>");
				out.println("		<a href=\"updateUserDetailsHandler\">Update User Details</a>");
				out.println("		<a href=\"logout\">Logout</a>");
				out.println("  </div>");
				out.println("</div>");
				
		        out.println("\t\t</div>");
		    }
		%>		
	</div>
<hr>
</div>

<div id="dialog-form" title="Update user details">
	<p class="validateTips">All form fields are required.</p>
	<form method="post" action="updateUserDetailsHandler">
		<fieldset style="padding: 0; border: 0; margin-top: 25px; align: center;">
			<table>
				<tr>
					<td>First Name:</td>
					<td><input id="FirstName" name="FirstName" type="text" value="<%= u_fname == null ? "" : u_fname %>" /></td>
				</tr>
				<tr>
					<td>Last Name:</td>
					<td><input id="LastName" name="LastName" type="text" value="<%=  u_lname == null ? "" : u_lname %>" /></td>
				</tr>
				<tr>
					<td>Email:</td>
					<td><input id="Email" name="Email" type="text" value="<%= u_email == null ? "" : u_email %>" /></td>
				</tr>
				<tr>
					<td>Organization:</td>
					<td><input id="Organization" name="Organization" type="text" value="<%= u_organization == null ? "" : u_organization %>" /></td>
				</tr>
			</table>
			<input type="submit" tabindex="-1" style="position: absolute; top: -1000px" />
		</fieldset>
	</form>
</div>
