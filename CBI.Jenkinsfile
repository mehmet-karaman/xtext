pipeline {
  agent {
    kubernetes {
      label 'xtext-eclipse-' + env.BRANCH_NAME + '-' + env.BUILD_NUMBER
      defaultContainer 'xtext-buildenv'
      yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jnlp
    image: 'eclipsecbi/jenkins-jnlp-agent'
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
    resources:
      limits:
        memory: "0.4Gi"
        cpu: "0.2"
      requests:
        memory: "0.4Gi"
        cpu: "0.2"
    volumeMounts:
    - mountPath: /home/jenkins/.ssh
      name: volume-known-hosts
  - name: xtext-buildenv
    image: docker.io/smoht/xtext-buildenv:0.7
    tty: true
    resources:
      limits:
        memory: "3.6Gi"
        cpu: "1.0"
      requests:
        memory: "3.6Gi"
        cpu: "1.0"
    volumeMounts:
    - name: settings-xml
      mountPath: /home/jenkins/.m2/settings.xml
      subPath: settings.xml
      readOnly: true
    - name: m2-repo
      mountPath: /home/jenkins/.m2/repository
    - name: volume-known-hosts
      mountPath: /home/jenkins/.ssh
  volumes:
  - name: volume-known-hosts
    configMap:
      name: known-hosts
  - name: settings-xml
    configMap: 
      name: m2-dir
      items:
      - key: settings.xml
        path: settings.xml
  - name: m2-repo
    emptyDir: {}
    '''
    }
  }
  
  environment {
    DOWNSTREAM_JOBS = 'xtext-xtend'
  }

  parameters {
    choice(name: 'target_platform', choices: ['oxygen', 'latest', 'r201812', 'r201809', 'r201903', 'r201906', 'photon'], description: 'Which Target Platform should be used?')
    booleanParam(
      name: 'TRIGGER_DOWNSTREAM_BUILD', 
      defaultValue: (env.BRANCH_NAME.startsWith('milestone')||env.BRANCH_NAME.startsWith('release')), 
      description: 'Should downstream jobs for the same branch be triggered on successful build?'
    )
  }

  options {
    buildDiscarder(logRotator(numToKeepStr:'5'))
    disableConcurrentBuilds()
    timeout(time: 150, unit: 'MINUTES')
    timestamps()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        
        script {
          if (params.target_platform == 'latest') {
            currentBuild.displayName = "#${BUILD_NUMBER}(4.13)"
          } else if (params.target_platform == 'r201906') {
            currentBuild.displayName = "#${BUILD_NUMBER}(4.12)"
          } else if (params.target_platform == 'r201903') {
            currentBuild.displayName = "#${BUILD_NUMBER}(4.11)"
          }  else if (params.target_platform == 'r201812') {
            currentBuild.displayName = "#${BUILD_NUMBER}(4.10)"
          } else if (params.target_platform == 'r201809') {
            currentBuild.displayName = "#${BUILD_NUMBER}(4.9)"
          } else if (params.target_platform == 'photon') {
            currentBuild.displayName = "#${BUILD_NUMBER}(4.8)"
          } else {
            currentBuild.displayName = "#${BUILD_NUMBER}(4.7)"
          }
        }

        sh '''
            sed_inplace() {
                if [[ "$OSTYPE" == "darwin"* ]]; then
                    sed -i '' "$@"
                else
                    sed -i "$@"
                fi
            }
            
            targetfiles="$(find releng -type f -iname '*.target')"
            for targetfile in $targetfiles
            do
                echo "Redirecting target platforms in $targetfile to $JENKINS_URL"
                sed_inplace "s?<repository location=\\".*/job/\\([^/]*\\)/job/\\([^/]*\\)/?<repository location=\\"$JENKINS_URL/job/\\1/job/\\2/?" $targetfile
            done
        '''
      }
    }

    stage('Build') {
      steps {
          sh """
            /home/vnc/.vnc/xstartup.sh
            ./1-maven-build.sh -s /home/jenkins/.m2/settings.xml --tp=${params.target_platform} --local-repository=/home/jenkins/.m2/repository
          """
      }
    }
  }

  post {
    always {
      junit testResults: '**/target/surefire-reports/*.xml'
    }
    success {
      archiveArtifacts artifacts: 'build/**'
      script {
        if (params.TRIGGER_DOWNSTREAM_BUILD==true) {
          DOWNSTREAM_JOBS.split(',').each {
            def downstreamUrl = new URL("${env.JENKINS_URL}/job/$it/job/${env.BRANCH_NAME}")
            def boolean downstreamJobExists = sh(script: "curl -L -s -o /dev/null -I -w '%{http_code}' ${downstreamUrl}", returnStdout: true) == "200"
            if (downstreamJobExists) {
              build job: "$it/${env.BRANCH_NAME}", wait: false, parameters: [booleanParam(name: 'TRIGGER_DOWNSTREAM_BUILD', value: "${params.TRIGGER_DOWNSTREAM_BUILD}")]
            }
          }
        }
      }
    }
    failure {
      archiveArtifacts artifacts: '**/target/work/data/.metadata/.log, **/hs_err_pid*.log'
    }
    cleanup {
      script {
        def envName = ''
        if (env.JENKINS_URL.contains('ci.eclipse.org/xtext')) {
          envName = ' (JIRO)'
        } else if (env.JENKINS_URL.contains('ci-staging.eclipse.org/xtext')) {
          envName = ' (JIRO)'
        }
        
        def sendNotification = true
        def color = '#00FF00'
        def curResult = currentBuild.currentResult
        def lastResult = 'NONE'
        if (currentBuild.previousBuild != null) {
          lastResult = currentBuild.previousBuild.result
        }
        if (lastResult == 'NONE') {
          curResult = "NEW: ${curResult}"
        } else if (curResult == 'SUCCESS' && lastResult == 'SUCCESS') {
          sendNotification = false
        } else if (curResult == 'SUCCESS' && lastResult != 'SUCCESS') {
          curResult = 'FIXED'
        } else if (curResult == 'UNSTABLE') {
          curResult = 'STILL FAILING (UNSTABLE)'
          color = '#FFFF00'
        } else { // FAILURE, ABORTED, NOT_BUILD
          curResult = 'STILL FAILING'
          color = '#FF0000'
        }
        if (sendNotification) {
          slackSend message: "${curResult}: <${env.BUILD_URL}|${env.JOB_NAME}#${env.BUILD_NUMBER}${envName}>", botUser: true, channel: 'xtext-builds', color: "${color}"
        }
      }
    }
  }
}
