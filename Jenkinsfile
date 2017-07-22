pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        git(url: 'git@github.com:kuona/kuona-project.git', branch: 'master', poll: true)
      }
    }
    stage('Dependencies') {
      steps {
        dir(path: 'dashboard') {
          sh 'npm install'
        }
        
        sh 'sh dev-setup'
      }
    }
    stage('Build') {
      steps {
        sh 'ls -la'
        sh 'sh build'
      }
    }
  }
}