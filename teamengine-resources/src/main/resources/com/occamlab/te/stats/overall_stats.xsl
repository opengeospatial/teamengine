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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" version="2.0">

	<xsl:output encoding="UTF-8" indent="yes" method="html" standalone="no" omit-xml-declaration="yes" />
	<xsl:output name="html" method="html" indent="yes" omit-xml-declaration="yes" />

	<xsl:param name="testSuiteNames" />
	<xsl:param name="year" />
	<xsl:param name="allTestSuiteRunDetails" />
	<xsl:param name="testsRunPerMonth" />
	<xsl:param name="usersPerMonth" />
	<xsl:param name="numberOfUsersAndTestSuite" />

	<xsl:template match="/">
		<html>
			<head>
				<title>TeamEngine Overall Statistics Report</title>
				<script src = "https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js">  </script>
		    	<link rel = "stylesheet" type = "text/css" href = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.min.css"></link>
		    	<script src = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.js">  </script>
		    	<script src = "https://cdn.jsdelivr.net/npm/chartjs-plugin-colorschemes">  </script>
		    	<style>
		    	    table, td, th {
		    	        border: 1px solid #ddd;
		    	        text-align: center;
		    	        font-family:sans-serif;
		    	    }
		    	    table {
		    	    	border-collapse: collapse;
		    	    	position:relative;
		    	    	margin-left:auto;
		    	    	margin-right:auto;
		    	    }
		    	    th, td {
		    	    	padding: 10px;
		    	    }
		    	</style>
			</head>
			<body>
			    <h2 style="text-align: center; padding-top: 10px">TeamEngine Overall Statistics</h2>
				<hr />
				<br />
				<div class="chart-container">
					<canvas id="pieChartAllTestSuiteRunDetailsContainer"
						style="position:relative; width:80vw; height:80vh">
					</canvas>
				</div>
				<br />
				<hr />
				<br />
				<div class="chart-container">
					<canvas id="barLineChartTestsRunPerMonthContainer"
						style="position:relative; width:80vw; height:80vh">
					</canvas>
				</div>
				<br />
				<hr />
				<br />
				<div class="chart-container">
					<canvas id="pieChartNumberOfUsersAndTestSuiteContainer"
						style="position:relative; width:80vw; height:80vh">
					</canvas>
				</div>
				<hr />
				<br />
				<h3 style="text-align: center; font-size:18px; color:#111111; font-family:sans-serif;">Statistics report for each standard</h3>
				<table>
				    <tr>
					    <th>Test Suites</th>
					</tr>
					<xsl:for-each select="$testSuiteNames">
						<xsl:variable name="link">
							<xsl:value-of select="." />
						</xsl:variable>
						<tr>
							<td>
								<a style="text-decoration: none;">
								    <xsl:attribute name="href">
									    <xsl:value-of select="concat(replace($link, ' ', '_'), '_stats.html')" />
								    </xsl:attribute>
									<xsl:value-of select="." />
								</a>
							</td>
						</tr>
					</xsl:for-each>
				</table>
				<script language="JavaScript">
					$(function () {

					<!-- pieChartAllTestSuiteRunDetailsContainer -->
					var allTestSuiteRunDetails_pie_chart = $("#pieChartAllTestSuiteRunDetailsContainer");
					var sorted_pie_data = ArraySort(<xsl:value-of select="$allTestSuiteRunDetails" />, 
												function (a, b) {return a - b});

					var s_label = Object.keys(sorted_pie_data);
					var s_data = Object.values(sorted_pie_data);

					new Chart(allTestSuiteRunDetails_pie_chart, {
						type: &apos;pie&apos;,
						data: {
							labels: s_label,
							datasets: [{
								label: &quot;teamengine&quot;,
								data: s_data,
							}]
						},
						options: {
							responsive: true,
							title: {
								display: true,
								position: &quot;top&quot;,
								text: &quot;Tests run per standard in <xsl:value-of select="$year" />&quot;,
								fontSize: 18,
								fontColor: &quot;#111&quot;
							},
							legend: {
								display: true,
								position: &quot;bottom&quot;,
								labels: {
									boxWidth: 15,
									fontColor: &quot;#333&quot;,
									fontSize: 12
								}
							}
						}
					});

					<!-- barLineChartTestsRunPerMonthContainer -->
					var barLineChartTestsRunPerMonthContainer = $(&quot;#barLineChartTestsRunPerMonthContainer&quot;);
					
					new Chart(barLineChartTestsRunPerMonthContainer, {
						type: &apos;bar&apos;,
						data: {
							datasets: [{
								label: &apos;Total tests per month in <xsl:value-of select="$year" />&apos;,
								yAxisID: &apos;A&apos;,
								data:<xsl:value-of select="$testsRunPerMonth" />
							}, 
							{
								label: &apos;Number of users per month in <xsl:value-of select="$year" />&apos;,
								yAxisID: &apos;B&apos;,
								data:<xsl:value-of select="$usersPerMonth" />,

								// Changes this dataset to become a line
								type: &apos;line&apos;,
								fill: false
							}],
							labels: [&apos;Jan&apos;, &apos;Feb&apos;, &apos;Mar&apos;, &apos;Apr&apos;, 
									&apos;May&apos;, &apos;Jun&apos;, &apos;Jul&apos;, &apos;Aug&apos;, 
									&apos;Sep&apos;, &apos;Oct&apos;, &apos;Nov&apos;, &apos;Dec&apos;]
						},
						options: {
							title: {
								display: true,
								position: &quot;top&quot;,
								text: &apos;Total number of tests and users per month in <xsl:value-of select="$year" />&apos;,
								fontSize: 18,
								fontColor: &quot;#111&quot;
							},
							legend: {
								display: true,
								position: &apos;bottom&apos;,
							},
							scales: {
								xAxes: [{
									gridLines: {
										display:false
									}
								}],
								yAxes: [{
									id: &apos;A&apos;,
									ticks: {
										beginAtZero: true,
										precision: 0
									},
									position: &apos;left&apos;,
									scaleLabel: {
										display: true,
										labelString: &apos;Total tests per month in <xsl:value-of select="$year" />&apos;
									},
									gridLines: {
										display:false
									}
								}, 
								{
									id: &apos;B&apos;,
									ticks: {
										beginAtZero: true,
										precision: 0
									},
									position: &apos;right&apos;,
									scaleLabel: {
										display: true,
										labelString: &apos;Number of users per month in <xsl:value-of select="$year" />&apos;
									},
									gridLines: {
										display:false
									}
								}]
							}
						}
					});

					<!-- pieChartNumberOfUsersAndTestSuiteContainer -->
					var pieChartNumberOfUsersAndTestSuiteContainer = $(&quot;#pieChartNumberOfUsersAndTestSuiteContainer&quot;);
					var sorted_pie_data = ArraySort(<xsl:value-of select="$numberOfUsersAndTestSuite" />, 
												function (a, b) {return a - b});
					var s_label = Object.keys(sorted_pie_data);
					var s_data = Object.values(sorted_pie_data);

					new Chart(pieChartNumberOfUsersAndTestSuiteContainer, {
						type: &apos;pie&apos;,
						data: {
							labels: s_label,
							datasets: [{
								label: &quot;Users&quot;,
								data: s_data,
							}]
						},
						options: {
							responsive: true,
							title: {
								display: true,
								position: &quot;top&quot;,
								text: &quot;Number of users per test suite in <xsl:value-of select="$year" />&quot;,
								fontSize: 18,
								fontColor: &quot;#111&quot;
							},
							legend: {
								display: true,
								position: &quot;bottom&quot;,
								labels: {
									boxWidth: 15,
									fontColor: &quot;#333&quot;,
									fontSize: 12
								}
							}
						}
					});

					});
					<!-- Toggle drilldown pie chart -->
					function toggleChart(){
						$("#failure-pie-chart-div").hide();
						$("#drilldown-pie").show();
					}

					<!-- Function to sort Associative Array by its values. -->
					ArraySort = function (array, sortFunc) {
						var tmp = [];
						var aSorted = [];
						var oSorted = {};

						for (var k in array) {
							if (array.hasOwnProperty(k))
							tmp.push({
								key: k,
								value: array[k]
							});
						}

						tmp.sort(function (o1, o2) {
							return sortFunc(o1.value, o2.value);
						});

						if (Object.prototype.toString.call(array) === '[object Array]') {
							$.each(tmp, function (index, value) {
								aSorted.push(value.value);
							});
							return aSorted;
						}

						if (Object.prototype.toString.call(array) === '[object Object]') {
							$.each(tmp, function (index, value) {
								oSorted[value.key] = value.value;
							});
							return oSorted;
						}
					};
				</script>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
