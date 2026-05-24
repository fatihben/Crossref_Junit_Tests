pipeline {
    agent any

    tools {
        allure 'allure'
    }

    stages {
        stage('Run QA Tests') {
            steps {
                bat '''
                    cd "C:\\Users\\DELL\\IdeaProjects\\Crossref_JUnit"
                    mvn clean test
                    xcopy /E /I /Y "target\\allure-results" "%WORKSPACE%\\allure-results"
                '''
            }
        }
    }

    post {
        always {
            allure([
                includeProperties: false,
                jdk: '',
                results: [[path: 'allure-results']]
            ])
        }
    }
}