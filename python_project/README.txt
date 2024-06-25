Jenkins - Python Project Pipeline

Stage 1: Git Clone
Stage 2: Building & compiling the code
Stage 3: Code Coverage
  --> In python, we have coverage library that will be used to run coverage
  --> Test cases are defined for the code  
  --> coverage command will be ran on the node and it will generate coverage.ml file that can be loaded into SonarQube project

Stage 4: SonarQube Analysis
  --> In this Jenkins stage, we will provide SonarQube project, creds and coverage.xml file that should be loaded into SonarQube project.

Stage 5: Quality Gate
  --> Check the status of Quality Gate to decide the pipeline status.
  --> If Quality status is failed, make the pipeline fail

Stage 6: Upload the build into Artifactory

