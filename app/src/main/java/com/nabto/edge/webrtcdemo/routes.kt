package com.nabto.edge.webrtcdemo

private class RouteBuilder(val root: String) {
    private val pathSegments: MutableList<String> = mutableListOf()
    private val queries: MutableMap<String, String> = mutableMapOf()

    fun build() = buildString {
        append(root)
        pathSegments.forEach {
            append("/")
            append(it)
        }
        if (queries.isNotEmpty()) {
            append("?")
            queries.forEach { (key, value) ->
                append("${key}=${value}&")
            }
        }
    }

    fun pathSegment(segment: String): RouteBuilder {
        pathSegments.add(segment)
        return this
    }

    fun query(key: String, value: String): RouteBuilder {
        if (key.isNotEmpty() && value.isNotEmpty()) {
            queries[key] = value
        }
        return this
    }
}

object AppRoute {
    fun home() = "home"
    fun pairingFlow() = "pairing_flow"
    fun settings() = "settings"
    fun login() = "login"

    fun pairDevice(productId: String, deviceId: String, password: String = "", sct: String = "") =
        RouteBuilder("pair_device")
            .pathSegment(productId)
            .pathSegment(deviceId)
            .query("password", password)
            .query("sct", sct)
            .build()

    fun appDevicePage(productId: String, deviceId: String) =
        RouteBuilder("app_device_page")
            .pathSegment(productId)
            .pathSegment(deviceId)
            .build()
}