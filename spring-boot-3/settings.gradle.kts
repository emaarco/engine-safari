rootProject.name = "engine-safari-spring-boot-3"

includeBuild("../common")

include("camunda-7")
project(":camunda-7").projectDir = file("../service/camunda-7")

include("camunda-7-with-process-engine-api")
project(":camunda-7-with-process-engine-api").projectDir = file("../service/camunda-7-with-process-engine-api")

include("cib-seven-with-process-engine-api")
project(":cib-seven-with-process-engine-api").projectDir = file("../service/cib-seven-with-process-engine-api")
