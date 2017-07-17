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
  }
}