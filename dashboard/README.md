# Dashboard

Kuona's UI Dashboard project. The dashboard relies on the kuona-api for data.

## Environment setup

If you run the 

```
dev-setup
```
script from the root of the project you're all set.

The dev-setup script in the root directory sets everything up but if you prefer manual configuration:

```bash
brew install node
npm install -g grunt-cli
```
For non OS X users, use the appropriate Node JS installer for your platform from the [official node website](https://nodejs.org/en/download/).

## Compiling for development
```bash
npm install
grunt dev
```

If your browser popped open with a page, the command works. As long as the `grunt dev` command is running, it will watch for changes in your code, compile, builds them and refresh the browser.

## Deployment build
```bash
npm install
grunt build
```
