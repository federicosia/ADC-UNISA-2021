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
**Args:** ```{0} repository name, {1} file name or directory, [list] file names separeted by a space```  

> ```remove```  
&emsp;Refers to ```removeFilesFromRepository```, allows the removal of one or multiple files from the local repository.  
**Syntax:** ```git remove {0} {1} || [list]```  
**Args:** ```{0} repository name, {1} file name or directory, [list] file names seprated by a space```  

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
The test class ```GitProtocolImplUseCaseTest``` simulate a use case of the ```GitProtocol``` with 4 peers with multiple repositories, in this test case all operations are tested together.