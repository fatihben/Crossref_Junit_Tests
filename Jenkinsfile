pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Run Crossref Tests') {
            steps {
                // Eklenti bağımlılığı yaratmamak için düz bat komutuyla testi tetikliyoruz
                bat 'mvn clean test'
            }
        }
    }
}