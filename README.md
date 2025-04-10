# Projecto de avaliação individual adc

## 


# ADC-App-Engine
 - Cloud app id:
&nbsp;
# COMMANDS (RUNNING AND DEPLOYMENT)
## GCLOUD
### Check gcloud version:
    gcloud -v
### Login to gcloud:
    gcloud auth login
### Initialize gcloud:
    gcloud init
&nbsp;
&nbsp;
## GIT
### Check Git version:
    git -v 
### Git help:
    git -h 
### Clone a repository:
    git clone [link] 
### Check repository status:
    git status 
### Add all files to the repository:
    git add . 
### Add a specific file to the repository:
    git add [file]
### Remove a file from the repository:
    git rm [file]
### Remove a file from the repository and the local directory:
    git rm -f [file]
### Commit the files to the repository:
    git commit -m "mensagem" 
### Push the files to the repository (local to remote):
    git push 
### Pull the files from the repository (remote to local):
    git pull 
&nbsp;
&nbsp;
## MAVEN
### Check Maven version:
    mvn -version 
### Clean the project:
    mvn clean 
### Run project tests (unit tests):
    mvn test 
### Verifies the project:
    mvn verify 
### Generate the project site:
    mvn site 
### Compile the project:
    mvn compile
### Package the project:
    mvn package 
### Install the project:
    mvn install 
### Command used to run the project locally (Package the project and runs it locally):
    mvn package appengine:run 
### Run the project locally with bind to listen to all addresses (ports must be open)
    mvn spring-boot:run -Dspring-boot.run.arguments=--server.address=0.0.0.0
or with app engine
    
    mvn package appengine:run -Dappengine.run.host=0.0.0.0

### Deploy the project (with the App Engine plugin):
    mvn appengine:deploy 
or

    mvn package appengine:deploy -Dapp.deploy.projectId=<id-da-aplicação> -Dapp.deploy.version=<version number>
example:

    mvn package appengine:deploy -Dapp.deploy.projectId=shining-expanse-453014-c4 -Dapp.deploy.version=1
    
- <id-da-aplicação> é o id único da vossa aplicação extraído da consola do Google Cloud (vimos no inicio)
- <version number> é um numero que identifica a versão da aplicação (costuma ser crescente e começa em 1)

### Run the project (with the App Engine plugin):
    mvn appengine:run
