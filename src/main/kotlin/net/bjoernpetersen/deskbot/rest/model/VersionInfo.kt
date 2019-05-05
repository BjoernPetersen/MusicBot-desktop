package net.bjoernpetersen.deskbot.rest.model

data class VersionInfo(val apiVersion: String, val implementation: ImplementationInfo)
data class ImplementationInfo(val projectInfo: String, val name: String, val version: String)
