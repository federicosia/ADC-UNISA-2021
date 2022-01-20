# P2P Git Protocol

## Introduction

This software simulates the Git protocol on a P2P network using **distributed hash tables** (*DHT*).

Each **peer** has one or more local *repositories* where it can store files dedicated to one or more projects.

Each **peer** can *push* its work remotely and store a **local repository** remotely, more precisely store the repository in a **DHT**.

Each **peer** has a **DHT** where it can store its work. A DHT is composed of *pairs*, each pair is composed by a *key*, in this case the name of the repository, which refers to a *value*, in this case the repository object shared by the peer. A peer in the network can download a repository of another peer's DHT by simply *pulling* its work by typing the repository name.

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

---

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

---

A ```crate``` is an object that wraps a file, keeps track of the state of a file that makes the repository, it contains:

- ```name```  
&emsp;The name of the file.  

- ```file```  
&emsp;An abstract representation of the file

- ```content```  
&emsp;The content of the file represented as an array of bytes.  

- ```id```  
&emsp;Unique id used to compare **crate** objects.


## Interactions

The user can interact with Git using these **keywords**, the correct usage of the commands is also written in the ***syntax*** section for each available command:

>```create```  
&emsp;Refers to ```createRepository```, 