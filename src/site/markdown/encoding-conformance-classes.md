# Encoding Conformance Classes

This page provides the information about how to encode conformance classes in CTL and TestNG tests to better provide information about the grouping of the conformance class and which one is the basic conformance class needed for certification.

The configuration in the tests to represent the conformance class (CC) and basic conformance class in tests are as follows:

## Configuration in CTL test

The conformance classes must be configured on the second level of test hierarchy.
Example: The starting-test of the suite references the test 'main' (which is the test on first level). The test 'main' executes several tests which should be marked as conformance classes, e.g. 'cc-one' and 'cc-one' (tests on second level). The tests assigned to a second level test may be grouped in hierarchies with undefined depth.
Neither the test on the first level nor the tests below the second level can be configured as conformance classes!

To represent the conformance class and basic conformance class we need to add two attributes i.e. **`isConformanceClass`** and **`isBasic`**.

#### isConformanceClass:

By using this attribute we are making a particular test take the role of a conformance class.

#### isBasic:

By using this attribute we can mark a test as basic conformance class, which is the minimum required for OGC certification.

* For Example:

```xml
<ctl:test name="ex:basic-main" isConformanceClass="true" isBasic="true">
......
......
</ctl:test>
```

**Note:**

1. For a CC to be basic it needs to have both attributes _isConformanceClass="true" isBasic="true"_ 
1. If the test is a CC but not a basic CC then the of _isBasic_ can be set to false.
1. If there is no attribute _isBasic_ in a CC, then the value of _isBasic_ is assumed to be false.
1. If the test is not a CC then there is no need to add any attribute.

## Configuration in TestNG tests

TestNG tests already have the notion of conformance classes (CC). Still needs to be configured to represent the basic conformance class. The following needs to be checked and performed:

* Add a snippet in the config.xml of the TestNG test under the **`suite`** element that explicitly states which tests are basic. For example:

```xml
<BasicConformanceClasses>
  <conformanceClass>Basic conformance class name</conformanceClass>
</BasicConformanceClasses>
```
