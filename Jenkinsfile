node {
    env.JAVA_HOME="${tool 'jdk-8u45'}"
    env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
    sh 'java -version'
    
    stages {
        stage('Checkout') {
            steps {
                git(url: 'git@github.com:kuona/kuona-project.git', branch: 'master', poll: true)
            }
        }
        stage('Environment setup') {
            steps {
                sh 'sh dev-setup'
            }
        }
        stage('Build') {
            steps {
                sh 'sh build'
            }
        }
    }
}
