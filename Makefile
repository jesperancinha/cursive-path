# Makefile for the frontend

.PHONY: run build clean

# Start the frontend development server
run:
	./gradlew :frontend:jsBrowserDevelopmentRun

# Build the frontend production bundle
build:
	./gradlew :frontend:jsBrowserProductionWebpack

# Clean the frontend build artifacts
clean:
	./gradlew :frontend:clean
