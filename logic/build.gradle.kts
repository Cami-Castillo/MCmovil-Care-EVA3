plugins { kotlin("jvm"); jacoco }
kotlin { jvmToolchain(21);compilerOptions {jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)} }
java {sourceCompatibility=JavaVersion.VERSION_17;targetCompatibility=JavaVersion.VERSION_17}
val mockAgent by configurations.creating
dependencies {
 mockAgent("net.bytebuddy:byte-buddy-agent:1.17.8")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
 implementation("com.squareup.retrofit2:retrofit:3.0.0")
 implementation("com.squareup.retrofit2:converter-gson:3.0.0")
 testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
 testImplementation("io.kotest:kotest-assertions-core:5.9.1")
 testImplementation("io.mockk:mockk:1.14.7")
 testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
tasks.test { useJUnitPlatform();doFirst {jvmArgs("-javaagent:${mockAgent.singleFile}")};systemProperty("kotest.framework.classpath.scanning.autoscan.disable","true");finalizedBy(tasks.jacocoTestReport) }
jacoco { toolVersion="0.8.13" }
tasks.jacocoTestReport {
 dependsOn(tasks.test)
 reports { xml.required.set(true);html.required.set(true) }
 classDirectories.setFrom(files(classDirectories.files.map { fileTree(it) { exclude("**/Equipo*","**/Atencion*","**/Perfil*","**/Feriado*","**/EstadoAutorizacion*","**/ArchivoSubido*","**/Datos*","**/CareState*") } }))
}
tasks.jacocoTestCoverageVerification {
 dependsOn(tasks.jacocoTestReport)
 classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
 violationRules { rule { limit { counter="LINE";value="COVEREDRATIO";minimum="0.80".toBigDecimal() } } }
}
