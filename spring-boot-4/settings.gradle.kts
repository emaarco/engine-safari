rootProject.name = "engine-safari-spring-boot-4"

includeBuild("../common")

include("operaton")
project(":operaton").projectDir = file("../service/operaton")

include("cib-seven")
project(":cib-seven").projectDir = file("../service/cib-seven")

include("cib-seven-with-process-engine-api")
project(":cib-seven-with-process-engine-api").projectDir = file("../service/cib-seven-with-process-engine-api")
