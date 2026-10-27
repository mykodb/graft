plugins {
    `java-library`
    `maven-publish`
    idea
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("net.neoforged.moddev") version "2.0.148"
}

// 从 gradle.properties 读取配置
val modVersion: String = property("modVersion") as String
val modGroupId: String = property("modGroupId") as String
val neoforgeVersion: String = property("neoforgeVersion") as String
val neoforgeVersionRange: String = property("neoforgeVersionRange") as String
val minecraftVersion: String = property("minecraftVersion") as String
val minecraftVersionRange: String = property("minecraftVersionRange") as String
val kffVersion: String = property("kffVersion") as String
val kffVersionRange: String = property("kffVersionRange") as String
val jvmVersion: String = property("jvmVersion") as String
val modId: String = property("modId") as String
val modName: String = property("modName") as String
val modLicense: String = property("modLicense") as String
val modDescription: String = property("modDescription") as String
val modAuthors: String = property("modAuthors") as String


version = modVersion
group = modGroupId

base {
    archivesName = modId
}

tasks.named<Wrapper>("wrapper") {
    // 在此定义 wrapper 属性，无需每次更新 gradlew.properties。
    // 切换为 Wrapper.DistributionType.ALL 会下载完整 Gradle 源码，
    // 鼠标悬停时会显示文档，但体积更大。切换后需运行两次 wrapper 任务。
    distributionType = Wrapper.DistributionType.BIN
}

sourceSets.main {
    resources {
        // 包含数据生成器生成的资源
        srcDir("src/generated/resources")

        // 排除开发专用资源
        exclude("**/*.bbmodel")
        exclude("src/generated/**/.cache")
    }
}

repositories {
    mavenLocal()
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

java.toolchain.languageVersion = JavaLanguageVersion.of(jvmVersion.toInt())

tasks.withType<JavaExec>().configureEach {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(jvmVersion.toInt())
    }
}

neoForge {

    version = neoforgeVersion

    runs {
        create("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        create("Data") {
            clientData()
            // 指定数据生成参数：模组 ID、输出目录、现有资源目录
            programArguments.addAll(
                "--mod", modId,
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
            )
        }

        configureEach {
            // 推荐日志标记：SCAN(模组扫描) REGISTRIES(注册事件) REGISTRYDUMP(注册表转储)
            systemProperty("forge.logging.markers", "REGISTRIES")

            // 控制台日志级别
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        // 定义模组与源代码的绑定，多模组项目每个模组定义一个
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}


dependencies {
    implementation("thedarkcolour:kotlinforforge-neoforge:$kffVersion")
    // JEI 可选依赖示例
    // compileOnly("mezz.jei:jei-${mc_version}-common-api:${jei_version}")
    // compileOnly("mezz.jei:jei-${mc_version}-neoforge-api:${jei_version}")
    // localRuntime("mezz.jei:jei-${mc_version}-neoforge:${jei_version}")

    // 本地 jar 依赖示例（放 ./libs 目录）
    // implementation("blank:coolmod-${mc_version}:${coolmod_version}")

    // 文件依赖示例
    // implementation(files("libs/coolmod-${mc_version}-${coolmod_version}.jar"))

    // 子项目依赖示例
    // implementation(project(":myproject"))
}

// 展开模板中的占位属性到生成的资源文件
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    group = "build"
    description = "展开 neoforge.mods.toml 模板中的占位符到构建资源目录"
    val replaceProperties = mapOf(
        "minecraftVersion" to minecraftVersion,
        "minecraftVersionRange" to minecraftVersionRange,
        "neoforgeVersion" to neoforgeVersion,
        "neoforgeVersionRange" to neoforgeVersionRange,
        "kffVersionRange" to kffVersionRange,
        "modId" to modId,
        "modName" to modName,
        "modLicense" to modLicense,
        "modDescription" to modDescription,
        "modAuthors" to modAuthors,
        "modVersion" to modVersion,
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}
// 将生成的元数据加入构建资源目录
sourceSets.main {
    resources.srcDir(generateModMetadata)
}
// 每次 IDE 同步时自动运行
neoForge.ideSyncTask(generateModMetadata)

// Maven 发布配置
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("file://$projectDir/repo")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// IDEA 不再自动下载源码和 javadoc，需显式开启
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
