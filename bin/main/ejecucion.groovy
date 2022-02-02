def call(){
    pipeline {
        agent any
        environment {
            NEXUS_USER         = credentials('user-nexus')
            NEXUS_PASSWORD     = credentials('password-nexus')
        }
        parameters {
            choice(
                name:'compileTool',
                choices: ['Maven', 'Gradle'],
                description: 'Seleccione herramienta de compilacion'
            )
        }
        stages {
            stage("Pipeline"){
                steps {
                    script{
                    switch(params.compileTool)
                        {
                            case 'Maven':
                                //def ejecucion = load 'maven.groovy'
                                //ejecucion.call()
                                maven.call()
                            break;
                            case 'Gradle':
                                //def ejecucion = load 'gradle.groovy'
                                //ejecucion.call() 
                                gradle.call()
                            break;
                        }
                    }
                }
                post{
                    success{
                        slackSend color: 'good', message: "[Cristian] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                    }
                    failure{
                        slackSend color: 'danger', message: "[Cristian] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage [${STAGE_NAME}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                    }
                }
            }
        }
    }
}

return this;