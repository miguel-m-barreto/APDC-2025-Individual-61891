# Projecto de avaliação individual adc

## 

&nbsp;
&nbsp;

## ADC-App-Engine
 - Cloud app id: shining-expanse-453014-c4

&nbsp;
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

## GCLOUD DATASTORE
### Run emulator:
    gcloud beta emulators datastore start
### CONFIG ENVIRONMENT VARIABLES (unix):
    gcloud config set project <project-id>
    export DATASTORE_USE_PROJECT_ID_AS_APP_ID=true
Before the first deploy, run:

    gcloud beta emulators datastore env-init
Example:

    gcloud config set project shining-expanse-453014-c4-id
    export DATASTORE_USE_PROJECT_ID_AS_APP_ID=true
    gcloud beta emulators datastore env-init

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
or
    
    mvn clean package appengine:run
### Run the project locally with bind to listen to all addresses (ports must be open):  
    mvn package appengine:run -Dappengine.run.host=0.0.0.0
or
    
    mvn clean package appengine:run -Dappengine.run.host=0.0.0.0
### Deploy the project (with the App Engine plugin):
    mvn appengine:deploy 
or

    mvn package appengine:deploy -Dapp.deploy.projectId=<app-id> -Dapp.deploy.version=<version number>
I am using
    
    mvn package -e -X appengine:deploy -Dapp.deploy.projectId=<app-id> -Dapp.deploy.version=<version number>

-   -e (short for --errors) Shows full stack traces if there’s an error. Without -e, Maven only shows a summary

-   -X (short for --debug) Enables full debug logging, this includes:
    -   Every phase and goal being executed
    -   Every system property, plugin config, and path
    -   Details about classloading, dependency resolution, etc.
    -   Useful if something isn’t behaving right or hunting for a weird bug.

example:

    mvn package appengine:deploy -Dapp.deploy.projectId=shining-expanse-453014-c4 -Dapp.deploy.version=1
    
- <id-da-aplicação> é o id único da vossa aplicação extraído da consola do Google Cloud (vimos no inicio)
- <version number> é um numero que identifica a versão da aplicação (costuma ser crescente e começa em 1)
### Run the project (with the App Engine plugin):
    mvn appengine:run    

&nbsp;
&nbsp;

# Summary
## GCLOUD
### Use older version:
- Para usar versões anteriores à versão de deployed mais atual basta adicionar um x. a seguir ao https://
- x é o número da versão que queremos usar 
- No browser vai dar um alerta devido ao certificado https
- Exemplo: https://1.shining-expanse-453014-c4.oa.r.appspot.com
### Migrate traffic:
- Para escolher-mos qual a versão que deve ser utilizada:
    -   vamos a app engine -> versions
    -   selecionamos a versão que desejamos
    -   clica-mos em Migrate Traffic

&nbsp;
&nbsp;

## REST

### GET:
@GET: Só pode receber parâmetros pelo URL (@Path)
### POST:
@POST: Pode receber parâmetros pelo URL (@Path) e pelo body do pedido HTTP (usando a anotação @Consumes)
### PUT:
@PUT: 
Muito parecido com o POST (mas usado para manipular algo que já existe) 
### DELETE:
@DELETE: Muito parecido com o GET (mas usado para remover recursos)

## DATASTORE

### Níveis de isolamento e consistência:
-   No modo Datastore é utilizado isolamento serializável: dados lidos ou modificados por uma transação não podem ser modificados simultaneamente.
-   As consultas numa transação veem um snapshot consistente da base de dados contendo o efeito de todas as transações e escritas completadas antes do início das transações.
-   ATENÇÃO que as escritas e remoções realizadas dentro de uma transação não ficam visíveis dentro da transação!
-   Fora das transações as consultas e buscas também têm isolamento serializável
### Modos de concorrência:
-   PESSIMISTA:
    -   As transações podem ficar bloqueadas quando duas ou mais transações de leitura-escrita (read-write) concorrentes leem ou escrevem os mesmos dados. Transações somente leitura (read) não ficam bloqueadas. Este é o modo padrão.
-   OTIMISTA:
    -   Quando duas ou mais transações de leitura-escrita concorrentes leem ou escrevem os mesmos dados, apenas a primeira transação a confirmar suas mudanças tem sucesso. Outras transações que realizam escritas falham aquando da confirmação.
-   OTIMISTA EM GRUPOS DE ENTIDADES:
    -   modo antigo

### Utilizações de transações:
-   Atualizar/escrever uma nova propriedade de uma entidade dependente de valores de propriedades de outras entidades
-   Obter (get) uma entidade a partir de uma chave e criar uma nova se não existir 
-   As falhas nas transações de leitura-escrita podem ser resolvidas tentando novamente a transação (um número limitado de vezes) ou propagando o erro para o utilizador
-   As transações de leitura são utilizadas quando é necessário ter uma visão consistente dos dados que é construída por diversas consultas independentes

## CLOUD STORAGE
Armazenamento genérico de objectos persistentes