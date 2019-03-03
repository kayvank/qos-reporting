##
## make file
## environment variables are set at gocd-pipeline level
##
BUILD_VERSION:=$(shell awk '{gsub(/[\"\r]/, ""); print $$5}' ./version.sbt | sed "s/\"//g")-b$(GO_PIPELINE_COUNTER)

APP_NAME=qos-reporting
IMAGE_NAME=vevo/${APP_NAME}

DC=docker-compose
DC_D=$(DC) -f docker-compose.deploy.yml
SLACK_NOTIFY=$(DC) -f docker-compose.slack.yml run --rm

# AWS_CONFIG will generates the aws_config in a volume later used by stringer
AWS_CONFIG=$(DC) -f docker-compose.deploy.yml run --rm config
DEV_AWS_CONFIG=AWS_ACCESS_KEY_ID=$(DEV_AWS_ACCESS_KEY_ID) AWS_SECRET_ACCESS_KEY=$(DEV_AWS_SECRET_ACCESS_KEY) $(AWS_CONFIG)
STG_AWS_CONFIG=AWS_ACCESS_KEY_ID=$(STG_AWS_ACCESS_KEY_ID) AWS_SECRET_ACCESS_KEY=$(STG_AWS_SECRET_ACCESS_KEY) $(AWS_CONFIG)
PRD_AWS_CONFIG=AWS_ACCESS_KEY_ID=$(PRD_AWS_ACCESS_KEY_ID) AWS_SECRET_ACCESS_KEY=$(PRD_AWS_SECRET_ACCESS_KEY) $(AWS_CONFIG)

# stringer
STRINGER=$(DC_D) run --rm stringer
DEV_STRINGER=AWS_ACCOUNT=dev $(STRINGER)
STG_STRINGER=AWS_ACCOUNT=stg $(STRINGER)
PRD_STRINGER=AWS_ACCOUNT=prd $(STRINGER)

login:
	@docker login -u "$(DOCKER_USER)" -p "$(DOCKER_PASS)"

clean:
	sbt clean
	$(DC_D) down --rmi local --remove-orphans -v
	$(DC_D) rm -f -v

compile:
	sbt compile

test: compile
	sbt test

publish: login
	sbt -Dbuild_number=$(GO_PIPELINE_COUNTER) docker:stage docker:publish

publishLocal: login
	sbt -Dbuild_number=$(GO_PIPELINE_COUNTER) docker:stage docker:publishLocal

build: clean test publish

deploy_dev:
	$(DEV_AWS_CONFIG)
	$(DEV_STRINGER) build --tags=kubernetes --extra-vars="docker_image=${IMAGE_NAME}:${BUILD_VERSION}"
	$(DEV_STRINGER) kubectl apply -R -f /build/kubernetes
	$(DEV_STRINGER) kubectl rollout status deployment/${APP_NAME} --namespace data

deploy_stg:
	$(STG_AWS_CONFIG)
	$(STG_STRINGER) build --tags=kubernetes --extra-vars="docker_image=${IMAGE_NAME}:${BUILD_VERSION}"
	$(STG_STRINGER) kubectl apply -R -f /build/kubernetes
	$(STG_STRINGER) kubectl rollout status deployment/${APP_NAME} --namespace data

deploy_prd:
	$(PRD_AWS_CONFIG)
	$(PRD_STRINGER) build --tags=kubernetes --extra-vars="docker_image=${IMAGE_NAME}:${BUILD_VERSION}"
	$(PRD_STRINGER) kubectl apply -R -f /build/kubernetes
	$(PRD_STRINGER) kubectl rollout status deployment/${APP_NAME} --namespace data

slack_success:
	$(SLACK_NOTIFY) success

slack_failure:
	$(SLACK_NOTIFY) failure

##
## target the print individual variables. make print-<VARIBALE-NAME>.
## or simiply use make -n <target>
##

print-%  : ; @echo $* = $($*)
