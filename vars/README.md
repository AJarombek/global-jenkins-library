### Overview

Files exposed as variables in Jenkins Groovy pipelines.  To have access to these files/functions, the following line 
must exist at the top of the pipeline code.

```groovy
@Library(['global-jenkins-library@master']) _
```

### Files

| Filename                | Description                                                                                  |
|-------------------------|----------------------------------------------------------------------------------------------|
| `cloudformation.groovy` | Functions containing reusable CloudFormation Stack pipeline steps.                           |
| `packer.groovy`         | Functions containing reusable Packer pipeline steps.                                         |
| `terraform.groovy`      | Functions containing reusable Terraform pipeline steps.                                      |