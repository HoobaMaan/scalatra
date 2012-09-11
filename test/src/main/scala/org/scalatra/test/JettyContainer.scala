package org.scalatra
package test

import servlet.HasMultipartConfig
import javax.servlet.{DispatcherType, Filter}
import javax.servlet.http.HttpServlet
import java.util.EnumSet
import org.eclipse.jetty.servlet._
import scala.deprecated
import java.util


object JettyContainer {
  private val DefaultDispatcherTypes: EnumSet[DispatcherType] =
    EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC)
}

trait JettyContainer extends Container {
  import JettyContainer._

  def servletContextHandler: ServletContextHandler

//  @deprecated("use addServlet(HttpServlet, String) or addFilter(Filter, String)", "2.0.0")
  def mount(klass: Class[_], path: String) = klass match {
    case servlet if classOf[HttpServlet].isAssignableFrom(servlet) =>
      addServlet(servlet.asInstanceOf[Class[_ <: HttpServlet]], path)
    case filter if classOf[Filter].isAssignableFrom(filter) =>
      addFilter(filter.asInstanceOf[Class[_ <: Filter]], path)
    case _ =>
      throw new IllegalArgumentException(klass + " is not assignable to either HttpServlet or Filter")
  }

//  @deprecated("renamed to addServlet", "2.0.0")
  def mount(servlet: HttpServlet, path: String) = addServlet(servlet, path)

  def mount(app: Filter, path: String, dispatches: EnumSet[DispatcherType] = DefaultDispatcherTypes) =
    addFilter(app, path, dispatches)

  def addServlet(servlet: HttpServlet, path: String) = {
    val holder = new ServletHolder(servlet)

    servlet match {
      case s: HasMultipartConfig => {
        holder.getRegistration.setMultipartConfig(
          s.multipartConfig.toMultipartConfigElement)
      }

      case _ =>
    }

    servletContextHandler.addServlet(holder, path)

  }

//  @deprecated("Adding servlet by class is deprecated. Please use addServlet(HttpServlet, String) instead",
//      since = "2.2.0")
  def addServlet(servlet: Class[_ <: HttpServlet], path: String) =
    servletContextHandler.addServlet(servlet, path)

  def addFilter(filter: Filter, path: String, dispatches: util.EnumSet[DispatcherType] = DefaultDispatcherTypes): FilterHolder = {
    val holder = new FilterHolder(filter)
    servletContextHandler.addFilter(holder, path, dispatches)
    holder
  }

  def addFilter(filter: Class[_ <: Filter], path: String): FilterHolder =
    addFilter(filter, path, DefaultDispatcherTypes)

  def addFilter(filter: Class[_ <: Filter], path: String, dispatches: util.EnumSet[DispatcherType]): FilterHolder =
    servletContextHandler.addFilter(filter, path, dispatches)

  // Add a default servlet.  If there is no underlying servlet, then
  // filters just return 404.
  addServlet(new DefaultServlet, "/")
}
