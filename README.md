# thirdparties-maven-plugin

thirdparties-maven-plugin easily download external resources and attach them as classified artifacts.

The project is hosted in maven central.
[here](http://search.maven.org/#search%7Cga%7C1%7Cthirdparties-maven-plugin) you'll find a quick copy/paste for the dependency.

## Attach third party files as classified artifacts

Use the following goal: attached

### Configuration using a property file

	<configuration>
	  <thirdPartiesFile>${project.basedir}/src/main/thirdparties/yours.properties</thirdPartiesFile>
	</configuration>

Property file syntax is as follow:

	[classifier].[type].src=[url]
	[classifier].[type].md5=[md5]

Here is a quick example:

	foo.tar.gz.src=http://example.com/Foo-1.2.3-r5635.tar.gz
	foo.tar.gz.md5=94c331b029ab45d7db5d39cccacdf0e1

The md5 statement is optional and used only if present.
If absent a file that already exists is not downloaded.

### Configuration inside the POM

Here is a quick example:

	<configuration>
	  <thirdParties>
		<thirdParty>
		  <classifier>foo</classifier>
		  <type>tar.gz</type>
		  <src>http://example.com/Foo-1.2.3-r5635.tar.gz</src>
		  <md5>94c331b029ab45d7db5d39cccacdf0e1</md5>
		</thirdParty>
	  </thirdParties>
	</configuration>

## Replace project main artifact with a third party file

Use the following goal: artifact

### Configuration inside the POM

Here is a quick example:

	<configuration>
          <src>http://example.com/Foo-1.2.3-r5635.jar</src>
          <md5>60851505f87a3569db7e143f573c2904</md5>
	</configuration>


