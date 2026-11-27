import java.util.Properties
plugins { id("com.android.application");kotlin("android");id("org.jetbrains.kotlin.plugin.compose");id("com.google.devtools.ksp") }
val firma=Properties().apply { val f=rootProject.file("keystore.properties");if(f.exists())f.inputStream().use{load(it)} }
android {
 namespace="com.example.appmcmovilcare_castillo_farias"
 compileSdk=35
 defaultConfig { applicationId="com.example.appmcmovilcare_castillo_farias.eva4";minSdk=26;targetSdk=35;versionCode=5;versionName="4.1";testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"
  buildConfigField("String","CATALOGO_URL","\"${providers.gradleProperty("CATALOGO_URL").getOrElse("http://127.0.0.1:8081/")}\"")
  buildConfigField("String","ATENCIONES_URL","\"${providers.gradleProperty("ATENCIONES_URL").getOrElse("http://127.0.0.1:8082/")}\"")
 }
 signingConfigs {create("entrega"){if(firma.isNotEmpty()){storeFile=rootProject.file(firma.getProperty("storeFile"));storePassword=firma.getProperty("storePassword");keyAlias=firma.getProperty("keyAlias");keyPassword=firma.getProperty("keyPassword");storeType="JKS"}}}
 buildTypes {debug {applicationIdSuffix=".debug"};release {isMinifyEnabled=false;if(firma.isNotEmpty())signingConfig=signingConfigs.getByName("entrega")}}
 compileOptions {sourceCompatibility=JavaVersion.VERSION_17;targetCompatibility=JavaVersion.VERSION_17}
 kotlinOptions {jvmTarget="17"}
 buildFeatures {compose=true;buildConfig=true}
 packaging {resources.excludes+=setOf("META-INF/AL2.0","META-INF/LGPL2.1","META-INF/LICENSE*","META-INF/NOTICE*")}
}
dependencies {
 implementation(project(":logic"))
 implementation(platform("androidx.compose:compose-bom:2025.05.01"))
 implementation("androidx.activity:activity-compose:1.10.1")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.compose.material:material-icons-extended")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
 implementation("androidx.navigation:navigation-compose:2.9.0")
 implementation("androidx.room:room-runtime:2.7.2")
 implementation("androidx.room:room-ktx:2.7.2")
 ksp("androidx.room:room-compiler:2.7.2")
 implementation("com.squareup.retrofit2:retrofit:3.0.0")
 implementation("com.squareup.retrofit2:converter-gson:3.0.0")
 implementation("io.coil-kt:coil-compose:2.7.0")
 androidTestImplementation(platform("androidx.compose:compose-bom:2025.05.01"))
 androidTestImplementation("androidx.compose.ui:ui-test-junit4")
 androidTestImplementation("androidx.test:runner:1.6.2")
 androidTestImplementation("junit:junit:4.13.2")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
 debugImplementation("androidx.compose.ui:ui-tooling")
}
ksp {arg("room.schemaLocation","$projectDir/schemas")}
tasks.register("verificarFirma"){doLast{check(firma.isNotEmpty()){ "Configura keystore.properties antes de release" }}}
tasks.configureEach {if(name=="preReleaseBuild")dependsOn("verificarFirma",":logic:jacocoTestCoverageVerification")}
