// Windows Jenkins agent: runs Maven directly (no Docker needed).
// The Docker/Kubernetes pipeline needs a Linux agent; keep it as Jenkinsfile.docker-k8s for later.
pipeline {
  agent any

  options {
    timeout(time: 60, unit: 'MINUTES')
    timestamps()
    disableConcurrentBuilds()
  }

  parameters {
    choice(name: 'TEST_ENV', choices: ['demo', 'qa'], description: 'Config file: src/test/resources/config/<env>.properties')
    // Named *_OVERRIDE on purpose: Jenkins exposes parameters as environment variables, and an empty
    // BASE_URL variable would override base.url in ConfigReader.
    string(name: 'BASE_URL_OVERRIDE', defaultValue: '', description: 'Optional. Leave empty to use the URL from the env file')
    choice(name: 'BROWSER', choices: ['chromium', 'firefox', 'webkit'])
    booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Must stay true on a Jenkins service (no desktop)')
    choice(name: 'TEST_GROUPS', choices: ['smoke','regression'], description: 'smoke | regression | empty for all')
  }

  stages {
    // "Declarative: Checkout SCM" already cloned the repo, so no extra checkout stage is needed.

    stage('Test') {
      steps {
        // Create in Jenkins: Manage Jenkins > Credentials > Username with password, ID = opencart-test-user
        withCredentials([usernamePassword(credentialsId: 'opencart-test-user',
                                          usernameVariable: 'USER_EMAIL',
                                          passwordVariable: 'USER_PASSWORD')]) {
          script {
            def args = "-Denv=${params.TEST_ENV} -Dbrowser=${params.BROWSER} -Dheadless=${params.HEADLESS}"
            if (params.TEST_GROUPS?.trim())      { args += " -Dgroups=${params.TEST_GROUPS.trim()}" }
            if (params.BASE_URL_OVERRIDE?.trim()) { args += " -Dbase.url=${params.BASE_URL_OVERRIDE.trim()}" }
            bat "mvn -B clean test ${args}"
          }
        }
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
      archiveArtifacts artifacts: 'target/traces/**, target/screenshots/**', allowEmptyArchive: true
      allure includeProperties: false, results: [[path: 'target/allure-results']]
    }
  }
}