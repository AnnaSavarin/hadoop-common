package org.apache.hadoop.net;

import javax.net.SocketFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.VersionedProtocol;
import org.apache.hadoop.util.ReflectionUtils;

public class NetUtils {

  /**
   * Get the socket factory for the given class according to its
   * configuration parameter
   * <tt>hadoop.rpc.socket.factory.class.&lt;ClassName&gt;</tt>. When no
   * such parameter exists then fall back on the default socket factory as
   * configured by <tt>hadoop.rpc.socket.factory.class.default</tt>. If
   * this default socket factory is not configured, then fall back on the JVM
   * default socket factory.
   * 
   * @param conf the configuration
   * @param clazz the class (usually a {@link VersionedProtocol})
   * @return a socket factory
   */
  public static SocketFactory getSocketFactory(Configuration conf,
      Class<?> clazz) {

    SocketFactory factory = null;

    String propValue =
        conf.get("hadoop.rpc.socket.factory.class." + clazz.getSimpleName());
    if ((propValue != null) && (propValue.length() > 0))
      factory = getSocketFactoryFromProperty(conf, propValue);

    if (factory == null)
      factory = getDefaultSocketFactory(conf);

    return factory;
  }

  /**
   * Get the default socket factory as specified by the configuration
   * parameter <tt>hadoop.rpc.socket.factory.default</tt>
   * 
   * @param conf the configuration
   * @return the default socket factory as specified in the configuration or
   *         the JVM default socket factory if the configuration does not
   *         contain a default socket factory property.
   */
  public static SocketFactory getDefaultSocketFactory(Configuration conf) {

    String propValue = conf.get("hadoop.rpc.socket.factory.class.default");
    if ((propValue == null) || (propValue.length() == 0))
      return SocketFactory.getDefault();

    return getSocketFactoryFromProperty(conf, propValue);
  }

  /**
   * Get the socket factory corresponding to the given proxy URI. If the
   * given proxy URI corresponds to an absence of configuration parameter,
   * returns null. If the URI is malformed raises an exception.
   * 
   * @param propValue the property which is the class name of the
   *        SocketFactory to instantiate; assumed non null and non empty.
   * @return a socket factory as defined in the property value.
   */
  public static SocketFactory getSocketFactoryFromProperty(
      Configuration conf, String propValue) {

    try {
      Class<?> theClass = conf.getClassByName(propValue);
      return (SocketFactory) ReflectionUtils.newInstance(theClass, conf);

    } catch (ClassNotFoundException cnfe) {
      throw new RuntimeException("Socket Factory class not found: " + cnfe);
    }
  }
}
