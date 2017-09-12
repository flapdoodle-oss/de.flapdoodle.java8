node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Checkout code from repository
   checkout scm

   // Get the maven tool.
   // ** NOTE: This 'M3' maven tool must be configured
   // **       in the global configuration.
   def mvnHome = tool 'M3'

   // Mark the code build 'stage'....
   stage 'Build'
   // Run the maven build
   sh "${mvnHome}/bin/mvn clean install"

   step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
	  step([$class: 'JacocoPublisher', 
              execPattern: '**/target/jacoco.exec', 
              classPattern: '**/target/classes', 
              sourcePattern: '**/src/main/java'])
}
