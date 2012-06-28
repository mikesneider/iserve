/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package uk.ac.open.kmi.iserve.discovery.engine;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.abdera.model.Feed;
import org.openrdf.repository.RepositoryException;

import com.sun.jersey.api.NotFoundException;

import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.AllServicesPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.RDFSClassificationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.RDFSInputOutputDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.util.DiscoveryUtil;

/**
 * Resource providing access to the discovery infrastructure.
 * 
 * TODO: Provide pagination support for discovery results 
 * TODO: Enable the configuration of ordering and scoring functionality for 
 * discovery results (composite or simple)
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
@Path("/disco")
public class DiscoveryResource {

	// Base URI for this resource
	@Context  UriInfo uriInfo;

	public static String NEW_LINE = System.getProperty("line.separator");

	private Map<String, ServiceDiscoveryPlugin> servicePlugins = new HashMap<String, ServiceDiscoveryPlugin>();
	private Map<String, OperationDiscoveryPlugin> operationPlugins = new HashMap<String, OperationDiscoveryPlugin>();

	public DiscoveryResource() throws RepositoryException, 
	TransformerConfigurationException, IOException, 
	ParserConfigurationException {

		// Register all plugins manually for now
		DiscoveryPlugin plugin;
		plugin = new AllServicesPlugin();
		registerPlugin(plugin);

		plugin = new RDFSInputOutputDiscoveryPlugin();
		registerPlugin(plugin);

		plugin = new RDFSClassificationDiscoveryPlugin();
		registerPlugin(plugin);

		//		plugin = new IMatcherDiscoveryPlugin(connector);
		//		plugins.put(plugin.getName(), plugin);
	}

	/**
	 * Registers a plugin within the discovery infrastructure
	 * 
	 * @param plugin The plugin to be registered
	 */
	public void registerPlugin(DiscoveryPlugin plugin) {
		if (plugin != null) {
			// Register the plugin for the appropriate kinds
			if (plugin instanceof ServiceDiscoveryPlugin) {
				servicePlugins.put(plugin.getName(), (ServiceDiscoveryPlugin) plugin);
			}

			if (plugin instanceof OperationDiscoveryPlugin) {
				operationPlugins.put(plugin.getName(), (OperationDiscoveryPlugin) plugin);
			}
		}
	}

	@GET
	@Produces(MediaType.TEXT_HTML)	
	public Response listPluginsRegistered() throws WebApplicationException {
		return Response.ok(generateListOfPlugins()).build();
	}

	/**
	 * Generates an HTML page with the list of plugins currently loaded, their
	 * URLs, and further details
	 * 
	 * @return
	 */
	private String generateListOfPlugins() {	
		StringBuffer result = new StringBuffer(insertHeader());
		result.append("<H2>iServe Discovery Plugins Registered</H2>");
		result.append("<H3>Service Discovery Plugins</H3>");
		result.append(generatePluginsTable(ServiceDiscoveryPlugin.class, servicePlugins));
		result.append("<H3>Operation Discovery Plugins</H3>");
		result.append(generatePluginsTable(OperationDiscoveryPlugin.class, operationPlugins));
		result.append(insertFooter());
		return result.toString();
	}

	/**
	 * Generate an HTML table with the details of the plugins in the Map handed
	 * 
	 * @param <T> the type of plugins which extends Discovery Plugin
	 * @param typeOfPlugin the type of plugin being listed
	 * @param pluginsMap The map of plugins to use
	 * @return An HTML table with the details of the plugins in the map
	 */
	private <T extends DiscoveryPlugin> String generatePluginsTable(Class typeOfPlugin, Map<String, T> pluginsMap) {

		StringBuffer result = new StringBuffer();
		if (pluginsMap != null && !pluginsMap.isEmpty()) {
			Set<Entry<String, T>> plugins = pluginsMap.entrySet();

			// Create table
			result.append("<table border=\"1\" width=\"90%\"><tr>" + NEW_LINE);
			result.append("<th>Plugin</th>"+ NEW_LINE);
			result.append("<th>Version</th>"+ NEW_LINE);
			result.append("<th>Description</th>"+ NEW_LINE);
			result.append("<th>Parameters</th>"+ NEW_LINE);
			result.append("<th>Endpoint</th>"+ NEW_LINE);
			result.append("</tr>"+ NEW_LINE);

			String interPath = "";
			for (java.util.Map.Entry<String, T> entry : plugins) {
				DiscoveryPlugin regPlugin = entry.getValue();
				result.append("<tr>"+ NEW_LINE);
				result.append("<td>" + regPlugin.getName() + "</td>" + NEW_LINE);
				result.append("<td>" + regPlugin.getVersion() + "</td>" + NEW_LINE);
				result.append("<td>" + regPlugin.getDescription() + "</td>" + NEW_LINE);

				// List parameter details
				result.append("<td><ul>");
				Set<Entry<String, String>> parameters = regPlugin.getParametersDetails().entrySet();
				for (Entry<String, String> param : parameters) {
					result.append("<li><b>" + param.getKey() + "</b>: " + param.getValue() + "</li>" + NEW_LINE);
				}
				result.append("</ul></td>" + NEW_LINE);

				// Include the endpoint 
				UriBuilder ub = uriInfo.getAbsolutePathBuilder();
				// Fix the intermediate path
				if (typeOfPlugin.equals(OperationDiscoveryPlugin.class)) {
					interPath = "op";
				}
				if (typeOfPlugin.equals(ServiceDiscoveryPlugin.class)) {
					interPath = "svc";
				}
				String pluginUri = ub.path(interPath + "/" + regPlugin.getName()).build().toString();
				result.append("<td><a href=" + pluginUri + ">" + pluginUri + "</a></td>" + NEW_LINE);
				result.append("</tr>" + NEW_LINE);
			}

			// Close table
			result.append("</table>" + NEW_LINE);

		} else {
			result.append("No plugins registered." + NEW_LINE);
		}

		return result.toString();
	}

	/**
	 * TODO: Replace with a common HTML footer for all iServe related pages
	 * @return
	 */
	private String insertFooter() {
		StringBuffer result = new StringBuffer("</body>" + NEW_LINE);
		result.append("</html>" + NEW_LINE);
		return result.toString();
	}

	/**
	 * TODO: Replace with a common HTML header for all iServe related pages
	 * @return
	 */
	private String insertHeader() {
		StringBuffer result = new StringBuffer("<!DOCTYPE html>" + NEW_LINE);
		result.append("<html lang=\"en-UK\">" + NEW_LINE);
		result.append("<head>" + NEW_LINE);
		result.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + NEW_LINE);
		result.append("<title>iServe Service Discovery Plugins</title>" + NEW_LINE);
		result.append("</head>" + NEW_LINE);
		result.append("<body>" + NEW_LINE);

		return result.toString();
	}

	@GET
	@Produces({MediaType.APPLICATION_ATOM_XML,
		MediaType.APPLICATION_JSON,
		MediaType.TEXT_XML})
		@Path("/op/{name}")
		public Response discoverOperations(@PathParam("name") String name, @Context UriInfo ui) throws WebApplicationException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

		// find discovery plug-in
		OperationDiscoveryPlugin plugin = operationPlugins.get(name);
		if ( plugin == null ) {
			throw new NotFoundException("Plugin named '" + name + "' is not available for operation discovery.");
		}

		SortedSet<org.apache.abdera.model.Entry> matchingResults;
		try {
			matchingResults = plugin.discoverOperations(queryParams);
		} catch (DiscoveryException e) {
			throw new WebApplicationException(e, e.getStatus());
		}

		// process the results
		Feed feed = DiscoveryUtil.getAbderaInstance().getFactory().newFeed();
		feed.setId(ui.getRequestUri().toString());
		feed.addLink(ui.getRequestUri().toString(),"self");
		feed.setTitle(plugin.getFeedTitle());
		feed.setUpdated(new Date());

		UriBuilder ub = uriInfo.getAbsolutePathBuilder();
		//FIXME: we should include the version
		feed.setGenerator(ub.path(plugin.getName()).build().toString(), null, plugin.getDescription()); 

		if ( matchingResults != null ) {
			for ( org.apache.abdera.model.Entry result : matchingResults ) {
				feed.addEntry(result);
			}
		}

		return Response.ok(feed).build();
	}

	@GET
	@Produces({MediaType.APPLICATION_ATOM_XML,
		MediaType.APPLICATION_JSON,
		MediaType.TEXT_XML})
		@Path("/svc/{name}")
		public Response discoverServices(@PathParam("name") String name, @Context UriInfo ui) throws WebApplicationException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

		// Else find discovery plug-in and apply it
		ServiceDiscoveryPlugin plugin = servicePlugins.get(name);
		if ( plugin == null ) {
			throw new NotFoundException("Plugin named '" + name + "' is not available for service discovery.");
		}

		SortedSet<org.apache.abdera.model.Entry> matchingResults;
		try {
			matchingResults = plugin.discoverServices(queryParams);
		} catch (DiscoveryException e) {
			throw new WebApplicationException(e, e.getStatus());
		}

		// process the results
		Feed feed = DiscoveryUtil.getAbderaInstance().getFactory().newFeed();
		feed.setId(ui.getRequestUri().toString());
		feed.addLink(ui.getRequestUri().toString(),"self");
		feed.setTitle(plugin.getFeedTitle());
		feed.setUpdated(new Date());

		UriBuilder ub = uriInfo.getAbsolutePathBuilder();
		//FIXME: we should include the version
		feed.setGenerator(ub.path(plugin.getName()).build().toString(), null, plugin.getDescription()); 

		if ( matchingResults != null ) {
			for ( org.apache.abdera.model.Entry result : matchingResults ) {
				feed.addEntry(result);
			}
		}

		return Response.ok(feed).build();
	}

}
