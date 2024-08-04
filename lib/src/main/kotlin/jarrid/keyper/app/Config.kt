package jarrid.keyper.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigRenderOptions
import io.klogging.NoCoLogger
import io.klogging.NoCoLogging
import io.klogging.noCoLogger
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream


class Config(path: String = "/app.yaml") : NoCoLogging {
    override val logger: NoCoLogger = noCoLogger(Config::class)

    private val base: InputStream =
        Config::class.java.getResourceAsStream(path)
            ?: throw IllegalArgumentException("App config file not found: $path")

    fun get(): App {
        return merge()
    }

    private fun convert(config: Config): App {
        val deserialized = config.root().render(ConfigRenderOptions.concise())
        val app: App = Json.decodeFromString(deserialized)
        return app
    }

    private fun getYamlConfig(input: InputStream): Config {
//        val yaml = Yaml()
//        val map = yaml.decodeFromStream<Map<String, Any>>(input)
        val yamlMapper = ObjectMapper(YAMLFactory())
        val map = yamlMapper.readValue(input, Map::class.java) as Map<String, Any>
        val json = ObjectMapper().writeValueAsString(map)
        val config = ConfigFactory.parseString(json, ConfigParseOptions.defaults())
        return config
    }

    private fun getEnvConfig(env: ENV): Config? {
        val path = "/app.$env.yaml"
        val file = File(Config::class.java.getResource(path)?.file ?: "")
        if (!file.exists()) {
            logger.warn { "Override env config file not found: $path" }
            return null
        }
        val stream: InputStream = file.inputStream()
        val config = getYamlConfig(stream)
        return config
    }

    private fun merge(): App {
        val baseConfig = getYamlConfig(base)
        val baseApp = convert(baseConfig)
        val envConfig = getEnvConfig(env = baseApp.env)
        val merged = envConfig?.withFallback(baseConfig)?.resolve() ?: baseConfig
        val app = convert(merged)
        return app
    }
}