.PHONY: build test run clean docker-build docker-run docker-stop help

help:
	@echo "Usage: make <target>"
	@echo ""
	@echo "Targets:"
	@echo "  build       Build the project using Gradle"
	@echo "  test        Run all tests"
	@echo "  run         Run the service locally on port 8080"
	@echo "  clean       Clean build artifacts"
	@echo "  docker-build  Build Docker image"
	@echo "  docker-run    Run service in Docker container"
	@echo "  docker-stop   Stop and remove Docker container"

build:
	./gradlew build

test:
	./gradlew test

run:
	./gradlew bootRun

clean:
	./gradlew clean
	rm -rf data/

docker-build:
	docker compose build

docker-run:
	docker compose up -d

docker-stop:
	docker compose down
