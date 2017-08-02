pipeline {
    agent any
    tools {
        jdk 'jdk8'
    }
    stages {
        stage('Setup') {
            steps {
                sh './dev-setup'
            }
        }
        stage('Build') {
            steps {
                sh './build'
            }
        }
    }
}
