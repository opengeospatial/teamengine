# CTL scripts

The OGC test harness is capable of running tests implemented using the CTL 
([Compliance Test Language](http://portal.opengeospatial.org/files/?artifact_id=33085)) 
scripting language developed by the OGC. The CTL grammar can be regarded as a 
kind of XML-based domain-specific language for defining test suites. A test 
suite is processed to produce XSLT 2.0 templates that are then executed by the 
Saxon 9.0 XSLT processor as shown in the following figure.

__Figure 1:__ Executing CTL scripts

![Executing CTL scripts](images/ctl-execution.png)

The teamengine-core-\${project.version}-base.zip archive contains a sample test 
suite in `scripts/note/1.0/ctl/note.ctl`. The OGC compliance testing program 
also maintains a set of test suites that are publicly available from GitHub at 
this location: [https://github.com/opengeospatial](https://github.com/opengeospatial).

## Grouping tests

If one ctl:code element is used to execute multiple tests, failures in subtests can lead to confusing test results.

For example, this piece of ctl code:

	<ctl:test name="Test">
		<ctl:code>
			<ctl:call-test name="Subtest">
			</ctl:call-test>
			<xsl:if test="assertion 1">
				<ctl:message>[FAILURE] 1.</ctl:message>
				<ctl:fail/>
			</xsl:if>
			
			<ctl:call-test name="Subtest">
			</ctl:call-test>
			<xsl:if test="assertion 2">
				<ctl:message>[FAILURE] 2.</ctl:message>
				<ctl:fail/>
			</xsl:if>
		</ctl:code>
	</ctl:test>
	
	<ctl:test name="Subtest">
		<ctl:code>
			<xsl:if test="assertion 3">
				<ctl:message>[FAILURE] 3.</ctl:message>
				<ctl:fail/>
			</xsl:if>
		</ctl:code>
	</ctl:test>

The test Subtest is called twice, e.g. evaluating some common prerequisites for assertion 1 and 2.

If assertion 2 would evaluate to true, thus causing a ctl:fail, the following HTML report would	be generated:

![TestNG results summary](images/ctl-subtests-misleading.png)

No information would be given regarding the reason of failure. The ctl:message would only be found in the logs.

This can be confusing for users of the test suite.

As a good practice, we would suggest to create different Subtests that include the assertions of the xsl:if statements.

	<ctl:test name="Test">
		<ctl:code>
			<ctl:call-test name="Subtest 1">
			</ctl:call-test>
			
			<ctl:call-test name="Subtest 2">
			</ctl:call-test>
		</ctl:code>
	</ctl:test>
	
	<ctl:test name="Subtest 1">
		<ctl:code>
			<xsl:if test="assertion 3">
				<ctl:message>[FAILURE] 3.</ctl:message>
				<ctl:fail/>
			</xsl:if>
			<xsl:if test="assertion 1">
				<ctl:message>[FAILURE] 1.</ctl:message>
				<ctl:fail/>
			</xsl:if>
		</ctl:code>
	</ctl:test>
	
	<ctl:test name="Subtest 2">
		<ctl:code>
			<xsl:if test="assertion 3">
				<ctl:message>[FAILURE] 3.</ctl:message>
				<ctl:fail/>
			</xsl:if>
			<xsl:if test="assertion 2">
				<ctl:message>[FAILURE] 2.</ctl:message>
				<ctl:fail/>
			</xsl:if>
		</ctl:code>
	</ctl:test>
	
Now, if assertion 2 would evaluate to true, the following HTML report would be generated:

![TestNG results summary](images/ctl-subtests-good-practice.png)

Now the source of the error can be investigated further by clicking on the failed Subtest 2.
