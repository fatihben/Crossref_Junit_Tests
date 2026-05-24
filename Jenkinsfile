pipeline {
    agent any

    tools {
        allure 'allure'  // Tools'da tanımladığınız isim
    }

    stages {
        stage('Run QA Tests') {
            steps {
                bat 'cd "C:\\Users\\DELL\\IdeaProjects\\Crossref_JUnit" && mvn clean test'
            }
        }
    }

    post {
        always {
            // Allure raporu oluştur
            allure([
                includeProperties: false,
                jdk: '',
                results: [[path: 'C:\\Users\\DELL\\IdeaProjects\\Crossref_JUnit\\target\\allure-results']]
            ])
        }
    }
}