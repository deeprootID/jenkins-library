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
					expression { params.agent == 'dockerworker' }
				}
				steps {
					sh "docker build -t ${param.appname} ."
				}
			}
			stage('Run app in docker container') {
				when {
					expression { params.agent == 'dockerworker' }
				}
				steps {
					sh "docker run -p ${param.appname}"
				}
			}
			stage('Run app in VM') {
				when {
					expression { params.agent == 'vmworker' }
				}
				steps {
					sh "java -jar ${param.appname}.jar"
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
