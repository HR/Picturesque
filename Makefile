# Makefile for Picturesque Android App

# Use bash for all exec
SHELL := /bin/bash

# None of these targets create files, so run them everytime ragardless of whether files with their names exist
.PHONY: alpha shell simulate_android_kill

# Build alpha and release it
alpha:
	bundle exec fastlane alpha

# Build beta and release it
beta:
	bundle exec fastlane beta

# Build release and ship it
release:
	bundle exec fastlane release