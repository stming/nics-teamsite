To turn this project into an Eclipse project do the following.

1.	Copy the .classpath from config/.classpath to the root and 
	change the properties for the project.
	
		  i. Source
		  	 Here you set the 'root' of different Java source paths.
		  	 Select classes/src/java and classes/src/junit and
		  	 this will allow for things like code complete.
		  	 
		 ii. Projects
		 	 Here you can choose to add other Eclipse projects.
		 	 Not required here.
		 	 
		iii. Libraries
			 Here you can internal (project jars) external jars as well as
			 well as Eclipse Jars. For the project add all of the libraries
			 under {root}/lib as well as the eclipse JRE jar.
			 
		 iv. Order and Export
		 	 Sets the order of jar files in case of clashes.
		
	
2.  You can use eclipse to run the ant build.xml. If you also want to run the 
	junit tasks via the ant script then you will need to modify the preferences
	for the ant plugin. First though you need to make sure that the following 
	classes are available (either from your installed ant or Eclipse ant plugin)
	
	ant-junit.jar
	junit 3.8.jar
	junitee 3.8.jar
	junitee-anttask.jar
	
	You then need to make sure they are seen via the ant plugin as libraries, do this
	by selecting 
	Window --> Preferences --> Ant --> Runtime --> Classpath --> Ant Home Entries --> Add External Jars 
	Choose the above mentioned jar files.
	
	You should then be able to run the individual tasts in the build.xml from the Eclipse IDE