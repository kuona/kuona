pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        git(url: 'git@github.com:kuona/kuona-project.git', branch: 'master', poll: true)
        sh '''git submodule init
git submodule update
git submodule foreach git checkout master
git submodule foreach git pull'''
      }
    }
    stage('build libraries') {
      steps {
        dir(path: 'kuona-collector-lib') {
          sh 'lein install'
        }
        
      }
    }
    stage('git-collector') {
      steps {
        parallel(
          "git-collector": {
            dir(path: 'git-collector') {
              sh 'lein uberjar'
            }
            
            
          },
          "environment-service": {
            dir(path: 'environment-service') {
              sh 'lein uberjar'
            }
            
            
          }
        )
      }
    }
  }
}