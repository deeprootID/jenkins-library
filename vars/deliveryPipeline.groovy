#!/usr/bin/env groovy


def call(Map param){
	pipeline {
		agent {
			label "'${param.agent}'"
		}
		stages {
			stage ("telegram notif"){
				steps{
					echo "${getMessage()}"
				}
			}
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
			stage('Build docker image') {
				when {
					expression { param.agent == 'dockerworker' }
				}
				steps {
					sh "docker build -t ${param.appname} ."
				}
			}
			stage('Run app in docker container') {
				when {
					expression { param.agent == 'dockerworker' }
				}
				steps {
					sh "docker run -p 8080:8181 ${param.appname}"
				}
			}
			stage('Run app in VM') {
				when {
					expression { param.agent == 'vmworker' }
				}
				steps {
					sh "java -jar target/*.jar"
				}
			}
		}
		post {
			always {
				deleteDir()
			}
		}
    }
}

def getMessage (){
	def commiter = sh(script: "git show -s --pretty=%cn",returnStdout: true).trim()
	def message = "$commiter deploying app"
	return message
}
