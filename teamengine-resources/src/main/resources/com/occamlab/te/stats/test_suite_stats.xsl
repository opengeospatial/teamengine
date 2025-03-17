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

	<xsl:param name="testSuiteName" />
	<xsl:param name="year" />
	<xsl:param name="numberOfUsersExecutedTestSuitePerMonth" />
	<xsl:param name="testSuiteRunPerMonth" />
	<xsl:param name="successArray" />
	<xsl:param name="failureArray" />
	<xsl:param name="incompleteArray" />
	<xsl:param name="testSuiteStatusWithDrilldown" />
	<xsl:param name="testSuiteFailedTestDrillDownMap" />

	<xsl:template match="/">
		<html>
			<head>
				<title><xsl:value-of select="$testSuiteName" /> Statistics Report</title>
				<script src = "https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js">  </script>
		    	<link rel = "stylesheet" type = "text/css" href = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.min.css"></link>
		    	<script src = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.js">  </script>
		    	<script src = "https://cdn.jsdelivr.net/npm/chartjs-plugin-colorschemes">  </script>
			</head>
			<body>
			    <h2 style="text-align: center; padding-top: 10px"><xsl:value-of select="$testSuiteName" /> Statistics</h2>
				<hr />
				<br />
				<div class="chart-container">
					<canvas id="standardRunPerMonthContainer"
						style="position:relative; width:80vw; height:80vh">
					</canvas>
				</div>
				<br />
				<hr />
				<br />
				<div class="chart-container">
					<canvas id="userExecutedStandardRunsPerMonthContainer"
						style="position:relative; width:80vw; height:80vh">
					</canvas>
				</div>
				<br />
				<hr />
				<br />
				<div class="chart-container">
					<canvas id="standardSuccessFailureContainer"
						style="position:relative; width:80vw; height:80vh">
					</canvas>
				</div>
				<br />
				<hr />
				<br />
				<div class="chart-container">
					<canvas id="standardSuccessFailureDrillDownContainer"
						style="position:relative; width:80vw; height:70vh"></canvas>
				</div>
				<div class="chart-container" style="position:relative;"
					id="failure-pie-chart-div">
					<canvas id="failure-pie-chart"
						style="position:relative; width:80vw; height:70vh"></canvas>
					<button type="button"
						style="position:absolute; top:100px; right:200px;"
						onclick="toggleChart();">Back </button>
				</div>
				<script language="JavaScript">
					$(function () {

                        <!-- standardRunPerMonthContainer -->
						new Chart(document.getElementById(&quot;standardRunPerMonthContainer&quot;),
						{
							type: &apos;line&apos;,
							responsive: true,
							maintainAspectRatio: false,
							data: {
								labels: [&apos;Jan&apos;, &apos;Feb&apos;, &apos;Mar&apos;, &apos;Apr&apos;, 
										&apos;May&apos;, &apos;Jun&apos;, &apos;Jul&apos;, &apos;Aug&apos;, 
										&apos;Sep&apos;, &apos;Oct&apos;, &apos;Nov&apos;, &apos;Dec&apos;],
								datasets: [{
									data:<xsl:value-of select="$testSuiteRunPerMonth" />,
									label: &quot;<xsl:value-of select="$testSuiteName" />&quot;,
									borderColor: &quot;#3e95cd&quot;,
									fill: false
								}]
							},
							options: {
								title: {
									display: true,
									text: &apos;Standard run per month in <xsl:value-of select="$year" />&apos;,
									fontSize: 18
								},
								legend: {
									display: true,
									position: &apos;bottom&apos;,
								},
								scales: {
									xAxes: [{
										gridLines: {
											display: false
										}
									}],
									yAxes: [{
										ticks: {
											beginAtZero: true,
											precision: 0
										},
										scaleLabel: {
											display: true,
											labelString: &apos;Test Count&apos;
										}
									}]
								}
							}
						});
                        
						<!-- userExecutedStandardRunsPerMonthContainer -->
						new Chart(document.getElementById(&quot;userExecutedStandardRunsPerMonthContainer&quot;), 
						{
							type: &apos;line&apos;,
							responsive: true,
							maintainAspectRatio: false,
							data: {
								labels: [&apos;Jan&apos;, &apos;Feb&apos;, &apos;Mar&apos;, &apos;Apr&apos;, 
										&apos;May&apos;, &apos;Jun&apos;, &apos;Jul&apos;, &apos;Aug&apos;, 
										&apos;Sep&apos;, &apos;Oct&apos;, &apos;Nov&apos;, &apos;Dec&apos;],
								datasets: [{
									data:<xsl:value-of select="$numberOfUsersExecutedTestSuitePerMonth" />,
									label: &quot;<xsl:value-of select="$testSuiteName" />&quot;,
									borderColor: &quot;#3e95cd&quot;,
									fill: false
								}]
							},
							options: {
								title: {
									display: true,
									text: &apos;Number of users executed the standard per month in <xsl:value-of select="$year" />&apos;,
									fontSize: 18
								},
								legend: {
									display: true,
									position: &apos;bottom&apos;,
								},
								scales: {
									xAxes: [{
										gridLines: {
											display: false
										}
									}],
									yAxes: [{
										ticks: {
											beginAtZero: true,
											precision: 0
										},
										scaleLabel: {
											display: true,
											labelString: &apos;Test Count&apos;
										}
									}]
								}
							}
						});

						<!-- standardSuccessFailureContainer -->
						new Chart(document.getElementById(&quot;standardSuccessFailureContainer&quot;),
						{
							type: &apos;bar&apos;,
							responsive: true,
							maintainAspectRatio: false,
							data: {
								labels: [&apos;Jan&apos;, &apos;Feb&apos;, &apos;Mar&apos;, &apos;Apr&apos;, 
										&apos;May&apos;, &apos;Jun&apos;, &apos;Jul&apos;, &apos;Aug&apos;, 
										&apos;Sep&apos;, &apos;Oct&apos;, &apos;Nov&apos;, &apos;Dec&apos;],
								datasets: [{
									label: &quot;Success&quot;,
									backgroundColor: &quot;#33cc33&quot;,
									borderWidth: 1,
									data:<xsl:value-of select="$successArray" />
								},
								{
									label: &quot;Failure&quot;,
									backgroundColor: &quot;#ff0000&quot;,
									borderWidth: 1,
									data:<xsl:value-of select="$failureArray" />
								},
								{
									label: &quot;Incomplete&quot;,
									backgroundColor: &quot;#ffff00&quot;,
									borderWidth: 1,
									data:<xsl:value-of select="$incompleteArray" />
								}]
							},
							options: {
								title: {
									display: true,
									text: &apos;Passing, failing and incomplete tests run in <xsl:value-of select="$year" />&apos;,
									fontSize: 18
								},
								legend: {
									display: true,
									position: &apos;bottom&apos;,
								},
								scales: {
									xAxes: [{
										gridLines: {
											display: false
										}
									}],
									yAxes: [{
										ticks: {
											beginAtZero: true,
											precision: 0
										},
										scaleLabel: {
											display: true,
											labelString: &apos;Test Count&apos;
										}
									}]
								}
							}
						});

						<!--  Drilldown Pie Chart  -->

						$(&quot;#failure-pie-chart-div&quot;).hide();
						var drilldown_pie_chart = $(&quot;#standardSuccessFailureDrillDownContainer&quot;);
						var s_label = Object.keys(<xsl:value-of select="$testSuiteStatusWithDrilldown" />);
						var s_data = Object.values(<xsl:value-of select="$testSuiteStatusWithDrilldown" />);
						var drilldownPieChart = new Chart(drilldown_pie_chart, 
						{
							type: &apos;pie&apos;,
							data: {
								labels: s_label,
								datasets: [{
									label: &quot;<xsl:value-of select="$testSuiteName" />&quot;,
									data: s_data,
									backgroundColor: [&apos;#ffff00&apos;, &apos;#33cc33&apos;, &apos;#ff0000&apos;]
								}]
							},
							options: {
								responsive: true,
								title: {
									display: true,
									position: &quot;top&quot;,
									text: &quot;Passing, failing and incomplete tests run in <xsl:value-of select="$year" />&quot;,
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
								},
								&apos;onClick&apos; : function (e, item) {
									var activePoints = drilldownPieChart.getElementsAtEvent(e);
									var selectedIndex = activePoints[0]._index;
									var failureLabel = this.data.labels[selectedIndex];

									if(failureLabel == &apos;Failure&apos;) {
										$(&quot;#standardSuccessFailureDrillDownContainer&quot;).hide();
										$(&quot;#failure-pie-chart-div&quot;).show();
										$(&apos;#failure-pie-chart-div&apos;).focus();
									}
								}
							}
						});
					<!-- Failure pie chart graph -->
					var sorted_failure_pie_data = ArraySort(<xsl:value-of select="$testSuiteFailedTestDrillDownMap" />, 
														function (a, b) {return a - b});
					var s_failure_pie_label = Object.keys(sorted_failure_pie_data);
					var s_failure_pie_data = Object.values(sorted_failure_pie_data);

					new Chart(document.getElementById(&quot;failure-pie-chart&quot;), 
					{
						type: &apos;pie&apos;,
						data: {
							labels: s_failure_pie_label,
							datasets: [{
								label: &quot;Failure tests&quot;,
								data: s_failure_pie_data
							}]
						},
						options: {
							title: {
								display: true,
								position: &quot;top&quot;,
								text: &apos;Passing, failing and incomplete tests run in <xsl:value-of select="$year" />&apos;,
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
						$("#standardSuccessFailureDrillDownContainer").show();
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
