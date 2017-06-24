package de.gesellix.docker.compose

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import de.gesellix.docker.compose.types.Command
import de.gesellix.docker.compose.types.ComposeConfig
import de.gesellix.docker.compose.types.Config
import de.gesellix.docker.compose.types.Deploy
import de.gesellix.docker.compose.types.DriverOpts
import de.gesellix.docker.compose.types.Environment
import de.gesellix.docker.compose.types.Exposes
import de.gesellix.docker.compose.types.External
import de.gesellix.docker.compose.types.ExtraHosts
import de.gesellix.docker.compose.types.Healthcheck
import de.gesellix.docker.compose.types.Ipam
import de.gesellix.docker.compose.types.Labels
import de.gesellix.docker.compose.types.Limits
import de.gesellix.docker.compose.types.Logging
import de.gesellix.docker.compose.types.Network
import de.gesellix.docker.compose.types.Placement
import de.gesellix.docker.compose.types.PortConfig
import de.gesellix.docker.compose.types.PortConfigs
import de.gesellix.docker.compose.types.Reservations
import de.gesellix.docker.compose.types.Resources
import de.gesellix.docker.compose.types.RestartPolicy
import de.gesellix.docker.compose.types.Secret
import de.gesellix.docker.compose.types.Service
import de.gesellix.docker.compose.types.ServiceNetwork
import de.gesellix.docker.compose.types.ServiceSecret
import de.gesellix.docker.compose.types.Ulimits
import de.gesellix.docker.compose.types.UpdateConfig
import de.gesellix.docker.compose.types.Volume
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.nio.file.Paths
import kotlin.test.assertEquals

class ComposeFileReaderTest : Spek({

    given("parse/sample.yaml") {

        val composeFile = ComposeFileReaderTest::class.java.getResource("parse/sample.yaml")

        // TODO do we really need to wrap our `on()` actions with `listOf(...)`?
        listOf(

                on("ComposeFileReader().loadYaml()") {
                    val json = ComposeFileReaderTest::class.java.getResourceAsStream("parse/sample.json")?.let { inputStream ->
                        Parser().parse(inputStream)
                    } as JsonObject
                    val expected = json.map as HashMap<String, Any?>

                    val result = ComposeFileReader().loadYaml(composeFile.openStream()) as HashMap<String, Any?>
                    it("should return the same content as the reference json") {
                        assertEquals(expected, result)
                    }
                },

                on("ComposeFileReader().load()") {
                    val expected = newSampleConfig()
                    val result = ComposeFileReader().load(composeFile.openStream(), Paths.get(composeFile.toURI()).parent.toString(), System.getenv())!!

                    it("should return the same services as the reference") {
                        assertEquals(expected.services, result.services)
                    }
                    it("should return the same networks as the reference") {
                        assertEquals(expected.networks, result.networks)
                    }
                    it("should return the same volumes as the reference") {
                        assertEquals(expected.volumes, result.volumes)
                    }
                    it("should return the same secrets as the reference") {
                        assertEquals(expected.secrets, result.secrets)
                    }
                    it("should return the same result as the reference") {
                        assertEquals(expected.version, result.version)
                    }
                })
    }

    given("environment/sample.yaml") {

        val composeFile = ComposeFileReaderTest::class.java.getResource("environment/sample.yaml")

        on("ComposeFileReader().load()") {
            val expectedEnv = Environment(hashMapOf(
                    Pair("FOO", "1"),
                    Pair("BAR", "2"),
                    Pair("BAZ", "2.5"),
                    Pair("QUUX", "")
            ))
            val result = ComposeFileReader().load(composeFile.openStream(), Paths.get(composeFile.toURI()).parent.toString(), System.getenv())!!

            it("should load environments as dict") {
                assertEquals(expectedEnv, result.services!!["dict-env"]!!.environment)
            }

            it("should load environments as list") {
                assertEquals(expectedEnv, result.services!!["list-env"]!!.environment)
            }
        }
    }

    given("version_3_1/sample.yaml") {

        val composeFile = ComposeFileReaderTest::class.java.getResource("version_3_1/sample.yaml")

        on("ComposeFileReader().load()") {
            val sampleConfig = newSampleConfigVersion_3_1()
            val result = ComposeFileReader().load(composeFile.openStream(), Paths.get(composeFile.toURI()).parent.toString(), System.getenv())!!

            it("should load a config based on a 3.1 schema") {
                assertEquals(sampleConfig.services, result.services)
                assertEquals(sampleConfig.secrets, result.secrets)
                assertEquals(sampleConfig, result)
            }
        }
    }

    given("attachable/sample.yaml") {

        val composeFile = ComposeFileReaderTest::class.java.getResource("attachable/sample.yaml")

        on("ComposeFileReader().load()") {
            val sampleConfig = hashMapOf<String, Network>().apply {
                put("mynet1", Network(driver = "overlay", attachable = true))
                put("mynet2", Network(driver = "bridge", attachable = false))
            }
            val result = ComposeFileReader().load(composeFile.openStream(), Paths.get(composeFile.toURI()).parent.toString(), System.getenv())!!

            it("should load a config with attachable networks") {
                assertEquals(sampleConfig["mynet1"], result.networks!!["mynet1"])
                assertEquals(sampleConfig["mynet2"], result.networks!!["mynet2"])
            }
        }
    }

    given("portformats/sample.yaml") {
        val composeFile = ComposeFileReaderTest::class.java.getResource("portformats/sample.yaml")

        on("ComposeFileReader().load()") {
            val sampleConfig = newSampleConfigPortFormats()
            val result = ComposeFileReader().load(composeFile.openStream(), Paths.get(composeFile.toURI()).parent.toString(), System.getenv())!!

            it("should load expanded port formats") {
                assertEquals(sampleConfig, result.services!!["web"]!!.ports)
            }
        }
    }

    given("interpolation/sample.yaml") {

        val composeFile = ComposeFileReaderTest::class.java.getResource("interpolation/sample.yaml")

        on("ComposeFileReader().load()") {
            val home = "/home/foo"
            val expectedLabels = Labels(hashMapOf<String, String>().apply {
                put("home1", home)
                put("home2", home)
                put("nonexistent", "")
                put("default", "default")
            })
            val result = ComposeFileReader().load(
                    composeFile.openStream(),
                    Paths.get(composeFile.toURI()).parent.toString(),
                    hashMapOf(
                            Pair("HOME", home),
                            Pair("FOO", "foo"))
            )!!

            it("should interpolate environment variables") {
                assertEquals(expectedLabels, result.services!!["test"]!!.labels)
                assertEquals(home, result.networks!!["test"]!!.driver)
                assertEquals(home, result.volumes!!["test"]!!.driver)
            }
        }
    }

    given("full/sample.yaml") {

        val composeFile = ComposeFileReaderTest::class.java.getResource("full/sample.yaml")

        on("ComposeFileReader().load()") {
            val sampleConfig = newSampleConfigFull()
            val result = ComposeFileReader().load(composeFile.openStream(), Paths.get(composeFile.toURI()).parent.toString(), System.getenv())!!

            it("should load expanded port formats") {
                assertEquals(sampleConfig.version, result.version)
                assertEquals(sampleConfig.volumes, result.volumes)
                assertEquals(sampleConfig.networks, result.networks)
                assertEquals(sampleConfig.services, result.services)
                assertEquals(sampleConfig.secrets, result.secrets)
                assertEquals(sampleConfig, result)
            }
        }
    }
})

fun newSampleConfigPortFormats(): PortConfigs {
    return PortConfigs(listOf(
            PortConfig(
                    "ingress",
                    8080,
                    80,
                    "tcp"
            ),
            PortConfig(
                    "ingress",
                    8081,
                    81,
                    "tcp"
            ),
            PortConfig(
                    "ingress",
                    8082,
                    82,
                    "tcp"
            ),
            PortConfig(
                    "ingress",
                    8090,
                    90,
                    "udp"
            ),
            PortConfig(
                    "ingress",
                    8091,
                    91,
                    "udp"
            ),
            PortConfig(
                    "ingress",
                    8092,
                    92,
                    "udp"
            ),
            PortConfig(
                    "ingress",
                    8500,
                    85,
                    "tcp"
            ),
            PortConfig(
                    "ingress",
                    8600,
                    0,
                    "tcp"
            ),
            PortConfig(
                    "",
                    53,
                    10053,
                    "udp"
            ),
            PortConfig(
                    "host",
                    22,
                    10022,
                    ""
            )
    ))
}

fun newSampleConfig(): ComposeConfig {
    return ComposeConfig(
            "3",
            hashMapOf<String, Service>().apply {
                put("foo", Service(
                        image = "busybox",
                        environment = Environment(),
                        networks = hashMapOf(Pair("with_me", null)))
                )
                put("bar", Service(
                        image = "busybox",
                        environment = Environment(entries = hashMapOf(Pair("FOO", "1"))),
                        networks = hashMapOf(Pair("with_ipam", null)))
                )
            },
            hashMapOf<String, Network>().apply {
                put("default", Network(
                        driver = "bridge",
                        driverOpts = DriverOpts(options = hashMapOf(Pair("beep", "boop"))))
                )
                put("with_ipam", Network(
                        ipam = Ipam(
                                driver = "default",
                                config = listOf(Config(subnet = "172.28.0.0/16"))))
                )
            },
            hashMapOf<String, Volume>().apply {
                put("hello", Volume(
                        driver = "default",
                        driverOpts = DriverOpts(options = hashMapOf(Pair("beep", "boop")))
                ))
            }
    )
}

fun newSampleConfigVersion_3_1(): ComposeConfig {
    return ComposeConfig(
            version = "3.1",
            services = hashMapOf<String, Service>().apply {
                put("foo", Service(
                        image = "busybox",
                        secrets = arrayListOf(
                                hashMapOf<String, ServiceSecret?>(Pair("super", null)),
                                // 292 decimal == 0444 octal
                                hashMapOf<String, ServiceSecret?>(Pair("duper", ServiceSecret(source = "duper", mode = 292))))))
            },
            secrets = hashMapOf<String, Secret>().apply {
                put("super", Secret(external = External(external = true)))
                put("duper", Secret(external = External(external = true)))
            })
}

fun newSampleConfigFull(): ComposeConfig {
//        workingDir, err := os.Getwd()
//        homeDir := os.Getenv("HOME")

    val fooService = Service(
            capAdd = setOf("ALL"),
            capDrop = setOf("NET_ADMIN", "SYS_ADMIN"),
            cgroupParent = "m-executor-abcd",
            command = Command(parts = arrayListOf("bundle", "exec", "thin", "-p", "3000")),
            containerName = "my-web-container",
            dependsOn = setOf(
                    "db",
                    "redis"
            ),
            deploy = Deploy(
                    mode = "replicated",
                    replicas = 6,
                    labels = Labels(hashMapOf(Pair("FOO", "BAR"))),
                    updateConfig = UpdateConfig(
                            parallelism = 3,
                            delay = "10s",
                            failureAction = "continue",
                            monitor = "60s",
                            maxFailureRatio = 0.3f
                    ),
                    resources = Resources(
                            limits = Limits(
                                    nanoCpus = "0.001",
                                    memory = "50M"
                            ),
                            reservations = Reservations(
                                    nanoCpus = "0.0001",
                                    memory = "20M"
                            )
                    ),
                    restartPolicy = RestartPolicy(
                            condition = "on_failure",
                            delay = "5s",
                            maxAttempts = 3,
                            window = "120s"
                    ),
                    placement = Placement(constraints = listOf("node=foo"))
            ),
            devices = setOf("/dev/ttyUSB0:/dev/ttyUSB0"),
            dns = listOf(
                    "8.8.8.8",
                    "9.9.9.9"
            ),
            dnsSearch = listOf(
                    "dc1.example.com",
                    "dc2.example.com"),
            domainname = "foo.com",
            entrypoint = listOf("/code/entrypoint.sh", "-p", "3000"),
            envFile = listOf(
                    "./example1.env",
                    "./example2.env"
            ),
            environment = Environment(hashMapOf<String, String>().apply {
                put("RACK_ENV", "development")
                put("SHOW", "true")
                put("SESSION_SECRET", "")
//                put("FOO", "1")
//                put("BAR", "2")
                put("BAZ", "3")
            }),
            expose = Exposes(arrayListOf("3000",
                    "8000")),
            externalLinks = setOf(
                    "redis_1",
                    "project_db_1:mysql",
                    "project_db_1:postgresql"
            ),
            extraHosts = ExtraHosts(hashMapOf<String, String>().apply {
                put("otherhost", "50.31.209.229")
                put("somehost", "162.242.195.82")
            }),
            //                            HealthCheck: &types.HealthCheckConfig{
//                    Test:     types.HealthCheckTest([]string{"CMD-SHELL", "echo \"hello world\""}),
//                },
            healthcheck = Healthcheck(
                    interval = "10s",
                    retries = 5f,
                    test = Command(parts = arrayListOf("echo \"hello world\"")),
                    timeout = "1s"
            ),
            hostname = "foo",
            image = "redis",
            ipc = "host",
            labels = Labels(hashMapOf<String, String>().apply {
                put("com.example.description", "Accounting webapp")
                put("com.example.number", "42")
                put("com.example.empty-label", "")
            }),
            links = setOf(
                    "db",
                    "db:database",
                    "redis"
            ),
            logging = Logging(
                    driver = "syslog",
                    options = hashMapOf(Pair("syslog-address", "tcp://192.168.0.42:123"))
            ),
            networks = hashMapOf<String, ServiceNetwork?>().apply {
                put("some-network", ServiceNetwork(
                        aliases = listOf("alias1", "alias3")
                ))
                put("other-network", ServiceNetwork(
                        ipv4Address = "172.16.238.10",
                        ipv6Address = "2001:3984:3989::10"
                ))
                put("other-other-network", null)
            },
            macAddress = "02:42:ac:11:65:43",
            networkMode = "container:0cfeab0f748b9a743dc3da582046357c6ef497631c1a016d28d2bf9b4f899f7b",
            pid = "host",
            ports = PortConfigs(
                    listOf(
                            PortConfig(
                                    "ingress",
                                    3000,
                                    0,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    3000,
                                    0,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    3001,
                                    0,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    3002,
                                    0,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    3003,
                                    0,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    3004,
                                    0,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    3005,
                                    0,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    8000,
                                    8000,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    8080,
                                    9090,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    8081,
                                    9091,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    22,
                                    49100,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    8001,
                                    8001,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5000,
                                    5000,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5001,
                                    5001,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5002,
                                    5002,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5003,
                                    5003,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5004,
                                    5004,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5005,
                                    5005,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5006,
                                    5006,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5007,
                                    5007,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5008,
                                    5008,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5009,
                                    5009,
                                    "tcp"
                            ),
                            PortConfig(
                                    "ingress",
                                    5010,
                                    5010,
                                    "tcp"
                            )
                    )
            ),
            privileged = true,
            readOnly = true,
            restart = "always",
            securityOpt = setOf(
                    "label=level:s0:c100,c200",
                    "label=type:svirt_apache_t"
            ),
            stdinOpen = true,
            stopGracePeriod = "20s",
            stopSignal = "SIGUSR1",
            tmpfs = listOf("/run", "/tmp"),
            tty = true,
            //                Ulimits: map[string]*types.UlimitsConfig{
//                    "nproc": {
//                        Single: 65535,
//                    },
//                    "nofile": {
//                        Soft: 20000,
//                        Hard: 40000,
//                    },
//                },
            ulimits = Ulimits(),
            user = "someone",
            //                Volumes: []string{
//                    "/var/lib/mysql",
//                    "/opt/data:/var/lib/mysql",
//                    fmt.Sprintf("%s:/code", workingDir),
//                    fmt.Sprintf("%s/static:/var/www/html", workingDir),
//                    fmt.Sprintf("%s/configs:/etc/configs/:ro", homeDir),
//                    "datavolume:/var/lib/mysql",
//                },
            volumes = setOf(
                    "/var/lib/mysql",
                    "/opt/data:/var/lib/mysql",
                    ".:/code",
                    "./static:/var/www/html",
                    "~/configs:/etc/configs/:ro",
                    "datavolume:/var/lib/mysql"
            ),
            workingDir = "/code"
    )

    val composeConfig = ComposeConfig()

    composeConfig.version = "3"

    composeConfig.services = hashMapOf(Pair("foo", fooService))

    composeConfig.networks = hashMapOf<String, Network?>().apply {
        put("some-network", null)
        put("other-network", Network(
                driver = "overlay",
                driverOpts = DriverOpts(hashMapOf<String, String>().apply {
                    put("foo", "bar")
                    put("baz", "1")
                }),
                ipam = Ipam(
                        driver = "overlay",
                        config = listOf(
                                Config("172.16.238.0/24"),
                                Config("2001:3984:3989::/64")))))
        put("external-network", Network(
                external = External(
                        //name = "external-network",
                        external = true)
        ))
        put("other-external-network", Network(
                external = External(
                        name = "my-cool-network",
                        external = true)
        ))
    }

    composeConfig.volumes = hashMapOf<String, Volume?>().apply {
        put("some-volume", null)
        put("other-volume", Volume(
                driver = "flocker",
                driverOpts = DriverOpts(hashMapOf(
                        Pair("foo", "bar"),
                        Pair("baz", "1")
                ))
        ))
        put("external-volume", Volume(
                external = External(
                        //name = "external-volume",
                        external = true)
        ))
        put("other-external-volume", Volume(
                external = External(
                        name = "my-cool-volume",
                        external = true)
        ))
    }

    composeConfig.secrets = null

    return composeConfig
}
