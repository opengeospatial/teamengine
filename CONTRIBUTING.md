# Contributing

Rules for submitting a pull request:

* **Small Contribution / Single Source Code File:** A committer of TEAM Engine can review and apply the change on your behalf if it affects a single source code file. This allows us to adopt a small fix, correct spelling mistakes or clarify a javadoc.

* **Large Contribution / Multiple Files / New Files:** To contribute new files or change several existing files, you have to be in a business relationship with OGC, part of an OGC Testbed or OGC member (see https://www.ogc.org/about-ogc/ogc-member-list/).

## How to contribute

- Have a look at the Issues board and identify an issue you would like to address.
- Fork the team engine repository, so that you have a copy of the repository under your GitHub handle `your-github-handle/teamengine`.
- Now clone the repository to your local machine using the following command from the terminal:

`git clone https://github.com/your-github-handle/teamengine.git`.

- Create a new branch for your change using the following command from the terminal:

`git checkout -b your-new-branch`.

If the branch is intended to fix an issue then ideally mention the issue number in the name of the branch e.g. `fix-for-issue-1234`

- Make the appropriate changes for the issue you are trying to address or the feature that you want to add.
- Add the files you wish to commit to the index that holds a snapshot of the state of the project by using `git add` (e.g. to add all files in the folder you can use `git add -A`). 
- Commit the changes and attach a message to commit by using the command `git commit -m "This is my short message"`.
- Push the changes to the remote repository using `git push origin your-new-branch`.
- Use GitHub to create a Pull Request (PR), ensuring that the issue addressed by the fix is mentioned in the title of the PR and linked to in the PR's description. Describe the changes made in the Pull Request's description.
- Submit the Pull Request.
- Paitently wait for a response from the team of maintainers. 
- If changes are requested by the maintainers, make them as soon as possible.
- After your pull request is merged, let the world know on social media!
- Invite other developers to contribute.

## Commit Guidelines



**Copyright headers**
   * If you are modifying an existing file that does not have a copyright header, then add a copyright header indicating the year the file was created and who owns the copyright. An example is below:

   ```
   /* (c) 2020 Open Geospatial Consortium - All rights reserved
    * This code is licensed under the Apache License, Version 2.0 license, available at https://github.com/opengeospatial/teamengine/blob/master/LICENSE.txt
    * 
    */
   ```

   * There is no need to update the copyright year when updating an existing file.
   * If there is no copyright header, create one.
