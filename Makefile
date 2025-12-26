.PHONY: help build test run clean docker-build docker-run docker-push k8s-deploy

# Variables
APP_NAME := fincalc-pro
VERSION := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null || echo "1.0.0")
DOCKER_REGISTRY ?= docker.io
IMAGE_TAG ?= $(VERSION)
K8S_NAMESPACE ?= fincalc

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ==================== Build ====================

build: ## Build the application
	./mvnw clean package -DskipTests

build-full: ## Build with tests
	./mvnw clean package

compile: ## Compile only
	./mvnw compile

# ==================== Test ====================

test: ## Run all tests
	./mvnw test

test-unit: ## Run unit tests only
	./mvnw test -Dtest="*Test"

test-integration: ## Run integration tests
	./mvnw test -Dtest="*IntegrationTest"

coverage: ## Generate test coverage report
	./mvnw jacoco:report
	@echo "Coverage report: target/site/jacoco/index.html"

# ==================== Run ====================

run: ## Run locally (default profile)
	./mvnw spring-boot:run

run-local: ## Run with local profile
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local

run-dev: ## Run with dev profile
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

run-prod: ## Run with prod profile
	./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# ==================== Quality ====================

lint: ## Check code style
	./mvnw checkstyle:check

security-check: ## Run OWASP dependency check
	./mvnw dependency-check:check

sonar: ## Run SonarQube analysis
	./mvnw sonar:sonar

# ==================== Clean ====================

clean: ## Clean build artifacts
	./mvnw clean

clean-all: clean ## Clean everything including Docker
	docker rmi $(DOCKER_REGISTRY)/$(APP_NAME):$(IMAGE_TAG) 2>/dev/null || true
	docker rmi $(DOCKER_REGISTRY)/$(APP_NAME):latest 2>/dev/null || true

# ==================== Docker ====================

docker-build: ## Build Docker image
	docker build -t $(DOCKER_REGISTRY)/$(APP_NAME):$(IMAGE_TAG) .
	docker tag $(DOCKER_REGISTRY)/$(APP_NAME):$(IMAGE_TAG) $(DOCKER_REGISTRY)/$(APP_NAME):latest

docker-run: ## Run Docker container
	docker run -d --name $(APP_NAME) -p 8002:8002 $(DOCKER_REGISTRY)/$(APP_NAME):$(IMAGE_TAG)

docker-stop: ## Stop Docker container
	docker stop $(APP_NAME) && docker rm $(APP_NAME)

docker-push: ## Push Docker image to registry
	docker push $(DOCKER_REGISTRY)/$(APP_NAME):$(IMAGE_TAG)
	docker push $(DOCKER_REGISTRY)/$(APP_NAME):latest

docker-compose-up: ## Start with docker-compose
	docker-compose up -d --build

docker-compose-down: ## Stop docker-compose
	docker-compose down

docker-compose-prod: ## Start production docker-compose
	docker-compose -f docker-compose.prod.yml up -d

docker-logs: ## View Docker logs
	docker logs -f $(APP_NAME)

# ==================== Kubernetes ====================

k8s-deploy: ## Deploy to Kubernetes
	kubectl apply -f k8s/namespace.yaml
	kubectl apply -f k8s/configmap.yaml
	kubectl apply -f k8s/service.yaml
	kubectl apply -f k8s/deployment.yaml
	kubectl apply -f k8s/hpa.yaml
	kubectl apply -f k8s/pdb.yaml

k8s-deploy-ingress: ## Deploy with ingress
	kubectl apply -f k8s/ingress.yaml

k8s-status: ## Check Kubernetes deployment status
	kubectl get all -n $(K8S_NAMESPACE)

k8s-logs: ## View Kubernetes logs
	kubectl logs -f -l app=$(APP_NAME) -n $(K8S_NAMESPACE)

k8s-delete: ## Delete Kubernetes deployment
	kubectl delete -f k8s/ --ignore-not-found

k8s-rollout: ## Rollout restart
	kubectl rollout restart deployment/$(APP_NAME) -n $(K8S_NAMESPACE)

k8s-rollback: ## Rollback to previous version
	kubectl rollout undo deployment/$(APP_NAME) -n $(K8S_NAMESPACE)

# ==================== Info ====================

info: ## Show project info
	@echo "Application: $(APP_NAME)"
	@echo "Version: $(VERSION)"
	@echo "Docker Registry: $(DOCKER_REGISTRY)"
	@echo "Image Tag: $(IMAGE_TAG)"

deps: ## Show dependency tree
	./mvnw dependency:tree

versions: ## Check for dependency updates
	./mvnw versions:display-dependency-updates
