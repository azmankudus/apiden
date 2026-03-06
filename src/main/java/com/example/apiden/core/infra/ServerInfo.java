package com.example.apiden.core.infra;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Captures and exposes static information about the server environment at startup.
 * 
 * <p>This includes details such as instance name, operating system version (with detailed
 * resolution for Linux and Windows), Java runtime version, and the server's IP address.</p>
 */
@Singleton
public final class ServerInfo {

  private static final Logger logger = LoggerFactory.getLogger(ServerInfo.class);

  private final String instanceName;
  private final String os;
  private final String runtime;
  private final String ip;

  /**
   * Initializes ServerInfo by resolving environment details.
   *
   * @param instanceName the configured name of this application instance
   * @param serverHost the configured server host (optional)
   */
  ServerInfo(
      @Value("${application.instance.name:apiden}") final String instanceName,
      @Value("${micronaut.server.host:}") final String serverHost) {
    this.instanceName = instanceName;
    this.os = resolveOs();
    this.runtime = System.getProperty("java.vendor") + " " + System.getProperty("java.version");
    this.ip = resolveIp(serverHost);

    logger.info("ServerInfo initialized: instance={}, os={}, runtime={}, ip={}",
        instanceName, os, runtime, ip);
  }

  /**
   * Resolves the IP address to be used for server identification.
   *
   * @param host The configured host name.
   * @return The resolved IP address.
   */
  private String resolveIp(final String host) {
    if (host == null || host.isBlank() || "localhost".equalsIgnoreCase(host) || "0.0.0.0".equals(host)) {
      return getLocalIp();
    }
    try {
      final InetAddress addr = InetAddress.getByName(host);
      final String hostIp = addr.getHostAddress();
      if (host.equals(hostIp)) {
        return hostIp;
      } else {
        return hostIp + " (" + host + ")";
      }
    } catch (final Exception e) {
      return getLocalIp();
    }
  }

  /**
   * Iterates through network interfaces to find the first non-loopback IPv4 address.
   *
   * @return The local IP address or 127.0.0.1 as fallback.
   */
  private String getLocalIp() {
    try {
      final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        final NetworkInterface iface = interfaces.nextElement();
        if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
          continue;
        }
        final Enumeration<InetAddress> addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          final InetAddress addr = addresses.nextElement();
          if (addr instanceof java.net.Inet4Address) {
            return addr.getHostAddress();
          }
        }
      }
    } catch (final SocketException e) {
      logger.error("Failed to resolve local IP", e);
    }
    return "127.0.0.1";
  }

  /**
   * Resolves the operating system name and version.
   * 
   * <p>On Linux, it attempts to read /etc/os-release for a prettier name.
   * On Windows, it attempts to execute the 'ver' command.</p>
   *
   * @return A descriptive OS string.
   */
  private String resolveOs() {
    final String osName = System.getProperty("os.name");
    final String osVersion = System.getProperty("os.version");

    if (osName.toLowerCase().contains("linux")) {
      final Path osRelease = Paths.get("/etc/os-release");
      if (Files.exists(osRelease)) {
        try (final BufferedReader reader = Files.newBufferedReader(osRelease)) {
          String line;
          while ((line = reader.readLine()) != null) {
            if (line.startsWith("PRETTY_NAME=")) {
              String prettyName = line.substring(12);
              if (prettyName.startsWith("\"") && prettyName.endsWith("\"")) {
                prettyName = prettyName.substring(1, prettyName.length() - 1);
              }
              return prettyName + " (Linux " + osVersion + ")";
            }
          }
        } catch (final Exception e) {
          logger.trace("Failed to read /etc/os-release: {}", e.getMessage());
        }
      }
    } else if (osName.toLowerCase().contains("windows")) {
      try {
        final Process process = new ProcessBuilder("cmd", "/c", "ver").start();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
          final String line = reader.lines().filter(l -> !l.isBlank()).findFirst().orElse("");
          if (!line.isEmpty()) {
            return line;
          }
        }
      } catch (final Exception e) {
        logger.trace("Failed to execute ver command: {}", e.getMessage());
      }
    }

    return osName + " " + osVersion;
  }

  /**
   * @return the application instance name
   */
  public String getInstanceName() {
    return instanceName;
  }

  /**
   * @return the operating system name and version
   */
  public String getOs() {
    return os;
  }

  /**
   * @return the Java runtime version and vendor
   */
  public String getRuntime() {
    return runtime;
  }

  /**
   * @return the resolved IP address of the server
   */
  public String getIp() {
    return ip;
  }
}
