node ("gretl") {
   echo 'Hello World'
   git 'https://github.com/chrira/greteljobs.git'

   sh 'ls -la /home/gradle/libs'

   dir('inttest/jobs/iliValidator') {
       sh 'pwd'
       sh 'ls -la'

       sh 'gretl validate'
    }
}
