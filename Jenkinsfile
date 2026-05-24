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
                // Jenkins'e testi koşarken lokaldeki tüm Maven argümanlarını dikkate almasını söylüyoruz
                bat 'mvn clean test -Dallure.results.directory=allure-results'
            }
        }
    }
}