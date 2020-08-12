#!/usr/bin/env groovy

def call(Map param){
	pipeline {
		agent {
			node{
				label 'dockerworker'
			}
		}
		stages {
			stage('Build') {
				steps {
					sh 'mvn -B -DskipTests clean package'
				}
			}
			stage('Test') {
				steps {
					sh 'mvn test'
				}
				post {
					always {
						junit 'target/surefire-reports/*.xml'
					}
				}
			}
		}
		post {
        always {
            telegramSend(message: 'Hello World', chatId: -499110677)
        }
    }
	}
}
