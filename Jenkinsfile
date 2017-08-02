pipeline {
  agent any
  stages {
    stage('Setup') {
      steps {
        sh 'sh dev-setup'
      }
    }
    stage('Build') {
      steps {
        tool(name: 'Java 8', type: 'JDK')
        sh 'build'
      }
    }
  }
}