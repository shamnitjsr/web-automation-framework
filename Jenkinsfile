pipeline {
  agent any
  options { timeout(time: 90, unit: 'MINUTES'); timestamps(); disableConcurrentBuilds() }

  parameters {
    choice(name: 'RUN_MODE', choices: ['DOCKER', 'KUBERNETES'], description: 'DOCKER = one container on this agent, KUBERNETES = sharded Indexed Job')
    choice(name: 'TEST_ENV', choices: ['qa', 'demo'])
    choice(name: 'BROWSER', choices: ['chromium', 'firefox', 'webkit'])
    string(name: 'TEST_GROUPS', defaultValue: 'smoke', description: 'smoke | regression | empty for all')
    string(name: 'SHARDS', defaultValue: '4', description: 'Number of parallel pods (KUBERNETES mode)')
  }

  environment {
    REGISTRY = 'registry.example.com/qa'                      // TODO: your registry
    IMAGE    = "${REGISTRY}/web-automation:${BUILD_NUMBER}"
    RUN_ID   = "jenkins-${BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') { steps { checkout scm } }

    stage('Build image') {
      steps { sh 'docker build -t "$IMAGE" .' }
    }

    stage('Run in Docker') {
      when { expression { params.RUN_MODE == 'DOCKER' } }
      steps {
        withCredentials([usernamePassword(credentialsId: 'opencart-test-user', usernameVariable: 'USER_EMAIL', passwordVariable: 'USER_PASSWORD'),
                         usernamePassword(credentialsId: 'opencart-negative-user', usernameVariable: 'USER_NEGATIVE_EMAIL', passwordVariable: 'USER_NEGATIVE_PASSWORD')]) {
          sh '''
            mkdir -p results
            docker run --rm --ipc=host \
              -e TEST_ENV="$TEST_ENV" -e BROWSER="$BROWSER" -e TEST_GROUPS="$TEST_GROUPS" \
              -e USER_EMAIL -e USER_PASSWORD -e USER_NEGATIVE_EMAIL -e USER_NEGATIVE_PASSWORD \
              -e RESULTS_DIR=/results -e RUN_ID="$RUN_ID" \
              -v "$PWD/results:/results" "$IMAGE"
          '''
        }
      }
    }

    stage('Run on Kubernetes') {
      when { expression { params.RUN_MODE == 'KUBERNETES' } }
      steps {
        sh 'docker push "$IMAGE"'
        withCredentials([file(credentialsId: 'k8s-kubeconfig', variable: 'KUBECONFIG')]) {
          sh 'SHARDS="$SHARDS" TEST_GROUPS="$TEST_GROUPS" ./scripts/k8s-run.sh'
        }
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'results/**, shard-results.txt', allowEmptyArchive: true
      allure includeProperties: false, results: [[path: 'results/' + env.RUN_ID + '/shard-0/allure-results']]  // merge all shards in a later step
    }
  }
}
