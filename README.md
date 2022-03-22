# P2P Git Protocol

## Introduction

This software simulates the Git protocol on a P2P network using **distributed hash tables** (*DHT*).

Each **peer** has one or more local *repositories* where it can store files dedicated to one or more projects.

Each **peer** can *push* its work remotely and store a **local repository** remotely, more precisely store the repository in a **DHT**.

Each **peer** has a **DHT** where it can store its work. A DHT is composed of *pairs*, each pair is composed by a *key*, in this case the name of the repository, which refers to a *value*, in this case the repository object shared by the peer. A peer in the network can download a repository of another peer's DHT by simply *pulling* its work by typing the repository name.

<br>

## Implementation

The P2P Git protocol developed is composed, mainly, of six operations plus one of support:

1. ```createRepository```  
&emsp;Creates a new local repository, with a name,  in a directory pointed by the user.

2. ```addFilesToRepository```  
&emsp;Adds one or more files in the local repository, the files are **NOT CREATED** but are tracked by Git, so it's up to the user **physically** create the files in the local repository.

3. ```removeFilesToRepository```  
&emsp;Stops tracking one or more files in the local repository and removes them from the file system.

4. ```commit```  
&emsp;Apply the changes to the files in the local repository.  

5. ```push```  
&emsp;Pushes files over the network. If the state of the remote repository is different (I.E it has commits that the pushed repository does not) than the push fails, asking for a pull.

6. ```pull```  
&emsp;Pulls files from the network. If there are conflicts, the system keep both files and the user should fix the conflicts manually.

7. ```status```  
&emsp;Shows the state of the local repository and the staging area. It shows which changes have been staged, which haven't, and which files are tracked and aren't tracked by Git.

---
### Repository  

The repository is represented by the ```Repository``` class, is allows the user to create, manage and control the state of the repositories. It's composed by the following fields:  

- ```name```  
&emsp;The name of the repository  

- ```path```  
&emsp;The path of the repository in the file system.  

- ```tracked files```  
&emsp;An **hash map** that keeps track of the files that are currently tracked by Git, it's composed by pairs of **<key, value>** where the *key* is the name of the file, and the *value* is a ```crate``` object.

- ```staged files```  
&emsp;An **hash map** that keeps files that are **staged**, namely files that are modified by the user with *add* or *remove* commands. It's composed by pairs of **<key, value>** where the *key* is the name of the file, and the *value* is a ```crate``` object. When a commit is made the staged files replace the tracked ones.

- ```commits```  
&emsp;A list of ```commit``` made by the users.  

- ```id```  
&emsp;Unique id

A repository uses other specific classes to operate, **commit** and **crate**.  

#### Commit  

A ```commit``` is an object that keeps some informations of a commit made by an user:  

- ```repository name```  
&emsp;The name of the repository  

- ```message```  
&emsp;The message written by the user  

- ```date```  
&emsp;The date when the commit was made  

- ```id```  
&emsp; Unique id  

- ```pushed```  
&emsp;Tells if the commits has been pushed or not.  

- ```file names```  
&emsp;List of filenames that where modified by this commit.  

#### Crate  

A ```crate``` is an object that wraps a file, keeps track of the state of a file that makes the repository, it contains:

- ```name```  
&emsp;The name of the file.  

- ```file```  
&emsp;An abstract representation of the file

- ```content```  
&emsp;The content of the file represented as an array of bytes.  

- ```id```  
&emsp;Unique id used to compare **crate** objects.

---

### Storage  

Storage is an ```interface``` used as the bases to developing the **local storage** class and the **remote storage** class where repositories are stored locally and remotely, respectively. Repositories are stored in *<key, value>* pairs where the key is an identifier and value is the repository itself. 

#### Local Storage

The local storage class is called ```GitStorage```, in this object are stored all the user's local repositories, the user can modify the repository he wants just by adding the repository name when he runs a Git command with the terminal.  

#### Remote Storage

The remote storage class is called ```DHTStorage```, with this class we simulate the P2P Network using a DHT for each user thanks to TomP2P API. The user can interact with the Network using the commands **pull**, to download a repository from a DHT, and **push**, to upload the repository's state into a DHT.  

<br>

## Interactions  

The user can interact with Git using these **keywords**, this section will also explain how to write a proper command, every command execute one method of the ```GitProtocol```:
 
> ```create```  
&emsp;Refers to ```createRepository```, allows the creation of a new local repository.  
**Syntax:** ```git create {0} {1}```  
**Args:** ```{0} repository name, {1} directory name.```  

> ```add```  
&emsp;Refers to ```addFilesToRepository```, allows the addition of one or multiple files to the local repository.  
**Syntax:** ```git add {0} {1} || [list]```  
**Args:** ```{0} repository name, {1} file name, [list] file names separeted by a space```  

> ```remove```  
&emsp;Refers to ```removeFilesFromRepository```, allows the removal of one or multiple files from the local repository.  
**Syntax:** ```git remove {0} {1} || [list]```  
**Args:** ```{0} repository name, {1} file name, [list] file names seprated by a space```  

> ```commit```  
&emsp;Refers to ```commit```, allows you the creation of commit in the local repository.  
**Syntax:** ```git commit {0}, {1}```  
**Args:** ```args: {0} repository name, {1} message```  

> ```push```  
&emsp;Refers to ```push```, allows pushing of files to the remote repository.  
**Syntax:** ```git push {0}```  
**Args:** ```args: {0} repository name```  

> ```pull```  
&emsp;Refers to ```pull```, allows to get files from the remote repository and store them in the local repository.  
**Syntax:** ```git pull {0}```  
**Args:** ```args: {0} repository name```  

> ```status```
&emsp;Refers to ```status```, allows to check the status of the local repository, it shows the name of files that are staged, unstaged, tracked and untracked.  
**Syntax:** ```git status {0}```  
**Args:** ```args: {0} repository name```  

<br>

## Test cases  

Test cases are written using **JUnit 5**, each test case creates and delete directories to simulate the local repositories of each user in a Network.  
The test class ```GitProtocolImplMethodsTest``` have one test method for each method in ```GitProtocol```, every method is tested separately.  
The test class ```GitProtocolImplUseCaseTest``` simulate a use case of the ```GitProtocol``` with 4 peers with multiple repositories, all operations are tested together in a single test method.

<br>

## Apache Maven  

Apache Maven is used as project management tool, using a file named **pom.xml** we add plugins to help builds and, with the *Maven Central Repository*, extra dependencies.  
Dependencies used are the following:  

- ```net.tomp2p```  
&emsp;DHT features needed to simulate a P2P network.  

- ```org.juint.jupiter```  
&emsp;Used for testing.

- ```org.beryx```  
&emsp;For creating Java console applications.

- ```args4j```  
&emsp;Utility API that makes command line parsing very easy by using annotations.  

Plugins used and configurations made are listed below (standard plugins are not listed):  

- ```maven-surefire-plugin```  
&emsp;Used to run tests cases before build. Tests are ignored because when running ```mvn package``` the peer's directories, used in the test cases, are not deleted. Running tests without Maven shouldn't give any problems.  

- ```maven-jar-plugin```  
&emsp;Used to build a jar file of the project.

- ```maven-assembly-plugin```  
&emsp;Used to merge all dependecies into a single ```.jar``` file, without this plugin the dependecies would be missing. ```<descriptorRef>``` is used to perform a custom assembly of the ```.jar``` file. The ```appendAssemblyId``` tag is set to false to rename the ```.jar``` file built with this plugin, otherwise the name would be ```<finalName>-jar-with-dependencies```, setting false ```-jar-with-dependencies``` will be omitted.  
The built ```.jar``` will also be the *artifact* of the project. Maven will give a warning when run ```mvn package``` because two ```.jar``` files have the same artifact id, but it's not a problem because the last ```.jar``` created, in this case the ```.jar``` with all the dependencies needed, will survive as the main artifact because it was built last.

<br>

## Dockerization  

There is also a ```Dockerfile``` in the project folder to convert the application solution to run within a Docker container. The container will be based on the ```openjdk:11``` image to have a ready-to-use Java environment, then a working dir called ```app``` is created where the ```.jar``` and the local repositories created at run-time are stored. After the initial setup of the container, two commands are executed:  

- ```apt-get update```  
&emsp;to have the container up-to-date  

- ```apt-get install nano```  
&emsp;to have a simple text editor to edit the files, otherwise we wouldn't be able to modify the content of files in the repositories inside the container.

After that, two ```arg``` are set, ```ip``` to set the ip address of the peer and an ```id``` to identify the peer in the network.  
When the container starts, ```java -jar p2p-git-protocol.jar``` with `-m` for the ip address and `-id` for the unique id of the peer is run  

## Build the app in a Docker container  

Build a docker container:  
```docker build --no-cache -t p2p-git-protocol .```

### Start the master peer  

```docker run -i --name MASTER-PEER -e ip="172.17.0.1" -e id=0 p2p-pp-client```

### Start a peer  

```docker run -i --name PEER_1 -e ip="172.17.0.2" -e id=1 p2p-pp-client```
