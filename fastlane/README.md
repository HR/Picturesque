fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android lol
```
fastlane android lol
```

### android test
```
fastlane android test
```
Runs all the tests
### android bump_code
```
fastlane android bump_code
```
Bump the release version code
### android bump_name
```
fastlane android bump_name
```
Bump the release version name
### android bump_tag
```
fastlane android bump_tag
```
Git tag the release after bumping version (name & code)
### android alpha
```
fastlane android alpha
```
Deploy a new version to the Google Play - Alpha channel
### android beta
```
fastlane android beta
```
Deploy a new version to the Google Play - Beta channel
### android release
```
fastlane android release
```
Deploy a new version to the Google Play - Release channel

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
