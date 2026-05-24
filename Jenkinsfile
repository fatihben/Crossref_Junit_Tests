pipeline {
    agent any

    tools {
        allure 'allure'
    }

    stages {
        stage('Run QA Tests') {
            steps {
                bat 'cd "C:\\Users\\DELL\\IdeaProjects\\Crossref_JUnit" && mvn clean test'
            }
        }

        stage('Copy Allure Results') {
            steps {
                bat 'xcopy /E /I /Y "C:\\Users\\DELL\\IdeaProjects\\Crossref_JUnit\\target\\allure-results" "%WORKSPACE%\\allure-results"'
            }
        }

        stage('Generate Allure Report') {
            steps {
                allure([
                    includeProperties: false,
                    jdk: '',
                    results: [[path: 'allure-results']]
                ])
            }
        }
    }
}