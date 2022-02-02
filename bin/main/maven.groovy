/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/
def call(stages){
        def stagesList = stages.split(';')
    sh "echo ${stagesList}"

    sCompile()
    sTest()
    sBuild()
    sSonar()
    sCurlSpring()   
    sUpNexus()
    sDownNexus()
    sUpJar()
    sCurlJar()
}

def sCompile(){
    stage("Paso 1: Compliar"){
        env.STAGE = env.STAGE_NAME
        sh "mvn clean compile -e"
    }
}

def sTest(){
    stage("Paso 2: Testear"){
        env.STAGE = env.STAGE_NAME
        sh "mvn clean test -e"
    }
}

def sBuild(){
    stage("Paso 3: Build .Jar"){
        env.STAGE = env.STAGE_NAME
        sh "mvn clean package -e"
    }
}

def sSonar(){
    stage("Paso 4: Sonar - Análisis Estático"){
        env.STAGE = env.STAGE_NAME
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }
}

def sCurlSpring(){
    stage("Paso 5: Curl Springboot Gradle sleep 20"){
        env.STAGE = env.STAGE_NAME
        sh "gradle bootRun&"
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

def sUpNexus(){
    stage("Paso 6: Subir Nexus"){
        env.STAGE = env.STAGE_NAME
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [
                $class: 'MavenPackage',
                mavenAssetList: [
                    [
                        classifier: '',
                        extension: '.jar',
                        filePath: 'build/DevOpsUsach2020-0.0.1.jar'
                    ]
                ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }
}

def sDownNexus(){
    stage("Paso 7: Descargar Nexus"){
        env.STAGE = env.STAGE_NAME
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}

def sUpJar(){
    stage("Paso 8: Levantar Artefacto Jar"){
        env.STAGE = env.STAGE_NAME
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}

def sCurlJar(){
    stage("Paso 9: Testear Artefacto - Dormir(Esperar 20sg) "){
        env.STAGE = env.STAGE_NAME
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;