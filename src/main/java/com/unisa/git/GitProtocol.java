package com.unisa.git;

import java.io.File;
import java.util.List;

public interface GitProtocol {

	/**
	 * Creates new repository in a directory
	 * @param _repo_name a String, the name of the repository.
	 * @param _directory a File, the directory where create the repository.
	 * @return true if it is correctly created, false otherwise.
	 */
	public boolean createRepository(String _repo_name, File _directory);
	/**
	 * Adds a list of File to the given local repository. 
	 * @param _repo_name a String, the name of the repository.
	 * @param files a list of Files to be added to the repository.
	 * @return true if it is correctly added, false otherwise.
	 */
	public boolean addFilesToRepository(String _repo_name, List<File> files);

	/**
	 * Removes a list of File to the given local repository
	 * @param _repo_name a String, the name of the repository.
	 * @param files a list of Files to be removed to the repository.
	 * @return true if it is correctly removed, false otherwise.
	 */
	public boolean removeFilesFromRepository(String _repo_name, List<File> files);

	/**
	 * Apply the changing to the files in  the local repository.
	 * @param _repo_name a String, the name of the repository.
	 * @param _message a String, the message for this commit.
	 * @return true if it is correctly committed, false otherwise.
	 */
	public boolean commit(String _repo_name, String _message);
	/**
	 * Push all commits on the Network. If the status of the remote repository is changed, 
	 * the push fails, asking for a pull.
	 * @param _repo_name _repo_name a String, the name of the repository.
	 * @return a String, operation message.
	 */
	public String push(String _repo_name);
	/**
	 * Pull the files from the Network. If there is a conflict, the system duplicates 
	 * the files and the user should manually fix the conflict.
	 * @param _repo_name _repo_name a String, the name of the repository.
	 * @return a String, operation message.
	 */
	public String pull(String _repo_name);

	/**
	 * Shows the state of the local repository and the staging area. It shows
	 * which changes have been staged, which haven't, and which files are tracked 
	 * and aren't tracked by Git.
	 * @param _repo_name a String, the name of the repository
	 * @return a String, operation message.
	 */
	public String status(String _repo_name);
}